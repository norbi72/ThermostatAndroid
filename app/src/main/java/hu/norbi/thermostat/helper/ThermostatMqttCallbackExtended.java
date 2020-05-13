package hu.norbi.thermostat.helper;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;

public class ThermostatMqttCallbackExtended implements MqttCallbackExtended {
    private MainActivity mainActivity;
    final private String uptimeFormat;
    final private String temperatureFormat1D;
    final private String temperatureFormat2D;

    public ThermostatMqttCallbackExtended(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        uptimeFormat = mainActivity.getResources().getText(R.string.uptime).toString();
        temperatureFormat1D = mainActivity.getResources().getText(R.string.temperature_1d).toString();
        temperatureFormat2D = mainActivity.getResources().getText(R.string.temperature_2d).toString();
    }

    private void writeUptime(String uptime) {
        final int up = Integer.parseInt(uptime);
        final int days = up / (24 * 60 * 60);
        final int daysRem = up % (24 * 60 * 60);
        final int hours = daysRem / (60 * 60);
        final int hoursRem = daysRem % (60 * 60);
        final int mins = hoursRem / 60;
        final int secs = hoursRem % 60;
        mainActivity.statusView.setText(String.format(uptimeFormat, days, hours, mins, secs));
    }

    private void writeCurrentTarget(String tempValue) {
        mainActivity.currentTargetView.setText(String.format(temperatureFormat1D, Double.valueOf(tempValue)));
    }

    private void writeCurrent(String tempValue) {
        mainActivity.currentTempView.setText(String.format(temperatureFormat2D, Double.valueOf(tempValue)));
    }

    @Override
    public void connectComplete(boolean b, String s) {
        Log.w("mqtt","Connected " + b + " - " + s);
        mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"uptime\"}");
        mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"currentTarget\"}");
        mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"reference\"}");
        mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"icon\"}");
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        String msg = mqttMessage.toString();
        Log.w("Debug", "Topic: " + topic + " Msg: " + msg);
        //dataReceived.setText(mqttMessage.toString());
        if (topic.endsWith("/uptime")) {
            writeUptime(msg);
        } else if (topic.endsWith("/response")) {
            if (msg.contains("\"uptime\":")) {
                writeUptime(msg.substring(msg.indexOf("\"uptime\":")+9, msg.length()-1));
            } else if (msg.contains("\"currentTarget\":")) {
                writeCurrentTarget(msg.substring(msg.indexOf("\"currentTarget\":")+16, msg.length()-1));
            } else if (msg.contains("\"reference\":")) {
                writeCurrent(msg.substring(msg.indexOf("\"reference\":")+12, msg.length()-1));
            }
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
