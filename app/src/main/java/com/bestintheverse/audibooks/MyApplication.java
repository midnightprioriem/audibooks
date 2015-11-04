package com.bestintheverse.audibooks;


import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
       // formKey = "", // This is required for backward compatibility but not used
        mailTo = "bestintheverse.audibooks@gmail.com", // my email here
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.send_crash_report
)

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

}
