package com.example.randyp.bulletindesolde.Activities.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.randyp.bulletindesolde.R;

public class LoginsuccessActivity extends Activity {

    Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginsuccess);

        next =findViewById(R.id.btn_next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // rty connection before the main activity login
                Intent intent = new Intent(LoginsuccessActivity.this,
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
