package com.hansion.hdownloder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hansion.hdownloder.download.HDownloadManager;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private String url = "http://192.72.1.1/DCIM/100DSCIM/M0240024.MP4";
    private EditText mUrlEdit;
    private Button mAddDownloadTaskBt;
    private Button mGoDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUrlEdit = (EditText) findViewById(R.id.mUrlEdit);
        mAddDownloadTaskBt = (Button) findViewById(R.id.mAddDownloadTaskBt);
        mGoDownloadManager = (Button) findViewById(R.id.mGoDownloadManager);

        mAddDownloadTaskBt.setOnClickListener(this);
        mGoDownloadManager.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mAddDownloadTaskBt :
                String url = mUrlEdit.getText().toString().trim();
                if(TextUtils.isEmpty(url) || !url.startsWith("http://")) {
                    Toast.makeText(MainActivity.this,"请输入正确的下载地址",Toast.LENGTH_SHORT).show();
                    return;
                }
                HDownloadManager.getInstance().down(url);
                break;
            case R.id.mGoDownloadManager :
                startActivity(new Intent(this,DownloadManagerActivity.class));
                break;
        }
    }
}
