package com.remss.bis;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ReceiverTimer extends BroadcastReceiver
{

    NotificationCompat.Builder builder;
    public static final int NOTIFY_ID = 1;
    public static final String CHANNEL_ID = "bisnotifyid";
    public static JSONObject resp;
    static String old_resp = "";
//    Intent show_intent;
    Context gContext;
    Intent gIntent;
    //String resp;


        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("TAG", "onReceive notify" );
            gContext = context;
            gIntent = intent;

            SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(context);
            final String s_username = appSettings.getString("username","");
            final String s_password = appSettings.getString("password","");
            final String s_manufacturer_model = MainActivity.manufacturer_model;
            final String s_androidID = MainActivity.androidID;
            final String url = appSettings.getString("uri","");

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            // response
                            Log.d("TAG", response);
                            Log.d("TAG", "RESPONSE: " + response);
                            Log.d("TAG", "URL: " + url);

                            try
                            {
                                resp = new JSONObject("{}");
                                resp = new JSONObject(response);

                                //show_intent.putExtra( "NotificationMessage" , resp.toString() );
                                // records_count = resp.getInt("records");
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }

                            if (!old_resp.equals(response))
                            {
                                try
                                {
                                    resp = new JSONObject("{}");
                                    resp = new JSONObject(response);
                                    //show_intent.putExtra( "NotificationMessage" , resp.toString() );
                                    // records_count = resp.getInt("records");
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }

//                                if (!response.isEmpty()) {
//                                    // builder.setContentText(records_count + " новых документов");
//                                    Log.d("TAG", "show new notify. old resp: " + old_resp);
//                                    showNotify(gContext, gIntent, resp);
//                                }
                            }
                            else
                            {
                                Log.d("TAG", "old resp same like new resp ");
                            }

                            old_resp = String.valueOf(response);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            // error
                            Log.d("TAG", "error on post response");
                        }
                    }
            )
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("user", s_username);
                    params.put("pass", s_password);
                    params.put("manufacturer_model", s_manufacturer_model);
                    params.put("androidID", s_androidID);
//                    params.put("reqpage", "mdoc_is_newdoc.app.php");

                    Log.d("TAG", "PARAMS: " + params);

                    return params;
                }
            };
            RequestQueue requestQueue;
            requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(postRequest);
        }

    public void showNotify(Context context, Intent intent, JSONObject json_resp)
    {
        int records_count = 0;
        String notifyTitle = "";
        String notifyText = "";
        try
        {
            records_count = json_resp.getInt("records");
            notifyTitle = json_resp.getString("notifyTitle");
            notifyText = json_resp.getString("notifyText");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if(records_count == 0)
        {
            Log.d("TAG", "showNotify: response records count is 0");
            return;
        }

        Intent show_intent = new Intent(context, MainActivity.class);
        show_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        show_intent.putExtra( "NotificationMessage" , json_resp.toString() );
        show_intent.addCategory(Intent. CATEGORY_LAUNCHER ) ;
        show_intent.setAction(Intent. ACTION_MAIN ) ;
        //show_intent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP ) ;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), show_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_document)
                .setContentTitle(notifyTitle)
                .setContentText(notifyText)
                //.setNumber(records_count)
                // .setStyle(new NotificationCompat.BigTextStyle()
                //        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        //builder.setContentText(records_count + " новых документов");

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }
}
