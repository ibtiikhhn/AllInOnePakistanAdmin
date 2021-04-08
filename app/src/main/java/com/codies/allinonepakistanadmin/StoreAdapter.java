package com.codies.allinonepakistanadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    public static final String TAG = "RVVV";

    int row_index=0;
    Context context;
    List<Store> storeList;
    StoreClickListener storeClickListener;

    public StoreAdapter(Context context,StoreClickListener storeClickListener) {
        this.context = context;
        storeList = new ArrayList<>();
        this.storeClickListener = storeClickListener;
    }

    public void setList(List<Store> storeList) {
        this.storeList = storeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_cv, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
//        holder.storeIMG.setImageDrawable(storeList.get(position).getStoreIMG());
        Glide.with(context).load(storeList.get(position).getStoreIMG()).into(holder.storeIMG);
        holder.storeTV.setText(storeList.get(position).getStoreName());
        holder.removeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeClickListener.storeOnClick(storeList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        public TextView storeTV;
        public ImageView storeIMG;
        public Button removeBT;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeTV = itemView.findViewById(R.id.storeNameTV);
            storeIMG = itemView.findViewById(R.id.storeIV);
            removeBT = itemView.findViewById(R.id.storeRemoveBT);
        }
    }
}
