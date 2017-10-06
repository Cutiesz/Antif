package com.korsolution.antif;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CuteFeedJsonUtil {

    public static ArrayList<JSONObject> feed(String result) {

        ArrayList<JSONObject> _feedList = new ArrayList<JSONObject>();

        /*HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);*/
        //HttpResponse response;
        try {
            //response = httpclient.execute(httpget);
            //HttpEntity entity = response.getEntity();
            //if (entity != null) {
        	if (result != null) {
                //InputStream instream = entity.getContent();
                //String result = CMStringUtil.convertStreamToString(instream);
                //JSONArray ja = new JSONArray(result);
        		JSONArray ja = new JSONArray(result);
                for (int j = 0; j < ja.length(); j++) {
                    _feedList.add((JSONObject) ja.get(j));
                }
                // Closing the input stream will trigger connection release
                //instream.close();
            }
        } catch (Exception e) {
            return null;
        }
        return _feedList;
    }

}
