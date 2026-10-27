package com.luoshitu.video.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 洛视频自有数据库。与基座 AppDatabase（库名 "tv"）完全隔离：
 * 不共享版本与迁移，绝不允许 destructive 回退（F9.3 数据不可丢）。
 */
@Database(entities = {SiteHealthEntity.class}, version = 1, exportSchema = false)
public abstract class LsDatabase extends RoomDatabase {

    public static final String NAME = "luoshitu";

    private static volatile LsDatabase instance;

    public static LsDatabase get(Context context) {
        if (instance == null) {
            synchronized (LsDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), LsDatabase.class, NAME).build();
                }
            }
        }
        return instance;
    }

    public abstract SiteHealthDao getSiteHealthDao();
}
