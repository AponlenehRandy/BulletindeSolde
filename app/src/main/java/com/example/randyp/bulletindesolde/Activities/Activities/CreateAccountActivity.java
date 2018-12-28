package com.example.randyp.bulletindesolde.Activities.Activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.randyp.bulletindesolde.Activities.Sync_framework.BDSAuthenticator;
import com.example.randyp.bulletindesolde.Activities.Sync_framework.BDSRegLoginHelper;
import com.example.randyp.bulletindesolde.R;

public class CreateAccountActivity extends AppCompatActivity {

    private String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
    }

    public void createAccount(View view){
        final String userId = ((EditText) findViewById(R.id.user)).getText().toString();
        final String passWd = ((EditText) findViewById(R.id.password)).getText().toString();
        final String name = ((EditText) findViewById(R.id.name)).getText().toString();

        if(!BDSRegLoginHelper.validateAccountInfo(name,userId, passWd)){
            ((TextView)findViewById(R.id.error)).setText("Please Enter Valid Information");
        }

        String authToken = BDSRegLoginHelper.createAccount(name,userId, passWd);
        String authTokenType = BDSRegLoginHelper.getTokenType(userId);

        if(authToken.isEmpty()){
            ((TextView)findViewById(R.id.error)).setText("Account couldn't be registered, please try again.");
        }

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, userId);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        data.putString(BDSAuthenticator.PASSWORD, passWd);
        data.putString(BDSAuthenticator.TOKEN_TYPE, authTokenType);

        final Intent result = new Intent();
        result.putExtras(data);

        setResult(RESULT_OK, result);
        finish();
    }
}
