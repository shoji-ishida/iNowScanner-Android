package com.example.ishida.inowscanner;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ishida on 2015/01/21.
 */
public class iNowBeaconListAdapter extends ArrayAdapter<iNowBeacon> {
    private static final int MAX_SCAN_INTERVAL = 15000;
    //private List<iNowBeacon> beacons;
    private Handler handler;
    private ScheduledExecutorService service;

    public iNowBeaconListAdapter(Context context, int resource, int textViewResourceId, List<iNowBeacon> beacons) {
        super(context, resource, beacons);
        //this.beacons = beacons;
        handler = new Handler(context.getMainLooper());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_cell, null);
        }
        iNowBeacon beacon = getItem(position);
        if (beacon != null) {
            TextView tempText = (TextView) v
                    .findViewById(R.id.textView2);
            TextView humidText = (TextView) v
                    .findViewById(R.id.textView4);
            TextView illumText = (TextView) v
                    .findViewById(R.id.textView6);
            if (beacon.isiNow) {
                tempText.setText(beacon.temperature);
                humidText.setText(beacon.humidity);
                illumText.setText(beacon.illuminance);
            } else {
                tempText.setText("ーーー");
                humidText.setText("ーー");
                illumText.setText("ーーー");
            }


            TextView uuidText = (TextView) v
                    .findViewById(R.id.textView11);
            TextView majorText = (TextView) v
                    .findViewById(R.id.textView13);
            TextView minorText = (TextView) v
                    .findViewById(R.id.textView15);
            uuidText.setText(beacon.proximityUUID.toString());
            //majorText.setText(beacon.major);
            //minorText.setText(beacon.minor);
        }
        return v;
    }

    public boolean contains(iNowBeacon beacon) {
        int pos = getPosition(beacon);
        return (pos >= 0) ? true : false;
    }

    public boolean contains(String address) {
        for (int i = 0; i < getCount(); i++) {
            iNowBeacon b = getItem(i);
            if (b.address.equals(address)) {
                return true;
            }
        }
        return false;
    }

    public iNowBeacon getItem(String address) {
        int pos = getPosition(address);
        if (pos >= 0) {
            return getItem(pos);
        } else {
            return null;
        }
    }

    public int getPosition(String address) {
        for (int i = 0; i < getCount(); i++) {
            iNowBeacon b = getItem(i);
            if (b.address.equals(address)) {
                return i;
            }
        }
        return -1;
    }

    public void runScheduler() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Log.d(MainActivity.TAG, "clean up beacons");
                for (int i = 0; i < getCount(); i++) {
                    final iNowBeacon b = getItem(i);
                    long delta = now - b.lastUpdate;
                    Log.d(MainActivity.TAG, "delta = " + delta);
                    if (delta > MAX_SCAN_INTERVAL) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                remove(b);
                            }
                        });
                    }
                }
            }
        }, 0, MAX_SCAN_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stopScheduler() {
        service.shutdown();

    }

    /*
    public boolean set(String address, byte[] scanRecord) {
        iNowBeacon temp = new iNowBeacon(address);
        int pos = getPosition(temp);
        temp = getItem(pos);
        temp = iNowBeacon.parse(temp, scanRecord);

        if (temp != null) {
            return true;
        } else {
            return false;
        }
    }
    */

}
