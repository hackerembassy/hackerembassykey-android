package com.keimoger.hackerembassy.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.keimoger.hackerembassy.R;
import com.keimoger.hackerembassy.util.PreferencesUtil;
import com.keimoger.hackerembassy.util.fragment.FragmentNavigator;
import com.keimoger.hackerembassy.util.fragment.MyFragmentBuilder;
import com.keimoger.hackerembassy.viewmodel.DoorlockViewModel;

public class MainActivity extends AppCompatActivity {

    private FragmentNavigator mFragmentNavigator;

    private boolean mLinkProcessed = false;

    private DoorlockViewModel mDoorlockViewModel;

    private Mqtt5RxClient mMQTTClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDoorlockViewModel = new ViewModelProvider(this).get(DoorlockViewModel.class);
        mFragmentNavigator = new FragmentNavigator(getSupportFragmentManager(), R.id.main_container, new MyFragmentBuilder());

        PreferencesUtil mPreferencesUtil = new PreferencesUtil(getApplicationContext());

        setupBottomNavigation();

        if (mPreferencesUtil.getUsername() == null || mPreferencesUtil.getUsername().equals("") ||
                mPreferencesUtil.getPassword() == null || mPreferencesUtil.getPassword().equals("") ||
                mPreferencesUtil.getHost() == null || mPreferencesUtil.getHost().equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            setupService(mPreferencesUtil.getUsername(), mPreferencesUtil.getPassword(), mPreferencesUtil.getHost());

            if (savedInstanceState == null) {
                mFragmentNavigator.openFragment("doorlock");
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_doorlock) {
                mFragmentNavigator.openFragment("doorlock");
                return true;
            } else if (itemId == R.id.navigation_settings) {
                mFragmentNavigator.openFragment("settings");
                return true;
            } else if (itemId == R.id.navigation_info) {
                mFragmentNavigator.openFragment("info");
                return true;
            }
            return false;
        });
    }

    public void setupService(String username, String password, String host) {
        mMQTTClient = MqttClient.builder()
                .identifier("HackerEmbassy")
                .serverHost(host)
                .serverPort(1883)
                .useMqttVersion5()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .buildRx();
    }

    public Mqtt5RxClient getMQTTClient() {
        return mMQTTClient;
    }
}
