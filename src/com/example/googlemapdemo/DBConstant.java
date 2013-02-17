package com.example.googlemapdemo;

import android.provider.BaseColumns;

public abstract class DBConstant implements BaseColumns {
    public static final String TABLE_DIARY = "user_diary";
//    public static final String DIARY_COARSE_LATLNG = "coarse_latlng";
    public static final String DIARY_PRECISE_LATITUDE = "precise_latitude";
    public static final String DIARY_PRECISE_LONGITUDE = "precise_longitude";
    public static final String DIARY_TEXT_CREATE_TIME = "create_time";
    public static final String DIARY_IS_SHARED = "is_share";
    public static final String DIARY_REAL_LOCATION = "world_loaction_discription";
    public static final String DIARY_TEXT = "text_body";
    
    // 我的关注列表
    public static final String TABLE_INTEREST = "my_interest";
    public static final String INTEREST_NAME = "interest_id";
    
    public static final String CMD_CREATE_DIARY = "create table if not exists " + TABLE_DIARY +
    								    " (" + _ID + " integer primary key, "
    								    	 + DIARY_REAL_LOCATION + " text default 'unknown', "
    										 + DIARY_PRECISE_LATITUDE + " real, "
    										 + DIARY_PRECISE_LONGITUDE + " real, "
    										 + DIARY_TEXT_CREATE_TIME + " text, " 
    										 + DIARY_IS_SHARED + " integer default 0, "
    										 + DIARY_TEXT + " text )";
    public static final String CMD_CREATE_INTEREST = "create table if not exists " + TABLE_INTEREST +
    									 "(" + _ID + " integer primary key, "
											 + INTEREST_NAME + " text uniq)";
    
    public static final String CMD_DROP_DIRARY = "drop table if exists "+TABLE_DIARY;
    public static final String CMD_DROP_INTEREST = "drop table if exists "+TABLE_INTEREST;
    // Prevents from being instantiated.
    private DBConstant(){}
}