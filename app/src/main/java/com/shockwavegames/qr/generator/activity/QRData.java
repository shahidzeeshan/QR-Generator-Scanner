package com.shockwavegames.qr.generator.activity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.sql.Timestamp;
import java.util.List;

@Entity(tableName = "qrdata")
public class QRData {

    @PrimaryKey(autoGenerate = true)
    public int uid;
//    @ColumnInfo(name = "qrType")
    public String historyType;
    public String qrType;
    public String creationTime;
    public String content;

}
