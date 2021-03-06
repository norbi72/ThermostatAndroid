package hu.norbi.thermostat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Objects;

import hu.norbi.thermostat.helper.ChartHelper;
import hu.norbi.thermostat.helper.MainActivityViewModel;
import hu.norbi.thermostat.helper.MqttHelper;
import hu.norbi.thermostat.helper.OnSwipeTouchListener;
import hu.norbi.thermostat.helper.ThermostatMqttCallbackExtended;
import hu.norbi.thermostat.ui.graph.ChartFragment;
import hu.norbi.thermostat.ui.graph.SettingsFragment;
import io.apptik.widget.MultiSlider;

@RequiresApi(api = Build.VERSION_CODES.O)
@TargetApi(Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    public MqttHelper mqttHelper;
    public TextView statusView;
    public TextView currentTargetView;
    public TextView currentTempView;
    public ImageView statusIconView;
    public ImageView powerIconView;
    public ChartHelper mChart;
    public Button btnSaveSettings;
    @SuppressWarnings("FieldCanBeLocal")
    private LineChart chart;
    public long REFERENCE_TIMESTAMP;
    @SuppressWarnings("FieldCanBeLocal")
    private View currentTempLayout;
    public MainActivityViewModel mViewModel;
    View baseLayout;
    ChartFragment chartFragment;
    private SettingsFragment settingsFragment;
    private boolean chartVisible = true;
    public ThermostatMqttCallbackExtended thermostatMqttCallback;

    public MultiSlider multiSlider;

    public TextView nightStartEdit;
    public TextView nightEndEdit;
    public TextView dayStartEdit;
    public TextView dayEndEdit;
    public TextView tvStartEdit;
    public TextView tvEndEdit;

    public TextView weekdayNightTempEdit;
    public TextView weekdayDayTempEdit;
    public TextView weekdayTvTempEdit;
    public TextView weekendNightTempEdit;
    public TextView weekendDayTempEdit;
    public TextView weekendTvTempEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // onAppStart and onRotate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("main", "onCreate");

        REFERENCE_TIMESTAMP = getResources().getInteger(R.integer.referenceTimestamp);
        baseLayout = findViewById(R.id.baseLinearLayout);
        statusView = findViewById(R.id.statusView);
        currentTargetView = findViewById(R.id.targetTempView);
        currentTempView = findViewById(R.id.currentTempView);
        statusIconView = findViewById(R.id.statusIconView);
        statusIconView.setVisibility(View.INVISIBLE);
        powerIconView = findViewById(R.id.powerIconView);
        powerIconView.setVisibility(View.VISIBLE);
        powerIconView.setImageAlpha(50);
        currentTempLayout = findViewById(R.id.currentTempLayout);

        // Extra icon in ActionBar
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_thermostat_24dp);
        Objects.requireNonNull(getSupportActionBar()).setDisplayUseLogoEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        if (savedInstanceState == null) {
            Log.d("main", "onCreate savedInstanceState is null -> start with TempChart");

            chartFragment = ChartFragment.newInstance(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, chartFragment)
                    .commitNow();
        } else {
            Log.d("main", "onCreate savedInstanceState: " + savedInstanceState.toString());
            this.chartVisible = savedInstanceState.getBoolean("chartVisible");
            if (this.chartVisible) {
                chartFragment = ChartFragment.newInstance(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, chartFragment)
                        .commitNow();
                chart = findViewById(R.id.chart);
            } else {
                settingsFragment = SettingsFragment.newInstance(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, settingsFragment)
                        .commitNow();
            }
        }

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext(), this);
        thermostatMqttCallback = new ThermostatMqttCallbackExtended(this);
        mqttHelper.setCallback(thermostatMqttCallback);
    }

    public void requestChartData() {
        if (null != chartFragment) {
            chartFragment.requestChartData();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("chartVisible", chartVisible);
    }

    public void switchToChart(MenuItem item) {
        chartFragment = ChartFragment.newInstance(this);
        if (null != findViewById(R.id.fragment_placeholder)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, chartFragment)
                    .commitNow();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_fragment, chartFragment)
                    .commitNow();
        }
        chartVisible = true;
    }

    public void switchToSettings(MenuItem item) {
        settingsFragment = SettingsFragment.newInstance(this);
        if (null != findViewById(R.id.fragment_placeholder)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, settingsFragment)
                    .commitNow();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.chart_fragment, settingsFragment)
                    .commitNow();
        }
        chartVisible = false;
    }
}
