package com.bestintheverse.audibooks;


import android.widget.SeekBar;

public class SeekBarListener {

    private MediaService mediaSrv;
    private SeekBar seekBar;
    private MainActivity mActivity;

    public void setListener(MediaService mService, MainActivity mainActivity){

        mediaSrv = mService;
        mActivity = mainActivity;
        seekBar = (SeekBar) mainActivity.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean playing = true;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaSrv != null && fromUser) {
                    mediaSrv.seek(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!mActivity.isPlaying()) {
                    mediaSrv.go();
                    playing = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!playing) {
                    mActivity.pause();
                }
                playing = true;
            }
        });


    }



}
