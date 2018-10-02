package com.example.randyp.bulletindesolde.Activities.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;
    private ProgressDialog pDialog;
    private SessionManager session;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText =findViewById(R.id.email);
        passwordText=findViewById(R.id.password);
        loginButton=findViewById(R.id.btn_login);
        signupLink=findViewById(R.id.link_signup);

        //SQLite database handler
        db=new DatabaseHelper(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                startActivity(new Intent(LoginActivity.this,
                        SignupActivity.class));
                finish();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            return;
        }

        loginButton.setEnabled(false);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        loginuser(email,password);
    }


    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordText.setError("The password must be at least 6 characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    private void loginuser(final String email, final String password) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Authenticating...");
        pDialog.setCancelable(false);
        pDialog.show();


        //Passing login parameters
        Map<String,String> params = new HashMap<>();
        params.put("email",email);
        params.put("password", password);


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Appconfig.URL_LOGIN, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response.toString());
                        pDialog.hide();

                        try {
                            if (response.has("status")){
                                boolean error = response.getBoolean("status");

                                //checking for login successful
                                if (error){
                                    //User login successfully
                                    session.setLogin(true);

                                    //now store the user in the SQLite
                                    String name=response.getString("name");
                                    String email = response.getString("email");
                                    String token=response.getString("token");

                                    db.addUser(name,email,token);

                                    /**
                                     * Launching LoginSuccessActivity to welcome the user upon successful login data
                                     */
                                    Intent intent = new Intent(LoginActivity.this,
                                            LoginsuccessActivity.class);
                                    startActivity(intent);
                                    finish();

                                }else{
                                    /**
                                     * Creating an activity to display the user error info\
                                     */

                                    loginButton.setEnabled(true);

                                    startActivity(new Intent(LoginActivity.this, LoginErrorActivity.class));


                                }
                            }else {
                                loginButton.setEnabled(true);
                                //Error due to server breakdowm

                            }

                        } catch (JSONException e) {
                            loginButton.setEnabled(true);
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
