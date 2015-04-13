Promptus Sample App
===========================

In order start using sdk please follow the steps below:

#### Add token to AndroidManifest
    <meta-data
            android:name="sdk.labwerk.CUSTOMER_TOKEN"
            android:value="PLEASE_INSERT_TOKEN_HERE" />

#### Add all library dependency to project build script
    compile(name: 'labwerk-sdk', ext: 'aar')
    compile "de.greenrobot:eventbus:2.2.1"
    compile 'com.squareup.retrofit:retrofit:1.9.0'
    compile 'com.android.support:support-v4:21.0.3'
    compile 'com.nostra13.universalimageloader:universal-image-loader:1.9.3'
    compile 'com.davemorrissey.labs:subsampling-scale-image-view:3.1.3'


### Add map to layout, add icons to show
         <sdk.labwerk.core.ui.widgets.IndoorMap
                    android:id="@+id/indoor_map"
                    style="@style/MatchParent"
                    android:layout_centerInParent="true"
                    labwerk:pin_icon="@drawable/pin_image_green"
                    labwerk:pin_with_arrow_icon="@drawable/pinpoint_with_arrow"
                    labwerk:exhibitor_icon="@drawable/pin_image_blue" />
    
### Subscribe on getting beacons and match scanned beacons with server beacons
    eventBus = EventBus.getDefault();
    eventBus.register(this);
    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DeviceScanner.DiscoveredDeviceEvent discoveredDeviceEvent) {
        List<Beacon> beacons = discoveredDeviceEvent.getDiscoveredDevice();
        new MatcherFinder(beacons).execute();
    }
    
#### Request BLE permission and start scanning for beacons
    private void requestPermissionAndStartScanning() {
            bleManager = new BleManager(this);
            if (!bleManager.isBleSupported()) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!bleManager.isEnabled()) {
                bleManager.requestPermission(this);
            } else if (!bleManager.isScanning()) {
                bleManager.startScanning();
            }
    }

    

