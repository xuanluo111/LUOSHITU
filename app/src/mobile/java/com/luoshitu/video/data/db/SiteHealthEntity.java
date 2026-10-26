package com.luoshitu.video.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 逐源可用性体检结果（F1⑤）。独立于基座库（基座 AppDatabase 开了 fallbackToDestructiveMigration，
 * 升级会清库，不能承载我方数据）。
 */
@Entity(tableName = "SiteHealth", indices = {@Index("checkedAt")})
public class SiteHealthEntity {

    @PrimaryKey
    @NonNull
    public String siteKey;

    public String name;
    public int type;
    public boolean ok;
    /** ElderError.Kind，不可用时填写 */
    public String reasonKind;
    /** 家属模式可见的技术原因，老人界面绝不展示 */
    public String reasonTech;
    public long checkedAt;
    public long durationMs;

    public SiteHealthEntity(@NonNull String siteKey) {
        this.siteKey = siteKey;
    }
}
