package hu.norbi.thermostat.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hu.norbi.thermostat.R;

public class ChartHelper implements OnChartValueSelectedListener {

    private final int primaryColor;
    private final int primaryDarkColor;
    private final int backgroundColor;
    private final int secondaryColor;
    private final int primaryLightColor;
    private Resources resources;
    private Context applicationContext;
    private LineChart mChart;
    private Map<RoomSensorPair, Integer> lineDataSets = new HashMap<>();
    private final int REFERENCE_TIMESTAMP;

    public ChartHelper(Resources resources, Context applicationContext, LineChart chart) {
        this.resources = resources;
        this.applicationContext = applicationContext;

        primaryDarkColor = ResourcesCompat.getColor(resources, R.color.primaryDarkColor, null);
        primaryColor = ResourcesCompat.getColor(resources, R.color.primaryColor, null);
        primaryLightColor = ResourcesCompat.getColor(resources, R.color.primaryLightColor, null);
        backgroundColor = ResourcesCompat.getColor(resources, R.color.primaryExtraLightColor, null);
        secondaryColor = ResourcesCompat.getColor(resources, R.color.secondaryColor, null);
        REFERENCE_TIMESTAMP = resources.getInteger(R.integer.referenceTimestamp);

        mChart = chart;
        mChart.setOnChartValueSelectedListener(this);

        initChart();
    }

    private void initChart() {
        ChartMarkerView mv = new ChartMarkerView(applicationContext, R.layout.chart_marker_view_layout);
        // set the marker to the chart
        mChart.setMarker(mv);

        mChart.setHighlightPerTapEnabled(true);

        // no description text
        mChart.setNoDataText(resources.getString(R.string.chart_no_data));
        Description description = new Description();
        description.setText(resources.getString(R.string.chart_description));
        mChart.setDescription(description);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(backgroundColor);
        mChart.setBorderColor(primaryColor);
        mChart.setGridBackgroundColor(backgroundColor);


        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTypeface(Typeface.MONOSPACE);
        l.setTextColor(primaryColor);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTypeface(Typeface.MONOSPACE);
        xAxis.setTextColor(primaryColor);
        xAxis.setDrawGridLines(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(5*60f);
        xAxis.setValueFormatter((dateInSeconds, axis) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date((long) (dateInSeconds + REFERENCE_TIMESTAMP)*1000L));
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.MONOSPACE);
        leftAxis.setTextColor(primaryColor);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void reset() {
        LineData data = mChart.getData();

        int i = 0;
        while (data.removeDataSet(0)) { i++; }
        lineDataSets.clear();

        mChart.invalidate();
        //mChart.clear();
        //initChart(resources, applicationContext);

        mChart.notifyDataSetChanged();
    }

    public void setChart(LineChart chart){ this.mChart = chart; }

    public void addEntry(int roomId, int sensor, long timestamp, float value) {

        LineData data = mChart.getData();

        if (data != null){

            RoomSensorPair roomSensorPair = new RoomSensorPair(roomId, sensor);
            ILineDataSet set = null;
            Integer datasetNumber = 0;

            if (lineDataSets.containsKey(roomSensorPair)) {
                datasetNumber = lineDataSets.get(roomSensorPair);
                try {
                    //noinspection ConstantConditions
                    set = data.getDataSetByIndex(datasetNumber);
                } catch (NullPointerException ignored) {}
            }

            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(roomId, sensor);
                //Log.w("chart", "Dataset created: " + set);
                data.addDataSet(set);
                lineDataSets.put(roomSensorPair, lineDataSets.size());
                datasetNumber = lineDataSets.size()-1;
                //Log.w("chart", "Dataset number: " + datasetNumber);
            }

            //set.addEntry(new Entry(timestamp, value, roomSensorPair));
            data.addEntry(new Entry(timestamp, value, roomSensorPair), datasetNumber);
            Log.w("chart", "Dataset: " + datasetNumber + " " + set.getEntryForIndex(set.getEntryCount()-1).toString());

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            //mChart.setVisibleXRangeMaximum(30 * 60);    // 30 minutes
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            // move to the latest entry
            //mChart.moveViewTo(set.getEntryForIndex(set.getEntryCount()-1).getX(), data.getYMax(), YAxis.AxisDependency.LEFT);
            final long now = System.currentTimeMillis();
            final long newNow = now / 1000L - REFERENCE_TIMESTAMP;
            mChart.moveViewTo(newNow, data.getYMax(), YAxis.AxisDependency.LEFT);
//            mChart.setVisibleXRange(20*60, 35*60);
            mChart.getXAxis().setAxisMaximum(newNow + 5*60);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet(int roomId, int sensor) {
        LineDataSet set = new LineDataSet(null, String.format(resources.getString(R.string.chart_label_room), roomId, sensor));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        switch (roomId) {
            case 1:
                set.setColor(sensor == 1 ? primaryLightColor : primaryDarkColor);
                break;
            case 5:
                set.setColor(secondaryColor);
                break;
            default:
                set.setColor(Color.WHITE);
        }

        //set.setCircleColor(Color.WHITE);
        set.setLineWidth(3f);
        //set.setCircleRadius(4f);
//        set.setFillAlpha(65);
        set.setFillColor(backgroundColor);
        set.setHighLightColor(primaryDarkColor);
        set.setValueTextColor(primaryDarkColor);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString() + " HL " + h.toString());
    }

    @Override
    public void onNothingSelected(){
        Log.i("Nothing selected", "Nothing selected.");
    }

}