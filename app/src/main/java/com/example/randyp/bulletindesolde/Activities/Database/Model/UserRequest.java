package com.example.randyp.bulletindesolde.Activities.Database.Model;

public class UserRequest {
    public static final String TABLE_REQUEST = "userrequest";

    //Synchronised from the host server
    public static final String REQUEST_ID="request_id";
    public static final String REQUEST_MATRICULE="matricule";
    public static final String REQUEST_MONTH="month";
    public static final String REQUEST_YEAR="year";
    public static final String REQUEST_TRANSMISSION="transmission";
    public static final String REQUEST_CREATED_AT="created_at";


    private String request_matricule,
            request_month,request_year,request_transmission,request_created_at;
    private int request_id;

    //Create request table SQL query
    public static final String CREATE_TABLE_USER="CREATE TABLE " + TABLE_REQUEST + "("
            + REQUEST_ID + " INTEGER,"
            + REQUEST_MATRICULE + " TEXT,"
            + REQUEST_MONTH + " TEXT,"
            + REQUEST_YEAR + " TEXT,"
            + REQUEST_TRANSMISSION + " TEXT,"
            + REQUEST_CREATED_AT + " TEXT" + ")";


    public UserRequest() {
    }

    public UserRequest(int request_id, String request_matricule, String request_month,
                       String request_year, String request_transmission, String request_created_at) {
        this.request_id = request_id;
        this.request_matricule = request_matricule;
        this.request_month = request_month;
        this.request_year = request_year;
        this.request_transmission = request_transmission;
        this.request_created_at = request_created_at;
    }

    public int getRequest_id() {
        return request_id;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public String getRequest_matricule() {
        return request_matricule;
    }

    public void setRequest_matricule(String request_matricule) {
        this.request_matricule = request_matricule;
    }

    public String getRequest_month() {
        return request_month;
    }

    public void setRequest_month(String request_month) {
        this.request_month = request_month;
    }

    public String getRequest_year() {
        return request_year;
    }

    public void setRequest_year(String request_year) {
        this.request_year = request_year;
    }

    public String getRequest_transmission() {
        return request_transmission;
    }

    public void setRequest_transmission(String request_transmission) {
        this.request_transmission = request_transmission;
    }

    public String getRequest_created_at() {
        return request_created_at;
    }

    public void setRequest_created_at(String request_created_at) {
        this.request_created_at = request_created_at;
    }


}
