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

public class HistoryitemAdapter extends RecyclerView.Adapter<HistoryitemAdapter.MyViewHolder> {

    private List<InboxItem> inboxItems;
    private DatabaseHelper db;

    public HistoryitemAdapter(List<InboxItem> inboxItems) {
        this.inboxItems = inboxItems;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_recyclerview_row_item,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        InboxItem inboxItem = inboxItems.get(position);
        holder.matricle.setText(inboxItem.getMatricule());
        holder.month.setText(inboxItem.getMonth());
        holder.year.setText(inboxItem.getYear());
        holder.date.setText(inboxItem.getDate());
    }

    @Override
    public int getItemCount() {
        return inboxItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView matricle,month,year,date;
        ImageView request_delete;

        public MyViewHolder(View view) {
            super(view);

            matricle = view.findViewById(R.id.item_matricule);
            month = view.findViewById(R.id.item_month);
            year =view.findViewById(R.id.item_year);
            date = view.findViewById(R.id.item_date);


        }
    }


}
