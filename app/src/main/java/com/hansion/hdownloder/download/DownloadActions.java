package com.hansion.hdownloder.download;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 16:11
 */
public class DownloadActions {

    //添加一个下载任务
    public static final String ADD_DOWNLOADTASK = "com.hansion.hdownloader.adddownloadtask";
    //添加多个下载任务
    public static final String ADD_MULTI_DOWNTASK = "com.hansion.hdownloader.addmultidowntask";
    //取消一个下载任务
    public static final String CANCLE_DOWNTASK = "com.hansion.hdownloader.cancledownloadtask";
    //取消全部下载任务
    public static final String CANCLE_ALL_DOWNTASK = "com.hansion.hdownloader.canclealldownloadtask";
    //开启全部下载任务
    public static final String START_ALL_DOWNTASK = "com.hansion.hdownloader.startalldownloadtask";
    //继续下载
    public static final String RESUME_START_DOWNTASK = "com.hansion.hdownloader.resumedownloadtask";
    //暂停单个下载任务
    public static final String PAUSE_DOWNTASK = "com.hansion.hdownloader.pausedownloadtask";
    //暂停全部下载任务
    public static final String PAUSE_ALLTASK = "com.hansion.hdownloader.pausealldownloadtask";
}
