package sdk.labwerk.sample;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class SampleApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}
