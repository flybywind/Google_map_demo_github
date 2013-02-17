package com.example.googlemapdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLdb extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	
	public SQLdb(Context context) {
		super(context, "MYWAY_DB", null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(DBConstant.CMD_CREATE_DIARY);
		db.execSQL(DBConstant.CMD_CREATE_INTEREST);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(DBConstant.CMD_DROP_DIRARY);
		db.execSQL(DBConstant.CMD_DROP_INTEREST);
		
		onCreate(db);
	}
}