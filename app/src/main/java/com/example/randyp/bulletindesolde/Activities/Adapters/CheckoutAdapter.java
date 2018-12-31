package com.example.randyp.bulletindesolde.Activities.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.AppController.Appconfig;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.MyViewHolder> {

    private List<Checkout> checkoutList;
    private DatabaseHelper db;

    public CheckoutAdapter(List<Checkout> checkoutList) {
        this.checkoutList = checkoutList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkout_list_row,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Checkout checkout = checkoutList.get(position);
        holder.matricle.setText(checkout.getMatricle());
        holder.month.setText(checkout.getMonth());
        holder.year.setText(checkout.getYear());
    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView matricle,month,year;
        ImageView request_delete;

        public MyViewHolder(View view) {
            super(view);

            matricle = view.findViewById(R.id.checkout_matricle);
            month = view.findViewById(R.id.checkout_month);
            year =view.findViewById(R.id.checkout_year);
            request_delete=view.findViewById(R.id.checkoout_delete);
            request_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    /**
                     * Send data to the server to delete the saved request
                     * if successful, delete the data in the local database
                     * else do something else
                     */
                    final AlertDialog.Builder alertBox = new AlertDialog.Builder(view.getRootView().getContext());
                    alertBox.setMessage("Do you really want to delete this request?");
                    alertBox.setCancelable(false);
                    alertBox.setIcon(R.drawable.ic_info);
                    alertBox.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Do the actual delete with server response here
                            String request_matricule,request_month, request_year;
                            request_matricule=matricle.getText().toString();
                            final  String [] months = view.getResources().getStringArray(R.array.months);
                            request_month = month.getText().toString().trim();

                            int indexNum = Arrays.asList(months).indexOf(request_month)+1;
                            request_year = year.getText().toString();

                            final ProgressDialog progressDialog = new ProgressDialog(view.getRootView().getContext());
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage(view.getContext().getResources().getString(R.string.deleting_request));
                            progressDialog.show();

                            //Send request to the server with the user token, matricle,month and year
                            // Tag used to cancel the request
                            final String tag_json_obj = "json_obj_req";

                            // SqLite database handler
                            db = new DatabaseHelper(view.getContext());

                            // Fetching user verification token from sqlite
                            HashMap<String, String> user = db.getUserDetails();
                            final String token = user.get("verification_token");


                            //Passing login parameters
                            Map<String,String> params = new HashMap<>();
                            params.put("token",token);
                            params.put("matricule",request_matricule);
                            params.put("year",request_year);
                            params.put("month",String.valueOf(indexNum));


                            JSONObject user_params = new JSONObject(params);

                            final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                                    Appconfig.URL_DELETE_REQUEST, user_params,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                boolean error = response.getBoolean("status");

                                                //Checking for a successful request delete
                                                if (error){
                                                    // request succesful deleted and request removed from request table
                                                    removeAt(getPosition());
                                                    //update the count for the menu items
                                                    //((MainActivity)view.getRootView().getContext()).initializeCountDrawer();
                                                    progressDialog.dismiss();

                                                }else {
                                                    //Request not deleted and something went wrong
                                                    progressDialog.dismiss();
                                                 Toast.makeText(view.getContext(),view.getContext().getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();

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
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Do nothing here, cancel the dialog box

                        }
                    });

                    alertBox.show();

                }
            });

        }
    }

    // to remove or delete a row
    private void removeAt(int position) {
        checkoutList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,checkoutList.size());
    }

}
