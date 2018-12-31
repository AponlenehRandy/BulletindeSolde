package com.example.randyp.bulletindesolde.Activities.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.randyp.bulletindesolde.Activities.Database.Model.DatabaseHelper;
import com.example.randyp.bulletindesolde.R;

import java.util.List;

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
