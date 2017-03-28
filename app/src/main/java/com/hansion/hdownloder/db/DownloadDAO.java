package com.hansion.hdownloder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hansion.hdownloder.bean.DownloadTaskInfo;
import com.hansion.hdownloder.download.DownloadStatus;

import java.util.ArrayList;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/3/27 16:39
 */
public class DownloadDAO {

    private static DownloadDAO sInstance = null;
    private DownloadFileDB mDwnloadFileDB = null;


    public DownloadDAO(final Context context) {
        mDwnloadFileDB = DownloadFileDB.getInstance(context);
    }

    public static synchronized DownloadDAO getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DownloadDAO(context.getApplicationContext());
        }

        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + DownFileStoreColumns.NAME + " ("
                + DownFileStoreColumns.ID + " TEXT NOT NULL PRIMARY KEY,"
                + DownFileStoreColumns.TOOL_SIZE + " INT NOT NULL,"
                + DownFileStoreColumns.FILE_LENGTH + " INT NOT NULL, "
                + DownFileStoreColumns.URL + " TEXT NOT NULL,"
                + DownFileStoreColumns.DIR + " TEXT NOT NULL,"
                + DownFileStoreColumns.FILE_NAME + " TEXT NOT NULL,"
                + DownFileStoreColumns.DOWNSTATUS + " INT NOT NULL);");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DownFileStoreColumns.NAME);
        onCreate(db);
    }


    /**
     * 插入一条数据
     *
     * @param info
     */
    public synchronized void insert(DownloadTaskInfo info) {
        Log.d("dataen", " id = " + info.getDownloadId());
        final SQLiteDatabase database = mDwnloadFileDB.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(8);
            values.put(DownFileStoreColumns.ID, info.getDownloadId());
            values.put(DownFileStoreColumns.TOOL_SIZE, info.getTotalSize());
            values.put(DownFileStoreColumns.FILE_LENGTH, info.getCompletedSize());
            values.put(DownFileStoreColumns.URL, info.getUrl());
            values.put(DownFileStoreColumns.DIR, info.getSavePath());
            values.put(DownFileStoreColumns.FILE_NAME, info.getName());
            values.put(DownFileStoreColumns.DOWNSTATUS, info.getDownloadStatus());
            database.replace(DownFileStoreColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    /**
     * 更新数据
     *
     * @param info
     */
    public synchronized void update(DownloadTaskInfo info) {

        final SQLiteDatabase database = mDwnloadFileDB.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(6);
            values.put(DownFileStoreColumns.TOOL_SIZE, info.getTotalSize());
            values.put(DownFileStoreColumns.FILE_LENGTH, info.getCompletedSize());
            values.put(DownFileStoreColumns.URL, info.getUrl());
            values.put(DownFileStoreColumns.DIR, info.getSavePath());
            values.put(DownFileStoreColumns.FILE_NAME, info.getName());
            values.put(DownFileStoreColumns.DOWNSTATUS, info.getDownloadStatus());
            database.update(DownFileStoreColumns.NAME, values, DownFileStoreColumns.ID + " = ?",
                    new String[]{info.getDownloadId()});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    /**
     * 删除一条数据
     *
     * @param Id DownloadTask的id
     */
    public void deleteTask(String Id) {
        final SQLiteDatabase database = mDwnloadFileDB.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + " = ?", new String[]
                {String.valueOf(Id)});
    }

    /**
     * 删除多条数据
     *
     * @param Id DownloadTask的id
     */
    public void deleteTask(String[] Id) {
        final SQLiteDatabase database = mDwnloadFileDB.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + " = ?", Id);
    }


    /**
     * 删除全部正在下载的数据
     */
    public void deleteAllDownloadingTasks() {
        ArrayList<String> results = new ArrayList<>();
        final SQLiteDatabase database = mDwnloadFileDB.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(DownFileStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getInt(6) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        results.add(cursor.getString(0));

                } while (cursor.moveToNext());
            }
            String[] t = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                t[i] = results.get(i);
            }
            final StringBuilder selection = new StringBuilder();
            selection.append(DownFileStoreColumns.ID + " IN (");
            for (int i = 0; i < t.length; i++) {
                selection.append(t[i]);
                if (i < t.length - 1) {
                    selection.append(",");
                }
            }
            selection.append(")");
            database.delete(DownFileStoreColumns.NAME, selection.toString(), null);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    /**
     * 清空所有下载数据
     */
    public synchronized void deleteAll() {
        final SQLiteDatabase database = mDwnloadFileDB.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, null, null);
    }

    /**
     * 根据id取得下载任务信息
     *
     * @param Id
     * @return
     */
    public synchronized DownloadTaskInfo getDownLoadedList(String Id) {
        Cursor cursor = null;
        DownloadTaskInfo entity = null;
        try {
            cursor = mDwnloadFileDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
                    DownFileStoreColumns.ID + " = ?", new String[]{String.valueOf(Id)}, null, null, null);
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                do {
                    entity = new DownloadTaskInfo(cursor.getString(0), cursor.getString(5), cursor.getString(3),
                            cursor.getInt(6), cursor.getString(4), cursor.getLong(1), cursor.getLong(2));
                } while (cursor.moveToNext());
                return entity;
            } else return null;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    /**
     * 获取全部正在下载的任务信息
     *
     * @return
     */
    public synchronized ArrayList<DownloadTaskInfo> getDownLoadedListAllDowning() {
        ArrayList<DownloadTaskInfo> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDwnloadFileDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getInt(6) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        results.add(new DownloadTaskInfo(cursor.getString(0), cursor.getString(5), cursor.getString(3),
                                cursor.getInt(6), cursor.getString(4), cursor.getLong(1), cursor.getLong(2)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    /**
     * 获取全部正在下载的任务信息
     *
     * @return
     */
    public synchronized String[] getDownLoadedListAllDowningIds() {
        ArrayList<String> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDwnloadFileDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getInt(6) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        results.add(cursor.getString(0));

                } while (cursor.moveToNext());
            }
            String[] t = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                t[i] = results.get(i);
            }
            return t;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    public interface DownFileStoreColumns {
        //表名
        String NAME = "downfile_info";

        String ID = "id";
        String TOOL_SIZE = "totalsize";
        String FILE_LENGTH = "complete_length";
        String URL = "url";
        String DIR = "dir";
        String FILE_NAME = "file_name";
        String DOWNSTATUS = "notification_type";
    }

}
