package com.example.randyp.bulletindesolde.Activities.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Database.Model.User;
import com.example.randyp.bulletindesolde.Activities.Helper.AuthenticateAction;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Editprofile extends AppCompatActivity {

    private static final String TAG = "updating";
    EditText name, email, phone, whatsapp, region, address, old_pwd, new_pwd, verify_new_pwd;
    Button update, change_password;
    Spinner gender;
    private DatabaseHelper db;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //SQLite database handler
        db = new DatabaseHelper(getApplicationContext());

        Intent i = getIntent();
        name = findViewById(R.id.edit_user_name);
        region = findViewById(R.id.edit_user_region);
        address = findViewById(R.id.edit_user_address);
        phone = findViewById(R.id.edit_user_phone);
        email = findViewById(R.id.edit_user_email);
        gender = findViewById(R.id.user_gender);
        old_pwd = findViewById(R.id.old_password);
        new_pwd = findViewById(R.id.new_password);
        verify_new_pwd = findViewById(R.id.confirm_new_password);
        update = findViewById(R.id.update_profile);
        change_password = findViewById(R.id.change_password);


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changepassword()) {
                    //commit to the server
                }
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (canbeUpdated()) {
                    update.setEnabled(true);
                    update.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    update.setEnabled(false);
                    update.setBackgroundColor(getResources().getColor(R.color.inbox_divider));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (canbeUpdated()) {
                    update.setEnabled(true);
                    update.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    update.setEnabled(false);
                    update.setBackgroundColor(getResources().getColor(R.color.inbox_divider));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (canbeUpdated()) {
                    update.setEnabled(true);
                    update.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    update.setEnabled(false);
                    update.setBackgroundColor(getResources().getColor(R.color.inbox_divider));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        region.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (canbeUpdated()) {
                    update.setEnabled(true);
                    update.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    update.setEnabled(false);
                    update.setBackgroundColor(getResources().getColor(R.color.inbox_divider));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (canbeUpdated()) {
                    update.setEnabled(true);
                    update.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    update.setEnabled(false);
                    update.setBackgroundColor(getResources().getColor(R.color.inbox_divider));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        String genderM = i.getExtras().getString("gender");

        if (genderM.equals(getResources().getString(R.string.male))) {
            //Set the spinner to the male entry
            gender.setSelection(1);
        } else if (genderM.equals(getResources().getString(R.string.female))) {
            //set the spinner to the female entry
            gender.setSelection(2);
        } else {
            //set the spinner to the none entry
            gender.setSelection(0);
        }

        name.setText(i.getExtras().getString("name"));
        region.setText(i.getExtras().getString("region"));
        address.setText(i.getExtras().getString("address"));
        phone.setText(i.getExtras().getString("phone"));
        email.setText(i.getExtras().getString("email"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            //do the commit here to the server
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void updateProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.show();

        //http call to gather user information first

        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        final DatabaseHelper db = new DatabaseHelper(this);

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");


        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("name", name.getText().toString());
        params.put("email", email.getText().toString());
        params.put("phone", phone.getText().toString());
        params.put("address", address.getText().toString());
        params.put("region", region.getText().toString());
        params.put("gender", gender.getSelectedItem().toString());


        JSONObject user_params = new JSONObject(params);

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.UPDATE_PROFILE, user_params,
                new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);

                        try {
                            if (new AuthenticateAction(response).authenticateAction()) {

                                //checking for status error
                                boolean error = response.getBoolean("status");

                                //checking for request error
                                if (error) {
                                    /**
                                     * profile information successfully updated
                                     */
                                    progressDialog.dismiss();

                                    db.updateName(name.getText().toString());

                                    finish();
                                    openProfile();
                                } else {
                                    //something went wrong with the update
                                    progressDialog.dismiss();
                                }
                            } else {
                                progressDialog.dismiss();
                                ((MainActivity) getApplicationContext()).Logout();
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
        int MY_SOCKET_TIMEOUT_MS = 15000;
        int MY_RETRY_ITME = 1;
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                MY_RETRY_ITME,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private boolean hasedited() {
        boolean hasedited = false;
        Intent i = getIntent();
        /**
         * checking is there is any change in the profile
         */
        hasedited = !i.getExtras().getString("name").equals(name.getText().toString().trim()) ||
                !i.getExtras().getString("email").equals(email.getText().toString().trim()) ||
                !i.getExtras().getString("gender").equals(gender.getSelectedItem().toString().trim()) ||
                !i.getExtras().getString("phone").equals(phone.getText().toString().trim()) ||
                !i.getExtras().getString("region").equals(region.getText().toString().trim()) ||
                !i.getExtras().getString("address").equals(address.getText().toString().trim());
        return hasedited;
    }

    private boolean hasemptytext() {
        boolean emptyText = false;
        emptyText = !name.getText().toString().isEmpty() &&
                !email.getText().toString().isEmpty() &&
                !phone.getText().toString().isEmpty() &&
                !address.getText().toString().isEmpty() &&
                !region.getText().toString().isEmpty() &&
                !gender.getSelectedItem().equals(getResources().getString(R.string.none));
        return emptyText;
    }

    private boolean validate() {
        boolean valid = true;

        name = findViewById(R.id.edit_user_name);
        email = findViewById(R.id.edit_user_email);
        phone = findViewById(R.id.edit_user_phone);

        String user_name = name.getText().toString();
        String user_email = email.getText().toString();
        String user_phone = phone.getText().toString();


        if (user_email.isEmpty() || name.length() < 3) {
            name.setError(getResources().getString(R.string.name_error));
            valid = false;
        } else {
            name.setError(null);
        }

        if (user_email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {
            email.setError(getResources().getString(R.string.email_error));
            valid = false;
        } else {
            email.setError(null);
        }

        if (user_phone.isEmpty() || user_phone.length() < 9) {
            phone.setError(getResources().getString(R.string.phone_error));
            valid = false;
        } else {
            phone.setError(null);
        }

        return valid;
    }

    private boolean canbeUpdated() {
        boolean canbeupdated = false;
        canbeupdated = hasedited() && hasemptytext() && validate();
        return canbeupdated;

    }

    private boolean changepassword() {
        boolean changepassword = true;
        if (old_pwd.getText().toString().isEmpty() || old_pwd.getText().toString().length() < 6) {
            old_pwd.setError(getResources().getString(R.string.password_error));
            changepassword = false;
        } else {
            old_pwd.setError(null);
        }

        if (new_pwd.getText().toString().isEmpty() || new_pwd.getText().toString().length() < 6) {
            new_pwd.setError(getResources().getString(R.string.password_error));
            changepassword = false;
        } else {
            old_pwd.setError(null);
        }
        if (verify_new_pwd.getText().toString().equals(old_pwd.getText().toString())) {
            verify_new_pwd.setError(getResources().getString(R.string.password_mismatch));
            changepassword = false;
        } else {
            verify_new_pwd.setError(null);
        }
        return changepassword;
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);

        User user = new User();


        intent.putExtra("phone", phone.getText().toString());
        intent.putExtra("address", address.getText().toString());
        intent.putExtra("region", region.getText().toString());
        intent.putExtra("gender", gender.getSelectedItem().toString());
        intent.putExtra("email", email.getText().toString());
        intent.putExtra("name", name.getText().toString());

        startActivity(intent);
    }

}
