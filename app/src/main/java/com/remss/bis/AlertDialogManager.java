package com.remss.bis;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogManager {
    /**
     * Function to display simple Alert Dialog. Функция отображения простого диалогового окна предупреждений
     * @param context - application context. контекст приложения
     * @param title - alert dialog title. заголовок диалогового окна предупреждения
     * @param message - alert message. предупреждающее сообщение
     * @param status - success/failure (used to set icon). успех / неудача (используется для установки значка)
     *               - pass null if you don't want icon. передать null, если значок не нужен
     * */
    public void showAlertDialog(Context context, String title, String message, Boolean status)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        // Установка заголовка диалога
        alertDialog.setTitle(title);

        // Setting Dialog Message
        // Установка диалогового сообщения
        alertDialog.setMessage(message);

        if(status != null)
        {
            // Setting alert dialog icon
            // Установка значка диалогового окна предупреждения
            alertDialog.setIcon((status) ? R.drawable.ic_success : R.drawable.ic_fail);
        }

        // Setting OK Button
        // Настройка кнопки ОК
        alertDialog.setButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });

        // Showing Alert Message
        // Отображение предупреждающего сообщения
        alertDialog.show();
    }
}
