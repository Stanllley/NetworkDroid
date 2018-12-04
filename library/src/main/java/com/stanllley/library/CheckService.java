package com.stanllley.library;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.CallSuper;


/**
 * @Author xuyang
 * @Email xuyang@prudencemed.com
 * @Date 2018/10/10.
 * @Description
 */
abstract class CheckService {

    private long interval;
    private Context context;
    private Pinger pinger = new Pinger.PingOption()
            .address("www.baidu.com")
            .timeout(5000)
            .pingCallback(new PingCallback())
            .complete();

    private NetworkListener networkListener;
    private Handler handler = new Handler();
    private CheckTask checkTask = new CheckTask();
    private float lastLatency = -1;
    private State connectState = State.DISCONNECTED;

    protected CheckService(Builder builder) {
        this.interval = builder.interval;
        this.context = builder.context;
        this.networkListener = builder.listener;
    }

    public Context getContext() {
        return context;
    }

    final void start() {
        running = true;
    }

    abstract void onUnlistenSignal();

    private boolean running;

    @CallSuper
    void realStart(long interval) {
        handler.postDelayed(checkTask, interval);
    }

    @CallSuper
    public void stop() {
        onUnlistenSignal();
        handler.removeCallbacks(checkTask);
        running = false;
    }

    void onNetworkSignalChanged(int signal) {
        if (networkListener != null) {
            networkListener.onNetworkSignalChanged(signal, getNetworkType());
        }
    }

    void onNetworkLatencyChanged(float latency) {
        if (networkListener != null && latency != lastLatency) {
            networkListener.onNetworkLatencyChanged(latency, getNetworkType());
        }
        lastLatency = latency;
    }

    void onNetworkConnected() {
        connectState = State.CONNECTED;
        listenSignal();
        handler.post(checkTask);
        if (networkListener != null) {
            networkListener.onNetworkConnected(getNetworkType());
        }
    }

    void onNetworkDisconnected() {
        connectState = State.DISCONNECTED;
        onUnlistenSignal();
        handler.removeCallbacks(checkTask);
        pinger.stop();
        if (networkListener != null) {
            networkListener.onNetworkDisconnected(getNetworkType());
        }

    }

    abstract void listenSignal();

    abstract NetworkType getNetworkType();

    public static abstract class Builder {

        long interval = 1000 * 60;//1分钟
        NetworkListener listener;
        Context context;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        Builder checkInterval(long interval) {
            this.interval = interval;
            return this;
        }

        Builder networkListener(NetworkListener listener) {
            this.listener = listener;
            return this;
        }

        public abstract CheckService build();
    }

    boolean isConnected() {
        return State.CONNECTED == connectState;
    }

    private class PingCallback implements Pinger.Callback {

        @Override
        public void onStart() {

        }

        @Override
        public void onError(Throwable e) {
            if (running) {
                realStart(interval);
            }
        }

        @Override
        public void onFinish(Pinger.PingInfo pingInfo) {
            onNetworkLatencyChanged(pingInfo.exit == 0 ? pingInfo.getMdev() : -1);
            if (connectState == State.CONNECTED && running) {
                realStart(interval);
            }
        }
    }

    private class CheckTask implements Runnable {

        @Override
        public void run() {
            pinger.ping();
        }
    }

    enum State {
        CONNECTED, DISCONNECTED
    }

}
