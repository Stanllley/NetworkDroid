package com.stanllley.library;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author xuyang
 * @Email xuyang@prudencemed.com
 * @Date 2018/10/12.
 * @Description
 */
class NetworkController {

    private List<CheckService> serviceList = new LinkedList<>();

    private NetworkConnectReceiver receiver;

    private Context context;

    private boolean controlling;

    NetworkController(Context context){
        this.context = context;
    }

    void controlNetworkService(CheckService service){
        if(service != null && !serviceList.contains(service)){
            serviceList.add(service);
        }
    }

    void deControlNetworkService(CheckService service){
        serviceList.remove(service);
    }

    void startControl(){
        if(controlling){
            return;
        }
        if(receiver == null){
            receiver = new NetworkConnectReceiver();
            receiver.setOnConnectStateChangeListener(new NetworkConnectReceiver.OnConnectStateChangeListener() {
                @Override
                public void onConnectStateChanged(boolean connected, boolean available, NetworkType networkType) {
                    for(CheckService checkService:serviceList){
                        checkService.start();
                        if(connected){
                            if(checkService.getNetworkType() == networkType){
                                checkService.onNetworkConnected();
                            }else{
                                checkService.onNetworkDisconnected();
                            }
                        }else{
                            if(checkService.getNetworkType() == networkType){
                                checkService.onNetworkDisconnected();
                            }
                        }
                    }
                }
            });
        }
        context.registerReceiver(receiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        controlling = true;
    }

    void stopControl(){
        if(!controlling){
            return;
        }
        context.unregisterReceiver(receiver);
        for (CheckService service : serviceList){
            service.stop();
        }
        controlling = false;
    }

    boolean isCotrolling(){
        return controlling;
    }

}
