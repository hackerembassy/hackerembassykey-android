package com.keimoger.hackerembassy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.keimoger.hackerembassy.R;
import com.keimoger.hackerembassy.util.PreferencesUtil;
import com.keimoger.hackerembassy.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton mConnectButton;
    private EditText mUsernameText;
    private EditText mPasswordText;
    private EditText mHostText;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        mUsernameText = findViewById(R.id.mqtt_username_field);
        mPasswordText = findViewById(R.id.mqtt_password_field);
        mHostText = findViewById(R.id.mqtt_host_field);
        mConnectButton = findViewById(R.id.proceed_button);

        loginViewModel.getStatusData().observe(this, status -> {
            switch (status) {
                case LOADING:
                    mConnectButton.setEnabled(false);
                    mUsernameText.setEnabled(false);
                    mPasswordText.setEnabled(false);
                    mHostText.setEnabled(false);
                    break;
                case LOADED:
                    mConnectButton.setEnabled(true);
                    mUsernameText.setEnabled(true);
                    mPasswordText.setEnabled(true);
                    mHostText.setEnabled(true);
                    break;
                case INVALID:
                    loginViewModel.getMQTTConnectionErrorData().observe(this, error -> {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setPositiveButton("OK", (dialogInterface, id) -> dialogInterface.dismiss())
                                .setTitle(R.string.login_alert_error_title)
                                .setCancelable(true)
                                .setIcon(R.drawable.ic_round_error_outline_24)
                                .setMessage(loginViewModel.getMQTTConnectionErrorData().getValue())
                                .create();
                        dialog.show();
                    });
                    mConnectButton.setEnabled(true);
                    mUsernameText.setEnabled(true);
                    mPasswordText.setEnabled(true);
                    mHostText.setEnabled(true);
                    break;
                case OK:
                    new PreferencesUtil(this)
                            .setUsername(mUsernameText.getText().toString())
                            .setPassword(mPasswordText.getText().toString())
                            .setHost(mHostText.getText().toString());
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
            }
        });

        mConnectButton.setOnClickListener(v ->
                loginViewModel.authorise(mUsernameText.getText().toString(),
                        mPasswordText.getText().toString(),
                        mHostText.getText().toString()
                )
        );
    }
}
