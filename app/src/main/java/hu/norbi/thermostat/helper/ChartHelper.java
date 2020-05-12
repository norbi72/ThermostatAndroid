package hu.norbi.thermostat.helper;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import hu.norbi.thermostat.R;

public class ChartHelper implements OnChartValueSelectedListener {

    private final int primaryColor;
    private final int primaryDarkColor;
    private final int backgroundColor;
    private Resources resources;
    private LineChart mChart;

    public ChartHelper(Resources resources, LineChart chart) {
        this.resources = resources;

        primaryDarkColor = ResourcesCompat.getColor(resources, R.color.primaryDarkColor, null);
        primaryColor = ResourcesCompat.getColor(resources, R.color.primaryColor, null);
        backgroundColor = ResourcesCompat.getColor(resources, R.color.primaryExtraLightColor, null);

        mChart = chart;
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setNoDataText("Please wait...");
        Description description = new Description();
        description.setText("Temperatures");
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

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(Typeface.MONOSPACE);
        l.setTextColor(primaryColor);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(Typeface.MONOSPACE);
        xl.setTextColor(primaryColor);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.MONOSPACE);
        leftAxis.setTextColor(primaryColor);

        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    public void setChart(LineChart chart){ this.mChart = chart; }

    public void addEntry(float value) {

        LineData data = mChart.getData();

        if (data != null){

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(),value),0);
            Log.w("chart", set.getEntryForIndex(set.getEntryCount()-1).toString());

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(10);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewTo(set.getEntryCount()-1, data.getYMax(), YAxis.AxisDependency.LEFT);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Room 1");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(primaryColor);
        //set.setCircleColor(Color.WHITE);
        set.setLineWidth(3f);
        //set.setCircleRadius(4f);
//        set.setFillAlpha(65);
        set.setFillColor(backgroundColor);
//        set.setHighLightColor(Color.rgb(67, 164, 34));
        set.setHighLightColor(primaryDarkColor);
//        set.setValueTextColor(Color.rgb(67, 164, 34));
        set.setValueTextColor(primaryDarkColor);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected(){
        Log.i("Nothing selected", "Nothing selected.");
    }

}