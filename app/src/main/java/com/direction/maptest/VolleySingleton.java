package com.direction.maptest;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Hananideen on 13/9/2015.
 */
public class VolleySingleton {

    private static VolleySingleton Instance;
    private static RequestQueue requestQueue;
    private static ImageLoader imageLoader;

    private VolleySingleton(){

//        init();

    }

    public static void init(){
        Instance = getInstance();
        requestQueue = Volley.newRequestQueue(ApplicationLoader.getContext());
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {

            private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }


    public static VolleySingleton getInstance(){
        if(Instance == null) {
            Instance = new VolleySingleton();
        }
        return Instance;
    }

    public RequestQueue getRequestQueue(){
        return requestQueue;
    }

    public ImageLoader getImageLoader(){
        return imageLoader;
    }

}
