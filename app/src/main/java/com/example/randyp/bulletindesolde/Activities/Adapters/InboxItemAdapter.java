package com.example.randyp.bulletindesolde.Activities.Adapters;

import android.app.ProgressDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.randyp.bulletindesolde.Activities.AppController.AppController;
import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    /**
                     * Send data the server to download a validated payslip
                     * if successful, download the payslip
                     */
                    //Send request to the server with the user token, matricle,month and year
                    // Tag used to cancel the request
                    final String tag_json_obj = "json_obj_req";

                    // SqLite database handler
                    db = new DatabaseHelper(view.getContext());

                    // Fetching user verification token from SQLite
                    HashMap<String, String> user = db.getUserDetails();
                    final String token = user.get("verification_token");


// Tag used to cancel the request

                    String url = "https://www.bulletindesolde.com/api/request/download/666666J-3-2015-5831-1545911259.pdf";

                    final ProgressDialog pDialog = new ProgressDialog(view.getContext());
                    pDialog.setMessage(view.getContext().getResources().getString(R.string.downloading_file));
                    pDialog.show();

                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                            url, null,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    pDialog.hide();
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            pDialog.hide();
                        }
                    }) {

                        /**
                         * Passing some request headers
                         * */
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Authorization", token);
                            return headers;
                        }

                    };

// Adding request to request queue
                    AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
                }
            });


        }
    }

}
