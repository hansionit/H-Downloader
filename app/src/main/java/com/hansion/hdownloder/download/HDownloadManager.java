package com.hansion.hdownloder.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.hansion.hdownloder.App;
import com.hansion.hdownloder.utils.LogUtil;

import static com.hansion.hdownloder.download.DownloadActions.ADD_DOWNLOADTASK;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 18:36
 */
public class HDownloadManager {

    private DownloadService mService;
    private static HDownloadManager instance;
    private Context mContext = App.getAppContext();
    private ServiceConnection mServiceConn;

    private void initServiceConn() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((DownloadService.DownloadBinder)service).getService();
                LogUtil.e(mService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    public static HDownloadManager getInstance() {
        if (instance == null) {
            instance = new HDownloadManager();
        }
        return instance;
    }


    private HDownloadManager() {
        if (mService == null) {
            initServiceConn();
            Intent intent = new Intent(mContext, DownloadService.class);
            mContext.startService(intent);
            mContext.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
            LogUtil.e("mService是null 创建");
        }
    }

    public void setDownloadListener(String id, DownloadStatusListener listener) {
        if (mService != null) {
            mService.setDownloadListener(id, listener);
        }
    }

    public void removeDownloadListener() {
        if (mService != null) {
            mService.removeDownloadListener();
        }
    }



    public  void down(String url) {
        down(url,"");
    }

    public  void down(String url,String saveName) {
        if (mService != null) {
            mService.downloadAction(ADD_DOWNLOADTASK,saveName,url);
        } else {
            LogUtil.e("mService是null");
        }
    }

}
