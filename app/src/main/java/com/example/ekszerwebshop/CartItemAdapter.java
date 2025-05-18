package com.example.ekszerwebshop;

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

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    public interface OnItemRemoveListener {
        void onItemRemove(ShoppingItem item,int position);
    }

    private Context context;
    private ArrayList<ShoppingItem> cartItems;
    private OnItemRemoveListener removeListener;

    public CartItemAdapter(Context context, ArrayList<ShoppingItem> cartItems, OnItemRemoveListener removeListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = cartItems.get(position);
        holder.nameText.setText(item.getName());
        holder.priceText.setText(item.getPrice());

        holder.removeBtn.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onItemRemove(item,holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText;
        Button removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.cart_item_name);
            priceText = itemView.findViewById(R.id.cart_item_price);
            removeBtn = itemView.findViewById(R.id.cart_item_remove);
        }
    }


}
