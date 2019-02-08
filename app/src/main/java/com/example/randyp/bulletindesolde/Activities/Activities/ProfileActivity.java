package com.example.randyp.bulletindesolde.Activities.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.randyp.bulletindesolde.R;

public class ProfileActivity extends AppCompatActivity {
    TextView user_name, email_address, address, region, phone, whatsapp;
    ImageView edit_profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent i = getIntent();
        user_name = findViewById(R.id.user_name);
        region = findViewById(R.id.region);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone_number);
        whatsapp = findViewById(R.id.whatsapp_number);
        email_address = findViewById(R.id.email);

        user_name.setText(i.getExtras().getString("name"));
        region.setText(i.getExtras().getString("region"));
        address.setText(i.getExtras().getString("address"));
        phone.setText(i.getExtras().getString("phone"));
        whatsapp.setText(i.getExtras().getString("phone"));
        email_address.setText(i.getExtras().getString("email"));

        edit_profile = findViewById(R.id.edit_profile);
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                openEditProfile();
            }
        });

    }

    private void openEditProfile() {
        Intent i = getIntent();
        Intent intent = new Intent(this, Editprofile.class);

        intent.putExtra("name", user_name.getText());
        intent.putExtra("phone", phone.getText());
        intent.putExtra("whatsapp", whatsapp.getText());
        intent.putExtra("region", region.getText());
        intent.putExtra("address", address.getText());
        intent.putExtra("email", email_address.getText());
        intent.putExtra("gender", i.getExtras().getString("gender"));

        startActivity(intent);
    }

}
