package hu.norbi.thermostat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hu.norbi.thermostat.helper.ChartHelper;
import hu.norbi.thermostat.helper.MqttHelper;

public class MainActivity extends AppCompatActivity {

    MqttHelper mqttHelper;
    private TextView statusView;
    ChartHelper mChart;
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = findViewById(R.id.statusView);
        chart = findViewById(R.id.chart);
        mChart = new ChartHelper(getResources(), chart);

        startMqtt();
    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt","Connected " + b + " - " + s);
                mqttHelper.sendMessage("thermostat/state/cmd", "{\"get\":\"uptime\"}");
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", "Topic: " + topic + " Msg: " + mqttMessage.toString());
                //dataReceived.setText(mqttMessage.toString());
                if (topic.endsWith("/response")) {
                    statusView.setText(mqttMessage.toString());
                }

                mChart.addEntry((float) Math.random()*24);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
