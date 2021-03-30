package com.remss.bis;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.remss.bis.ReceiverTimer.CHANNEL_ID;

public class MainActivity extends AppCompatActivity
{
    WebView myWebView;
    WebAppInterface mWebAppInterface;

    ProgressBar mProgressBar;

    public static SharedPreferences appSettings;

    String old_uri = "";
    String old_username = "";
    String old_password = "";
    String curLoadedPage = "";
    String isShowDialog = "false";
    boolean clearHistory = false;
    boolean needShowDialog = false;

    public static String androidID = "";
    public static String manufacturer_model = "";

    TextView text_login_url;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint({"SetTextI18n", "HardwareIds", "NewApi", "LocalSuppress", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.logo);// set drawable icon
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBarLogo = getSupportActionBar();
        actionBarLogo.setHomeAsUpIndicator(R.drawable.logo1);
        actionBarLogo.setDisplayHomeAsUpEnabled(true);


        androidID = Settings.System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        manufacturer_model = Build.MANUFACTURER + " " + Build.MODEL;

        appSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mWebAppInterface = new WebAppInterface(this);
        myWebView = (WebView) findViewById(R.id.webView);

        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= 19)
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }


        //SwipeRefreshLayout
        final SwipeRefreshLayout finalMySwipeRefreshLayout1;
        finalMySwipeRefreshLayout1 = findViewById(R.id.swiperefresh);
        finalMySwipeRefreshLayout1.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                myWebView.loadUrl(myWebView.getUrl());
            }
        });

        mProgressBar = findViewById(R.id.pb);

        myWebView.setWebViewClient(new MyWebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Visible the progressbar
                mProgressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                finalMySwipeRefreshLayout1.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        // MyWebChromeClient в реализации класса можно реагировать на вызов из JS функции ALERT()
        myWebView.setWebChromeClient(new MyWebChromeClient(){
            public void onProgressChanged(WebView view, int newProgress){
                // Update the progress bar with page loading progress
                mProgressBar.setProgress(newProgress);
                if(newProgress == 100){
                    // Hide the progressbar
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        // прозрачный фон для WebView
        myWebView.setBackgroundColor(Color.TRANSPARENT);

        //возможность масштабирования страницы
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setSupportZoom(true);

        myWebView.getSettings().setAllowContentAccess(true);

        // установить интерфейс взаимодействия с JavaScript загружаемой web страницы
//        myWebView.addJavascriptInterface(new WebAppInterface(this),"Android");
        myWebView.addJavascriptInterface(mWebAppInterface,"Android");

        myWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearCache(true);

        loadWebView();

        new Updater().execute(this);

        createNotificationChannel();
        setAlarm(this);
    }


    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "bis_notify";
            String description = "bis notify";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

/*
    @Override
    protected void onNewIntent (Intent intent) {
        Log.d("TAG", "onNewIntent event");
        super .onNewIntent(intent) ;

        Log.d("TAG", "onNewIntent global response: " + ReceiverTimer.resp.toString());

        //final String s_username = appSettings.getString("username", "");
        //final String s_password = appSettings.getString("password", "");
        String url = appSettings.getString("uri", "");

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("TAG", "onResponse after check as readed");

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("TAG", "error on post after check as readed");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("user", s_username);
                //params.put("pass", s_password);
                params.put("reqpage", "mdoc_check_as_readed.app.php");
                params.put("jsondoc", ReceiverTimer.resp.toString());

                return params;
            }
        };
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(postRequest);
    }
*/

    @Override
    public void onBackPressed()
    {
        Log.d("TAG", "onBackPressed: try go to back...");
        Log.d("TAG", "onBackPressed: can go back is " + myWebView.canGoBack());
        if(isShowDialog.equals("true"))
        {
            Log.d("TAG", "onBackPressed: JS go to back");
            myWebView.loadUrl("javascript:window.goToBackDlg()");
        }
        else if (myWebView.canGoBack())
        {
            String historyUrl = "";
            WebBackForwardList mWebBackForwardList = myWebView.copyBackForwardList();
            if (mWebBackForwardList.getCurrentIndex() > 0)
            {
                historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1).getUrl();
            }

            Log.d("TAG", "onBackPressed: back to prev page " + historyUrl);

            myWebView.goBack();
        }
        else
        {
            Log.d("TAG", "onBackPressed: system back");
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch(id)
        {
            case R.id.action_settings :
                old_uri = uri();
                old_username = username();
                old_password = password();
                Intent intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_home :
                loadWebView();
                return  true;
            case R.id.action_refresh :
                Log.d("TAG", "URL MyWebView: " + myWebView.getUrl());
                myWebView.reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null)
        {
            if (intent.getExtras().getString("NotificationMessage") != null)
            {
                Log.d("TAG", "onResume: extra " + intent.getExtras().getString("NotificationMessage"));
                String url = appSettings.getString("uri", "");

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response)
                            {
                                // response
                                Log.d("TAG", "onResponse after check as readed");
                                Log.d("TAG", "RESPONSE MAIN" + response);
                                needShowDialog = true;
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                // error
                                Log.d("TAG", "error on post after check as readed");
                            }
                        }
                )
                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        //params.put("user", s_username);
                        //params.put("pass", s_password);
                        params.put("reqpage", "mdoc_check_as_readed.app.php");
                        params.put("jsondoc", ReceiverTimer.resp.toString());

                        return params;
                    }
                };
                RequestQueue requestQueue;
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(postRequest);

            }
            else if (intent.getExtras().getString("SettingsExtra") != null)
            {
                Log.d("TAG", "onResume: extra " + intent.getExtras().getString("SettingsExtra"));

                if( !uri().equals(old_uri) || !username().equals(old_username) || !password().equals(old_password) )
                {
                    Log.d("TAG", "onResume: start to load page");
                    loadWebView();
                };
            }
        }
        else { }
    }

    public static String uri()
    {
        return appSettings.getString("uri","");
    }
    public static String username()
    {
        return appSettings.getString("username","");
    }
    public static String password()
    {
        return appSettings.getString("password","");
    }


    @SuppressLint({"SetTextI18n", "HardwareIds"})
    public void loadWebView()
    {
        Log.d("TAG", "loadWebView: current loaded page: " + curLoadedPage);

        if( curLoadedPage.isEmpty() || curLoadedPage.equals(uri()) )
        {
            // подготовка параметров для отправки post запроса
            String postParams = "";
            try
            {
                postParams = "user=" + URLEncoder.encode(username(), "UTF-8") +
                        "&pass=" + URLEncoder.encode(password(), "UTF-8") +
                        "&type=" + URLEncoder.encode("app", "UTF-8") +
                        "&manufacturer_model=" + URLEncoder.encode(manufacturer_model, "UTF-8") +
                        "&androidID=" + URLEncoder.encode(androidID, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

        text_login_url = findViewById(R.id.login_url);
        if(username().isEmpty() && !uri().isEmpty())
        {
            text_login_url.setText("\tВы не ввели имя пользователя! \n\tАдресс страницы: " + uri());
        }
        else if(!username().isEmpty() && uri().isEmpty())
        {
            text_login_url.setText("\tИмя пользователя: " + username() + "\n\tВы не ввели адресс страницы!");
        }
        else if(!username().isEmpty() && !uri().isEmpty())
        {
            text_login_url.setText("\tИмя пользователя: " + username() + "\n\tАдресс страницы: " + uri());
        }
        else if(username().isEmpty() && uri().isEmpty())
        {
            text_login_url.setText("\tВы не ввели имя пользователя! \n\tВы не ввели адресс страницы!");
        }

            // загрузка страницы в браузер через post запрос
            curLoadedPage = uri();
            myWebView.postUrl(curLoadedPage, postParams.getBytes());
            // отчищаем историю браузера, т.к. после обновления одной и тойже страницы браузер пытается вернуться на неё же
            clearHistory = true;
            // сбрасываем признак отображения внутреннего диалога страницы
            isShowDialog = "false";
        }
        else {
//            myWebView.reload();
        }
    }


    // реализация интерфейса для взаимодействия с JavaScript загружаемых страниц
    public class WebAppInterface
    {
        Context mContext;
        // конструктор класса
        WebAppInterface(Context c)
        {
            mContext = c;
        }
        // методы которые можно вызывать из JavaScript внутри web страниц
        // например следующий метод можно вызвать со страницы следующим образом
        // Android.showToast(msg);
        // имя класса Android задаётся функцией  myWebView.addJavascriptInterface(new WebAppInterface(this),"Android");
        @JavascriptInterface
        public void showToast(String toast)
        {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void setUsername(String val)
        {
            appSettings.getString("username", val);
        }
        @JavascriptInterface
        public void setPassword(String val)
        {
            appSettings.getString("password", val);
        }


        @JavascriptInterface
        public void closeApp()
        {
            finish();
            System.exit(0);
        }

        @JavascriptInterface
        public void setIsShowDialog( String val )
        {
            isShowDialog = val;
            Log.d("TAG", "setIsShowDialog: " + isShowDialog);
        }
    }


    // реализация класса который контролирует переход по ссылкам
    private class MyWebViewClient extends WebViewClient
    {

//        @SuppressLint("SetJavaScriptEnabled")
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url)
//        {
////            // если адрес загружаемой страницы совпадает с указанной строкой - это наш сайт
////            if("45.13.18.46".equals(Uri.parse(url).getHost()))
////            {
////                Log.d("TAG", "shouldOverrideUrlLoading: return false");
////                return false;
////            }
////            // если запрашивается любой другой адрес - загружаем сайт через стандартный браузер
////            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
////            startActivity(intent);
////            Log.d("TAG", "shouldOverrideUrlLoading: return true");
////            return true;
//
//            Log.d("TAG", Uri.parse(url).getHost());
//            Log.d("TAG", url);
//
//            return false;
//        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if(url.startsWith("http:") || url.startsWith("https:"))
            {
                view.loadUrl(url);
                return true;
            }
            else if (url.startsWith("tel:"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                view.reload();
            }

            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            curLoadedPage = url;
            if (clearHistory)
            {
                clearHistory = false;
                view.clearHistory();
            }

            if(needShowDialog)
            {
                needShowDialog = false;
                myWebView.loadUrl("javascript:window.showIsNewDoc('user')");
            }

            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
        {
            Log.d("TAG", "onReceivedSslError: SSL error");
            handler.proceed(); // Ignore SSL certificate errors
        }
    }

    // реализация класса для того чтобы в JS работал Alert()
    private static class MyWebChromeClient extends WebChromeClient
    {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            return super.onJsAlert(view,url,message,result);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPermissionRequest(PermissionRequest request)
        {
            Log.i("TAG", "onPermissionRequest");
             request.grant(request.getResources());
        }
    }

     // устанавливает и настраивает период уведомлений
    public static void setAlarm(Context ctx)
    {
        AlarmManager am = (AlarmManager) ctx.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentToTimerReceiver = new Intent(ctx.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction("someAction");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx.getApplicationContext(),
                0,
                intentToTimerReceiver,
                PendingIntent.FLAG_UPDATE_CURRENT /* was -> FLAG_CANCEL_CURRENT*/ );

        int periodInMinutes = 1;
        long periodInMiliseconds = periodInMinutes * 60 * 1000;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, periodInMiliseconds, periodInMiliseconds, pendingIntent);
        }
        else
        {
            am.setRepeating(AlarmManager.RTC_WAKEUP, periodInMiliseconds, periodInMiliseconds, pendingIntent);
        }
    }

    //отменяет уведомление
    private static void cancelAlarm(Context ctx)
    {
        // Log.i(TAG, "cancelAlarm");
        AlarmManager am = (AlarmManager) ctx.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentToTimerReceiver = new Intent(ctx.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction("someAction");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx.getApplicationContext(), 0,
                intentToTimerReceiver,
                PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }


    // на обновление версии приложения
    public void Update(final Double lastAppVersion)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Доступно обновление приложения Brooke до версии " +
                        lastAppVersion + " - желаете обновиться? " +
                        "Если вы согласны - вы будете перенаправлены к скачиванию APK файла," +
                        " который затем нужно будет открыть.")
                        .setCancelable(true)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                        String apkUrl = "https://github.com/medvedevanatalya/bis/releases/download/" +
                                        lastAppVersion + "/app-debug.apk";
                                //intent.setDataAndType(Uri.parse(apkUrl), "application/vnd.android.package-archive");
                                intent.setData(Uri.parse(apkUrl));

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SettingsManager.put(MainActivity.this, "LastIgnoredUpdateVersion", lastAppVersion.toString());
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}



