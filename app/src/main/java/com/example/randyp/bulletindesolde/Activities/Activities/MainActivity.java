package com.example.randyp.bulletindesolde.Activities.Activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Fragments.History;
import com.example.randyp.bulletindesolde.Activities.Fragments.Inbox;
import com.example.randyp.bulletindesolde.Activities.Fragments.Request;
import com.example.randyp.bulletindesolde.Activities.Helper.AuthenticateAction;
import com.example.randyp.bulletindesolde.Activities.InternetCheck.ConnectivityReceiver;
import com.example.randyp.bulletindesolde.Activities.Preferences.SessionManager;
import com.example.randyp.bulletindesolde.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "MainActivity";
    private static final int request_code = 102;
    TextView userName, userEmail, InboxCount, RequestCount, HistoryCount;
    SessionManager session;
    private DatabaseHelper db;
    ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySelectionScreen(R.id.nav_Request);
            }
        });

        // SqLite database handler
        db = new DatabaseHelper(getApplicationContext());

        //session manager instantiate
        session = new SessionManager(getApplicationContext());

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("name");
        String email = user.get("email");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Retrieving an instance of the two text widget in the navigation drawer
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.user_name);
        userEmail = headerView.findViewById(R.id.user_email);
        userImage = headerView.findViewById(R.id.userimage);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                openUserProfile();
            }
        });

        /**
         * Retrieving instances of the inbox, request and
         * history count from teh navigation menue items
         */

        InboxCount = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_inbox));
        RequestCount = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_Request));
        HistoryCount = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_History));

        //This method will initialize the count value
        initializeCountDrawer();

        //change the user name based on data from the data base
        userName.setText(name);
        userEmail.setText(email);

        //add this line to display request form when the activity is loaded
        displaySelectionScreen(R.id.nav_Request);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            // SqLite database handler
            db = new DatabaseHelper(getApplicationContext());

            // Fetching user's token details from sqlite
            HashMap<String, String> user = db.getUserDetails();
            String token = user.get("verification_token");
            String email = user.get("email");
            Logout(token, email);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void Logout() {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(getResources().getString(R.string.logging_out));
        pDialog.setCancelable(false);
        pDialog.show();

        // SqLite database handler
        db = new DatabaseHelper(this);

        // Fetching user verification token from sqlite
        final HashMap<String, String> user = db.getUserDetails();
        final String email = user.get("email");

        /**
         * Close the login session
         */

        session.setLogin(false);

        //delete user data in the user data table
        db.deleteUsers();

        //removing account under account manager

        String accountype = "com.BDS";
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account account = new Account(email, accountype);
        Boolean success = accountManager.removeAccountExplicitly(account);

        Intent intent = new Intent(MainActivity.this,
                LoginActivity.class);
        startActivity(intent);
        finish();


        pDialog.dismiss();
    }


    private void Logout(final String token, final String email) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(getResources().getString(R.string.logging_out));
        pDialog.setCancelable(false);
        pDialog.show();


        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_LOGOUT, user_params,
                new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString() + token);
                        pDialog.hide();

                        try {
                            if (response.has("status")) {
                                boolean error = response.getBoolean("status");

                                //checking for logout successful
                                if (error) {
                                    /**
                                     * Close the login session
                                     */

                                    session.setLogin(false);

                                    //delete user data in the user data table
                                    db.deleteUsers();

                                    //removing account under account manager

                                    String accountype = "com.BDS";
                                    AccountManager accountManager = AccountManager.get(getApplicationContext());
                                    Account account = new Account(email, accountype);
                                    Boolean success = accountManager.removeAccountExplicitly(account);


                                    if (success) {
                                        showErrormsg("user account successfully removed in the account manager");
                                    } else {
                                        showErrormsg("failure to remove the account in account manager");
                                    }

                                    /**
                                     * Launching login activity for the next login
                                     */
                                    Intent intent = new Intent(MainActivity.this,
                                            LoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    /**
                                     * Creating an activity to display the user error info\
                                     */


                                }
                            } else {
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

        int MY_SOCKET_TIMEOUT_MS = 15000;
        int MY_RETRY_ITME = 1;
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                MY_RETRY_ITME,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the for contact us activity
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawer(GravityCompat.START);
            openUserProfile();
        }
        if (id == R.id.nav_contact_us) {
            // Handle the for contact us activity
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(MainActivity.this, Contact_us.class);
            startActivity(intent);

        }
        if (id == R.id.nav_about) {
            // show a dialog box about the application information
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawer(GravityCompat.START);

        } else {

            //calling the method displaySelectedScreen and passing the id of the selected menu
            displaySelectionScreen(item.getItemId());
            //make the method blank

        }
        return true;
    }

    public void displaySelectionScreen(int itemId) {

        //creating the fragment object which is selected
        android.support.v4.app.Fragment fragment = null;

        switch (itemId) {
            case R.id.nav_inbox:
                fragment = new Inbox();
                break;
            case R.id.nav_Request:
                fragment = new Request();
                break;
            case R.id.nav_History:
                fragment = new History();
                break;

        }

        //Replacing the fragment
        if (fragment != null) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

    }

    private void showErrormsg(String error) {

        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }

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

    public void initializeCountDrawer() {
        getHistoryCount();
        getInboxCount();
        getRequestCount();
    }

    private void getInboxCount() {
        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(this);

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");
        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        JSONObject user_params = new JSONObject(params);

        //Sending request for inbox count

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_REQUEST_VALIDATED, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //checking for authorization error
                            boolean error = response.getBoolean("authorized");

                            //checking for request error
                            if (error) {
                                JSONArray validatedArray = response.getJSONArray("validated");

                                if (validatedArray.length() != 0) {
                                    InboxCount.setGravity(Gravity.CENTER_VERTICAL);
                                    InboxCount.setTypeface(null, Typeface.BOLD);
                                    InboxCount.setTextColor(getResources().getColor(R.color.colorAccent));
                                    //count is added
                                    String count = String.valueOf(validatedArray.length());
                                    InboxCount.setText(count);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int MY_SOCKET_TIMEOUT_MS = 5000;
        int MY_RETRY_ITME = 1;
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                MY_RETRY_ITME,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void getRequestCount() {


        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(this);

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");
        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        JSONObject user_params = new JSONObject(params);

        //Sending request for inbox count

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_REQUEST_SAVED, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //checking for authorization error
                            boolean error = response.getBoolean("authorized");

                            //checking for request error
                            if (error) {
                                JSONArray validatedArray = response.getJSONArray("saved");

                                if (validatedArray.length() != 0) {
                                    RequestCount.setGravity(Gravity.CENTER_VERTICAL);
                                    RequestCount.setTypeface(null, Typeface.BOLD);
                                    RequestCount.setTextColor(getResources().getColor(R.color.colorAccent));
                                    //count is added
                                    String count = String.valueOf(validatedArray.length());
                                    RequestCount.setText(count);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int MY_SOCKET_TIMEOUT_MS = 5000;
        int MY_RETRY_ITME = 1;
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                MY_RETRY_ITME,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void getHistoryCount() {


        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(this);

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");
        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        JSONObject user_params = new JSONObject(params);

        //Sending request for inbox count

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_REQUEST_PENDING, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //checking for authorization error
                            boolean error = response.getBoolean("authorized");

                            //checking for request error
                            if (error) {
                                JSONArray validatedArray = response.getJSONArray("pending");

                                if (validatedArray.length() != 0) {
                                    HistoryCount.setGravity(Gravity.CENTER_VERTICAL);
                                    HistoryCount.setTypeface(null, Typeface.BOLD);
                                    HistoryCount.setTextColor(getResources().getColor(R.color.colorAccent));
                                    //count is added
                                    String count = String.valueOf(validatedArray.length());
                                    HistoryCount.setText(count);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int MY_SOCKET_TIMEOUT_MS = 5000;
        int MY_RETRY_ITME = 1;
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                MY_RETRY_ITME,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


    private void requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.permission_needed));
        builder.setMessage(getResources().getString(R.string.permssion_msg));
        builder.setPositiveButton(getResources().getString(R.string.GOTO_SETTING), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }

    public void openUserProfile() {
        //http call to gather user information first

        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(this);

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");
        final String email = user.get("email");
        final String name = user.get("name");


        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);


        JSONObject user_params = new JSONObject(params);

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.SHOW_PROFILE, user_params,
                new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (new AuthenticateAction(response).authenticateAction()) {

                                //checking for status error
                                boolean error = response.getBoolean("status");

                                //checking for request error
                                if (error) {
                                    /**
                                     * user token correct
                                     * Gathering data for the profile
                                     */

                                    JSONObject obj = response.getJSONObject("profile");
                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    intent.putExtra("phone", obj.getString("phone"));
                                    intent.putExtra("address", obj.getString("address"));
                                    intent.putExtra("region", obj.getString("region"));
                                    intent.putExtra("gender", obj.getString("gender"));
                                    intent.putExtra("email", email);
                                    intent.putExtra("name", name);
                                    startActivity(intent);

                                } else {
                                    //open edit profile page for the user to complete his/her user's information
                                    Intent intent = new Intent(MainActivity.this, Editprofile.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("name", name);
                                    intent.putExtra("phone", "");
                                    intent.putExtra("address", "");
                                    intent.putExtra("region", "");
                                    intent.putExtra("gender", "null");
                                    startActivity(intent);
                                }
                            } else {
                                Logout();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());


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







}