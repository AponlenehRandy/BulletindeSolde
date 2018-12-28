package com.example.randyp.bulletindesolde.Activities.Database.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "bulletindesolde_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create all tables here
        db.execSQL(User.CREATE_TABLE_USER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older tables if exist
        //updating all the rables in the database
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_USER);

        //Create table again
        onCreate(db);
    }


    /**
     * CRUD Operations for the user table
     */

    public void addUser(String name,String email,String verification_token){
        SQLiteDatabase db =this.getWritableDatabase();

        //adding user name in the user table
        ContentValues values = new ContentValues();
        values.put(User.USER_NAME, name); // Name
        values.put(User.USER_EMAIL, email); // Email
        values.put(User.USER__VERIFICATION_TOKEN, verification_token); // verification token

        // Inserting Row in the user table
        long id = db.insert(User.TABLE_USER, null, values);


        /**
         * Can do the same for other tables here before  closing the database
         */

        db.close(); // Closing database connection
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + User.TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("verification_token", cursor.getString(3));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(User.TABLE_USER, null, null);
        db.close();

    }
}
