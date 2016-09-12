package com.sslcertidemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class HttpConnectionUrl {

    public static String RESPONSECODE_ACTION_ON_BATCH_SUCCESS = "1";
    public static String RESPONSECODE_SUCCESS = "200";
    public static String RESPONSECODE_ERROR = "201";
    public static String RESPONSECODE_INVALIDMPIN = "201";
    public static String RESPONSECODE_FAILTOCREATEMPIN = "201";
    public static String RESPONSECODE_INVALIDCREDENTIAL = "202";
    public static String RESPONSECODE_EXPIREDOTAC = "202";
    public static String RESPONSECODE_INVALIDSESSION = "498";
    public static String RESPONSECODE_SESSIONTOKENMISSING = "498";
    public static String RESPONSECODE_REQUESTSUCCESS = "900";
    public static String RESPONSECODE_CONNECTIONTIMEOUT = "9001";
    public static String RESPONSECODE_SOCKETTIMEOUT = "903";

    public static String STATUS_CODE_200 = "200";// OK
    public static String STATUS_CODE_400 = "400";// error.400=Bad Request
    public static String STATUS_CODE_401 = "401";// error.401=Unauthorized
    public static String STATUS_CODE_402 = "402";// error.402=Payment Required
    public static String STATUS_CODE_403 = "403";// error.403=Forbidden
    public static String STATUS_CODE_404 = "404";// error.404=Not Found
    public static String STATUS_CODE_405 = "405";// error.405=Method Not Allowed
    public static String STATUS_CODE_408 = "408";// error.408=Request Timeout
    public static String STATUS_CODE_500 = "500";// error.500=Internal-Server-Error
    public static String STATUS_CODE_502 = "502";// error.502=Bad Gateway
    public static String STATUS_CODE_503 = "503";// error.503=Service-Unavailable
    public static String STATUS_CODE_504 = "504";// error.504=Gateway Timeout

    public static String STATUS_CODE_204 = "204";// For captcha

    private static int socket_timeout = 30 * 1000;
    private static int connection_timeout = 40 * 1000;

    /**
     * @param mContext
     * @return {@link String}
     */
    public static String getBaseURL(Context mContext) {
        return mContext.getResources().getString(R.string.base_url);
    }

    /**
     * @param mContext
     * @param type
     * @return {@link String}
     */
    public static String getFinalURL(Context mContext, String type) {
        return getBaseURL(mContext) + type;
    }

    /**
     * @param jsObj
     * @param key
     * @return {@link String}
     */
    public static String getJSONKeyvalue(JSONObject jsObj, String key) {
        String value = "";
        try {
            if (jsObj.has(key)) {
                if (jsObj.getString(key) != null && jsObj.getString(key).length() > 0 && !jsObj.getString(key).equalsIgnoreCase("null"))
                    value = jsObj.getString(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * @param context
     * @return {@link Boolean}
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean outcome = false;

        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

            for (NetworkInfo tempNetworkInfo : networkInfos) {

                /**
                 * Can also check if the user is in roaming
                 */
                if (tempNetworkInfo != null && tempNetworkInfo.isConnected()) {
                    outcome = true;
                    break;
                }
            }
        }

        return outcome;
    }

    /**
     * Http Post Request
     *
     * @param context
     * @param Url
     * @param values
     * @param header_list
     * @return
     * @throws ConnectTimeoutException
     */
    public static String[] post_httpclient(Context context, String Url, List<NameValuePair> values, List<NameValuePair> header_list) throws ConnectTimeoutException {

        String[] result = {"", ""};
        HttpClient httpclient = CustomHttpsClient.getMyHttpsClient(context, true);
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connection_timeout);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socket_timeout);
        httpclient.getParams().setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
        httpclient.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY, true);
        HttpPost httpPost = null;

        try {
            httpPost = new HttpPost(Url);
            Log.d("LINK URL -----> ", Url);

            if (header_list != null && header_list.size() > 0) {
                for (int i = 0; i < header_list.size(); i++) {
                    NameValuePair nameValuePair = header_list.get(i);
                    httpPost.addHeader(nameValuePair.getName(), nameValuePair.getValue());
                }
                Log.d("HEADER POST -----> ", header_list.toString());
            }

            if (values != null) {
                httpPost.setEntity(new UrlEncodedFormEntity(values));
                Log.d("VALUES POST -----> ", values.toString());
            }

            BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient.execute(httpPost);
            result[0] = String.valueOf(httpResponse.getStatusLine().getStatusCode());
            if (result[0].equals(STATUS_CODE_200)) {
                HttpEntity entity = httpResponse.getEntity();
                result[0] = HttpConnectionUrl.RESPONSECODE_REQUESTSUCCESS;
                result[1] = EntityUtils.toString(entity);
            }

            Log.d("API RESPONSE -----> ", result[1].toString());
        } catch (UnsupportedEncodingException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (IOException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (Exception e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Http Get Request
     *
     * @param context
     * @param Url
     * @return
     * @throws ConnectTimeoutException
     */
    public static String[] get_httpclient(Context context, String Url) throws ConnectTimeoutException {

        String[] result = {"", ""};
        HttpClient httpclient = CustomHttpsClient.getMyHttpsClient(context, true);
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connection_timeout);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socket_timeout);
        httpclient.getParams().setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
        httpclient.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY, true);
        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(Url);
            Log.d("LINK URL -----> ", Url);

            BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient.execute(httpGet);
            result[0] = String.valueOf(httpResponse.getStatusLine().getStatusCode());
            if (result[0].equals(STATUS_CODE_200)) {
                HttpEntity entity = httpResponse.getEntity();
                result[0] = HttpConnectionUrl.RESPONSECODE_REQUESTSUCCESS;
                result[1] = EntityUtils.toString(entity);
            }

            Log.d("API RESPONSE -----> ", result[1].toString());
        } catch (UnsupportedEncodingException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (IOException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (Exception e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Https URLConnection Get Request
     *
     * @param context
     * @param Url
     * @param values
     * @param header_list
     * @return
     * @throws ConnectTimeoutException
     */
    public static String[] post_httpurlconnection(Context context, String Url, List<NameValuePair> values, List<NameValuePair> header_list) throws ConnectTimeoutException {

        String[] result = {"", ""};
        try {

            HttpsURLConnection urlConnection = CustomHttpsURLConnection.getMyHttpsURLConnection(context, Url, false);
            Log.i("TAG_URL : ", Url);

            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(connection_timeout);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (Build.VERSION.SDK_INT > 13) {
                urlConnection.setRequestProperty("Connection", "close");
            }

            if (header_list != null && header_list.size() >= 1) {
                for (NameValuePair header : header_list) {
                    urlConnection.addRequestProperty(header.getName(), header.getValue());
                }
                Log.i("TAG_POST_HEADER : ", header_list.toString());
            }

            String postData = "";
            for (NameValuePair value : values) {
                postData = postData + value.getName() + "=" + URLEncoder.encode(value.getValue(), "UTF-8") + "&";
            }

            // Removing last & from the String
            if (!TextUtils.isEmpty(postData) && postData.length() > 2) {
                postData = postData.substring(0, postData.length() - 1);
            }

            Log.i("TAG_POSTDATA : ", postData);

            urlConnection.setUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setFixedLengthStreamingMode(postData.getBytes().length);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(postData);
            out.close();

            // always check HTTP response code first
            int responseCode = urlConnection.getResponseCode();
            result[0] = responseCode + "";

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get Response
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                if (!TextUtils.isEmpty(response)) {
                    result[0] = HttpConnectionUrl.RESPONSECODE_REQUESTSUCCESS;
                    result[1] = response.toString();
                    Log.i("TAG_RESPONSE : ", result[1]);
                }

            }

        } catch (UnsupportedEncodingException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (IOException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (Exception e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Https URLConnection Get Request
     *
     * @param context
     * @param Url
     * @return
     * @throws ConnectTimeoutException
     */
    public static String[] get_httpurlconnection(Context context, String Url) throws ConnectTimeoutException {

        String[] result = {"", ""};
        try {

            HttpsURLConnection urlConnection = CustomHttpsURLConnection.getMyHttpsURLConnection(context, Url, true);
            Log.i("TAG_URL : ", Url);

            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(connection_timeout);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (Build.VERSION.SDK_INT > 13) {
                urlConnection.setRequestProperty("Connection", "close");
            }

            urlConnection.setUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            // always check HTTP response code first
            int responseCode = urlConnection.getResponseCode();
            result[0] = responseCode + "";

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get Response
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                if (!TextUtils.isEmpty(response)) {
                    result[0] = HttpConnectionUrl.RESPONSECODE_REQUESTSUCCESS;
                    result[1] = response.toString();
                    Log.i("TAG_RESPONSE : ", result[1]);
                }

            }

        } catch (UnsupportedEncodingException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (IOException e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        } catch (Exception e) {
            result[0] = HttpConnectionUrl.RESPONSECODE_CONNECTIONTIMEOUT;
            e.printStackTrace();
        }
        return result;
    }
}
