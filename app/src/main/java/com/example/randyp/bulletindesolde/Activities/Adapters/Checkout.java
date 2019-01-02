package com.example.randyp.bulletindesolde.Activities.Adapters;

public class Checkout {

    private String Matricle, month, year;

    public Checkout() {
    }

    public Checkout(String matricle, String month, String year) {
        Matricle = matricle;
        this.month = month;
        this.year = year;
    }

    public String getMatricle() {
        return Matricle;
    }

    public void setMatricle(String matricle) {
        Matricle = matricle;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
