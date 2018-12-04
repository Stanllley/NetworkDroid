package com.stanllley.library;

import android.net.ConnectivityManager;

/**
 * @Author xuyang
 * @Email xuyang@prudencemed.com
 * @Date 2018/10/12.
 * @Description
 */
class Utils {

    static NetworkType parseNetworkType(int baseType) {
        NetworkType networkType;
        switch (baseType) {
            case ConnectivityManager.TYPE_MOBILE:
                networkType = NetworkType.MOBILE;
                break;
            case ConnectivityManager.TYPE_WIFI:
                networkType = NetworkType.WIFI;
                break;
            case -1:
                networkType = NetworkType.NONE;
                break;
            default:
                networkType = NetworkType.UNKOWN;
                break;
        }
        return networkType;
    }

}
