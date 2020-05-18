package hu.norbi.thermostat;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import hu.norbi.thermostat.helper.OnSwipeTouchListener;
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
    View baseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        REFERENCE_TIMESTAMP = getResources().getInteger(R.integer.referenceTimestamp);
        baseLayout = findViewById(R.id.baseLinearLayout);
        statusView = findViewById(R.id.statusView);
        currentTargetView = findViewById(R.id.targetTempView);
        currentTempView = findViewById(R.id.currentTempView);
        statusIconView = findViewById(R.id.statusIconView);
        powerIconView = findViewById(R.id.powerIconView);
        currentTempLayout = findViewById(R.id.currentTempLayout);
        chart = findViewById(R.id.chart);

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        startMqtt();

        baseLayout.setOnTouchListener(new OnSwipeTouchListener(getBaseContext()) {
//            public void onSwipeTop() {
//                Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
//            }
//            public void onSwipeRight() {
//                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
//            }
//            public void onSwipeLeft() {
//                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
//            }
            public void onSwipeBottom() {
                Toast.makeText(MainActivity.this, R.string.toast_refreshing, Toast.LENGTH_SHORT).show();
                requestChartData();
                mqttHelper.requestMqttData();
            }

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("main", "RESUME");
        if (null == mChart) {
            mChart = new ChartHelper(getResources(), getApplicationContext(), chart);
        } else {
            Log.d("main", "Chart exists");
//            mChart.reset();
//            requestChartData();
        }

        if (mViewModel.getTempRecords().isEmpty()) {
            Log.d("main", "No temperature records in cache -> load from server");
            requestChartData();
        } else {
            long max = mViewModel.getTempRecords().stream().mapToLong(tempRec -> tempRec.newTimestamp).max().orElse(0L);
            Log.d("main", "TempRecords count: " + mViewModel.getTempRecords().size() + " max: " + max);
            if ((max + REFERENCE_TIMESTAMP)+300 < System.currentTimeMillis()/1000L) {    // if the last data older than 5 minutes
                Log.d("main", "Newest temperature record in cache too old -> load from server");
                requestChartData();
            } else {
                Log.d("main", "Newest temperature record in cache is fresh enough  -> move chart to current time");
                if (null == chart.getData() || 0 == chart.getData().getDataSetCount()) {
                    mViewModel.getTempRecords().forEach(tempRecord -> mChart.addEntry(tempRecord.getRoomId(), tempRecord.getSensor(), tempRecord.getNewTimestamp(), tempRecord.getTemperature()));
                }
                mChart.moveToCurrent();
            }
        }
    }



    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new ThermostatMqttCallbackExtended(this));
    }

    public void requestChartData() {
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
//        OffsetDateTime odt = OffsetDateTime.now(ZoneId.systemDefault() );
//        ZoneOffset zoneOffset = odt.getOffset();
        ZoneOffset zoneOffset = ZoneOffset.of("+02");   // measured temperatures only valid in this timezone

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) {
                return;
            }

            mChart.reset();
            mViewModel.getTempRecords().clear();
            try {
                for (String line: result.split("\n")) {
                    Log.d("httpRequest", line);

                    final JSONObject jsonObject = new JSONObject(line);
                    final int roomId = jsonObject.getInt("roomId");
                    final int sensor = jsonObject.getInt("sensor");
                    final float temperature = (float) jsonObject.getDouble("temp");
                    // final int humidity = jsonObject.getInt("humi");
                    final String date = jsonObject.getString("time");

                    final long newTimestamp = LocalDateTime.parse(date, formatter).toEpochSecond(zoneOffset) - REFERENCE_TIMESTAMP;
                    mChart.addEntry(roomId, sensor, newTimestamp, temperature);
                    MainActivityViewModel.TempRecord tempRecord = new MainActivityViewModel.TempRecord(roomId, sensor, newTimestamp, temperature);
                    mViewModel.addTempRecord(tempRecord);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
