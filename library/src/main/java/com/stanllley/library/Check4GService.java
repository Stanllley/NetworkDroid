package com.stanllley.library;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;


/**
 * @Author xuyang
 * @Email xuyang@prudencemed.com
 * @Date 2018/10/10.
 * @Description
 */
class Check4GService extends CheckService {

    private PhoneStateListener phoneStateListener;

    private boolean signalListening;

    private Check4GService(Builder builder) {
        super(builder);
    }

    public static class Builder extends CheckService.Builder{

        public Builder(Context context) {
            super(context);
            interval = 1000 * 60 * 5;
        }

        public Check4GService build() {
            return new Check4GService(this);
        }

    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    void onUnlistenSignal() {
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(tm != null && phoneStateListener != null){
            tm.listen(phoneStateListener,PhoneStateListener.LISTEN_NONE);
        }
        signalListening = false;
    }

    @Override
    NetworkType getNetworkType() {
        return NetworkType.MOBILE;
    }

    @Override
    void listenSignal() {

        if(signalListening){
            return;
        }
        if(phoneStateListener == null){
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    onNetworkSignalChanged(signalStrength.getLevel());
                }
            };
        }
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            signalListening = true;
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }
}
