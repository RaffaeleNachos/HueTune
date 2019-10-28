package com.example.huetune;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GetSpotifySessionToken extends AsyncTask {

    private RequestQueue requestQueue;
    private Context ctx;
    private String sessionToken;

    GetSpotifySessionToken(String sessionToken, Context ctx){
        this.sessionToken=sessionToken;
        this.ctx=ctx;
        requestQueue = Volley.newRequestQueue(ctx);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        getSpotifyToken();
        return null;
    }

    //get session token from soptify web api call
    private void getSpotifyToken(){
        String rest = "https://accounts.spotify.com/api/token";
        StringRequest jsonreq = new StringRequest  //uso stringrequest perchè JSONObjreq fa override di getparams() e non chiama il metodo
                (Request.Method.POST, rest, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject token;
                        try {
                            token = new JSONObject(response);
                            sessionToken = token.getString("access_token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Log.w("errorresp", error.toString());
                                Toast.makeText(ctx, "Spotify Token not received, Restart App", Toast.LENGTH_SHORT).show();
                            }
                        })
        {
            // -H parametes
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String mydashkey = "24ffb05d1e82431b91638ab90386fc84:710d0f96762c4d4ca6c81d97aec82556"; //trasformo codiceid:keyid di spotify in base 64
                mydashkey = Base64.encodeToString(mydashkey.getBytes(), Base64.NO_WRAP); //nowrap per evitare \n
                mydashkey = "Basic " + mydashkey;
                //Log.w("key", mydashkey);
                headers.put("Authorization", mydashkey);
                return headers;
            }
            // -d parameters
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("grant_type", "client_credentials");
                return params;
            }
        };
        requestQueue.add(jsonreq);
    }

}
