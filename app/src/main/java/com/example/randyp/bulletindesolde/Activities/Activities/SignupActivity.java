package com.example.randyp.bulletindesolde.Activities.Activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
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
import com.example.randyp.bulletindesolde.Activities.InternetCheck.ConnectivityReceiver;
import com.example.randyp.bulletindesolde.Activities.Preferences.SessionManager;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener{

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

    private static final int request_code = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Manually checking internet connection
        checkConnection();

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


    }

    public void gotoLoginUser(View view){
        // Start the Signup activity
        startActivity(new Intent(SignupActivity.this,
                LoginActivity.class));
        finish();
    }

    public void registerUser(View view) {

        signupButton=findViewById(R.id.btn_signup);

        if (!validate()){
            return;
        }

        signupButton.setEnabled(false);

        nameText =findViewById(R.id.name);
        emailText=findViewById(R.id.email);
        passwordText=findViewById(R.id.password);

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO: Implement your own signup logic here.

        registerUser(name,email,password);
    }


    private boolean validate() {
        boolean valid = true;

        nameText =findViewById(R.id.name);
        emailText=findViewById(R.id.email);
        passwordText=findViewById(R.id.password);
        passwordverficationText=findViewById(R.id.password_verify);
        checkbox = findViewById(R.id.agreement_checkbox);


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
            showErrormsg(getResources().getString(R.string.check_agreement_error));
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
        pDialog.setMessage(getResources().getString(R.string.creating_account));
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

                                    createAccount(email,password,token);

                                    db.addUser(name,email,token);

                                    /**
                                     * Launching the registration success activity upon successful registration
                                     */

                                    Intent intent = new Intent(SignupActivity.this,
                                            SignupSuccessActivity.class);

                                    /**
                                     * PArsing user's information to be displayed
                                     * on the registration successful activity
                                     */
                                    intent.putExtra("user_name",name);
                                    intent.putExtra("user_email",email);

                                    startActivity(intent);
                                    finish();



                                }else{
                                    signupButton.setEnabled(true);
                                    emailText.setText(null);
                                    emailText.setError(getResources()
                                            .getString(R.string.insert_another_email));
                                    /**
                                     * creating a toast that display the error due to user account already taken
                                     */

                                    signupButton.setEnabled(true);

                                    showErrormsg(getResources().getString(R.string.registration_error));

                                }
                            }else {
                                //Error due to server break-dowm
                                signupButton.setEnabled(true);

                                showErrormsg(getResources().getString(R.string.server_error));

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


    private void showErrormsg(String error) {

        Toast.makeText(getApplicationContext(),error,Toast.LENGTH_SHORT).show();
    }

    public void createAccount(String email,String password,
                              String authToken){

        //Adding an account programmatically on my com.BDS account type
        String accountype= "com.BDS";
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account account = new Account(email,accountype);
        accountManager.addAccountExplicitly(account,password,null);
        //Saving authentication tokken under the account registered
        accountManager.setAuthToken(account,"full acces",authToken);

    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if (isConnected) {
            //show nothing if its connected
        } else {
            //move the connection lose activity
            Intent data = new Intent(this,InternetError.class);
            startActivityForResult(data,request_code);
        }
    }


    /**
     * These three next method should be copied to all other activitiest requiring
     * network change listener
     */

    @Override
    protected void onResume() {
        super.onResume();

        //Register connection status listener
        AppController.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        connectionlose(isConnected);
    }

    /**
     * Here is were the varoiuusu action for the connection lose or gain is taken care of
     * using the toast msg as a method of notifying the user for now
     * @param isConnected
     */
    private void connectionlose(boolean isConnected) {
        if (isConnected) {
            //show nothing if its connected
        } else {
            //move the connection lose activity
            Intent data = new Intent(this,InternetError.class);
            startActivityForResult(data,request_code);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == request_code)&&(resultCode==RESULT_OK)){

        }
    }

}
