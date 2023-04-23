package com.keimoger.hackerembassy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.keimoger.hackerembassy.R;

import io.reactivex.Completable;
import io.reactivex.Single;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<MQTTConnectionStatus> mMQTTStatusData = new MutableLiveData<>();
    private final MutableLiveData<Mqtt5Client> mMQTTClientData = new MutableLiveData<>();
    private final MutableLiveData<String> mMQTTConnectionErrorData = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public void authorise(String username, String password, String host) {
        mMQTTStatusData.setValue(MQTTConnectionStatus.LOADING);

        Mqtt5BlockingClient client = MqttClient.builder()
                .identifier(getApplication().getString(R.string.mqtt_id))
                .serverHost(host)
                .serverPort(1883)
                .useMqttVersion5()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .buildBlocking();

        Mqtt5ConnAck connAck;
        try {
            connAck = client.connect();
        } catch (Mqtt5ConnAckException | MqttClientStateException e) {
            mMQTTStatusData.postValue(MQTTConnectionStatus.LOADED);
            mMQTTStatusData.postValue(MQTTConnectionStatus.INVALID);
            mMQTTConnectionErrorData.postValue(e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }
        mMQTTStatusData.postValue(MQTTConnectionStatus.LOADED);
        mMQTTStatusData.postValue(MQTTConnectionStatus.OK);
        mMQTTClientData.postValue(client);
        client.disconnect();
    }

    public LiveData<MQTTConnectionStatus> getStatusData() {
        return mMQTTStatusData;
    }

    public MutableLiveData<Mqtt5Client> getMQTTClientData() {
        return mMQTTClientData;
    }

    public MutableLiveData<String> getMQTTConnectionErrorData() {
        return mMQTTConnectionErrorData;
    }

    public enum MQTTConnectionStatus {
        LOADING, LOADED, INVALID, OK
    }
}
