package hu.norbi.thermostat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import hu.norbi.thermostat.helper.ChartHelper;
import hu.norbi.thermostat.helper.MqttHelper;
import hu.norbi.thermostat.helper.ThermostatMqttCallbackExtended;

public class MainActivity extends AppCompatActivity {

    public MqttHelper mqttHelper;
    public TextView statusView;
    public ChartHelper mChart;
    private LineChart chart;
    public long REFERENCE_TIMESTAMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        REFERENCE_TIMESTAMP = getResources().getInteger(R.integer.referenceTimestamp);
        statusView = findViewById(R.id.statusView);
        chart = findViewById(R.id.chart);
        mChart = new ChartHelper(getResources(), getApplicationContext(), chart);

        startMqtt();
    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new ThermostatMqttCallbackExtended(this));
    }

}
