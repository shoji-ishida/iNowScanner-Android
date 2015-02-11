package com.example.ishida.inowscanner;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by ishida on 2015/01/20.
 */
public class iNowBeacon {
    private static final int INOW_OFFSET = 30;

    public UUID proximityUUID;
    public int major;
    public int minor;
    public int power;
    public String name;
    public int illuminance;
    public int temperature;
    public int humidity;
    public int battery;
    String address;
    long lastUpdate;
    private ByteBuffer bb;
    public boolean isiNow = false;

   Vector<Field> changedFields = new Vector<Field>();

    static public iNowBeacon create(BluetoothDevice device, byte[] scanRecord) {
        if (isBeacon(scanRecord)) {
            try {
                return new iNowBeacon(device.getAddress(), scanRecord);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    static public iNowBeacon parse(iNowBeacon beacon, byte[] scanRecord) {
        if (isBeacon(scanRecord)) {
            iNowBeacon copy = new iNowBeacon(beacon);
            beacon.bb = ByteBuffer.wrap(scanRecord);
            beacon.parseBeacon();
            if (isiNowBeacon(scanRecord)) {
                beacon.isiNow = true;
                beacon.parseiNowBeacon();
            }
            beacon.lastUpdate = System.currentTimeMillis();
            beacon.changedFields.clear();
            try {
                if (!beacon.proximityUUID.equals(copy.proximityUUID)) {
                    beacon.changedFields.add(beacon.getClass().getField("proximityUUID"));
                }
                if (beacon.major != copy.major) {
                    beacon.changedFields.add(beacon.getClass().getField("major"));
                }
                if (beacon.minor != copy.minor) {
                    beacon.changedFields.add(beacon.getClass().getField("minor"));
                }
                if (beacon.power != copy.power) {
                    beacon.changedFields.add(beacon.getClass().getField("power"));
                }
                if (beacon.lastUpdate != copy.lastUpdate) {
                    beacon.changedFields.add(beacon.getClass().getDeclaredField("lastUpdate"));
                }
                if (beacon.isiNow != copy.isiNow) {
                    beacon.changedFields.add(beacon.getClass().getField("isiNow"));
                }
                if (!beacon.name.equals(copy.name)) {
                    beacon.changedFields.add(beacon.getClass().getField("name"));
                }
                if (beacon.illuminance != copy.illuminance) {
                    beacon.changedFields.add(beacon.getClass().getField("illuminance"));
                }
                if (beacon.temperature != copy.temperature) {
                    beacon.changedFields.add(beacon.getClass().getField("temperature"));
                }
                if (beacon.humidity != copy.humidity) {
                    beacon.changedFields.add(beacon.getClass().getField("humidity"));
                }
                if (beacon.battery != copy.battery) {
                    beacon.changedFields.add(beacon.getClass().getField("battery"));
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }
            return beacon;
        } else {
            return null;
        }


    }

    private iNowBeacon(String address, byte[] scanRecord) throws NoSuchFieldException {
        this.address = new String(address);
        bb = ByteBuffer.wrap(scanRecord);
        parseBeacon();
        if (isiNowBeacon(scanRecord)) {
            isiNow = true;
            parseiNowBeacon();
        }
        lastUpdate = System.currentTimeMillis();
        changedFields.add(getClass().getField("proximityUUID"));
        changedFields.add(getClass().getField("major"));
        changedFields.add(getClass().getField("minor"));
        changedFields.add(getClass().getDeclaredField("lastUpdate"));
        changedFields.add(getClass().getField("power"));
        changedFields.add(getClass().getField("isiNow"));
        changedFields.add(getClass().getField("name"));
        changedFields.add(getClass().getField("illuminance"));
        changedFields.add(getClass().getField("temperature"));
        changedFields.add(getClass().getField("humidity"));
        changedFields.add(getClass().getField("battery"));
    }

    iNowBeacon(String address) {
        this.address = address;
    }

    iNowBeacon(iNowBeacon beacon) {
        this.proximityUUID = UUID.fromString(beacon.proximityUUID.toString());
        this.major = beacon.major;
        this.minor = beacon.minor;
        this.power = beacon.power;
        this.name = new String(beacon.name);
        this.illuminance = beacon.illuminance;
        this.temperature = beacon.temperature;
        this.humidity = beacon.humidity;
        this.battery = beacon.battery;
        this.address = new String(beacon.address);
        this.lastUpdate = beacon.lastUpdate;
        this.isiNow = beacon.isiNow;
    }

    public iNowBeacon() {};


    @Override
    public boolean equals(Object object) {
        iNowBeacon beacon = (iNowBeacon)object;

        return this.address.equals(beacon.address);
    }

    private void parseBeacon() {
        //DO NOT CHANGE ORDER
        proximityUUID = parseUUID();
        major = parseMajor();
        minor = parseMinor();
        power = parsePower();
    }

    private void parseiNowBeacon() {
        // DO NOT CHANGE ORDER
        name = parseName();
        illuminance = parseIlluminance();
        temperature = parseTemperature();
        humidity = parseHumidity();
        battery = parseBattery();
    }

    private UUID parseUUID() {
        bb.position(9);
        return new UUID(bb.getLong(), bb.getLong());
    }

    private int parseMajor() {
        //bb.position(25);
        return bb.getChar();
    }

    private int parseMinor() {
        //bb.position(27);
        return bb.getChar();
    }

    private int parsePower() {
        //bb.position(29);
        return bb.get();
    }

    private String parseName() {
        int len = bb.get() - 1;
        bb.get();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char b = (char)bb.get();
            buf.append(b);
        }
        return buf.toString();
    }

    private int parseIlluminance() {
        bb.getInt();
        byte b = bb.get();
        int val = 0xff & b;
        return val;
    }

    private int parseTemperature() {
        return bb.get();
    }

    private int parseHumidity() {
        byte b = bb.get();
        int val = 0xff & b;
        return val;
    }

    private int parseBattery() {
        byte b = bb.get();
        int val = 0xff & b;
        return val;
    }

    public UUID getProximityUUID() {
        return proximityUUID;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPower() {
        return power;
    }

    public String getName() {
        return name;
    }

    public int getIlluminance() {
        return illuminance;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getBattery() {
        return battery;
    }

    public String getLastUpdate() {
        Date date = new Date(lastUpdate);
        return date.toString();
    }

    /*
    public Map<String, Object> map2() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("proximityUUID", proximityUUID.toString());
        map.put("major", major);
        map.put("minor", minor);
        map.put("power", power);
        map.put("lastUpdate", getLastUpdate());
        map.put("isiNow", isiNow);
        if (isiNow) {
            map.put("name", name);
            map.put("illuminance", illuminance);
            map.put("temperature", temperature);
            map.put("humidity", humidity);
            map.put("battery", battery);
        }
        return map;
    }
    */

    public Map<String, Object> map() {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Field field : changedFields) {
            try {
                // looks ugly but should use getter instead
                if (field.getName().equals("lastUpdate")) {
                    map.put(field.getName(), getLastUpdate());
                } else {
                    map.put(field.getName(), field.get(this));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Log.d("iNowBeacon", "changedField=" + map);
        return map;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("uuid=" + proximityUUID.toString());
        buf.append(", major=" + major);
        buf.append(", minor=" + minor);
        buf.append(", power=" + power);
        if (isiNow) {
            buf.append(", name=" + name);
            buf.append(", illuminance=" + illuminance);
            buf.append(", temperature=" + temperature);
            buf.append(", humidity=" + humidity);
            buf.append(", battery=" + battery);
        }
        return buf.toString();
    }

    static private boolean isBeacon(byte[] scanRecord) {
        boolean flag = false;
        if(scanRecord.length > 30)
        {
            if((scanRecord[5] == (byte)0x4c) && (scanRecord[6] == (byte)0x00) &&
                    (scanRecord[7] == (byte)0x02) && (scanRecord[8] == (byte)0x15)) {
                flag = true;
            }
        }
        return flag;
    }

    static private boolean isiNowBeacon(byte[] scanRecord) {
        boolean flag = false;
        int offset = INOW_OFFSET + 3 + scanRecord[INOW_OFFSET];
        if(scanRecord.length > 30)
        {
            if((scanRecord[offset] == (byte)0x20) && (scanRecord[offset + 1] == (byte)0x18) &&
                    (scanRecord[offset + 13] == (byte)0x21) && (scanRecord[offset + 14] == (byte)0x18)) {
                flag = true;
            }
        }
        return flag;
    }

}
