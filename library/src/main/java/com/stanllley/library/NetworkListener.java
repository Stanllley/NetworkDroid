package com.stanllley.library;

/**
 * @Author xuyang
 * @Email youtouchyang@sina.com
 * @Date 2018/10/10.
 * @Description
 */
public interface NetworkListener {

    void onNetworkSignalChanged(int signal, NetworkType type);

    void onNetworkLatencyChanged(float latency, NetworkType type);

    void onNetworkConnected(NetworkType type);

    void onNetworkDisconnected(NetworkType type);

}
