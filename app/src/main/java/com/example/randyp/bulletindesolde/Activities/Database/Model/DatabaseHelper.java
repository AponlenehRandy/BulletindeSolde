package com.example.randyp.bulletindesolde.Activities.Database.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.randyp.bulletindesolde.Activities.Database.Model.User;

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
        db.execSQL(UserRequest.CREATE_TABLE_USER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older tables if exist
        //updating all the rables in the database
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + UserRequest.TABLE_REQUEST);

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


    /**
     * CRUD operations for the Request table
     */

    public void addRequest(String request_id, String request_matricule, String request_month,
                           String request_year, String request_transmission,
                           String request_created_at){

        SQLiteDatabase db =this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("request_id",request_id); //request id from the server
        values.put("matricule",request_matricule);
        values.put("month",request_month);
        values.put("year",request_year);
        values.put("transmission",request_transmission);
        values.put("created_at",request_created_at);

        //inserting row
        long id = db.insert(UserRequest.TABLE_REQUEST,null,values);
        db.close();
    }


    //Takes already existed request id and fetch the request details
    public UserRequest getRequest(long id){
        //get readable database as we are not inserting anything
        SQLiteDatabase db =this.getReadableDatabase();

        Cursor cursor = db.query(UserRequest.TABLE_REQUEST,
                new String[]{UserRequest.REQUEST_ID, UserRequest.REQUEST_MATRICULE,
                        UserRequest.REQUEST_MONTH, UserRequest.REQUEST_YEAR,
                        UserRequest.REQUEST_TRANSMISSION, UserRequest.REQUEST_CREATED_AT},
                UserRequest.REQUEST_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);


        if (cursor != null)
            cursor.moveToFirst();

        cursor.moveToFirst();

        //prepare request data

        UserRequest userRequest = new UserRequest(
                cursor.getInt(cursor.getColumnIndex(UserRequest.REQUEST_ID)),
                cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_MATRICULE)),
                cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_MONTH)),
                cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_YEAR)),
                cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_TRANSMISSION)),
                cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_CREATED_AT)));

        // close the db connection
        cursor.close();

        return userRequest;

    }

    public List<UserRequest> getAllsaveRequest(){
        List<UserRequest> savedRequests = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + UserRequest.TABLE_REQUEST + " ORDER BY " +
                UserRequest.REQUEST_ID + " DESC" +
                " WHERE " + UserRequest.REQUEST_TRANSMISSION + " = saved;";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                UserRequest savedRequest = new UserRequest();
                UserRequest userRequest = new UserRequest();
                userRequest.setRequest_id(cursor.getInt(cursor.getColumnIndex(UserRequest.REQUEST_ID)));
                userRequest.setRequest_matricule(cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_MATRICULE)));
                userRequest.setRequest_month(cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_MONTH)));
                userRequest.setRequest_year(cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_YEAR)));
                userRequest.setRequest_transmission(cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_TRANSMISSION)));
                userRequest.setRequest_created_at(cursor.getString(cursor.getColumnIndex(UserRequest.REQUEST_CREATED_AT)));

                savedRequests.add(savedRequest);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return all saved request list
        return savedRequests;
    }




}
