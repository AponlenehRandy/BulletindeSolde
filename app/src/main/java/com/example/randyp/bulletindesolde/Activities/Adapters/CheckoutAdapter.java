package com.example.randyp.bulletindesolde.Activities.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.randyp.bulletindesolde.R;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.MyViewHolder> {

    private List<Checkout> checkoutList;



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


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView matricle,month,year;
        ImageView request_delete;

        public MyViewHolder(View view) {
            super(view);

            matricle = view.findViewById(R.id.checkout_matricle);
            month = view.findViewById(R.id.checkout_month);
            year =view.findViewById(R.id.checkout_year);
            request_delete=view.findViewById(R.id.checkoout_delete);
            request_delete.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            
            if (view.equals(request_delete)){
                /**
                 * Send data to the server to delete the saved request
                 * if successful, delete the data in the local database
                 * else do something else
                 */


                removeAt(getPosition());
            }else{
                
            }
            
        }
    }

    private void removeAt(int position) {
        checkoutList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,checkoutList.size());
    }
}
