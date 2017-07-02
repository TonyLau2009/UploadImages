package jocelyn_test04.com.uploadimages;

import android.content.Context;

import com.android.volley.Request;

import java.util.ArrayList;


/**
 * Created by Jocelyn on 27/11/2016.
 */

public class MyCommand<T> {

    Context c;
    ArrayList<Request<T>> requestList = new ArrayList<>();

    public MyCommand(Context c) {
        this.c = c;
    }

    public void add(Request<T> request){
        requestList.add(request);
    }

    public void remove(Request<T> request){
        requestList.remove(request);
    }

    public void execute(){
        for(Request<T> request : requestList){
            MySingleton.getInstance(c).addToRequestQueue(request);
        }
    }
}
