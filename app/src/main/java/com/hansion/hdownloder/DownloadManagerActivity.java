package com.hansion.hdownloder;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hansion.hdownloder.bean.DownloadTaskInfo;
import com.hansion.hdownloder.db.DownloadDAO;
import com.hansion.hdownloder.download.DownloadStatusListener;
import com.hansion.hdownloder.download.HDownloadManager;
import com.hansion.hdownloder.utils.LogUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadManagerActivity extends AppCompatActivity {

    private ListView mDownloadList;
    private DownloadDAO downloadDao;
    public List<DownloadTaskInfo> mDownloadFileInfos = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        mDownloadList = (ListView) findViewById(R.id.mDownloadList);
        downloadDao = new DownloadDAO(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadFileInfos = downloadDao.getDownLoadedListAllDowning();
        mDownloadList.setAdapter(new DownloadListAdapter(this,mDownloadFileInfos));
    }

    @Override
    protected void onPause() {
        super.onPause();
        HDownloadManager.getInstance().removeDownloadListener();
    }

    public class DownloadListAdapter extends CommonBaseAdapter<FileViewHolder, DownloadTaskInfo> {
        private SparseArray<FileViewHolder> mViewHolderArray;

        public DownloadListAdapter(Context context, List<DownloadTaskInfo> list) {
            super(context, list);
            mViewHolderArray = new SparseArray<>();
        }


        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View view = inflate(R.layout.item_download, parent);
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            DownloadTaskInfo downloadFileInfo = mDownloadFileInfos.get(position);
            holder.setDownloadId(Integer.parseInt(downloadFileInfo.getDownloadId()));
            mViewHolderArray.put(Integer.parseInt(downloadFileInfo.getDownloadId()), holder);
            holder.mName.setText(downloadFileInfo.getName());
            HDownloadManager.getInstance().setDownloadListener(downloadFileInfo.getDownloadId(), downloadStatusListener);
        }

        private DownloadStatusListener downloadStatusListener = new DownloadStatusListener() {

            private FileViewHolder checkCurrentHolder(int downloadId) {
                final FileViewHolder holder = mViewHolderArray.get(downloadId);
                if (holder.id != downloadId) {
                    return null;
                }
                return holder;
            }


            @Override
            public void onPrepare(DownloadTaskInfo bean) {

            }

            @Override
            public void onStart(DownloadTaskInfo bean) {
                final FileViewHolder holder = checkCurrentHolder(Integer.parseInt(bean.getDownloadId()));
                if (holder == null) {
                    return;
                }
                holder.updateDownloading(
                        (int) (100 * bean.getCompletedSize() / bean.getTotalSize()),
                        bean.getDownloadSpeed(),
                        bean.getTotalSize());
            }

            @Override
            public void onProgress(DownloadTaskInfo bean) {
                final FileViewHolder holder = checkCurrentHolder(Integer.parseInt(bean.getDownloadId()));
                if (holder == null) {
                    return;
                }
                holder.updateDownloading(
                        (int) (100 * bean.getCompletedSize() / bean.getTotalSize()),
                        bean.getDownloadSpeed(),
                        bean.getTotalSize());
            }

            @Override
            public void onStop(DownloadTaskInfo bean) {

            }

            @Override
            public void onFinish(DownloadTaskInfo bean) {
                final FileViewHolder tag = checkCurrentHolder(Integer.parseInt(bean.getDownloadId()));
                if (tag == null) {
                    return;
                }
                tag.updateFinished();
            }

            @Override
            public void onError(DownloadTaskInfo bean) {

            }

            @Override
            public void onDelete(DownloadTaskInfo bean) {

            }
        };

    }

    static class FileViewHolder extends CommonBaseAdapter.ViewHolder {

        @BindView(R.id.mPic)
        ImageView mPic;
        @BindView(R.id.mName)
        TextView mName;
        @BindView(R.id.mNetSpeed)
        TextView mNetSpeed;
        @BindView(R.id.mFileSize)
        TextView mFileSize;
        @BindView(R.id.mProgress)
        ProgressBar mProgress;
        @BindView(R.id.mImgState)
        ImageView mImgState;
        @BindView(R.id.mTextState)
        TextView mTextState;
        @BindView(R.id.checkbox)
        CheckBox checkbox;

        private int id;
        private boolean setInitValue = false;

        public void setInitValue (Long totalSize) {
            if(!setInitValue && totalSize > 0) {
                mFileSize.setText(FormetFileSize(totalSize));
                setInitValue = true;
            }
        }

        public void setDownloadId(final int id) {
            this.id = id;
        }

        public void updateDownloading(int progress, long speed,Long totalSize) {
            mProgress.setProgress(progress);
            mNetSpeed.setVisibility(View.VISIBLE);
            mNetSpeed.setText(FormetFileSize(speed * 1024)+"/s");
            setInitValue(totalSize);
            LogUtil.i(FormetFileSize(speed * 1024)+"/s");
        }


        public void updateFinished() {
            mNetSpeed.setText("下载完成");
            mProgress.setProgress(100);

        }

        public void updateWait(int progress) {
            mProgress.setProgress(progress);
        }

        public FileViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mProgress.setMax(100);
            mPic.setImageResource(R.mipmap.ic_launcher);
        }

        /**
         * 换算文件的大小
         */
        public static String FormetFileSize(long fileSize) {// 转换文件大小
            if (fileSize <= 0) {
                return "0M";
            }
            DecimalFormat df = new DecimalFormat("#.00");
            String fileSizeString = "";
            if (fileSize < 1024) {
                fileSizeString = df.format((double) fileSize) + "B";
            } else if (fileSize < 1048576) {
                fileSizeString = df.format((double) fileSize / 1024) + "K";
            } else if (fileSize < 1073741824) {
                fileSizeString = df.format((double) fileSize / 1048576) + "M";
            } else {
                fileSizeString = df.format((double) fileSize / 1073741824) + "G";
            }
            return fileSizeString;
        }
    }

}
