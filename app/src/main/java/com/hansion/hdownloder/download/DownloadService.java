package com.hansion.hdownloder.download;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.hansion.hdownloder.bean.DownloadTaskInfo;
import com.hansion.hdownloder.db.DownloadDAO;
import com.hansion.hdownloder.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hansion.hdownloder.Constant.SAVE_PATH;
import static com.hansion.hdownloder.download.DownloadActions.ADD_DOWNLOADTASK;
import static com.hansion.hdownloder.download.DownloadActions.ADD_MULTI_DOWNTASK;
import static com.hansion.hdownloder.download.DownloadActions.CANCLE_ALL_DOWNTASK;
import static com.hansion.hdownloder.download.DownloadActions.CANCLE_DOWNTASK;
import static com.hansion.hdownloder.download.DownloadActions.PAUSE_ALLTASK;
import static com.hansion.hdownloder.download.DownloadActions.PAUSE_DOWNTASK;
import static com.hansion.hdownloder.download.DownloadActions.RESUME_START_DOWNTASK;
import static com.hansion.hdownloder.download.DownloadActions.START_ALL_DOWNTASK;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_CANCEL;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_ERROR;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_INIT;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_PAUSE;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_PREPARE;
import static com.hansion.hdownloder.download.DownloadStatus.DOWNLOAD_STATUS_START;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 15:46
 */
public class DownloadService extends Service {

    private Context mContext;
    private Binder mBinder;

    //监听集合
    private Map<String, DownloadStatusListener> mListeners = new ConcurrentHashMap<>();
    //下载任务集合
    private static ArrayList<String> prepareTaskList = new ArrayList<>();

    //当下载状态发送改变的时候回调
    private ExecuteHandler handler = new ExecuteHandler();

    //数据库操作者
    private static DownloadDAO downloadDAO;

    private ExecutorService mThreadPool;

    //下载任务数量
    private int downTaskCount = 0;

    //当前下载任务
    private DownloadTask currentTask;

    //同时只允许一条线程执行的线程池
    private ExecutorService executorService;

    private int downTaskDownloaded = -1;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        downloadDAO = DownloadDAO.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }


    public void downloadAction(String action, String name, String url) {

        switch (action) {
            case ADD_DOWNLOADTASK:          //添加一个下载任务
                addDownloadTask(name, url);
                break;
            case ADD_MULTI_DOWNTASK:        //添加多个下载任务

                break;
            case CANCLE_DOWNTASK:           //取消一个下载任务

                break;
            case CANCLE_ALL_DOWNTASK:       //取消全部下载任务

                break;
            case START_ALL_DOWNTASK:        //开启全部下载任务

                break;
            case RESUME_START_DOWNTASK:     //继续下载

                break;
            case PAUSE_DOWNTASK:            //暂停单个下载任务

                break;
            case PAUSE_ALLTASK:             //暂停全部下载任务

                break;
        }
    }

    /**
     * 添加一个下载任务
     *
     * @param name 保存文件名。若为空,以url最后一个/后面的字符串命名
     * @param url  下载地址
     */
    private void addDownloadTask(String name, String url) {

        if (TextUtils.isEmpty(name)) {
            name = url.substring(url.lastIndexOf("/") + 1);
        }

        String id = (url).hashCode() + "";
        if (prepareTaskList.contains(id)) {
            Toast.makeText(this, "已存在于下载列表中:"+name, Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadTaskInfo downLoadedList = downloadDAO.getDownLoadedList(id);
        if (downLoadedList != null && downLoadedList.getDownloadStatus() == DOWNLOAD_STATUS_COMPLETED) {
            File file = new File(getSaveDir() + "/" + name);
            if (file.exists()) {
                Toast.makeText(this, "该文件已下载:"+name, Toast.LENGTH_SHORT).show();
                return;
            }
        }


        Toast.makeText(this, "已添加至下载列表:"+name, Toast.LENGTH_SHORT).show();

        DownloadTaskInfo downloadTaskInfo = new DownloadTaskInfo(
                (url).hashCode() + "",
                name,
                url,
                DOWNLOAD_STATUS_INIT,
                getSaveDir(),
                0L,
                0L
        );

        //添加到数据库
        downloadDAO.insert(downloadTaskInfo);
        //添加到下载队列中
        prepareTaskList.add(downloadTaskInfo.getDownloadId());
        downTaskCount++;

        if (currentTask != null) {
            LogUtil.d("add task wrong, current task is not null");
            return;
        }

        startTask();
    }


    public void startTask() {
        if (currentTask != null) {
            LogUtil.d("start task wrong, current task is running");
            return;
        }
        if (prepareTaskList.size() > 0) {
            DownloadTask downloadTask = null;
            //根据下载队列中第一位的id从数据库中取出该任务的信息
            DownloadTaskInfo taskInfo = downloadDAO.getDownLoadedList(prepareTaskList.get(0));

            if (taskInfo != null) {
                downloadTask = new DownloadTask(taskInfo, this);
            }
            if (downloadTask == null) {
                LogUtil.d("can't create downloadtask");
                return;
            }
            executorService.submit(downloadTask);
            currentTask = downloadTask;
        }
    }


    public String getSaveDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(SAVE_PATH);
            if (!file.exists()) {
                boolean r = file.mkdirs();
                if (!r) {
                    Toast.makeText(mContext, "储存卡无法创建文件", Toast.LENGTH_SHORT).show();
                    return null;
                }
                return file.getAbsolutePath() + "/";
            }
            return file.getAbsolutePath() + "/";
        } else {
            Toast.makeText(mContext, "没有储存卡", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    @SuppressLint("HandlerLeak")
    private class ExecuteHandler extends Handler {
        private ExecuteHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadTaskInfo bean = (DownloadTaskInfo) msg.obj;
            if (mListeners.containsKey(bean.getDownloadId())) {
                DownloadStatusListener observer = mListeners.get(bean.getDownloadId());
                switch (bean.getDownloadStatus()) {
                    case DOWNLOAD_STATUS_START:         // 开始下载
                        observer.onStart(bean);
                        break;
                    case DOWNLOAD_STATUS_PREPARE:       // 准备下载
                        observer.onPrepare(bean);
                        break;
                    case DOWNLOAD_STATUS_DOWNLOADING:   // 下载中
                        observer.onProgress(bean);
                        break;
                    case DOWNLOAD_STATUS_PAUSE:         // 暂停
                        observer.onStop(bean);
                        break;
                    case DOWNLOAD_STATUS_COMPLETED:     // 下载完毕
                        observer.onFinish(bean);
                        if (prepareTaskList.size() > 0) {
                            if (currentTask != null) {
                                prepareTaskList.remove(currentTask.getID());
                            }
                        }
                        currentTask = null;
                        downTaskDownloaded++;
                        startTask();
                        break;
                    case DOWNLOAD_STATUS_ERROR:         // 下载失败
                        observer.onError(bean);
                        break;
                    case DOWNLOAD_STATUS_CANCEL:        // 取消下载
                        observer.onDelete(bean);
                        break;
                }
            }
        }
    }


    /**
     * 当下载状态发送改变的时候调用
     */
    public void notifyDownloadStateChanged(DownloadTaskInfo bean) {
        Message message = handler.obtainMessage();
        message.obj = bean;
        handler.sendMessage(message);
    }

    public void setDownloadListener(String id, DownloadStatusListener listener) {
        if (!mListeners.containsKey(id)) {
            mListeners.put(id, listener);
        }
    }

    public void removeDownloadListener() {
        mListeners.clear();
    }


    /**
     * 绑定服务类
     */
    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    public static ArrayList<String> getPrepareTasks() {
        return prepareTaskList;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (null == mBinder) {
            mBinder = new DownloadBinder();
        }
        return mBinder;
    }
}
