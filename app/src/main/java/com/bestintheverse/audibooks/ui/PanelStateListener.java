package com.bestintheverse.audibooks.ui;

import android.view.View;
import android.widget.ImageView;

import com.bestintheverse.audibooks.R;
import com.bestintheverse.audibooks.activities.MainActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class PanelStateListener {

    private ImageView expandButtonView;

    public void setListener(SlidingUpPanelLayout slidingLayout, MainActivity mActivity){

        MainActivity mainActivity = mActivity;
        expandButtonView = (ImageView) mainActivity.findViewById(R.id.expand_button_view);

        slidingLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {
                expandButtonView.setImageResource(R.drawable.expand_more_button);
            }

            @Override
            public void onPanelExpanded(View view) {
                expandButtonView.setImageResource(R.drawable.expand_less_button);
            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });



    }

}
