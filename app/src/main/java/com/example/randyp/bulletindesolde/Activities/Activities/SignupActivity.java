package com.example.randyp.bulletindesolde.Activities.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Preferences.SessionManager;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "Signing up";
    EditText nameText;
     EditText emailText;
     EditText passwordText;
     EditText passwordverficationText;
     CheckBox checkbox;
     Button signupButton;
     TextView loginLink;
     private SessionManager session;
     private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameText =findViewById(R.id.name);
        emailText=findViewById(R.id.email);
        passwordText=findViewById(R.id.password);
        passwordverficationText=findViewById(R.id.password_verify);
        checkbox = findViewById(R.id.agreement_checkbox);

        signupButton=findViewById(R.id.btn_signup);
        loginLink=findViewById(R.id.link_login);


        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new DatabaseHelper(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(SignupActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// Start the Signup activity
                startActivity(new Intent(SignupActivity.this,
                        LoginActivity.class));
                finish();
            }
        });

    }

    private void signup() {
        
        if (!validate()) {
            return;
        }

        signupButton.setEnabled(false);


        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO: Implement your own signup logic here.

        registerUser(name,email,password);

    }


    private boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordverification = passwordverficationText.getText().toString();


        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 ) {
            passwordText.setError("The password must be at least 6 characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (!passwordverification.equals(password)){
            passwordverficationText.setError("Password input doesn't match");
            valid=false;
        }else {
            passwordverficationText.setError(null);
        }

        if (!checkbox.isChecked()){
            valid=false;
        }

        return valid;
    }


    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Creating account...");
        pDialog.setCancelable(false);
        pDialog.show();


        //Passing registration parameters
        Map<String,String> params = new HashMap<>();
        params.put("name",name);
        params.put("email",email);
        params.put("password", password);


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Appconfig.URL_REGISTER, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response.toString());
                        pDialog.hide();

                        try {
                            if (response.has("status")){
                                boolean error = response.getBoolean("status");

                                //checking for registration successful
                                if (error){


                                    //Now store the user in the SQLite
                                    String name=response.getString("name");
                                    String email = response.getString("email");
                                    String token=response.getString("token");

                                    db.addUser(name,email,token);

                                    /**
                                     * Launching the registration success activity upon successful registration
                                     */



                                }else{
                                    signupButton.setEnabled(true);
                                    emailText.setText(null);
                                    emailText.setError(getResources()
                                            .getString(R.string.insert_another_email));
                                    /**
                                     * creating a dialog box that display the error due to user account already taken
                                     */

                                    signupButton.setEnabled(true);
                                    startActivity(new Intent(SignupActivity.this,signupErrorActivity.class));


                                }
                            }else {
                                //Error due to server breakdowm

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                // hide the progress dialog
                pDialog.hide();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

}
