package com.example.randyp.bulletindesolde.Activities.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.Adapters.InboxItem;
import com.example.randyp.bulletindesolde.Activities.Adapters.InboxItemAdapter;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.Activities.Decoration.MyDividerItemDecoration;
import com.example.randyp.bulletindesolde.Activities.ItemTouchHelper.RecyclerItemTouchHelper;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class Inbox extends android.support.v4.app.Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private List<InboxItem> inboxList = new ArrayList<>();
    private RecyclerView recyclerView;
    private InboxItemAdapter mAdapter;
    private DatabaseHelper db;
    TextView numb_files;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.inbox_main,container,false);

        recyclerView = view.findViewById(R.id.recycler_view);

        mAdapter = new InboxItemAdapter(inboxList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        //loading content from user's table on the server
        prepareInboxData();
        return view;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you set the title of the fragment here
        getActivity().setTitle(R.string.InboxTitle);
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
                Appconfig.URL_REQUEST_VALIDATED, user_params,
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
                                JSONArray validatedArray = response.getJSONArray("validated");

                                if (validatedArray.length()==0){
                                    /**
                                     * Launch the request payslip screen
                                     */
                                    //((MainActivity)getActivity()).displaySelectionScreen(R.id.nav_Request);

                                }else{
                                    inboxList.clear();
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

                                        inboxList.add(inboxitem);
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

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof InboxItemAdapter.MyViewHolder) {
            // get the removed item name to display it in snack bar
            final String matricule,month,year;
            matricule = inboxList.get(viewHolder.getAdapterPosition()).getMatricule();
            month = inboxList.get(viewHolder.getAdapterPosition()).getMonth();
            year = inboxList.get(viewHolder.getAdapterPosition()).getYear();

            // backup of removed item for undo purpose
            final InboxItem deletedItem = inboxList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            final View view=getActivity().findViewById(R.id.content_frame);
            final  String [] months = view.getResources().getStringArray(R.array.months);


            int indexNum = Arrays.asList(months).indexOf(month)+1;

            final ProgressDialog progressDialog = new ProgressDialog(view.getRootView().getContext());
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Deleting request...");
            progressDialog.show();

            //Send request to the server with the user token, matricle,month and year
            // Tag used to cancel the request
            final String tag_json_obj = "json_obj_req";

            // SqLite database handler
            db = new DatabaseHelper(view.getContext());

            // Fetching user verification token from sqlite
            final HashMap<String, String> user = db.getUserDetails();
            final String token = user.get("verification_token");


            //Passing login parameters
            final Map<String,String> params = new HashMap<>();
            params.put("token",token);
            params.put("matricule",matricule);
            params.put("year",year);
            params.put("month",String.valueOf(indexNum));


            final JSONObject user_params = new JSONObject(params);

            final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                    Appconfig.URL_DELETE_VALIDATED, user_params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                boolean error = response.getBoolean("status");

                                //Checking for a successful request delete
                                if (error){
                                    /**
                                     * request succesful deleted and request removed from validated table
                                     * show snackbar to undo delete
                                      */
                                    progressDialog.dismiss();
                                    mAdapter.removeItem(viewHolder.getAdapterPosition());

                                    // showing snack bar with Undo option
                                    Snackbar snackbar = Snackbar.make(view, matricule + " removed from cart!", 30000);
                                    snackbar.setAction("UNDO", new View.OnClickListener() {
                                        @Override
                                        public void onClick(final View view) {

                                            // undo is selected, restore the deleted item
                                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                                                    Appconfig.URL_RESTORE_VALIDATED, user_params,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            try {
                                                                boolean error = response.getBoolean("status");

                                                                if (error){
                                                                    /**
                                                                     * Serever restoration of the validated payslip completed
                                                                     * restore back the deleted validated payslip
                                                                     */
                                                                    mAdapter.restoreItem(deletedItem, deletedIndex);
                                                                }else{
                                                                    //something went wrong
                                                                    Toast.makeText(view.getContext(), "Oops!Something went Wrong", Toast.LENGTH_SHORT).show();
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
                                            AppController.getInstance().addToRequestQueue(jsonObjectRequest,tag_json_obj);
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.YELLOW);
                                    snackbar.show();

                                    //update the count for the menu items
                                    //((MainActivity)view.getRootView().getContext()).initializeCountDrawer();


                                }else {
                                    //Request not deleted and something went wrong
                                    progressDialog.dismiss();
                                    // undo is selected, restore the deleted item
                                    mAdapter.removeItem(viewHolder.getAdapterPosition());
                                    mAdapter.restoreItem(deletedItem, deletedIndex);
                                    Toast.makeText(view.getContext(),"Oops! Something Went Wrong. Try again later", Toast.LENGTH_SHORT).show();

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

            AppController.getInstance().addToRequestQueue(jsonObjReq,tag_json_obj);

            // remove the item from recycler view
            /**
             * Sending an http call to remove the item from the server
             */
        }
    }
}
