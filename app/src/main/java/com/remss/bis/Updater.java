package com.remss.bis;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

class Updater extends AsyncTask<MainActivity, Void, Void>
{
    private Exception exception;

    @Override
    protected Void doInBackground(MainActivity... activity)
    {
        checkUpdates(activity[0]);
        return null;
    }

    Double getLastAppVersion()
    {
        try
        {
            // Create a URL for the desired page
            // Создайте URL-адрес желаемой страницы
//            URL url = new URL("https://raw.githubusercontent.com/jehy/rutracker-free/master/app/build.gradle");
            URL url = new URL("https://raw.githubusercontent.com/medvedevanatalya/bis/main/app/build.gradle");

            // Read all the text returned by the server
            // Читаем весь текст, возвращаемый сервером
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String str;
            double lastVersion;

            while ((str = in.readLine()) != null)
            {
                int f = str.indexOf("releaseVersionCode");
                if (f != -1)
                {
                    str = str.substring(f + ("releaseVersionCode").length()).trim();
                    Log.d("Brooke", "Последняя версия приложения: " + str);
                    lastVersion = Double.parseDouble(str);
//                    return Integer.parseInt(str);
                    return lastVersion;
                }
            }
            in.close();
            Log.d("Brooke", "Не удалось получить последнюю версию приложения!");
        }
        catch (Exception e)
        {
            Log.d("Brooke", "Не удалось получить последнюю версию приложения:");
            e.printStackTrace();
        }
        return null;
    }

    void checkUpdates(final MainActivity activity)
    {
//        final Integer lastAppVersion = getLastAppVersion();
        final Double lastAppVersion = getLastAppVersion();
        if (lastAppVersion == null)
        {
            return;
        }
        if (lastAppVersion <= BuildConfig.VERSION_CODE)
        {
            Log.d("Brooke", "Версия приложения актуальна, обновление не требуется ");
            return;
        }

        String li = SettingsManager.get(activity, "LastIgnoredUpdateVersion");
        if (li != null)
        {
            Integer liInt = Integer.parseInt(li);
            if (liInt >= lastAppVersion)
                return;
        }

        activity.Update(lastAppVersion);
    }
}
