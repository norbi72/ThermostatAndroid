package hu.norbi.thermostat;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import hu.norbi.thermostat.helper.ChartHelper;
import hu.norbi.thermostat.helper.HttpManager;
import hu.norbi.thermostat.helper.MainActivityViewModel;
import hu.norbi.thermostat.helper.MqttHelper;
import hu.norbi.thermostat.helper.RequestPackage;
import hu.norbi.thermostat.helper.ThermostatMqttCallbackExtended;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    public MqttHelper mqttHelper;
    public TextView statusView;
    public TextView currentTargetView;
    public TextView currentTempView;
    public ImageView statusIconView;
    public ImageView powerIconView;
    public ChartHelper mChart;
    @SuppressWarnings("FieldCanBeLocal")
    private LineChart chart;
    public long REFERENCE_TIMESTAMP;
    @SuppressWarnings("FieldCanBeLocal")
    private View currentTempLayout;
    public MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        REFERENCE_TIMESTAMP = getResources().getInteger(R.integer.referenceTimestamp);
        statusView = findViewById(R.id.statusView);
        currentTargetView = findViewById(R.id.targetTempView);
        currentTempView = findViewById(R.id.currentTempView);
        statusIconView = findViewById(R.id.statusIconView);
        powerIconView = findViewById(R.id.powerIconView);
        currentTempLayout = findViewById(R.id.currentTempLayout);
        chart = findViewById(R.id.chart);

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        currentTempLayout.setOnClickListener(v -> requestData());
        mChart = new ChartHelper(getResources(), getApplicationContext(), chart);
        startMqtt();

        if (mViewModel.tempRecords.isEmpty()) {
            requestData();
        } else {
            mViewModel.tempRecords.forEach(rec -> mChart.addEntry(rec.roomId, rec.sensor, rec.newTimestamp, rec.temperature));
        }
    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new ThermostatMqttCallbackExtended(this));
    }

    public void requestData() {
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("GET");
        requestPackage.setUrl("http://192.168.0.222/MyWeb/thermostat/index.php/home/getTempLast30Min/1");

//        Log.d("httpRequest", Objects.requireNonNull(HttpManager.getData(requestPackage)));

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }


    @SuppressLint("StaticFieldLeak")
    private class Downloader extends AsyncTask<RequestPackage, String, String> {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        OffsetDateTime odt = OffsetDateTime.now( ZoneId.of("+02") );
//        ZoneOffset zoneOffset = odt.getOffset();
        ZoneOffset zoneOffset = ZoneOffset.of("+02");

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) return;

            mChart.reset();
            mViewModel.tempRecords.clear();
            try {
                for (String line: result.split("\n")) {
System.out.println("LINE " + line);
                    final JSONObject jsonObject = new JSONObject(line);
                    final int roomId = jsonObject.getInt("roomId");
                    final int sensor = jsonObject.getInt("sensor");
                    final float temperature = (float) jsonObject.getDouble("temp");
                    // final int humidity = jsonObject.getInt("humi");
                    final String date = jsonObject.getString("time");

                    final long newTimestamp = LocalDateTime.parse(date, formatter).toEpochSecond(zoneOffset) - REFERENCE_TIMESTAMP;
                    mChart.addEntry(roomId, sensor, newTimestamp, temperature);
                    MainActivityViewModel.TempRecord tempRecord = new MainActivityViewModel.TempRecord(roomId, sensor, newTimestamp, temperature);
                    mViewModel.tempRecords.add(tempRecord);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
