package com.example.randyp.bulletindesolde.Activities.Sync_framework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class BDSAuthenticatorService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        BDSAuthenticator authenticator = new BDSAuthenticator(this);
        return authenticator.getIBinder();
    }
}
