package com.example.randyp.bulletindesolde.Activities.Adapters;

public class InboxItem {
    private String matricule,month,year,date;

    public InboxItem() {
    }

    public InboxItem(String number, String matricule, String month, String year, String date) {
        this.matricule = matricule;
        this.month = month;
        this.year = year;
        this.date = date;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
