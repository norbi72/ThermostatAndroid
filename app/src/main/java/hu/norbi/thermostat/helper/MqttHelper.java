package hu.norbi.thermostat.helper;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

import hu.norbi.thermostat.R;

public class MqttHelper {
    private MqttAndroidClient mqttAndroidClient;

    private final String serverUri;

    @SuppressWarnings("FieldCanBeLocal")
    private final String clientId;
    private final String subscriptionTopic;

    private final String username;
    private final String password;

    public MqttHelper(Context context){
        serverUri = context.getString(R.string.mqtt_server_url);
        clientId = context.getString(R.string.mqtt_client_id);
        subscriptionTopic = context.getString(R.string.mqtt_subscription_topic);
        username = context.getString(R.string.mqtt_username);
        password = context.getString(R.string.mqrr_password);

        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                Log.w("mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("mqtt", "Failed to connect to: " + serverUri + " - " + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void sendMessage(String topic, String payload) {
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            mqttAndroidClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void requestMqttData() {
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"uptime\"}");
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"currentTarget\"}");
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"reference\"}");
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"icon\"}");
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"power\"}");
    }

    // {"target":[[19.5,20.9,20.5],[21.5,21.5,20.5]]}
    public void requestTargetTemperatures() {
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"target\"}");
    }

    // {"times":["5:45","17:30","22:45"]}
    public void requestTimes() {
        this.sendMessage("thermostat/state/cmd", "{\"get\":\"times\"}");
    }
}