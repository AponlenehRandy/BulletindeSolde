package com.example.randyp.bulletindesolde.Activities.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.Adapters.HistoryitemAdapter;
import com.example.randyp.bulletindesolde.Activities.Adapters.InboxItem;
import com.example.randyp.bulletindesolde.Activities.Adapters.InboxItemAdapter;
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

import static com.android.volley.VolleyLog.TAG;

public class History extends android.support.v4.app.Fragment {

    private List<InboxItem> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private HistoryitemAdapter mAdapter;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_main,container,false);

        recyclerView = view.findViewById(R.id.recycler_view_history);

        mAdapter = new HistoryitemAdapter(list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);


        //loading content from user's table on the server
        prepareInboxData();


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you set the title of the fragment here
        getActivity().setTitle(R.string.historyTitle);
    }

    private void prepareInboxData() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
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
                Appconfig.URL_REQUEST_PENDING, user_params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, "validated: "+response.toString());

                        try {
                            //checking for authorization error
                            boolean error = response.getBoolean("authorized");

                            //checking for request error
                            if (error){
                                /**
                                 * user token correct
                                 * Gathering data for the validated request
                                 */
                                JSONArray validatedArray = response.getJSONArray("pending");

                                if (validatedArray.length()==0){
                                    /**
                                     * display a page showing there is no history ro display for the user
                                     */


                                }else{
                                    list.clear();
                                    for (int i =0; i<validatedArray.length();i++) {
                                        JSONObject jsonObject = validatedArray.getJSONObject(i);

                                        Log.d(TAG, "loadvalided: "+jsonObject.toString());

                                        InboxItem inboxitem = new InboxItem();
                                        inboxitem.setMatricule(jsonObject.getString("matricule"));
                                        int month = Integer.parseInt(jsonObject.getString("month"));
                                        String requestMonth = months[month-1];
                                        inboxitem.setMonth(requestMonth);
                                        inboxitem.setYear(jsonObject.getString("year"));
                                        inboxitem.setDate(jsonObject.getString("date"));

                                        list.add(inboxitem);
                                    }
                                }



                                mAdapter.notifyDataSetChanged();
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
