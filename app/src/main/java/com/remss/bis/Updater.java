package com.remss.bis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.remss.bis.MainActivity;

class Updater extends AsyncTask<MainActivity, Void, Void>
{
    private Exception exception;

    @Override
    protected Void doInBackground(MainActivity... activity)
    {
        try {
            checkUpdates(activity[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //обновление приложения по версии
//    Double getLastAppVersion()
//    {
//        try
//        {
//            // Create a URL for the desired page
//            // Создайте URL-адрес желаемой страницы
//            URL url = new URL(MainActivity.uri() + "update/releaseVersionCode.txt");
//
//            // Read all the text returned by the server
//            // Читаем весь текст, возвращаемый сервером
//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//
//            String str;
//            double lastVersion;
//
//            while ((str = in.readLine()) != null)
//            {
//                int f = str.indexOf("releaseVersionCode");
//                if (f != -1)
//                {
//                    str = str.substring(f + ("releaseVersionCode").length()).trim();
//                    Log.d("Brooke", "Последняя версия приложения: " + str);
//                    lastVersion = Double.parseDouble(str);
//                    return lastVersion;
//                }
//            }
//            in.close();
//            Log.d("Brooke", "Не удалось получить последнюю версию приложения!");
//        }
//        catch (Exception e)
//        {
//            Log.d("Brooke", "Не удалось получить последнюю версию приложения: ");
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    void checkUpdates(final MainActivity activity)
//    {
//        final Double lastAppVersion = getLastAppVersion();
//        if (lastAppVersion == null)
//        {
//            return;
//        }
//        if (lastAppVersion <= Double.parseDouble(BuildConfig.VERSION_NAME))
//        {
//            Log.d("Brooke", "Версия приложения актуальна, обновление не требуется.");
//            return;
//        }
//
//        String li = SettingsManager.get(activity, "LastIgnoredUpdateVersion");
//        if (li != null)
//        {
//            Double liInt = Double.parseDouble(li);
//            if (liInt >= lastAppVersion)
//                return;
//        }
//
//        activity.Update(lastAppVersion);
//    }


    // обновление приложения по дате последнего изменения
    String getLastAppVersion()
    {
        try
        {
            // Создайте URL-адрес желаемой страницы
            URL url = new URL(MainActivity.uri() + "update/app-debug.apk");

            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000); // timing out in a minute
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String releaseLastDate = simpleDateFormat.format(conn.getLastModified());
            Log.d("Brooke", "Дата последнего изменения .apk файла: " + releaseLastDate);
            return releaseLastDate;
        }
        catch (Exception e)
        {
            Log.d("Brooke", "Не удалось получить последнюю версию приложения: ");
            e.printStackTrace();
        }
        return null;
    }

    void checkUpdates(final MainActivity activity) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd");

        final String releaseLastDate = getLastAppVersion();
        String buildDate = fmt.format(BuildConfig.buildTime);

        Calendar calendarReleaseLastDate = Calendar.getInstance();
        Calendar calendarBuildDate = Calendar.getInstance();
        calendarReleaseLastDate.setTime(fmt.parse(releaseLastDate));
        calendarBuildDate.setTime(fmt.parse(buildDate));

        if (calendarReleaseLastDate == null)
        {
            return;
        }

        // если дата последнего релиза .apk файла на сервере совпадает с датой сборки приложения либо меньше ее, то ничего не делать
        if (calendarReleaseLastDate.compareTo(calendarBuildDate) <= 0) {
            Log.d("Brooke", "Версия приложения актуальна, обновление не требуется.");
            return;
        }

        //иначе если дата последнего релиза больше даты сборки, то вызывать метод обновлнения
        activity.Update(releaseLastDate);
    }
}
