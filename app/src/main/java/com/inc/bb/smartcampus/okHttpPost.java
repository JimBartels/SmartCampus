package com.inc.bb.smartcampus;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;





/**
 * Created by s163310 on 3/8/2018.
 */

public class okHttpPost extends AsyncTask<String, Void, String[]> {

    String TAG = "OkHttpPost ";
    public interface AsyncResponse{
        void processFinish(String[] output);
    }
    public AsyncResponse delegate = null;

    public okHttpPost(AsyncResponse delegate){
        this.delegate=delegate;
    }
    @Override
    protected String[] doInBackground(String... strings) {
        try{
            String userId = strings[2];
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, strings[1]);
        Request request = new Request.Builder()
                .url(strings[0])
                .post(body)
                .addHeader("Content-Type", "application/json")
               // .addHeader("X-M2M-Origin", "Cae-guest")
               // .addHeader("X-M2M-Key", "guestguest")
               // .addHeader("Accept", "application/json")
               // .addHeader("Cache-Control", "no-cache")
               // .addHeader("Postman-Token", "8036b953-7456-48d3-b446-77702913fd82")
                .build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            String responseBody = response.body().string();
            if(responseBody!=null){
            try{
                JSONObject jObject = new JSONObject(responseBody);
                JSONArray jArray = jObject.getJSONObject("hits").getJSONArray("hits");
                int i=0;
                for(i=0 ; i<jArray.length();i++){
                    JSONObject jObjectIter = jArray.getJSONObject(i);
                    Log.d(TAG, "doInBackground: " + jObjectIter);
                    Log.d(TAG, "doInBackground: " + jObjectIter.getString("_id"));

                    if(jObjectIter.getString("_id").replace(".0","").equals(userId)){
                        return new String[]{responseBody,"true"};
                    }
                }
            }
            catch(JSONException e){
                Log.e(TAG, "doInBackground: " + e.toString());
            }

            return new String[]{responseBody, "false"}; }
            }
        }
        catch (IOException e){return new String[]{e.toString(),"nothing"};}
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        delegate.processFinish(result);
        super.onPostExecute(result);
    }
}
