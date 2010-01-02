/*
 * Copyright (C) 2010 Jayesh Salvi (http://www.altcanvas.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altcanvas.twitspeak;

import android.database.*;
import android.database.sqlite.*;
import android.content.*;

public class Database 
{
    private static final String TAG = "Database";
    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "twitspeak.db";
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;

    private static final String DB_CREATE_TWITS_SQL = 
        "create table TWITS "+
        "(_id integer primary key autoincrement, "+
        "TEXT text, "+
        "TIMESTAMP text, "+
        "USERNAME text, "+
        "SCREENNAME text, "+
        "SHOWN integer);";

    public Database(Context context)
    {
        dbHelper = new DatabaseHelper(context, DB_NAME);
        db = dbHelper.getWritableDatabase();
    }

    public void close()
    {
        dbHelper.close();
    }

    public void markShown(Twit twit)
    {
        ContentValues values = new ContentValues();
        values.put("SHOWN", 1);
        synchronized(db) {
            db.update("TWITS", values, "_id = \""+twit.id+"\"", null);
        }
    }

    public void insertTwit(Twit twit)
    {
        ContentValues values = new ContentValues();
        values.put("TEXT", twit.text);
        values.put("TIMESTAMP", twit.timestamp);
        values.put("USERNAME", twit.username);
        values.put("SCREENNAME", twit.screenname);
        values.put("SHOWN", 0);
        synchronized(db) {
            db.insert("TWITS", null, values);
        }
    }

    public void deleteAllTwits(String whereClause)
    {
        synchronized(db) {
            db.delete("TWITS", whereClause, null);
        }
    }

    public Twit getLatestTwit()
    {
        Cursor cursor = null;
        String rawQuery = 
            "select _id, text, timestamp, username, screenname "+
            "from TWITS "+
            "where SHOWN != 1 "+
            "order by timestamp desc "+
            "limit 0,1";
        synchronized(db) {
            cursor = db.rawQuery(rawQuery, null);
        }

        if(cursor == null) return null;

        Twit twit = null;
        if(cursor.moveToFirst()) {
            twit = new Twit();
            twit.id = cursor.getInt(0);
            twit.text = cursor.getString(1);
            twit.timestamp = cursor.getString(2);
            twit.username = cursor.getString(3);
            twit.screenname = cursor.getString(4);
        }
        cursor.close();
        return twit;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context, String dbname)
        {
            super(context, dbname, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DB_CREATE_TWITS_SQL);
        }

        public void onOpen(SQLiteDatabase db)
        {
            super.onOpen(db);
        }
    
        @Override
        public void onUpgrade(
            SQLiteDatabase db, int oldVersion, int newVersion)
        {
        }
    }
}

