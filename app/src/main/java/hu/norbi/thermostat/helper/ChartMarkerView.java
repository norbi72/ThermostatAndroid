package hu.norbi.thermostat.helper;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.norbi.thermostat.R;

public class ChartMarkerView extends MarkerView {

    private TextView tvContent;
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY. MM. dd HH:mm:ss", Locale.getDefault());
    private final int REFERENCE_TIMESTAMP;

    public ChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        REFERENCE_TIMESTAMP = getResources().getInteger(R.integer.referenceTimestamp);
        // this markerview only displays a textview
        tvContent = findViewById(R.id.tvContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        final String dateStr = sdf.format(new Date((long) (e.getX() + REFERENCE_TIMESTAMP) * 1000L));
        final String room = String.format(String.valueOf(getResources().getText(R.string.chart_label_room)), ((RoomSensorPair) e.getData()).getRoomId(), ((RoomSensorPair) e.getData()).getSensor());
        final String text = String.format(String.valueOf(getResources().getText(R.string.chart_marker)), e.getY(), dateStr, room);
        tvContent.setText(text); // set the entry-value as the display text
    }
}