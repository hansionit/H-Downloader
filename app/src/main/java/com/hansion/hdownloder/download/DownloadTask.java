package com.hansion.hdownloder.download;

import android.content.Intent;
import android.net.Uri;

import com.hansion.hdownloder.bean.DownloadTaskInfo;
import com.hansion.hdownloder.db.DownloadDAO;
import com.hansion.hdownloder.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_CANCEL;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_PAUSE;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 17:08
 */
public class DownloadTask implements Runnable {

    //正在下载的任务信息
    private DownloadTaskInfo downloadTaskInfo;
    private DownloadService downloadService;
    private DownloadDAO downloadDAO;
    private RandomAccessFile file;
    private int downloadStatus = DownloadStatus.DOWNLOAD_STATUS_INIT;


    private String downloadId;
    private String url;
    private String name;
    private String savePath;
    private Long totalSize;
    private Long completedSize;


    public DownloadTask(DownloadTaskInfo downloadTaskInfo, DownloadService downloadService) {
        this.downloadTaskInfo = downloadTaskInfo;
        this.downloadService = downloadService;
        init();
    }

    private void init() {
        downloadDAO = DownloadDAO.getInstance(downloadService);
        downloadId = downloadTaskInfo.getDownloadId();
        url = downloadTaskInfo.getUrl();
        name = downloadTaskInfo.getName();
        savePath = downloadTaskInfo.getSavePath();
        totalSize = downloadTaskInfo.getTotalSize();
        completedSize = downloadTaskInfo.getCompletedSize();
    }

    public void cancel() {
        downloadStatus = DOWNLOAD_STATUS_CANCEL;
        File temp = new File(savePath + name);
        if (temp.exists()) {
            temp.delete();
        }
    }

    public void pause() {
        downloadStatus = DOWNLOAD_STATUS_PAUSE;
    }


    @Override
    public void run() {
        //准备下载
        downloadStatus = DownloadStatus.DOWNLOAD_STATUS_PREPARE;
        notifyProgress();

        InputStream inputStream = null;
        BufferedInputStream bis = null;

        try {
            prepareDownload();

            Request request = new Request.Builder()
                    .url(url)
                    .header("RANGE", "bytes=" + completedSize + "-")//  Http value set breakpoints RANGE
                    .addHeader("Referer", url)
                    .build();

            //移动到completedSize位置
            file.seek(completedSize);

            //利用OkHttp发送请求
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            //取得响应
            ResponseBody responseBody = response.body();

            if (responseBody != null) {
                downloadStatus = DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING;
                if (totalSize <= 0) {
                    totalSize = responseBody.contentLength();
                }

                inputStream = responseBody.byteStream();
                bis = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[4 * 1024];
                int length;

                //如果downloadTaskInfo为null,代表数据库中未有此信息.
                if (downloadTaskInfo == null) {
                    //此处将完整信息插入数据库中
                    downloadTaskInfo = new DownloadTaskInfo(downloadId, name, url, downloadStatus, savePath, totalSize, completedSize);
                    downloadDAO.insert(downloadTaskInfo);
                }

                //用于计算下载速度
                long finishTemp = completedSize;
                long time = System.currentTimeMillis();
                long speed;

                //如果一直可以读取到数据并且下载状态不是取消或暂停,就一直写入文件
                while ((length = bis.read(buffer)) > 0 && downloadStatus != DOWNLOAD_STATUS_CANCEL && downloadStatus != DOWNLOAD_STATUS_PAUSE) {
                    file.write(buffer, 0, length);
                    completedSize += length;

                    //每隔一秒通知监听
                    if ((System.currentTimeMillis() - time) >= 1000) {
                        if (totalSize <= 0 || downloadTaskInfo.getTotalSize() <= 0) {
                            downloadTaskInfo.setTotalSize(totalSize);
                        }
                        downloadTaskInfo.setCompletedSize(completedSize);
                        downloadTaskInfo.setDownloadStatus(downloadStatus);
                        //计算下载速度。计算原理：单位时间内下载的大小就是下载速度
                        speed = (completedSize - finishTemp) / (System.currentTimeMillis() - time);
                        downloadTaskInfo.setDownloadSpeed(speed);
                        time = System.currentTimeMillis();
                        finishTemp = completedSize;
                        notifyProgress();
                        //插入数据库
                        downloadDAO.update(downloadTaskInfo);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
            notifyProgress();
            e.printStackTrace();
        } catch (IOException e) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
            notifyProgress();
            e.printStackTrace();
        } finally {
            downloadTaskInfo.setCompletedSize(completedSize);
            downloadTaskInfo.setName(name);
            downloadDAO.update(downloadTaskInfo);

            if (bis != null) try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file != null) try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //下载完成,广播通知media扫描文件
        if (totalSize.equals(completedSize)) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
            downloadTaskInfo.setDownloadStatus(downloadStatus);
            downloadDAO.update(downloadTaskInfo);
            Uri contentUri = Uri.fromFile(new File(savePath + name));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
            downloadService.sendBroadcast(mediaScanIntent);
        }

        //取消下载,删除临时文件
        if(downloadStatus == DOWNLOAD_STATUS_CANCEL) {
            downloadDAO.deleteTask(downloadTaskInfo.getDownloadId());
            File temp = new File(savePath + name);
            if (temp.exists()) {
                temp.delete();
            }
        }

        notifyProgress();
    }

    private void prepareDownload() throws IOException {
        //从数据库获取下载信息,已完成大小和总大小以数据库为准
        downloadTaskInfo = downloadDAO.getDownLoadedList(downloadId);
        if (downloadTaskInfo != null) {
            completedSize = downloadTaskInfo.getCompletedSize();
            totalSize = downloadTaskInfo.getTotalSize();
        }

        File fileTemp = new File(savePath + name);
        //创建随机读写文件
        file = new RandomAccessFile(fileTemp, "rw");
        long fileLength = file.length();

        //数据库存储的已完成的大小大于文件的长度,以文件的长度为准
        if (completedSize > fileLength) {
            completedSize = fileLength;
        }

        //如果文件长度等于下载总大小,代表下载完成
        if (fileLength != 0 && totalSize == fileLength) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
            completedSize = totalSize;

            //将数据保存到数据库中
            downloadTaskInfo = new DownloadTaskInfo(downloadId, name, url, downloadStatus, savePath, totalSize, completedSize);
            downloadDAO.insert(downloadTaskInfo);

            LogUtil.d(name + "下载完成!");
            notifyProgress();
            return;
        } else if (fileLength > totalSize) {    //如果文件大小超过下载总大小,下载错误,重新下载
            completedSize = 0L;
            totalSize = 0L;
        }

        downloadStatus = DownloadStatus.DOWNLOAD_STATUS_START;
        notifyProgress();
    }

    private void notifyProgress() {
        downloadService.notifyDownloadStateChanged(downloadTaskInfo);
//        Log.i("DownloadTask","下载状态："+downloadStatus+"----"+downloadTaskInfo.getDownloadSpeed());
    }
}
