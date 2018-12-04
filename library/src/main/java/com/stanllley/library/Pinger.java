package com.stanllley.library;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Author xuyang
 * @Email youtouchyang@sina.com
 * @Date 2018/9/12.
 * @Description
 */
public class Pinger {

    private static final String TAG = Pinger.class.getSimpleName();

    private static final int DEFAULT_PING_COUNT = 4;
    private static final int PING_MSG_START = 0;
    private static final int PING_MSG_FINISH = 1;
    private static final int PING_MSG_ERROR = -1;
    private Thread pingThread;
    private Runnable pingTask = new PingTask();
    private PingOption option;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Pinger.Callback callback = option.pingCallback;
            if (callback == null) {
                return;
            }
            switch (msg.what) {
                case PING_MSG_START:
                    callback.onStart();
                    break;
                case PING_MSG_FINISH:
                    callback.onFinish((PingInfo) msg.obj);
                    closePingThread();
                    break;
                case PING_MSG_ERROR:
                    callback.onError((Throwable) msg.obj);
                    closePingThread();
                    break;
            }
        }
    };

    public Pinger(PingOption option) {
        if (option == null) {
            throw new NullPointerException("the PingOption can not be null");
        }
        this.option = option;
    }

    /**
     * 运行于非UI线程
     */
    public void ping() {
        if (pingThread != null && pingThread.isAlive() && !pingThread.isInterrupted()) {
            return;
        }
        mainThreadHandler.sendEmptyMessage(PING_MSG_START);
        if (option.address == null || option.address.length() == 0) {
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(PING_MSG_ERROR, new NullPointerException("the address can not be null")));
            return;
        }

        if (option.timeout < 0) {
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(PING_MSG_ERROR, new RuntimeException("the timeout can not less than 0")));
            return;
        }
        if (pingThread == null) {
            pingThread = new Thread(pingTask);
        }
        pingThread.start();

    }

    private void closePingThread() {
        if (pingThread != null && !pingThread.isInterrupted()) {
            pingThread.interrupt();
            pingThread = null;
        }
    }

    public void stop(){
        closePingThread();
    }

    private void doPing() {
        String timeout = option.timeout > 0 ? (" -w " + option.timeout + " ") : " ";
        String pingCommand = "ping -c " + option.count + timeout + option.address;
        Log.i(TAG, "ping command : " + pingCommand);
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(pingCommand);
            int exit = process.waitFor();
            PingInfo pingInfo;
            if (exit == 0) {
                pingInfo = analysisPingResult(process.getInputStream());
            } else {
                pingInfo = analysisPingResult(process.getErrorStream());
            }
            pingInfo.exit = exit;
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(PING_MSG_FINISH, pingInfo));

        } catch (Exception e) {
            e.printStackTrace();
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(PING_MSG_ERROR, e));
        }
    }

    private static PingInfo analysisPingResult(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        PingInfo pingInfo = new PingInfo();
        while ((line = reader.readLine()) != null) {
            analysis(line, pingInfo);
        }
        return pingInfo;
    }

    private static void analysis(String s, PingInfo pingInfo) {
        Log.i(TAG, s);
        if (s.contains(" packets transmitted")) {
            String[] statistics = s.split(", ");
            int transmitted = Integer.parseInt(statistics[0].substring(0, statistics[0].indexOf(" packets transmitted")));
            pingInfo.received = Integer.parseInt(statistics[1].substring(0, statistics[1].indexOf(" received")));
            float lossPercent = Float.parseFloat(statistics[2].substring(0, statistics[2].indexOf("% packet loss"))) / 100;
            pingInfo.lost = Math.round(lossPercent * transmitted);
        }
        if (s.contains("min/avg/max/mdev")) {
            String[] rtts = s.substring(s.indexOf("=") + 2, s.indexOf("ms") - 1).split("/");
            pingInfo.minRTT = Float.parseFloat(rtts[0]);
            pingInfo.avgRTT = Float.parseFloat(rtts[1]);
            pingInfo.maxRTT = Float.parseFloat(rtts[2]);
            pingInfo.mdev = Float.parseFloat(rtts[3]);
        }
    }

    public static int staticPing() {
        String commond = "ping -w 5000 www.baidu.com";
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(commond);
            return process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static class PingInfo {
        int exit;
        int received;
        int lost;
        float minRTT;
        float avgRTT;
        float maxRTT;
        float mdev;

        public int getExit() {
            return exit;
        }

        public int getReceived() {
            return received;
        }

        public int getLost() {
            return lost;
        }

        public float getMinRTT() {
            return minRTT;
        }

        public float getAvgRTT() {
            return avgRTT;
        }

        public float getMaxRTT() {
            return maxRTT;
        }

        public float getMdev() {
            return mdev;
        }

        @Override
        public String toString() {
            return "PingInfo{" +
                    "exit=" + exit +
                    ", received=" + received +
                    ", lost=" + lost +
                    ", minRTT=" + minRTT +
                    ", avgRTT=" + avgRTT +
                    ", maxRTT=" + maxRTT +
                    ", mdev=" + mdev +
                    '}';
        }
    }

    public static class PingOption {

        String address;
        int count = DEFAULT_PING_COUNT;
        long timeout;
        Callback pingCallback;

        public PingOption address(String address) {
            this.address = address;
            return this;
        }

        public PingOption pingCount(int pingCount) {
            this.count = pingCount;
            return this;
        }

        public PingOption timeout(long timeoutMilliseconds) {
            this.timeout = timeoutMilliseconds;
            return this;
        }

        public PingOption pingCallback(Callback callback) {
            this.pingCallback = callback;
            return this;
        }

        public Pinger complete() {
            return new Pinger(this);
        }
    }

    public interface Callback {
        void onStart();

        void onError(Throwable e);

        void onFinish(PingInfo pingInfo);
    }

    private class PingTask implements Runnable {

        @Override
        public void run() {
            doPing();
        }
    }

}
