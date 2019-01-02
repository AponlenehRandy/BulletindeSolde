package com.example.randyp.bulletindesolde.Activities.Database.Model;

public class User {

    public static final String TABLE_USER = "userinfo";

    //Synchronised from the host server
    public static final String USER_ID = "id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER__VERIFICATION_TOKEN = "verification_token";
    //Create table SQL query
    public static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "("
            + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + USER_NAME + " TEXT,"
            + USER_EMAIL + " TEXT,"
            + USER__VERIFICATION_TOKEN + " TEXT" + ")";
    private int id;
    private String name, email, verification_token;

    public User() {
    }

    public User(int id, String name, String email, String verification_token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.verification_token = verification_token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerification_token() {
        return verification_token;
    }

    public void setVerification_token(String verification_token) {
        this.verification_token = verification_token;
    }
}
