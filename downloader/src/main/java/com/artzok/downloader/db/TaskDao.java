package com.artzok.downloader.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * name：赵坤 on 2018/12/21 18:31
 * email：zhaokun@ziipin.com
 */
@Dao
public interface TaskDao {

    @Query("select * from TaskMode where task_id = :taskId")
    TaskMode getTask(String taskId);

    @Query("select * from TaskMode")
    List<TaskMode> getAllTask();

    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addTask(TaskMode task); // return row id

    @Update
    int updateTask(TaskMode task);

    @Delete
    void delete(TaskMode task);
}
