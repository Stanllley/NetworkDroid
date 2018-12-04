package com.stanllley.library;

import android.content.Context;

/**
 * @Author xuyang
 * @Email xuyang@prudencemed.com
 * @Date 2018/11/13.
 * @Description
 */
public class NetworkDroid {

    static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }


}
