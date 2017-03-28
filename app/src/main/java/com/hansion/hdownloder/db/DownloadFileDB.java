package com.hansion.hdownloder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/14 9:26
 */
public class DownloadFileDB extends SQLiteOpenHelper {

    private static final String DATABASENAME = "mydownload.db";
    private final Context mContext;
    private static final int VERSION = 1;
    private static DownloadFileDB sInstance = null;

    private DownloadFileDB(final Context context) {
        super(context, DATABASENAME, null, VERSION);
        mContext = context;
    }

    public static final synchronized DownloadFileDB getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DownloadFileDB(context.getApplicationContext());
        }
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        DownloadDAO.getInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DownloadDAO.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
    }
}
