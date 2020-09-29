package hu.norbi.thermostat.ui.graph;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;
import hu.norbi.thermostat.helper.DecimalDigitsInputFilter;
import hu.norbi.thermostat.helper.MainActivityViewModel;

public class SettingsFragment extends Fragment {
    private long REFERENCE_TIMESTAMP;
    private MainActivity mainActivity;
    private MainActivityViewModel mViewModel;
    private boolean creating = false;

    public SettingsFragment() {
    }

    private SettingsFragment(long reference_timestamp, MainActivity mainActivity) {
        REFERENCE_TIMESTAMP = reference_timestamp;
        this.mainActivity = mainActivity;
    }

    public static SettingsFragment newInstance(MainActivity mainActivity) {
        return new SettingsFragment(mainActivity.REFERENCE_TIMESTAMP, mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // onAppStart and onRotate
        Log.d("settingsFragment", "Activity view created");
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("settingsFragment", "View created");
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        this.mainActivity.nightStartEdit = mainActivity.findViewById(R.id.nightStartEdit);
        this.mainActivity.nightEndEdit = mainActivity.findViewById(R.id.nightEndEdit);
        this.mainActivity.dayStartEdit = mainActivity.findViewById(R.id.dayStartEdit);
        this.mainActivity.dayEndEdit = mainActivity.findViewById(R.id.dayEndEdit);
        this.mainActivity.tvStartEdit = mainActivity.findViewById(R.id.tvStartEdit);
        this.mainActivity.tvEndEdit = mainActivity.findViewById(R.id.tvEndEdit);
        this.mainActivity.btnSaveSettings = mainActivity.findViewById(R.id.btnSave);

        this.mainActivity.weekdayNightTempEdit = mainActivity.findViewById(R.id.weekdayNightTempEdit);
        this.mainActivity.weekdayDayTempEdit = mainActivity.findViewById(R.id.weekdayDayTempEdit);
        this.mainActivity.weekdayTvTempEdit = mainActivity.findViewById(R.id.weekdayTvTempEdit);
        this.mainActivity.weekendNightTempEdit = mainActivity.findViewById(R.id.weekendNightTempEdit);
        this.mainActivity.weekendDayTempEdit = mainActivity.findViewById(R.id.weekendDayTempEdit);
        this.mainActivity.weekendTvTempEdit = mainActivity.findViewById(R.id.weekendTvTempEdit);

        this.mainActivity.weekdayNightTempEdit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2,1)});
        this.mainActivity.weekdayDayTempEdit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2,1)});
        this.mainActivity.weekdayTvTempEdit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2,1)});
        this.mainActivity.weekendNightTempEdit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2,1)});
        this.mainActivity.weekendDayTempEdit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2,1)});
        this.mainActivity.weekendTvTempEdit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2,1)});

        this.mainActivity.multiSlider = mainActivity.findViewById(R.id.range_slider5);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    System.out.println("Yes");
                    mainActivity.mqttHelper.requestStoreConfig(mViewModel.getTargetTemperatures(), mViewModel.getSliderPositions().stream().map(this::getTimeFrom).collect(Collectors.toList()));
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    System.out.println("No");
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        this.mainActivity.btnSaveSettings.setOnClickListener(b -> {
            AlertDialog dialog = builder
                    .setMessage(R.string.confirmSave)
                    .setPositiveButton(R.string.answerYes, dialogClickListener)
                    .setNegativeButton(R.string.answerNo, dialogClickListener)
                    .show();
            dialog.setCanceledOnTouchOutside(true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("settingsFragment", "Activity resumed");
        System.out.println(mViewModel);

        this.creating = true;
        this.mainActivity.multiSlider.setMax(24*60);
        this.mainActivity.multiSlider.setNumberOfThumbs(4);
        this.mainActivity.multiSlider.getThumb(0).setRange( new ColorDrawable(0xC0000000));  // Night black
        this.mainActivity.multiSlider.getThumb(1).setRange( new ColorDrawable(0xFFFDD835));  // SUN yellow
        this.mainActivity.multiSlider.getThumb(2).setRange( new ColorDrawable(0xFF2196F3));  // TV blue
        this.mainActivity.multiSlider.getThumb(3).setRange( new ColorDrawable(0xC0000000));  // Night black
        this.mainActivity.multiSlider.getThumb(3).setInvisibleThumb(true);
        this.creating = false;

        if (mViewModel.getSliderPositions().isEmpty()) {
            Log.d("settingsFragment", "Slider positions is empty in ViewModel");

            // Initiate read from device with MQTT
            this.mainActivity.mqttHelper.requestTargetTemperatures();
            this.mainActivity.mqttHelper.requestTimes();

            mViewModel.setSliderPositions(
                    this.mainActivity.multiSlider.getThumb(0).getValue(),
                    this.mainActivity.multiSlider.getThumb(1).getValue(),
                    this.mainActivity.multiSlider.getThumb(2).getValue()
            );
            final List<Double> targetTempsWeekday = this.mainActivity.thermostatMqttCallback.targetTempsWeekday;
            final List<Double> targetTempsWeekend = this.mainActivity.thermostatMqttCallback.targetTempsWeekend;
            final List<String> times = this.mainActivity.thermostatMqttCallback.times;
            if (!targetTempsWeekday.isEmpty()) {
                this.mainActivity.weekdayNightTempEdit.setText(String.valueOf(targetTempsWeekday.get(0)));
                this.mainActivity.weekdayDayTempEdit.setText(String.valueOf(targetTempsWeekday.get(1)));
                this.mainActivity.weekdayTvTempEdit.setText(String.valueOf(targetTempsWeekday.get(2)));
                this.mainActivity.weekendNightTempEdit.setText(String.valueOf(targetTempsWeekend.get(0)));
                this.mainActivity.weekendDayTempEdit.setText(String.valueOf(targetTempsWeekend.get(1)));
                this.mainActivity.weekendTvTempEdit.setText(String.valueOf(targetTempsWeekend.get(2)));

                final List<Double> targetTemps = new ArrayList<>(6);
                targetTemps.addAll(targetTempsWeekday);
                targetTemps.addAll(targetTempsWeekend);
                mViewModel.setTargetTemperatures(targetTemps);
            }
            if (!times.isEmpty()) {
                this.mainActivity.thermostatMqttCallback.setTimesEditText((String[]) times.toArray());
            }
        } else {
            Log.d("settingsFragment", "Slider positions present in ViewModel: " + mViewModel.getSliderPositions());
            for (int j = 0; j < 2; j++) {
                for(int i = 2; i >= 0; i--) {
                    this.mainActivity.multiSlider.getThumb(i).setValue(mViewModel.getSliderPosition(i));
                }
            }
            final String[] times = new String[3];
            for (int i = 0; i < 3; i++) {
                times[i] = getTimeFrom(mViewModel.getSliderPosition(i));
            }

            this.mainActivity.thermostatMqttCallback.setTimesEditText(times);

            final List<Double> targetTemperatures = mViewModel.getTargetTemperatures();
            Log.d("settingsFragment", "Target teperatures in ViewModel: " + targetTemperatures);
            if (!targetTemperatures.isEmpty()) {
                this.mainActivity.weekdayNightTempEdit.setText(String.valueOf(targetTemperatures.get(0)));
                this.mainActivity.weekdayDayTempEdit.setText(String.valueOf(targetTemperatures.get(1)));
                this.mainActivity.weekdayTvTempEdit.setText(String.valueOf(targetTemperatures.get(2)));
                this.mainActivity.weekendNightTempEdit.setText(String.valueOf(targetTemperatures.get(3)));
                this.mainActivity.weekendDayTempEdit.setText(String.valueOf(targetTemperatures.get(4)));
                this.mainActivity.weekendTvTempEdit.setText(String.valueOf(targetTemperatures.get(5)));
            }
        }

        this.mainActivity.multiSlider.setOnThumbValueChangeListener((multiSlider, thumb, thumbIndex, value) -> {
            Log.d("settingsFragment", "Thumb: " + thumbIndex + " value: " + value);
            if (thumbIndex > 2) return;
            if (!creating) mViewModel.setSliderPosition(thumbIndex, value);

            String time = getTimeFrom(value);
            switch (thumbIndex) {
                case 0:
                    this.mainActivity.nightEndEdit.setText(time);
                    this.mainActivity.dayStartEdit.setText(time);
                    break;
                case 1:
                    this.mainActivity.dayEndEdit.setText(time);
                    this.mainActivity.tvStartEdit.setText(time);
                    break;
                case 2:
                    this.mainActivity.tvEndEdit.setText(time);
                    this.mainActivity.nightStartEdit.setText(time);
                    break;
            }

        });
    }

    @SuppressLint("DefaultLocale")
    private String getTimeFrom(int value) {
        return String.format("%1$02d:%2$02d", value / 60, value % 60);
    }


}
