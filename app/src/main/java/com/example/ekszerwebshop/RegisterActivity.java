package com.example.ekszerwebshop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText passwordConfirmEditText;

    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

       // Bundle bundle = getIntent().getExtras();
        // int secret_key = bundle.getInt("SECRET_KEY");
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if(secret_key != 99){
            finish();
        }

        userNameEditText = findViewById(R.id.usernameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordAgainEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
      String userName =   preferences.getString("userName", "");
       String password = preferences.getString("password", "");

       userNameEditText.setText(userName);
       passwordEditText.setText(password);
       passwordConfirmEditText.setText(password);

       mAuth = FirebaseAuth.getInstance();


        Log.i(LOG_TAG,"onCreate");

    }

    public void register(View view) {
        String userName = userNameEditText.getText().toString();
        String email = userEmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();

        if (!password.equals(passwordConfirm)){
            Log.e(LOG_TAG,"Nem egyenlő a jelszó és a megerősítése.");
            return;
        }

            Log.i(LOG_TAG, "Bejelentkezett: " + userName + ", e-mail: " + email);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "User created successfully");
                    startShopping();
                }else{
                    Log.d(LOG_TAG,"User wasn't created successfully");
                    Toast.makeText(RegisterActivity.this, "User wasn't created successfully: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }



    public void cancel(View view) {
        finish();
    }

    private void startShopping(){
        Intent intent = new Intent(this, ShopListActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG,"onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG,"onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"onDestroy");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG,"onPause");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG,"onRestart");

    }
}