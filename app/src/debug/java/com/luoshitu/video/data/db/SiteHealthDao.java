package com.luoshitu.video.data.db;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface SiteHealthDao {

    @Upsert
    void upsert(List<SiteHealthEntity> entities);

    @Query("SELECT * FROM SiteHealth ORDER BY checkedAt DESC")
    List<SiteHealthEntity> findAll();

    @Query("SELECT COUNT(*) FROM SiteHealth WHERE ok = 0")
    int countUnavailable();

    @Query("DELETE FROM SiteHealth")
    void clear();
}
