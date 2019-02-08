package com.example.randyp.bulletindesolde.Activities.Activities;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Helper.AuthenticateAction;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contact_us extends AppCompatActivity {

    Spinner category_spinner;
    EditText comment;
    Button send_feedback;
    List categoryList = new ArrayList<String>();
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        category_spinner = findViewById(R.id.category);
        comment = findViewById(R.id.comment);
        send_feedback = findViewById(R.id.send_feedback);

        send_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (comment.getText().toString().trim().isEmpty()) {
                    //dont send comment
                } else {
                    sendFeedback();
                    comment.setText("");
                }
            }
        });

        /**
         * Attaching category to the spinner
         */

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getCategory());

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(categoryAdapter);

    }

    private void sendFeedback() {

        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(getResources().getString(R.string.sending_comment));
        pDialog.setCancelable(false);
        pDialog.show();


        // SqLite database handler
        db = new DatabaseHelper(getApplicationContext());

        // Fetching user's token details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("subject", category_spinner.getSelectedItem().toString());
        params.put("message", comment.getText().toString().trim());


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Appconfig.URL_CONTACT_US, user_params,
                new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();

                        try {
                            if (new AuthenticateAction(response).authenticateAction()) {

                                if (response.has("status")) {
                                    boolean error = response.getBoolean("status");
                                    if (error) {
                                        pDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),
                                                getResources().getString(R.string.mesasge_send), Toast.LENGTH_LONG).show();
                                        comment.setText("");
                                    } else {
                                        pDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),
                                                getResources().getString(R.string.an_internet_error_occured_please_try_again), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                pDialog.dismiss();
                                //logging out user because unverified is false
                                ((MainActivity) getBaseContext()).Logout();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
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

    private List<String> getCategory() {
        String[] cat = getResources().getStringArray(R.array.categoroy);

        for (int i = 0; i < cat.length; i++) {
            categoryList.add(cat[i]);
        }
        return categoryList;
    }


}
