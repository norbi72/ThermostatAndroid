package hu.norbi.thermostat.helper;

import android.content.res.Resources;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;

public class ThermostatMqttCallbackExtended implements MqttCallbackExtended {
    private MainActivity mainActivity;
    final private String uptimeFormat;

    private void setUptime(String uptime) {
        final int up = Integer.parseInt(uptime);
        final int days = up / (24 * 60 * 60);
        final int daysRem = up % (24 * 60 * 60);
        final int hours = daysRem / (60 * 60);
        final int hoursRem = daysRem % (60 * 60);
        final int mins = hoursRem / 60;
        final int secs = hoursRem % 60;
        mainActivity.statusView.setText(String.format(uptimeFormat, days, hours, mins, secs));
    }

    public ThermostatMqttCallbackExtended(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        uptimeFormat = mainActivity.getResources().getText(R.string.uptime).toString();
    }

    @Override
    public void connectComplete(boolean b, String s) {
        Log.w("mqtt","Connected " + b + " - " + s);
        mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"uptime\"}");
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String msg = mqttMessage.toString();
        Log.w("Debug", "Topic: " + topic + " Msg: " + msg);
        //dataReceived.setText(mqttMessage.toString());
        if (topic.endsWith("/response")) {
            if (msg.contains("\"uptime\":")) {
                setUptime(msg.substring(msg.indexOf("\"uptime\":")+9, msg.length()-1));
            }
            //mainActivity.statusView.setText(msg);

            final long timestamp = System.currentTimeMillis();
            final long newTimestamp = timestamp/1000L - mainActivity.REFERENCE_TIMESTAMP;

            mainActivity.mChart.addEntry(1, 1, newTimestamp, (float) Math.random()*24);
            mainActivity.mChart.addEntry(1, 2, newTimestamp, (float) Math.random()*24);

            mainActivity.mChart.addEntry(5, 1, newTimestamp, (float) Math.random()*24);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
