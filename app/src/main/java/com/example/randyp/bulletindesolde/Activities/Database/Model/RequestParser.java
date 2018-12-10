package com.example.randyp.bulletindesolde.Activities.Database.Model;

import org.json.JSONObject;

public class RequestParser {
    public static RequestArticle parse(JSONObject jsonRequestArticle){
        RequestArticle requestArticle=new RequestArticle();
        requestArticle.setId(jsonRequestArticle.optString("id"));
        requestArticle.setMatricule(jsonRequestArticle.optString("matricule"));
        requestArticle.setMonth(jsonRequestArticle.optString("month"));
        requestArticle.setYear(jsonRequestArticle.optString("year"));
        requestArticle.setTransmission(jsonRequestArticle.optString("transmission"));
        requestArticle.setUpdated_at(jsonRequestArticle.optString("update_at"));
        return requestArticle;
    }
}
