
package com.attendanceapp.webserviceCommunicator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.AsyncTask;

/**
 * <h1>WebServiceHandler</h1>
 * It's responsible for calling Web APIS and return response to caller class
 *
 * @author Canopus
 * @version 1.0
 * @since 2015-02-20
 */
public class WebServiceHandler {

    public static WebServiceHandler mRef = null;

    public static WebServiceHandler getInstance() {
        if (mRef == null) {
            mRef = new WebServiceHandler();
        }
        return mRef;
    }

    /**
     * Method use to call regular Web Service
     *
     * @param params
     * @return String
     */
    public String webServiceCall(List<NameValuePair> params, String url) {
        String resultString = "";
        try {
//            HttpParams httpParams = new BasicHttpParams();
//            HttpConnectionParams.setConnectionTimeout(httpParams, 60000);

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = null;

            httpPost = new HttpPost(url);


            //HttpEntity request = ;
            if (params != null) {
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            }
            HttpResponse response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
//			if (code == 200) {
            HttpEntity resultEntity = response.getEntity();
            InputStream inputStream = resultEntity.getContent();
            resultString = convertStreamToString(inputStream);
            System.out.println("-------------------------Response" + resultString);
            inputStream.close();
//			}

        } catch (NoHttpResponseException noResponse) {
            noResponse.printStackTrace();
            resultString = "Connection Error";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }

    /**
     * method for convert input stream to String
     *
     * @return String
     */
    private String convertStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(
                is));
        try {
            while ((line = bufferReader.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    class WebServiceAsynTask extends AsyncTask<Object, String, String> {

        private IServerResponse context;
        private int id;
        private String response;
        private String append;
        private String callType;

        @SuppressWarnings("unchecked")
        @Override
        protected String doInBackground(Object... params) {
            try {
                List<NameValuePair> para = null;
                if (params[0] instanceof List<?>) {
                    para = ((List<NameValuePair>) params[0]);
                }
                if (params[1] instanceof IServerResponse) {
                    context = (IServerResponse) params[1];
                }
                if (params[2] instanceof Integer) {
                    id = (Integer) params[2];
                }
                response = new WebServiceHandler().webServiceCall(para, append);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                context.serverResponse(response, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void callWebService(List<NameValuePair> para,
                               Context context, int id) {
        WebServiceAsynTask asyn = new WebServiceAsynTask();
        asyn.execute(para, context, id);
    }
}
