package com.example.randyp.bulletindesolde.Activities.Activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener {


    private static final String TAG = "LoginActivity";
    private static final int request_code = 100;
    EditText emailText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;
    private ProgressDialog pDialog;
    private SessionManager session;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_login);

        // Manually checking internet connection
        checkConnection();

        findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenWebsite();
            }
        });


        //SQLite database handler
        db = new DatabaseHelper(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }


    public void gotoRegisterUser(View view) {
        // Start the Signup activity
        startActivity(new Intent(LoginActivity.this,
                SignupActivity.class));
        finish();
    }

    public void loginUser(View view) {

        loginButton = findViewById(R.id.btn_login);
        Log.d(TAG, "Login");

        if (!validate()) {
            return;
        }


        loginButton.setEnabled(false);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        loginuser(email, password);
    }

    public boolean validate() {
        boolean valid = true;

        emailText = findViewById(R.id.login_email);
        passwordText = findViewById(R.id.login_password);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getResources().getString(R.string.email_error));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordText.setError(getResources().getString(R.string.password_error));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    private void loginuser(final String email,
                           final String password) {

        loginButton = findViewById(R.id.btn_login);

        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(getResources().getString(R.string.logging_in));
        pDialog.setCancelable(false);
        pDialog.show();


        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Appconfig.URL_LOGIN, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();

                        try {
                            if (response.has("status")) {
                                boolean error = response.getBoolean("status");

                                //checking for login successful
                                if (error) {
                                    //User login successfully
                                    session.setLogin(true);

                                    //now store the user in the SQLite
                                    String name = response.getString("name");
                                    String email = response.getString("email");
                                    String token = response.getString("token");

                                    createAccount(email, password, token);

                                    db.addUser(name, email, token);


                                    /**
                                     * Launching LoginSuccessActivity to welcome the user upon successful login data
                                     */
                                    Intent intent = new Intent(LoginActivity.this,
                                            LoginsuccessActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    /**
                                     * displaying the error message to the user as toast notification\
                                     */

                                    loginButton.setEnabled(true);
                                    showErrormsg(getResources().getString(R.string.login_error));

                                }
                            } else {
                                loginButton.setEnabled(true);
                                //Error due to server breakdowm

                                showErrormsg(getResources().getString(R.string.server_error));


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
                pDialog.dismiss();
            }
        });
        int MY_SOCKET_TIMEOUT_MS = 15000;
        int MY_RETRY_ITME = 1;
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                MY_RETRY_ITME,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void showErrormsg(String error) {

        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }


    public void createAccount(String email, String password,
                              String authToken) {

        //Adding an account programmatically on my com.BDS account type
        String accountype = "com.BDS";
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account account = new Account(email, accountype);
        accountManager.addAccountExplicitly(account, password, null);
        //Saving authentication tokken under the account registered
        accountManager.setAuthToken(account, "full acces", authToken);

    }


    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if (isConnected) {
            //show nothing if its connected
        } else {
            //move the connection lose activity
            Intent data = new Intent(this, InternetError.class);
            startActivityForResult(data, request_code);
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
     *
     * @param isConnected
     */
    private void connectionlose(boolean isConnected) {
        if (isConnected) {
            //show nothing if its connected
        } else {
            //move the connection lose activity
            Intent data = new Intent(this, InternetError.class);
            startActivityForResult(data, request_code);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == request_code) && (resultCode == RESULT_OK)) {

        }
    }

    private void OpenWebsite() {

        if (Locale.getDefault().getLanguage().equals("en")) {
            Uri uri = Uri.parse(Appconfig.FORGOTTEN_PASSWORD_URL_EN);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            Uri uri = Uri.parse(Appconfig.FORGOTTEN_PASSWORD_URL_FR);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
