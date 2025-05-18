package com.example.ekszerwebshop;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> implements Filterable {
    public ArrayList<ShoppingItem> mShoppingItemsdata;
    public ArrayList<ShoppingItem> mShoppingItemsDataAll;
    public Context mContext;
    private int lastPosition = -1;

    ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemsData){
        this.mShoppingItemsdata = itemsData;
        this.mShoppingItemsDataAll = itemsData;
        this.mContext = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ShoppingItem currentItem = mShoppingItemsdata.get(position);

        holder.bindTo(currentItem);
//lehet getBindingAdapterPosition vagy absolute
        if(holder.getAdapterPosition()  > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mShoppingItemsdata.size();
    }

    @Override
    public Filter getFilter() {
        return shoppingFilter;
    }
    public Filter shoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ShoppingItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0){
                results.count = mShoppingItemsDataAll.size();
                results.values = mShoppingItemsDataAll;
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(ShoppingItem item : mShoppingItemsDataAll){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mShoppingItemsdata = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mTitleText;
        public TextView mInfoText;
        public TextView mPriceText;
        public ImageView mItemImage;
        public RatingBar mRatingBar;
        public ViewHolder( View itemView) {
            super(itemView);

             mTitleText = itemView.findViewById(R.id.itemTitle);
             mInfoText = itemView.findViewById(R.id.subTitle);
             mPriceText = itemView.findViewById(R.id.price);
             mItemImage = itemView.findViewById(R.id.itemImage);
             mRatingBar = itemView.findViewById(R.id.ratingBar);



        }

        public void bindTo(ShoppingItem currentItem) {


            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mRatingBar.setRating(currentItem.getRatedInfo());

            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);

            itemView.findViewById(R.id.add_to_cart).setOnClickListener(view -> ((ShopListActivity)mContext).updateAlertIcon(currentItem));

            itemView.findViewById(R.id.delete).setOnClickListener(view -> ((ShopListActivity)mContext).deleteItem(currentItem));
        }
    }


}


