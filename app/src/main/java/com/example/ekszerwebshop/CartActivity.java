package com.example.ekszerwebshop;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ShoppingItem> cartItems;
    private CartItemAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartItems = new ArrayList<>();
        adapter = new CartItemAdapter(this, cartItems, this::removeItemFromCart);
        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "Be kell jelentkezned a kosárhoz.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCartItems();
    }

    private void loadCartItems() {
        db.collection("kosarak")
                .document(user.getUid())
                .collection("termekek")
                .get()
                .addOnSuccessListener(query -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        cartItems.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a kosár betöltésekor", Toast.LENGTH_SHORT).show();
                    Log.e("CART", "Firestore hiba", e);
                });
    }

    private void removeItemFromCart(ShoppingItem item, int position) {
        db.collection("kosarak")
                .document(user.getUid())
                .collection("termekek")
                .document(item._getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Törölve a kosárból", Toast.LENGTH_SHORT).show();

                    // Törlés pozíció alapján
                    cartItems.remove(position);
                    adapter.notifyItemRemoved(position);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Nem sikerült törölni", Toast.LENGTH_SHORT).show();
                });
    }


}
