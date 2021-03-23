package com.remss.bis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static com.remss.bis.MainActivity.androidID;
import static com.remss.bis.MainActivity.manufacturer_model;
import static com.remss.bis.MainActivity.password;
import static com.remss.bis.MainActivity.username;

public class LoginActivity extends AppCompatActivity
{
    // Username, password edittext
    EditText txtUsername, txtPassword;

    // login button
    Button btnLogin;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Session Manager Class
    SessionManager session;

    public static String username;
    public static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Session Manager
        session = new SessionManager(getApplicationContext());

        // Username, Password input text
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        Toast.makeText(getApplicationContext(), "Статус входа пользователя: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        // Login button
        btnLogin = (Button) findViewById(R.id.btnLogin);

        // Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0) {
                // Get username, password from EditText
                username = txtUsername.getText().toString();
                password = txtPassword.getText().toString();


                // НЕОБХОДИМО ПОДКЛЮЧЕНИЕ К БАЗЕ И ЧТЕНИЕ ДАННЫХ ИЗ НЕЕ ДЛЯ ПРОВЕРКИ НА КОРРЕКТНОСТЬ ВВОДА ДАННЫХ

                // Check if username, password is filled
                // Проверить заполнены ли логин/пароль
                if (username.trim().length() > 0 && password.trim().length() > 0) {
                    // For testing puspose username, password is checked with sample data
                    // В целях тестирования имя пользователя, пароль проверяется с помощью выборочных данных.
                    // username = test
                    // password = test
                    if(username.equals("test") && password.equals("test"))
                    {
                        // Creating user login session
                        // For testing i am stroing name, email as follow
                        // Use user real data
                        // Создание сеанса входа пользователя
                        // Для тестирования выводятся данных указанные ниже
                        // Использовать реальные данные пользователя
//                        session.createLoginSession("Android Hive", "anroidhive@gmail.com");
                        session.createLoginSession(username);

                        // Staring MainActivity
                        // запуск MainActivity
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else{
                        // username / password doesn't match
                        // имя пользователя / пароль не совпадают
                        alert.showAlertDialog(LoginActivity.this, "Ошибка входа...", "Логин/Пароль введены некорректно", false);
                    }
                } else {
                    // user didn't entered username or password
                    // Show alert asking him to enter the details
                    // пользователь не ввел логин или пароль
                    // Показать предупреждение с просьбой ввести данные
                    alert.showAlertDialog(LoginActivity.this, "Ошибка входа...", "Пожалуйста введите Логин и Пароль", false);
                }
            }
        });
    }
}