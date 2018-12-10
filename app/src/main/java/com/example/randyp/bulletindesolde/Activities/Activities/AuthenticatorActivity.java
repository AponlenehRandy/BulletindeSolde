package com.example.randyp.bulletindesolde.Activities.Activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.randyp.bulletindesolde.Activities.Sync_framework.BDSAuthenticator;
import com.example.randyp.bulletindesolde.Activities.Sync_framework.BDSRegLoginHelper;
import com.example.randyp.bulletindesolde.R;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    private AccountManager accountManager;

    private final int REQ_REGISTER = 11;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_authenticator);

        accountManager = AccountManager.get(getBaseContext());
    }


    public void createAccount(View view) {
        Intent intent = new Intent(getBaseContext(), CreateAccountActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivityForResult(intent, REQ_REGISTER);


    }

    public void login(View view) {
        final String userId = ((EditText) findViewById(R.id.user)).getText().toString();
        final String passWd = ((EditText) findViewById(R.id.password)).getText().toString();

        final String accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                Bundle data = new Bundle();

                String authToken = BDSRegLoginHelper.authenticate(userId, passWd);
                String tokenType = BDSRegLoginHelper.getTokenType(userId);

                data.putString(AccountManager.KEY_ACCOUNT_NAME, userId);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(BDSAuthenticator.TOKEN_TYPE, tokenType);
                data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                data.putString(BDSAuthenticator.PASSWORD, passWd);

                final Intent result = new Intent();
                result.putExtras(data);

                return result;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                setLoginResult(intent);
            }
        }.execute();
    }

    private void setLoginResult(Intent intent) {

        String userId = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String passWd = intent.getStringExtra(BDSAuthenticator.PASSWORD);

        final Account account = new Account(userId, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(BDSAuthenticator.ADD_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String tokenType = intent.getStringExtra(BDSAuthenticator.TOKEN_TYPE);

            accountManager.addAccountExplicitly(account, passWd, null);
            accountManager.setAuthToken(account, tokenType, authtoken);
        } else {
            accountManager.setPassword(account, passWd);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK &&  requestCode == REQ_REGISTER) {
            setLoginResult(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
