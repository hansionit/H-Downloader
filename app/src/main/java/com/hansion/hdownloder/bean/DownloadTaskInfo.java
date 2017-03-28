package com.hansion.hdownloder.bean;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 15:50
 */
public class DownloadTaskInfo {

    private String downloadId;
    private String url;
    private String name;
    private String savePath;
    private int downloadStatus;
    private long downloadSpeed;
    private Long totalSize;
    private Long completedSize;

    public DownloadTaskInfo(String downloadId, String name, String url, int downloadStatus, String savePath, Long totalSize,Long completedSize) {
        this.downloadId = downloadId;
        this.url = url;
        this.totalSize = totalSize;
        this.downloadStatus = downloadStatus;
        this.savePath = savePath;
        this.completedSize = completedSize;
        this.name = name;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getCompletedSize() {
        return completedSize;
    }

    public void setCompletedSize(Long completedSize) {
        this.completedSize = completedSize;
    }
}
