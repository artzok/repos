package com.artzok.downloader.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * name：赵坤 on 2018/12/19 18:54
 * email：zhaokun@ziipin.com
 */
@Database(entities = {TaskMode.class}, version = 1, exportSchema = false)
public abstract class DbHelper extends RoomDatabase {
    public abstract TaskDao taskDao();
}
