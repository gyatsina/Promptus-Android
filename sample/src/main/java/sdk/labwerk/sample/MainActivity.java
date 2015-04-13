package sdk.labwerk.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import sdk.labwerk.core.concurency.BaseAsyncTask;
import sdk.labwerk.core.model.api.Beacon;
import sdk.labwerk.core.services.BeaconService;
import sdk.labwerk.core.services.ble.BleManager;
import sdk.labwerk.core.services.ble.DeviceScanner;
import sdk.labwerk.core.ui.widgets.IndoorMap;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SEARCH = 1;
    private static final int MAIN = 0;

    private BleManager bleManager;
    private IndoorMap indoorMapView;
    private EventBus eventBus;


    private BeaconsAdapter aAdpt;
    private ViewFlipper flipper;
    private MenuItem searchItem;
    private CheckBox navigationSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        indoorMapView = (IndoorMap) findViewById(R.id.indoor_map);
        flipper = (ViewFlipper) findViewById(R.id.flipper);

        navigationSwitcher = (CheckBox) findViewById(R.id.navigation_switcher);
        navigationSwitcher.setOnCheckedChangeListener(new OnNavigationChange());

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {
                Beacon beacon = (Beacon) parentAdapter.getItemAtPosition(position);
                indoorMapView.addBeacon(beacon);
                searchItem.collapseActionView();
            }
        });
        aAdpt = new BeaconsAdapter(this);
        new BeaconLoader().execute();
        lv.setAdapter(aAdpt);

        eventBus = EventBus.getDefault();
        eventBus.register(this);

    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        indoorMapView.release();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bleManager.isRequestCanceled(requestCode, resultCode)) {
            finish();
            return;
        } else if (bleManager.isRequestOk(requestCode, resultCode)) {
            bleManager.startScanning();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.start_searching);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        searchView.setOnQueryTextListener(new SimpleTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText) && navigationSwitcher.isChecked()) {
                    navigationSwitcher.setChecked(false);
                }
                flipper.setDisplayedChild(TextUtils.isEmpty(newText) ? MAIN : SEARCH);
                aAdpt.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        requestPermissionAndStartScanning();
    }

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

    @Override
    public void onStop() {
        bleManager.stopScanning();
        super.onStop();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DeviceScanner.DiscoveredDeviceEvent discoveredDeviceEvent) {
        List<Beacon> beacons = discoveredDeviceEvent.getDiscoveredDevice();
        new MatcherFinder(beacons).execute();
    }

    private class OnNavigationChange implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                indoorMapView.startNavigation();
            } else {
                indoorMapView.stopNavigation();
            }
        }
    }

    private class MatcherFinder extends BaseAsyncTask<Object, Object, List<Beacon>> {

        private final List<Beacon> beacons;

        public MatcherFinder(List<Beacon> beacons) {
            this.beacons = beacons;
        }

        @Override
        public void onResult(List<Beacon> beacons) {
            indoorMapView.finMatch(beacons);
        }

        @Override
        public void onException(Exception e) {
            Log.e(TAG, "Error during match finding", e);
        }

        @Override
        public List<Beacon> performInBackground(Object[] params) throws Exception {
            return BeaconService.getInstance(MainActivity.this).getBeaconMatch(beacons);
        }
    }

    private class BeaconLoader extends BaseAsyncTask<Object, Object, List<Beacon>> {
        private ProgressDialog dlg;

        @Override
        protected void onPreExecute() {
            dlg = new ProgressDialog(MainActivity.this);
            dlg.setTitle("Beacon loading...");
            dlg.setIndeterminate(false);
            dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dlg.show();
        }

        @Override
        public void onResult(List<Beacon> beacons) {
            if (beacons != null) {
                aAdpt.addAll(beacons);
            } else {
                aAdpt.addAll(new ArrayList<Beacon>());
            }
            dlg.cancel();
        }

        @Override
        public void onException(Exception e) {
            dlg.cancel();
        }

        @Override
        public List<Beacon> performInBackground(Object... params) throws Exception {
            return BeaconService.getInstance(MainActivity.this).getVisibleBeacons();
        }
    }
}
