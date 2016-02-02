package me.arkadiy.geronplayer.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;

public class EqualizerFragment extends Fragment implements MainActivity.ServiceListener {
    private Equalizer equalizer;
    private Virtualizer virtualizer;
    private BassBoost bassBoost;
    private TextView preset;
    private String[] presetNames;
    private Dialog dialog;
    private SwitchCompat equalizerButton;
    private LinearLayout layout;
    private SeekBar bassSeekBar;
    private SeekBar virtSeekBar;
    private LayoutInflater inflater;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initEqualizer(MusicService mService) {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        equalizer = mService.getEqualizer();
        virtualizer = mService.getVirtualizer();
        bassBoost = mService.getBassBoost();
        for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(equalizer.getPresetName(i));
        }
        presetNames = new String[equalizerPresetNames.size()];
        equalizerPresetNames.toArray(presetNames);
    }

    public Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(presetNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                equalizer.usePreset((short) i);
                updatePresetText();
                Log.e("equlizer", "preset No " + equalizer.getCurrentPreset());
            }
        });
        return builder.create();
    }

    private void enableDisableAllView(boolean enable) {
        virtSeekBar.setEnabled(enable);
        bassSeekBar.setEnabled(enable);
        disableEnableControls(enable, layout);
    }

    private void disableEnableControls(boolean enable, ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_equalizer, container, false);
        layout = (LinearLayout) view.findViewById(R.id.layout);
        preset = ((TextView) view.findViewById(R.id.preset_name));
        preset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = createDialog();
                dialog.show();
            }
        });
        equalizerButton = (SwitchCompat) view.findViewById(R.id.equalizer_button);
        equalizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enable = equalizerButton.isChecked();
                equalizer.setEnabled(enable);
                virtualizer.setEnabled(enable);
                bassBoost.setEnabled(enable);
//                layout.setEnabled(enable);
                enableDisableAllView(enable);
            }
        });


        bassSeekBar = ((SeekBar) view.findViewById(R.id.bass_seek_bar));
        bassSeekBar.setMax(10);
        bassSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    bassBoost.setStrength((short) (i * 100));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        virtSeekBar = ((SeekBar) view.findViewById(R.id.virtualize_seek_bar));
        virtSeekBar.setMax(10);
        virtSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    virtualizer.setStrength((short) (i * 100));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }

    private void inflateEqualizer(LayoutInflater inflater) {
        short numberFrequencyBands = equalizer.getNumberOfBands();
        final short lowerLevel = equalizer.getBandLevelRange()[0];
        final short upperLevel = equalizer.getBandLevelRange()[1];

        for (short i = 0; i < numberFrequencyBands; i++) {
            final short ii = i;
            ViewGroup seekBarLayout = (ViewGroup) inflater.inflate(R.layout.seekbar_layout, layout, false);
            final TextView freqInfo = ((TextView) seekBarLayout.findViewById(R.id.frequency_text));
            final TextView currDb = ((TextView) seekBarLayout.findViewById(R.id.current_db));
            SeekBar seekBar = ((SeekBar) seekBarLayout.findViewById(R.id.seek_bar));
            int frequency = equalizer.getCenterFreq(ii) / 1000;
            if (frequency > 10000) {
                frequency /= 1000;
                freqInfo.setText(String.format("%2d%s", frequency, "kHz"));
            } else if (frequency > 1000) {
                frequency /= 100;
                freqInfo.setText(String.format("%2d%s", frequency, "hHz"));
            } else {
                freqInfo.setText(String.format("%3d%s", frequency, "Hz"));
            }
            seekBar.setMax((upperLevel - lowerLevel) / 100);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    if (b) {
                        equalizer.setBandLevel(ii, (short) (100 * progress + lowerLevel));
                        currDb.setText(String.format("%3d%s", (equalizer.getBandLevel(ii) / 100), "dB"));
                        preset.setText(R.string.user_preset);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBarLayout.setTag("" + i);
            layout.addView(seekBarLayout);
        }
        bassSeekBar.setProgress(bassBoost.getRoundedStrength() / 100);
        virtSeekBar.setProgress(virtualizer.getRoundedStrength() / 100);
    }

    public void updatePresetText() {
        String presetName = equalizer.getPresetName(equalizer.getCurrentPreset());
        int lowerLevel = equalizer.getBandLevelRange()[0];
        equalizerButton.setChecked(equalizer.getEnabled());
        enableDisableAllView(equalizer.getEnabled());
        if (presetName.length() == 0)
            preset.setText(R.string.user_preset);
        else preset.setText(presetName);
        if (getView() != null) {
            for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                View view = getView().findViewWithTag(String.valueOf(i));
                if (view != null) {
                    ((TextView) view.findViewById(R.id.current_db)).setText(String.format("%3d%s", (equalizer.getBandLevel(i) / 100), "dB"));
                    ((SeekBar) view.findViewById(R.id.seek_bar)).setProgress((equalizer.getBandLevel(i) - lowerLevel) / 100);
                }
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).addServiceListener(this);

    }

    @Override
    public void onDestroy() {
        equalizer = null;
        virtualizer = null;
        bassBoost = null;
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onStop();
    }

    public static Fragment newInstance() {
        EqualizerFragment fragment = new EqualizerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onServiceConnected(MusicService service) {
        initEqualizer(service);
        inflateEqualizer(inflater);
        updatePresetText();
    }
}
