package com.example.randyp.bulletindesolde.Activities.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.randyp.bulletindesolde.Activities.InternetCheck.ConnectivityReceiver;
import com.example.randyp.bulletindesolde.R;

public class InternetError extends AppCompatActivity {

    private Button try_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_error);

        try_button =findViewById(R.id.internet_try_again);

    }


    public void onclick(View view){
        //Manually checking internet connection
        checkConnection();

    }

    //Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        getBack(isConnected);
    }

    //Method to get back to the calling activity
    private void getBack(boolean isConnected) {
        if (isConnected){
            finish();
        }else {
            return;
        }
    }

    @Override
    public void finish() {

        Intent data = new Intent();
        setResult(RESULT_OK,null);
        super.finish();

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

}
