package com.keimoger.hackerembassy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.keimoger.hackerembassy.R;

public class DoorlockViewModel extends AndroidViewModel {
    private Mqtt5BlockingClient mMQTTClient;

    public DoorlockViewModel(@NonNull Application application) {
        super(application);
        mClientReady.setValue(null);
    }

    private final MutableLiveData<Boolean> mClientReady = new MutableLiveData<>();
    private final MutableLiveData<String> mMQTTConnectionError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDoorOpened = new MutableLiveData<>();

    private String username;
    private String password;
    private String host;

    public void setupMQTT(String username, String password, String host) {
        this.username = username;
        this.password = password;
        this.host = host;
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
            mClientReady.postValue(false);
            mMQTTConnectionError.postValue(e.getLocalizedMessage());
            e.printStackTrace();
            return;
        } catch (ConnectionFailedException e) {
            mClientReady.postValue(false);
            mMQTTConnectionError.postValue(e.getCause().getMessage());
            e.printStackTrace();
            return;
        }
        mMQTTClient = client;
        mClientReady.postValue(true);

        mMQTTClient.toAsync().subscribeWith()
                .topicFilter("door/response")
                .callback(msg -> mDoorOpened.postValue(true))
                .send();
    }

    public void openDoor() {
        try {
            mMQTTClient.publishWith()
                    .topic("door")
                    .retain(false)
                    .qos(MqttQos.AT_MOST_ONCE)
                    .payload("1".getBytes())
                    .send();
        } catch (Exception e) {
            mClientReady.postValue(false);
            if (e.getCause() != null)
                mMQTTConnectionError.postValue(e.getCause().getMessage());
            else
                mMQTTConnectionError.postValue(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        mMQTTClient.disconnect();
    }

    public MutableLiveData<Boolean> getClientReady() {
        return mClientReady;
    }

    public MutableLiveData<String> getMQTTConnectionError() {
        return mMQTTConnectionError;
    }

    public MutableLiveData<Boolean> getDoorOpenedData() {
        return mDoorOpened;
    }
}
