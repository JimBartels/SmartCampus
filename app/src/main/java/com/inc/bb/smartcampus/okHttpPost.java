package com.inc.bb.smartcampus;

import android.os.AsyncTask;
import android.os.Bundle;
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

public class okHttpPost extends AsyncTask<String, Void, Bundle> {

    String TAG = "OkHttpPost ";
    public interface AsyncResponse{
        void processFinish(Bundle output);
    }
    public AsyncResponse delegate = null;

    public okHttpPost(AsyncResponse delegate){
        this.delegate=delegate;
    }
    @Override
    protected Bundle doInBackground(String... strings) {
        try{
            String userId = strings[2];
            boolean j=false;
            Bundle returnMessage = new Bundle();
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, strings[1]);
        Request request = new Request.Builder()
                .url(strings[0])
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
            Log.d(TAG, "doInBackground: request sent");
        if(response.isSuccessful()){
            String responseBody = response.body().string();
            Log.d(TAG, "doInBackground: " + responseBody);
            if(responseBody!=null){
            try{
                JSONArray jArray1 = new JSONArray(responseBody);
                JSONObject jsonObject = jArray1.getJSONObject(0);
                JSONArray jArrayHits = jsonObject.getJSONObject("hits").getJSONArray("hits");
                int i=0;
                for(i=0 ; i<jArrayHits.length();i++){
                    JSONObject jObjectIter = jArrayHits.getJSONObject(i);
                    Log.d(TAG, "doInBackground: " + jObjectIter);
                    Log.d(TAG, "doInBackground: " + jObjectIter.getString("_id"));

                    if(jObjectIter.getString("_id").replace(".0","").equals(userId)){
                        j = true;
                    }
                    else{j = false;}
                }
                JSONObject jRectangleObject = jArray1.getJSONObject(1);
                JSONArray jCoordinates = jRectangleObject.getJSONObject("query").getJSONObject("geo_shape").getJSONObject("location").getJSONObject("shape").getJSONArray("coordinates").getJSONArray(0);
                double[] rectangleLat = new double[5];
                double[] rectangleLon = new double[5];
                for(i=0 ; i<jCoordinates.length();i++){
                    JSONArray jObjectIter = jCoordinates.getJSONArray(i);
                    Log.d(TAG, "doInBackground: " + jObjectIter);
                    rectangleLat[i] = jObjectIter.getDouble(0);
                    rectangleLon[i] = jObjectIter.getDouble(1);
                }
                returnMessage.putDoubleArray("rectangleLat",rectangleLat);
                returnMessage.putDoubleArray("rectangleLon",rectangleLon);
                Log.d(TAG, "doInBackground: " + jCoordinates);
                returnMessage.putString("returnMessage",responseBody);
                returnMessage.putBoolean("isInRectangle",j);

               return returnMessage;
            }
            catch(JSONException e){
                Log.e(TAG, "doInBackground: " + e.toString());
            }

            returnMessage.putString("returnMessage",responseBody);
            returnMessage.putBoolean("isInRectangle",j);
            return returnMessage; }
            }
            if(!response.isSuccessful()){
                Log.d(TAG, "doInBackground: not succesful");
            }
        }
        catch (IOException e){
            Bundle returnError = new Bundle();
            returnError.putString("error",e.toString());
            return returnError;}
        return null;
    }

    @Override
    protected void onPostExecute(Bundle result) {
        delegate.processFinish(result);
        super.onPostExecute(result);
    }
}
