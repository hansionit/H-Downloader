package com.hansion.hdownloder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hansion.hdownloder.download.HDownloadManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity  {

    @BindView(R.id.mUrlEdit)
    EditText mUrlEdit;

    private String url = "http://192.72.1.1/DCIM/100DSCIM/M0240024.MP4";
    private String url1 = "http://study.163.com/pub/study-android-official.apk";
    private String url2 = "http://s1.music.126.net/download/android/CloudMusic_3.4.1.133604_official.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.mAddDownloadTaskBt, R.id.mAddDownloadTaskBt1, R.id.mAddDownloadTaskBt2, R.id.mGoDownloadManager})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mAddDownloadTaskBt:
                String url = mUrlEdit.getText().toString().trim();
                if (TextUtils.isEmpty(url) || !url.startsWith("http://")) {
                    Toast.makeText(MainActivity.this, "请输入正确的下载地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                HDownloadManager.getInstance().down(url);
                break;
            case R.id.mAddDownloadTaskBt1:
                HDownloadManager.getInstance().down(url1);
                break;
            case R.id.mAddDownloadTaskBt2:
                HDownloadManager.getInstance().down(url2);
                break;
            case R.id.mGoDownloadManager:
                startActivity(new Intent(this, DownloadManagerActivity.class));
                break;
        }
    }
}
