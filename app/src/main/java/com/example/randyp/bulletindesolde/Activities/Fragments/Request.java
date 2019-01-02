package com.example.randyp.bulletindesolde.Activities.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.Activities.MainActivity;
import com.example.randyp.bulletindesolde.Activities.Adapters.Checkout;
import com.example.randyp.bulletindesolde.Activities.Adapters.CheckoutAdapter;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Decoration.MyDividerItemDecoration;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request extends android.support.v4.app.Fragment {
    private static final String TAG = "Request";
    Spinner month, year;
    EditText matricule;
    Button add_to_cart;
    Button checkOut;
    List monthList = new ArrayList<String>();
    List yearList = new ArrayList<String>();
    RecyclerView recyclerView;
    Checkout_bottom_sheetFragment bottom_sheetFragment;
    //checkout recycler view
    private List<Checkout> checkoutList = new ArrayList<>();
    private CheckoutAdapter checkoutAdapter;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        //creating a view to instantiate ui widget from the xml file
        View view = inflater.inflate(R.layout.request_main, container, false);


        month = view.findViewById(R.id.request_month);
        year = view.findViewById(R.id.request_year);
        matricule = view.findViewById(R.id.request_matricle);
        add_to_cart = view.findViewById(R.id.add_to_cart);
        checkOut = view.findViewById(R.id.checkout);

        // SqLite database handler
        db = new DatabaseHelper(getActivity());

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");


        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String charPattern = "[^A-za-z0-9]";

                String requestMatricule = matricule.getText().toString().replaceAll(charPattern, "");

                String matricleModel1 = "^(\\d{6})([A-za-z]{1})$";

                String matricleModel2 = "^([A-za-z]{1})(\\d{6})$";

                if (requestMatricule.matches(matricleModel1) || requestMatricule.matches(matricleModel2)) {
                    // matricule number correct, therefore save request to the server
                    saveRequest(token, requestMatricule,
                            String.valueOf((month.getSelectedItemPosition() + 1)),
                            year.getSelectedItem().toString().trim());
                    matricule.setText("");
                } else {
                    //send an alert dialog box to the user for invalid matricule number
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setMessage(getResources().getString(R.string.Matricule_invalid))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    matricule.setText("");
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });


        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getResources().getString(R.string.loading));
                progressDialog.show();

                final String[] months = getResources().getStringArray(R.array.months);

                //Send request to the server with the user token, matricle,month and year
                // Tag used to cancel the request
                final String tag_json_obj = "json_obj_req";

                // SqLite database handler
                db = new DatabaseHelper(getActivity());

                // Fetching user verification token from sqlite
                HashMap<String, String> user = db.getUserDetails();
                final String token = user.get("verification_token");


                //Passing login parameters
                Map<String, String> params = new HashMap<>();
                params.put("token", token);


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
                                    if (error) {
                                        /**
                                         * Fetching data from the server to display the invoice
                                         */

                                        bottom_sheetFragment = new Checkout_bottom_sheetFragment();
                                        bottom_sheetFragment.show(getFragmentManager(), bottom_sheetFragment.getTag());

                                        progressDialog.dismiss();


                                    } else {
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
        });


        // Adapter for recycler view for save payslip
        recyclerView = view.findViewById(R.id.checkout_recycler_view);
        checkoutAdapter = new CheckoutAdapter(checkoutList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(checkoutAdapter);

        //loading content from user's table on the server
        loadSaveRequest();

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //you set the title of the fragment here
        getActivity().setTitle(R.string.requestTitle);
    }

    private List<String> monthListCalc(int current_month) {
        String[] months = getResources().getStringArray(R.array.months);
        for (int i = 0; i < current_month; i++) {
            monthList.add(months[i]);
        }
        return monthList;
    }

    private List<String> YearListCalc(int current_year, int duration) {
        int year = current_year;
        yearList.add(current_year);
        for (int i = 0; i < duration - 1; i++) {
            year = year - 1;
            yearList.add(year);
        }
        return yearList;
    }

    private void saveRequest(String token, String matricule, String month, String year) {

        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";


        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("matricule", matricule);
        params.put("month", month);
        params.put("year", year);


        JSONObject user_params = new JSONObject(params);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                Appconfig.URL_REQUEST, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, "onResponse: "+response.toString());


                        try {
                            //checking for status error
                            if (response.has("status")) {
                                boolean error = response.getBoolean("status");

                                //checking for request error
                                if (error) {
                                    /**
                                     * request succesful
                                     * parsing data to the user request table
                                     */
                                    loadSaveRequest();
                                    ((MainActivity) getActivity()).initializeCountDrawer();

                                } else {
                                    /**
                                     * Creating an activity to display the user error info\
                                     */
                                }
                            } //checking for authorization error
                            else if (response.has("authorized")) {
                                boolean authorized = response.getBoolean("authorised");

                                if (!authorized) {
                                    //send an alert dialog box to notif the user that he/she is invalid to the account
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                    builder.setMessage("Unauthorized request")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });

                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }

                            } else {

                                //Error due to server breakdown

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

    private void loadSaveRequest() {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();

        final String[] months = getResources().getStringArray(R.array.months);

        //Send request to the server with the user token, matricle,month and year
        // Tag used to cancel the request
        final String tag_json_obj = "json_obj_req";

        // SqLite database handler
        db = new DatabaseHelper(getActivity());

        // Fetching user verification token from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String token = user.get("verification_token");


        //Passing login parameters
        Map<String, String> params = new HashMap<>();
        params.put("token", token);


        JSONObject user_params = new JSONObject(params);

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
                                /**
                                 * user token correct
                                 * Gathering data for the saved request
                                 */
                                JSONArray savedArray = response.getJSONArray("saved");
                                JSONObject obj = response.getJSONObject("periods");
                                final int current_year = obj.getInt("current_year");
                                int duration = obj.getInt("duration");
                                final int current_month = obj.getInt("current_month");
                                //Attaching values to the year spinner
                                final ArrayAdapter<String> yearSpinnerAdapter = new ArrayAdapter<String>(
                                        getContext(), android.R.layout.simple_spinner_item, YearListCalc(current_year, duration));
                                yearSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                year.setAdapter(yearSpinnerAdapter);
                                //Attaching values for the months
                                final ArrayAdapter<String> monthSpinnerAdapter = new ArrayAdapter<String>(
                                        getContext(), android.R.layout.simple_spinner_item, monthListCalc(current_month));
                                monthSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                month.setAdapter(monthSpinnerAdapter);
                                year.setOnItemSelectedListener(
                                        new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> adapterView, View view, int j, long l) {
                                                int selected_year = (int) year.getSelectedItem();
                                                String[] months = getResources().getStringArray(R.array.months);
                                                if (selected_year != current_year) {
                                                    monthList.clear();
                                                    for (int i = 0; i < months.length; i++) {
                                                        monthList.add(months[i]);
                                                    }
                                                } else {
                                                    monthList.clear();
                                                    for (int i = 0; i < current_month; i++) {
                                                        monthList.add(months[i]);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> adapterView) {

                                            }
                                        });

                                checkoutList.clear();
                                for (int i = 0; i < savedArray.length(); i++) {
                                    JSONObject jsonObject = savedArray.getJSONObject(i);
                                    Checkout checkout = new Checkout();
                                    checkout.setMatricle(jsonObject.getString("matricule"));
                                    int month = Integer.parseInt(jsonObject.getString("month"));
                                    String requestMonth = months[month - 1];
                                    checkout.setMonth(requestMonth);
                                    checkout.setYear(jsonObject.getString("year"));
                                    year.setAdapter(yearSpinnerAdapter);
                                    checkoutList.add(checkout);
                                }

                                checkoutAdapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                            } else {
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
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                // hide the progress dialog
                progressDialog.dismiss();
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
