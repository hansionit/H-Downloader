package com.hansion.hdownloder.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.hansion.hdownloder.App;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static com.hansion.hdownloder.download.DownloadActions.ADD_DOWNLOADTASK;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 18:36
 */
public class HDownloadManager {

    private static HDownloadManager instance;


    private DownloadService mService;
    private Context mContext = App.getAppContext();
    private ServiceConnection mServiceConn;
    private Handler mHandler;
    //任务列表 保证在Service为null时能够将添加的下载保存起来，直到service不为null时,添加至下载队列
    private LinkedList<String> mListTask;
    //信号量确保对任务列表操作的原子性
    private Semaphore mSemaphList ;
    //绑定服务成功
    private static final int MSG_BIND_SUC = 0x01;


    public static HDownloadManager getInstance() {
        if (instance == null) {
            instance = new HDownloadManager();
        }
        return instance;
    }


    private HDownloadManager() {
        mListTask = new LinkedList<>();
        mSemaphList = new Semaphore(1);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if(msg.what == MSG_BIND_SUC) {  //服务绑定成功
                    try {
                        mSemaphList.acquire();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while(!mListTask.isEmpty()) {
                        Bundle data = msg.getData();
                        String name = data.getString("name");
                        //执行列表的下载任务，并删除任务
                        mService.downloadAction(ADD_DOWNLOADTASK,name,mListTask.removeLast());
                    }
                    mSemaphList.release();
                }
                super.handleMessage(msg);
            }
        };
    }

    /**
     * 根据下载任务的id设置下载监听
     * @param id
     * @param listener
     */
    public void setDownloadListener(String id, DownloadStatusListener listener) {
        if (mService != null) {
            mService.setDownloadListener(id, listener);
        }
    }

    /**
     * 移除全部下载监听
     */
    public void removeDownloadListener() {
        if (mService != null) {
            mService.removeDownloadListener();
        }
    }


    /**
     * 添加一个下载任务
     * @param url
     */
    public  void down(String url) {
        down(url,"");
    }



    /**
     * 添加一个下载任务，并将文件名保存为saveName
     * @param url
     * @param saveName
     */
    public  void down(String url,String saveName) {
        if(TextUtils.isEmpty(url)) {
            return ;
        }

        if(mService == null) {  //服务未绑定
            bindService(saveName,url);      //绑定服务
            try {
                mSemaphList.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(mListTask.contains(url)) {  //是否已经存在此任务
                return;
            }
            mListTask.add(url);      //将下载任务添加到下载列表
            mSemaphList.release();
        }else {
            mService.downloadAction(ADD_DOWNLOADTASK,saveName,url);
        }
    }


    /**
     * 绑定服务
     * 先启动服务，再绑定服务
     */
    public void bindService(String saveName,String url) {
        initServiceConn(saveName,url);
        Intent intent = new Intent(mContext,DownloadService.class);
        mContext.startService(intent);
        mContext.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 初始化服务绑定
     */
    private void initServiceConn(final String fileName, final String url) {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                mService = ((DownloadService.DownloadBinder)service).getService();
                if(null != mService) {
                    Message message = new Message();
                    message.what = MSG_BIND_SUC;
                    Bundle bundle = new Bundle();
                    bundle.putString("name",fileName);
                    message.setData(bundle);
                    mHandler.sendMessage(message);  //绑定成功
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

}
