package com.example.randyp.bulletindesolde.Activities.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticateAction {

    JSONObject response;

    public AuthenticateAction(JSONObject response) {
        this.response = response;
    }

    public boolean authenticateAction() {
        boolean action = false;
        try {
            Boolean isverified = response.getBoolean("isverified");
            Boolean authorized = response.getBoolean("authorized");

            action = isverified && authorized;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return action;
    }

}
