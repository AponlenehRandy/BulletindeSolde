package com.example.randyp.bulletindesolde.Activities.Sync_framework;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.io.IOException;

public class BDSSyncAdapter extends AbstractThreadedSyncAdapter {

    private AccountManager accountManager;

    public BDSSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        accountManager = AccountManager.get(context);
    }

    public BDSSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        accountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {

        try {
            String authToken = accountManager.blockingGetAuthToken(account, BDSAuthenticator.TOKEN_TYPE, true);
            /**
             * Use the authToken and write your sync logic.
             * Skip the previous call if authToken is not required
             */
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        }


    }
}
