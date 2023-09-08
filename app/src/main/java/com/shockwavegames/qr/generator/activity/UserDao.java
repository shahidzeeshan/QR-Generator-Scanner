package com.shockwavegames.qr.generator.activity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM qrdata ORDER BY creationTime DESC")
    List<QRData> getAll();

    @Insert
    void insert(QRData user);

    @Delete
    void delete(QRData user);
}
