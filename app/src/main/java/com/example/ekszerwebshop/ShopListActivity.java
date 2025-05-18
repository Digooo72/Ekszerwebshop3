package com.example.ekszerwebshop;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    private NotificationHandler mNotificationHandler;

   private FrameLayout redCircle;
   private TextView contentTextView ;

   private FirebaseFirestore mFirestore;

   private CollectionReference mItems;

   private int queryLimit = 10;

   private AlarmManager mAlarmManager;
   private JobScheduler mJobScheduler;



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

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");

        queryData();


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReciever,filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        setAlarmManager();
        requestNotificationPermission();

        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        setJobScheduler();

    }

    BroadcastReceiver powerReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {

                return;
            }
            switch(action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }
            queryData();
        }
    };

    private void queryData(){
        mItemList.clear();

        //mItems.whereEqualTo()
        mItems.orderBy("cartedCount",Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots ->  {
           for (QueryDocumentSnapshot document : queryDocumentSnapshots){
               ShoppingItem item = document.toObject(ShoppingItem.class);
               item.setId(document.getId());
               mItemList.add(item);
           }
           if(mItemList.size()== 0){
               initializeData();
               queryData();
           }
            mAdapter.notifyDataSetChanged();
        });



    }


    public void deleteItem(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(success ->{
            Log.d(LOG_TAG, "Item is succesfully deleted: " + item._getId());
        }).addOnFailureListener(failure ->{
            Toast.makeText(this, "Item " + item._getId() + "cannot be deleted", Toast.LENGTH_LONG).show();
        });

        queryData();
        mNotificationHandler.cancel();

    }



    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);




        for (int i = 0; i < itemsList.length; i++) {

            mItems.add(new ShoppingItem(itemsList[i],itemsInfo[i],itemsPrice[i], itemsRate.getFloat(i,0), itemsImageResource.getResourceId(i, 0), 0));

        }





        itemsImageResource.recycle();


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
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
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

    public void updateAlertIcon(ShoppingItem item){
        cartItems = (cartItems + 1);
        if(0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));

        }else{
            contentTextView.setText("");
        }
        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount()+1)
                .addOnFailureListener(failure->{
                    Toast.makeText(this, "Item " + item._getId() + "cannot be changed", Toast.LENGTH_LONG).show();
                });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection("kosarak")
                    .document(uid)
                    .collection("termekek")
                    .document(item._getId())
                    .set(item)
                    .addOnSuccessListener(unused -> Log.d(LOG_TAG, "Kosárhoz hozzáadva: " + item.getName()))
                    .addOnFailureListener(e -> Log.e(LOG_TAG, "Hiba a kosárhoz adáskor", e));
        }

        mNotificationHandler.send(item.getName());

        queryData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReciever);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    private void setAlarmManager(){
        //long repeateinterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long repeateinterval = 60*100;
        long triggerTime = SystemClock.elapsedRealtime()+repeateinterval;
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                repeateinterval,
                pendingIntent);

        //mAlarmManager.cancel(pendingIntent);
    }

    private void setJobScheduler(){
        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadLine = 5000;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0,name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadLine);

        mJobScheduler.schedule(builder.build());

        //mJobScheduler.cancel(0);
    }




}



