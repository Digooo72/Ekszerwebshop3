package com.example.ekszerwebshop;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;



import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecycleView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;
    private int gridNumber = 1;
    private boolean viewRow = true;
    private int cartItems = 0;

   private FrameLayout redCircle;
   private TextView contentTextView ;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);
        mAuth = FirebaseAuth.getInstance();


        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Log.d(LOG_TAG,"Authenticated user!");
        }else{
            Log.d(LOG_TAG,"Unauthenticated user!");
            finish();
        }

        mRecycleView = findViewById(R.id.recyclerView);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemList);

        mRecycleView.setAdapter(mAdapter);

        initializeData();
    }

    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        mItemList.clear();
        for (int i = 0; i < itemsList.length; i++) {
            mItemList.add(new ShoppingItem(itemsList[i],itemsInfo[i],itemsPrice[i], itemsRate.getFloat(i,0), itemsImageResource.getResourceId(i, 0)));
            
        }
        itemsImageResource.recycle();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG, newText);
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* switch (item.getItemId()){
            case R.id.log_out_button :
                Log.d(LOG_TAG,"Log out clicked");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings_button:
                Log.d(LOG_TAG,"Setting button clicked");

                return true;
            case R.id.cart:
                Log.d(LOG_TAG,"Cart clicked");

                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG,"View selector clicked");
                if(viewRow){
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                }else{
                    changeSpanCount(item, R.drawable.ic_view_row, 2);

                }

                return true;
            default: return super.onOptionsItemSelected(item);





        }*/

        int id = item.getItemId();

        if (id == R.id.log_out_button) {
            Log.d(LOG_TAG,"Log out clicked");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (id == R.id.settings_button) {
            Log.d(LOG_TAG,"Setting button clicked");

            return true;
        } else if (id == R.id.cart) {
            Log.d(LOG_TAG,"Cart clicked");

            return true;
        } else if (id == R.id.view_selector) {
            Log.d(LOG_TAG,"View selector clicked");
            if(viewRow){
                changeSpanCount(item, R.drawable.ic_view_grid, 1);
            }else{
                changeSpanCount(item, R.drawable.ic_view_row, 2);

            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }





    private void changeSpanCount(MenuItem item, int icViewGrid, int i) {
        viewRow = !viewRow;
        item.setIcon(icViewGrid);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecycleView.getLayoutManager();
        layoutManager.setSpanCount(i);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuITem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuITem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);


        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuITem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(){
        cartItems = (cartItems + 1);
        if(0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));

        }else{
            contentTextView.setText("");
        }
        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);
    }


}



