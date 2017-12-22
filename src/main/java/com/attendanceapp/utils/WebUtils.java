package com.attendanceapp.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.attendanceapp.AppConstants;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.attendanceapp.AppConstants.TAG;

public final class WebUtils {

    public String connectToServer(String url, String requestMethod, Map<String, ?> keysValues) {
        new WorkAsync().execute(url, requestMethod, keysValues);
        return null;
    }

    public String connectToServer(String url, String requestMethod) {
        new WorkAsync().execute(url, requestMethod);
        return null;
    }

    private boolean isPostWorkDone;
    private String postResult;

    public String post(String endpoint, Map<String, String> params) throws IOException {


        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());

            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;

        try {
            Log.e("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }

            postResult = readInputStream(conn.getInputStream());

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }



        return postResult;
    }

    private String readInputStream(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line).append("\n");
        }



        return builder.toString();
    }

    private class WorkAsync extends AsyncTask<Object, Void, String> {
        String result = null;

        @Override
        protected String doInBackground(Object... params) {
            String url = (String) params[0];
            String requestMethod = (String) params[0];
            Map<String, String> keysValues = (Map<String, String>) params[2];


            DefaultHttpClient dhc = new DefaultHttpClient();
            ResponseHandler<String> res = new BasicResponseHandler();
            HttpPost postMethod = new HttpPost(url);

            List<NameValuePair> namepairs = new ArrayList<NameValuePair>();
            Iterator<String> iterator = keysValues.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = keysValues.get(key);
                namepairs.add(new BasicNameValuePair(key, value));
            }
            try {
                postMethod.setEntity(new UrlEncodedFormEntity(namepairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                result = dhc.execute(postMethod, res);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null) {
//                makeToast("Error in adding new student");

            } else {
                try {
                    JSONObject jObject = new JSONObject(s);

                    // check if result contains Error
                    if (jObject.has("Error")) {
//                        makeToast(jObject.getString("Error"));

                    } else if (jObject.has("Message")) {
//                        makeToast(jObject.getString("Message"));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @SuppressWarnings("deprecation")
    public static String getBeaconsJson() throws IOException {

        DefaultHttpClient dhc = new DefaultHttpClient();
        ResponseHandler<String> res = new BasicResponseHandler();
        HttpGet getMethod = new HttpGet(AppConstants.URL_BEACON_LIST);

        getMethod.addHeader("Accept-Charset", "utf-8");
        getMethod.addHeader("Accept", "application/json");
        getMethod.addHeader("app_1e2ims1641", "2ec3ee5dce97647cdd50d19a7fbe4561");
        getMethod.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("app_1e2ims1641", "2ec3ee5dce97647cdd50d19a7fbe4561"), "UTF-8", false));

        return dhc.execute(getMethod, res);
    }
}
