package com.example.ishida.inowscanner;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    final static String TAG = "iNowScanner";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ListFragment fragment;
    private iNowBeaconListAdapter listAdapter;
    private Handler handler;
    private Firebase fireRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            fragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
            listAdapter = new iNowBeaconListAdapter(this,
                    R.layout.list_cell,
                    new ArrayList<iNowBeacon>());
            fragment.setListAdapter(listAdapter);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handler = new Handler(Looper.getMainLooper());

        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //Log.d(TAG, "Le advertisement received: " + device);
                //Log.d(TAG, "scanRecord: " + byteArrayToHex(scanRecord));
                iNowBeacon beacon;
                boolean newBeacon;
                if (listAdapter.contains(device.getAddress())) {
                    //Log.d(TAG, "already scanned");
                    iNowBeacon temp = new iNowBeacon(device.getAddress());
                    int position = listAdapter.getPosition(temp);
                    temp = listAdapter.getItem(position);
                    beacon = iNowBeacon.parse(temp, scanRecord);
                    newBeacon = false;
                } else {
                    beacon = iNowBeacon.create(device, scanRecord);
                    newBeacon = true;
                }
                if (beacon != null) {
                    final iNowBeacon b = beacon;
                    final boolean newB = newBeacon;
                    Log.d(TAG, beacon.toString());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (newB) {
                                    Log.d(TAG, "add");
                                    listAdapter.add(b);
                                }
                                listAdapter.notifyDataSetChanged();
                                Firebase devicesRef = fireRef.child("devices");
                                if (newB) {
                                    devicesRef.setValue(listAdapter.getBeacons());
                                } else {
                                    Firebase childRef = devicesRef.child(b.address);
                                    Map<String, Object> meow = b.map();
                                    childRef.updateChildren(meow);
                                }
                            }
                        });
                } else {
                    //Log.d(TAG, "not a beacon");
                }
            }
        };

        Firebase.setAndroidContext(this);
        fireRef = new Firebase("https://intense-heat-9521.firebaseio.com/iNow");

        listAdapter.setListener(new iNowBeaconListAdapter.UpdateListener() {
            @Override
            public void onRemoved(iNowBeacon beacon) {
                fireRef.child("devices").child(beacon.address).removeValue();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        listAdapter.runScheduler();
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        listAdapter.stopScheduler();
        bluetoothAdapter.stopLeScan(leScanCallback);
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();
        fireRef.removeValue();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x ", b & 0xff));
        return sb.toString();
    }
}
