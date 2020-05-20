package hu.norbi.thermostat.ui.graph;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;
import hu.norbi.thermostat.helper.ChartHelper;
import hu.norbi.thermostat.helper.HttpManager;
import hu.norbi.thermostat.helper.MainActivityViewModel;
import hu.norbi.thermostat.helper.RequestPackage;
import hu.norbi.thermostat.helper.TempRecord;

public class ChartFragment extends Fragment {

    private MainActivity mainActivity;
    private MainActivityViewModel mViewModel;
    private LineChart chart;
    private ChartHelper mChart;
    private long REFERENCE_TIMESTAMP;

    private ChartFragment(long reference_timestamp, MainActivity mainActivity) {
        REFERENCE_TIMESTAMP = reference_timestamp;
        this.mainActivity = mainActivity;
    }

    public static ChartFragment newInstance(MainActivity mainActivity) {
        return new ChartFragment(mainActivity.REFERENCE_TIMESTAMP, mainActivity);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("chartFragment", "Activity view resumed");

        if (null == mChart) {
            Log.d("main", "Create new chart");
            mChart = new ChartHelper(getResources(), mainActivity.getApplicationContext(), chart);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // onAppStart and onRotate
        Log.d("chartFragment", "Activity view created");
        return inflater.inflate(R.layout.chart_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // onAppStart and onRotate
        super.onActivityCreated(savedInstanceState);
        Log.d("chartFragment", "Activity created");
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        chart = mainActivity.findViewById(R.id.chart);
        System.out.println("CHART " + chart);


    }

    public void requestChartData() {
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(getString(R.string.db_url_last30mindata));

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
            if (null == result || result.isEmpty()) {
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
                    TempRecord tempRecord = new TempRecord(roomId, sensor, newTimestamp, temperature);
                    mViewModel.addTempRecord(tempRecord);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
