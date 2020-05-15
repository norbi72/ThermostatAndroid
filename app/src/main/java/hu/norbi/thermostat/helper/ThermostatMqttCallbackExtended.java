package hu.norbi.thermostat.helper;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;

@RequiresApi(api = Build.VERSION_CODES.O)
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

        if (null != mainActivity.mViewModel.uptime) writeUptime(mainActivity.mViewModel.uptime);
        if (null != mainActivity.mViewModel.targetTemp) writeCurrentTarget(mainActivity.mViewModel.targetTemp);
        if (null != mainActivity.mViewModel.currentTemp) writeCurrent(mainActivity.mViewModel.currentTemp);
        if (null != mainActivity.mViewModel.icon) changeIcon(mainActivity.mViewModel.icon);
        if (null != mainActivity.mViewModel.state) setPower(mainActivity.mViewModel.state);
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
        mainActivity.mViewModel.uptime = uptime;
    }

    private void writeCurrentTarget(String tempValue) {
        Double temperature = Double.valueOf(tempValue);
        mainActivity.currentTargetView.setText(String.format(temperatureFormat1D, temperature));
        mainActivity.mViewModel.targetTemp = tempValue;
    }

    private void writeCurrent(String tempValue) {
        Double temperature = Double.valueOf(tempValue);
        mainActivity.currentTempView.setText(String.format(temperatureFormat2D, temperature));
        mainActivity.mViewModel.currentTemp = tempValue;
    }

    private void changeIcon(String iconId) {
        Drawable icon;
        Integer iconValue = Integer.valueOf(iconId);
        switch (iconValue) {
            case 0:
                icon = mainActivity.getResources().getDrawable(R.drawable.ic_sun, null);
                break;
            case 1:
                icon = mainActivity.getResources().getDrawable(R.drawable.ic_moon, null);
                break;
            case 2:
                icon = mainActivity.getResources().getDrawable(R.drawable.ic_tv, null);
                break;
            default:
                icon = mainActivity.getResources().getDrawable(R.drawable.ic_power, null);
                break;
        }
        mainActivity.statusIconView.setImageDrawable(icon);
        mainActivity.statusIconView.setVisibility(View.VISIBLE);
        mainActivity.mViewModel.icon = iconId;
    }

    private void setPower(String state) {
        mainActivity.powerIconView.setVisibility(state.equalsIgnoreCase("heating") ? View.VISIBLE : View.INVISIBLE);
        mainActivity.mViewModel.state = state;
    }

    @Override
    public void connectComplete(boolean b, String s) {
        Log.w("mqtt","Connected " + b + " - " + s);
        if (null == mainActivity.mViewModel.uptime) {
            mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"uptime\"}");
            mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"currentTarget\"}");
            mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"reference\"}");
            mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"icon\"}");
            mainActivity.mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"power\"}");
        }
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
            } else if (msg.contains("\"icon\":")) {
                changeIcon(msg.substring(msg.indexOf("\"icon\":")+7, msg.length()-1));
            } else if (msg.contains("\"power\":")) {
                setPower(msg.substring(msg.indexOf("\"power\":")+9, msg.length()-2));
            }
        } else if (topic.endsWith("/screen")) {
            if (msg.contains("reference temp changed to ")) {
                writeCurrent(msg.substring(msg.indexOf("reference temp changed to ")+26));
            } else if (msg.contains("Screen refresh at")) {
                mainActivity.requestData();
            } else {
                if (msg.contains("icon changed to ")) {
                    int txtpos = msg.indexOf("icon changed to ");
                    changeIcon(msg.substring(txtpos+16, txtpos+17));
                }
                if (msg.contains("target temp changed to ")) {
                    int txtpos = msg.indexOf("target temp changed to ");
                    int txtend = msg.indexOf(",", txtpos + 23);
                    writeCurrentTarget(msg.substring(txtpos+23, txtend));
                }
                if (msg.contains("status changed to ")) {
                    int txtpos = msg.indexOf("status changed to ");
                    int txtend = msg.indexOf(",", txtpos + 18);
//                    enum Status {
//                        OFF = 0,
//                        STANDBY,
//                        HEATING
//                    };
                    String statusNum = msg.substring(txtpos + 18, txtend);
                    setPower(statusNum.equals("2") ? "heating" : "standby");
                }
                if (msg.contains("reference temp changed to ")) {
                    int txtpos = msg.indexOf("reference temp changed to ");
                    int txtend = msg.indexOf(",", txtpos + 26);
                    writeCurrent(msg.substring(txtpos+26, txtend));
                }
            }
        } else if (topic.equalsIgnoreCase("/info")) {
            if (msg.contains("HEATING")) {
                setPower("heating");
            } else if (msg.contains("STANDBY")) {
                setPower("standby");
            }
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
