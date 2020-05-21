package hu.norbi.thermostat.ui.graph;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import hu.norbi.thermostat.MainActivity;
import hu.norbi.thermostat.R;
import hu.norbi.thermostat.helper.MainActivityViewModel;
import io.apptik.widget.MultiSlider;

public class SettingsFragment extends Fragment {
    private long REFERENCE_TIMESTAMP;
    private MainActivity mainActivity;
    private MainActivityViewModel mViewModel;

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
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        // onAppStart and onRotate
//        super.onActivityCreated(savedInstanceState);
//        Log.d("settingsFragment", "Activity created");
//        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
//    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("settingsFragment", "Activity resumed");
        System.out.println(mViewModel);

        MultiSlider multiSlider5 = mainActivity.findViewById(R.id.range_slider5);
        multiSlider5.setMax(24*60);
        multiSlider5.setNumberOfThumbs(4);
//        multiSlider5.setForegroundTintList(new ColorStateList());
        multiSlider5.getThumb(0).setRange( new ColorDrawable(0xC0000000));  // Night black
        multiSlider5.getThumb(1).setRange( new ColorDrawable(0xFFFDD835));  // SUN yellow
        multiSlider5.getThumb(2).setRange( new ColorDrawable(0xFF2196F3));  // TV blue
        multiSlider5.getThumb(3).setRange( new ColorDrawable(0xC0000000));  // Night black
        multiSlider5.getThumb(3).setInvisibleThumb(true);

        if (mViewModel.getSliderPositions().isEmpty()) {
            mViewModel.setSliderPositions(
                    multiSlider5.getThumb(0).getValue(),
                    multiSlider5.getThumb(1).getValue(),
                    multiSlider5.getThumb(2).getValue()
            );
        } else {
            for(int i = 0; i < 3; i++) {
                multiSlider5.getThumb(i).setValue(mViewModel.getSliderPosition(i));
            }
        }

        multiSlider5.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider,
                                       MultiSlider.Thumb thumb,
                                       int thumbIndex,
                                       int value)
            {
                Log.d("settings", "Thumb: " + thumbIndex + " value: " + value);
                mViewModel.setSliderPosition(thumbIndex, value);
//                if (thumbIndex == 0) {
//                    doSmth(String.valueOf(value));
//                } else {
//                    doSmthElse(String.valueOf(value));
//                }
            }
        });
    }


}
