package com.sslcertidemo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        url = getString(R.string.base_url);
        context = MainActivity.this;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            new Async().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Async().execute();
        }
    }


    private class Async extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
//                String[] responsedata = HttpConnectionUrl.post_httpclient(context, url, postdata, postheader);
//                String[] responsedata = HttpConnectionUrl.post_httpclient(context, url, postdata, postheader);

                String[] responsedata = HttpConnectionUrl.get_httpclient(context, url);
//                String[] responsedata = HttpConnectionUrl.get_httpurlconnection(context, url);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }


}
