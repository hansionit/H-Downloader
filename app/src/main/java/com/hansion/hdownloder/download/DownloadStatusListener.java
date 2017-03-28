package com.hansion.hdownloder.download;


import com.hansion.hdownloder.bean.DownloadTaskInfo;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/1 13:03
 */
public interface DownloadStatusListener {
    /**
     * 准备下载
     */
    void onPrepare(DownloadTaskInfo bean);

    /**
     * 开始下载
     */
    void onStart(DownloadTaskInfo bean);

    /**
     * 下载中
     */
    void onProgress(DownloadTaskInfo bean);

    /**
     * 暂停
     */
    void onStop(DownloadTaskInfo bean);

    /**
     * 下载完成
     */
    void onFinish(DownloadTaskInfo bean);

    /**
     * 下载失败
     */
    void onError(DownloadTaskInfo bean);

    /**
     * 删除成功
     */
    void onDelete(DownloadTaskInfo bean);
}
