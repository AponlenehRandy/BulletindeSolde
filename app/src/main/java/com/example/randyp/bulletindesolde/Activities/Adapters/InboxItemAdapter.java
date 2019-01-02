package com.example.randyp.bulletindesolde.Activities.Adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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

import static com.android.volley.VolleyLog.TAG;

public class InboxItemAdapter extends RecyclerView.Adapter<InboxItemAdapter.MyViewHolder> {

    private List<InboxItem> inboxList;
    private DatabaseHelper db;

    public InboxItemAdapter(List<InboxItem> inboxList) {
        this.inboxList = inboxList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_recyclerview_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        InboxItem item = inboxList.get(position);

        holder.matricule.setText(item.getMatricule());
        holder.month.setText(item.getMonth());
        holder.year.setText(item.getYear());
        holder.date.setText(item.getDate());

    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }

    public void removeItem(int position) {
        inboxList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(InboxItem inboxItem, int position) {
        inboxList.add(position, inboxItem);
        // notify item added by position
        notifyItemInserted(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView number, matricule, month, year, date;
        public ImageView item_delete, item_download;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);

            matricule = view.findViewById(R.id.item_matricule);
            month = view.findViewById(R.id.item_month);
            year = view.findViewById(R.id.item_year);
            date = view.findViewById(R.id.item_date);
            item_download = view.findViewById(R.id.item_download);
            item_delete = view.findViewById(R.id.item_delete);

            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);

            item_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    final ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(view.getContext().getResources().getString(R.string.loading));
                    progressDialog.show();

                    final String[] months = view.getContext().getResources().getStringArray(R.array.months);

                    //Send request to the server with the user token, matricle,month and year
                    // Tag used to cancel the request
                    final String tag_json_obj = "json_obj_req";

                    // SqLite database handler
                    db = new DatabaseHelper(view.getContext());

                    // Fetching user verification token from sqlite
                    HashMap<String, String> user = db.getUserDetails();
                    final String token = user.get("verification_token");

                    String request_matricule, request_month, request_year;
                    request_matricule = matricule.getText().toString().trim();
                    request_month = month.getText().toString().trim();
                    int indexNum = Arrays.asList(months).indexOf(request_month) + 1;
                    request_year = year.getText().toString().trim();

                    //Passing login parameters
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    params.put("matricule", request_matricule);
                    params.put("month", String.valueOf(indexNum));
                    params.put("year", request_year);

                    JSONObject user_params = new JSONObject(params);

                    final JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                            Appconfig.URL_DOWNLOAD_PAYSLIP, user_params,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, "onResponse: " + response.toString());

                                    try {
                                        //checking for authorization error
                                        boolean error = response.getBoolean("status");

                                        //checking for request error
                                        if (error) {
                                            /**
                                             * user token correct
                                             * Gathering data for the saved request
                                             */

                                            String filename = response.getString("filename");

                                            String url = Appconfig.URL_DOWNLOAD_PAYSLIP + "/" + filename;

                                            Toast.makeText(view.getContext(), url, Toast.LENGTH_LONG).show();

                                            progressDialog.dismiss();

                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                            request.setDescription(view.getResources().getString(R.string.downloading));
                                            request.setTitle(filename);
                                            // in order for this if to run, you must use the android 3.2 to compile your app
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                request.allowScanningByMediaScanner();
                                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            }
                                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                                            // get download service and enqueue file
                                            DownloadManager manager = (DownloadManager) view.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                            manager.enqueue(request);


                                        } else {
                                            /**
                                             * Creating an activity to display the user error info\
                                             */
                                            progressDialog.dismiss();
                                            Toast.makeText(view.getContext(), view.getResources().
                                                    getString(R.string.an_internet_error_occured_please_try_again), Toast.LENGTH_LONG).show();

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
                    int MY_SOCKET_TIMEOUT_MS = 15000;
                    int MY_RETRY_ITME = 1;
                    jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                            MY_RETRY_ITME,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
                }
            });

        }
    }


}
