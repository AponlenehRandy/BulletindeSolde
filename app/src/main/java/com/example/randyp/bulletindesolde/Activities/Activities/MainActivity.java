package com.example.randyp.bulletindesolde.Activities.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Fragments.Document;
import com.example.randyp.bulletindesolde.Activities.Fragments.History;
import com.example.randyp.bulletindesolde.Activities.Fragments.Message;
import com.example.randyp.bulletindesolde.Activities.Fragments.Request;
import com.example.randyp.bulletindesolde.Activities.Fragments.Setting;
import com.example.randyp.bulletindesolde.Activities.Preferences.SessionManager;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DatabaseHelper db;
    TextView userName, userEmail;
    SessionManager session;

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
                displaySlecetedScreen(R.id.nav_Request);
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Retrieving an instance of the two text widget in the navigation drawer
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.user_name);
        userEmail = headerView.findViewById(R.id.user_email);

        //change the user name based on data from the data base
        userName.setText(name);
        userEmail.setText(email);

        //add this line to display request form when the activity is loaded
        displaySlecetedScreen(R.id.nav_Request);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            Logout(token);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Logout(final String token) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Logging out...");
        pDialog.setCancelable(false);
        pDialog.show();


        //Passing login parameters
        Map<String,String> params = new HashMap<>();
        params.put("token",token);


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_LOGOUT, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response.toString()+token);
                        pDialog.hide();

                        try {
                            if (response.has("status")){
                                boolean error = response.getBoolean("status");

                                //checking for logout successful
                                if (error){
                                    /**
                                     * Close the login session
                                     */

                                    session.setLogin(false);

                                    //delete user data in the user data table
                                    db.deleteUsers();


                                    /**
                                     * Launching login activity for the next login
                                     */
                                    Intent intent = new Intent(MainActivity.this,
                                            LoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                }else{
                                    /**
                                     * Creating an activity to display the user error info\
                                     */



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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //calling the method displaySelectedScreen and passing the id of the selected menu
        displaySlecetedScreen(item.getItemId());
        //make the method blank
        return true;
    }

    private void displaySlecetedScreen (int itemId){

        //creating the fragment object which is selected
        android.support.v4.app.Fragment fragment = null;

        switch (itemId){
            case R.id.nav_Request:
                fragment = new Request();
                break;
            case R.id.nav_History:
                fragment = new History();
                break;
            case R.id.nav_messages:
                fragment = new Message();
                break;
            case R.id.nav_setting:
                fragment = new Setting();
                break;
            case R.id.nav_document:
                fragment = new Document();
                break;
        }

        //Replacing the fragment
        if (fragment!=null){
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame,fragment);
            ft.commit();
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

    }

}
