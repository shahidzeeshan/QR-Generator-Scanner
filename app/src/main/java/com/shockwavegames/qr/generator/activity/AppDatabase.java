package com.shockwavegames.qr.generator.activity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {QRData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
