package com.fiuber.fiuber;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.Volley;

public class MyRequestQueue {

    private static MyRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    private static HttpStack mHttpStack;

    private MyRequestQueue(Context context, HttpStack httpStack) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized MyRequestQueue getInstance(Context context, HttpStack httpStack) {
        if (mInstance == null) {
            mInstance = new MyRequestQueue(context, mHttpStack);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), mHttpStack);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
