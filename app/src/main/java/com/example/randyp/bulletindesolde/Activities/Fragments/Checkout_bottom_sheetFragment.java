package com.example.randyp.bulletindesolde.Activities.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Checkout_bottom_sheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "Bottom sheet";
    private TextView delivery_time,unit_price_quantity, charges,total_amount;
    private EditText momo_number;
    private Button pay_now;

    private DatabaseHelper db;

    public static Checkout_bottom_sheetFragment newInstance() {
        return new Checkout_bottom_sheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payout_bottom_sheet, container,false);

        /**
         * Getting the instances of the views and adding listeners to the views
         */

        delivery_time = view.findViewById(R.id.checkout_delivery_time);
        unit_price_quantity = view.findViewById(R.id.checkout_unit_price_quantity);
        charges = view.findViewById(R.id.checkout_charges);
        total_amount = view.findViewById(R.id.checkout_total_amount);

        /**
         * retrieving data from the request fragment and setting them to the text views
         */

        invoice();

        momo_number = view.findViewById(R.id.momo_number);

        pay_now = view.findViewById(R.id.pay_now);

        pay_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payNow(momo_number.getText().toString().trim());
            }
        });

        return view;
    }

    private void invoice(){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        final String [] months=getResources().getStringArray(R.array.months);

        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(getActivity());

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");


        //Passing login parameters
        Map<String,String> params = new HashMap<>();
        params.put("token",token);


        JSONObject user_params = new JSONObject(params);

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_CHECKOUT, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            //checking for authorization error
                            boolean error = response.getBoolean("status");

                            //checking for request error
                            if (error){
                                /**
                                 * Fetching data from the server to display the invoice
                                 */

                                JSONObject jsonObject = response.getJSONObject("invoice");

                                delivery_time.setText(jsonObject.getString("delivery"));
                                unit_price_quantity.setText(jsonObject.getString("quantity")+" X "+ jsonObject.getString("unitprice"));
                                charges.setText(jsonObject.getString("charges"));
                                total_amount.setText(jsonObject.getString("total"));

                                progressDialog.dismiss();

                            }else{
                                /**
                                 * Creating an activity to display the user error info\
                                 */
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // hide the progress dialog
                progressDialog.dismiss();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public void payNow(String number){



        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        final String [] months=getResources().getStringArray(R.array.months);

        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(getActivity());

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");


        //Passing login parameters
        Map<String,String> params = new HashMap<>();
        params.put("token",token);
        params.put("momo",number);


        JSONObject user_params = new JSONObject(params);

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_PAYMENT, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            //checking for authorization error
                            boolean error = response.getBoolean("status");

                            //checking for request error
                            if (error){
                                /**
                                 * waiting for response to retrurn ot teh user(Succes or vrious responses)
                                 */

                                momo_number.setText("");


                                progressDialog.dismiss();


                            }else{
                                /**
                                 * Creating an activity to display the user error info\
                                 */
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // hide the progress dialog
                progressDialog.dismiss();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

}
