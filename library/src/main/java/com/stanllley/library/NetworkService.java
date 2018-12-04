package com.stanllley.library;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * @Author xuyang
 * @Email youtouchyang@sina.com
 * @Date 2018/10/11.
 * @Description
 */
public class NetworkService {

    private Context context;

    private static NetworkService service;

    private CheckService mobileCheckService;

    private CheckService wifiCheckService;

    private long wifiCheckInterval;
    private long mobileCheckInterval;
    private NetworkListener listener;
    private NetworkController controller;


    private NetworkService(Application context) {
        this.context = context.getApplicationContext();
        controller = new NetworkController(this.context);
    }


    public static NetworkService get(Application application) {
        if (application == null) {
            throw new RuntimeException("the application is null");
        }
        if (service == null) {
            synchronized (NetworkService.class) {
                service = new NetworkService(application);
            }
        }
        return service;
    }

    /**
     * 是否有连上网络
     * @return
     */
    public boolean isNetworkConnected() {
        return wifiCheckService.isConnected() || mobileCheckService.isConnected();
    }

    /**
     * wifi检测间隔
     * @param interval
     * @return
     */
    public NetworkService wifiCheckInterval(long interval) {
        this.wifiCheckInterval = interval;
        return this;
    }

    /**
     * 移动网路检测间隔
     * @param interval
     * @return
     */
    public NetworkService mobileCheckInterval(long interval) {
        this.mobileCheckInterval = interval;
        return this;
    }

    /**
     * 设置网络监听
     * @param listener
     * @return
     */
    public NetworkService listen(NetworkListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 开始检测
     */
    public void start() {
        initMobileAndWifiService();
        controller.controlNetworkService(mobileCheckService);
        controller.controlNetworkService(wifiCheckService);
        controller.startControl();
    }

    /**
     * 停止检测
     */
    public void stop() {
        controller.stopControl();
    }

    /**
     * 网络是否在检测中
     * @return
     */
    public boolean isChecking() {
        return controller.isCotrolling();
    }

    private void initMobileAndWifiService() {
        if (mobileCheckService == null) {
            mobileCheckService = new Check4GService.Builder(context)
                    .checkInterval(mobileCheckInterval)
                    .networkListener(listener)
                    .build();
        }
        if (wifiCheckService == null) {
            wifiCheckService = new CheckWifiService.Builder(context)
                    .checkInterval(wifiCheckInterval)
                    .networkListener(listener)
                    .build();
        }
    }

    public static boolean isNetConnected() {
        ConnectivityManager cm = (ConnectivityManager) NetworkDroid.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    public static void destroy(){
        if(service != null){
            service.stop();
            service = null;
        }
    }

}
