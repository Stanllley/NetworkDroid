package com.stanllley.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.net.ConnectivityManager.EXTRA_NETWORK_TYPE;

/**
 * @Author xuyang
 * @Email youtouchyang@sina.com
 * @Date 2018/10/12.
 * @Description
 */
class NetworkConnectReceiver extends BroadcastReceiver {

    private OnConnectStateChangeListener listener;

    NetworkConnectReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int currentType = intent.getIntExtra(EXTRA_NETWORK_TYPE, -1);
        NetworkType networkType = Utils.parseNetworkType(currentType);
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && listener!= null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo == null){
                listener.onConnectStateChanged(false,false,networkType);
                return;
            }
            listener.onConnectStateChanged(networkInfo.isConnected(),networkInfo.isAvailable(),networkType);
        }
    }

    void setOnConnectStateChangeListener(OnConnectStateChangeListener listener){
        this.listener = listener;
    }

    interface OnConnectStateChangeListener {

        void onConnectStateChanged(boolean connected, boolean available, NetworkType networkType);

    }

}
