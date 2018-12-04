package com.stanllley.library;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;


/**
 * @Author xuyang
 * @Email youtouchyang@sina.com
 * @Date 2018/10/10.
 * @Description
 */
class CheckWifiService extends CheckService {

    private Handler handler = new Handler();
    private WifiSignalTask signalTask;

    private CheckWifiService(Builder builder) {
        super(builder);

    }

    public static class Builder extends CheckService.Builder {

        public Builder(Context context) {
            super(context);
            interval = 1000 * 60;//1分钟
            this.context = context.getApplicationContext();
        }

        public CheckService build() {
            return new CheckWifiService(this);
        }
    }


    @Override
    public void stop() {
        super.stop();
        handler.removeCallbacks(signalTask);
    }

    @Override
    NetworkType getNetworkType() {
        return NetworkType.WIFI;
    }


    @Override
    void listenSignal() {
        if(signalTask == null){
            signalTask = new WifiSignalTask();
        }
        handler.post(signalTask);
    }

    @Override
    void onUnlistenSignal() {
        if(signalTask != null){
            signalTask.lastSignalLevel = -1;
            handler.removeCallbacks(signalTask);
        }
    }

    private class WifiSignalTask implements Runnable {
        WifiManager wifiManager;
        int lastSignalLevel = -1;

        WifiSignalTask() {
            wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        @Override
        public void run() {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int signal = 0;
            if (wifiInfo.getBSSID() != null) {
                signal = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 6);
            }
            if (signal != lastSignalLevel) {
                onNetworkSignalChanged(signal);
            }
            lastSignalLevel = signal;
            handler.postDelayed(this, 2500);
        }
    }

}
