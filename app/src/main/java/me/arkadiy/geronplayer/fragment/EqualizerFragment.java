package me.arkadiy.geronplayer.fragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;

public class EqualizerFragment extends Fragment {
    private TextView preset;
    private String[] presetNames;
    private Dialog dialog;
    private SwitchCompat equalizerButton;
    private LinearLayout layout;
    private SeekBar bassSeekBar;
    private SeekBar virtSeekBar;
    private LayoutInflater inflater;
    private boolean mBound;
    private boolean isInflated;
    private MusicService mService;
    private TextView bassBoostLevel;
    private TextView virtualizeLevel;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) serviceBinder;
            mService = binder.getService();
            mBound = true;

            if (!isInflated) {
                initEqualizer(binder.getService());
                inflateEqualizer(binder.getService(), inflater);
            }
            updatePresetText();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            onUnbind();
        }
    };


    public static Fragment newInstance() {
        EqualizerFragment fragment = new EqualizerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

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
        Equalizer equalizer = mService.getEqualizer();
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
                if (mBound) {
                    mService.getEqualizer().usePreset((short) i);
                    updatePresetText();
                }
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
        equalizerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBound) {
                    mService.getEqualizer().setEnabled(isChecked);
                    mService.getVirtualizer().setEnabled(isChecked);
                    mService.getBassBoost().setEnabled(isChecked);
                    enableDisableAllView(isChecked);
                }
            }
        });

        bassSeekBar = ((SeekBar) view.findViewById(R.id.bass_seek_bar));
        bassSeekBar.setMax(10);
        bassSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mBound && b) {
                    mService.getBassBoost().setStrength((short) (i * 100));
                    setBassLevel(i);
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
                if (mBound && b) {
                    mService.getVirtualizer().setStrength((short) (i * 100));
                    setVirtualizeLevel(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bassBoostLevel = (TextView) view.findViewById(R.id.bass_boost_level);
        virtualizeLevel = (TextView) view.findViewById(R.id.virtualize_level);
        return view;
    }

    private void inflateEqualizer(MusicService service, LayoutInflater inflater) {
        Equalizer equalizer = service.getEqualizer();

        final short lowerLevel = equalizer.getBandLevelRange()[0];
        final short upperLevel = equalizer.getBandLevelRange()[1];
        short numberFrequencyBands = equalizer.getNumberOfBands();

        for (short i = 0; i < numberFrequencyBands; i++) {
            final short bandNumber = i;

            ViewGroup seekBarLayout = (ViewGroup) inflater.inflate(R.layout.seekbar_layout, layout, false);
            final TextView freqInfo = ((TextView) seekBarLayout.findViewById(R.id.frequency_text));
            final TextView currDb = ((TextView) seekBarLayout.findViewById(R.id.current_db));
            SeekBar seekBar = ((SeekBar) seekBarLayout.findViewById(R.id.seek_bar));

            int frequency = equalizer.getCenterFreq(bandNumber) / 1000;
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
                    if (b && mBound) {
                        mService.getEqualizer().setBandLevel(bandNumber, (short) (100 * progress + lowerLevel));
                        currDb.setText(String.format("%3d%s", (mService.getEqualizer().getBandLevel(bandNumber) / 100), "dB"));
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
        isInflated = true;
    }

    public void updatePresetText() {
        Equalizer equalizer = mService.getEqualizer();

        equalizerButton.setChecked(equalizer.getEnabled());
        enableDisableAllView(equalizer.getEnabled());

        String presetName = equalizer.getPresetName(equalizer.getCurrentPreset());
        if (presetName.length() == 0) {
            preset.setText(R.string.user_preset);
        } else {
            preset.setText(presetName);
        }

        if (getView() != null) {
            int lowerLevel = equalizer.getBandLevelRange()[0];
            for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                View view = getView().findViewWithTag(String.valueOf(i));
                if (view != null) {
                    ((TextView) view.findViewById(R.id.current_db)).setText(String.format("%3d%s", (equalizer.getBandLevel(i) / 100), "dB"));
                    ((SeekBar) view.findViewById(R.id.seek_bar)).setProgress((equalizer.getBandLevel(i) - lowerLevel) / 100);
                }
            }
        }

        int bassProgress = mService.getBassBoost().getRoundedStrength() / 100;
        bassSeekBar.setProgress(bassProgress);
        setBassLevel(bassProgress);

        int virtualizeProgress = mService.getVirtualizer().getRoundedStrength() / 100;
        virtSeekBar.setProgress(virtualizeProgress);
        setVirtualizeLevel(virtualizeProgress);
    }

    private void setVirtualizeLevel(int virtualizeProgress) {
        virtualizeLevel.setText(String.format("%3d%%", virtualizeProgress * 10));
    }

    private void setBassLevel(int bassProgress) {
        bassBoostLevel.setText(String.format("%3d%%", bassProgress * 10));
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        if (mBound) {
            getActivity().getApplicationContext().unbindService(connection);
            onUnbind();
        }
        super.onPause();
    }

    private void onUnbind() {
        mService = null;
        mBound = false;
    }

    @Override
    public void onStop() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onStop();
    }
}
