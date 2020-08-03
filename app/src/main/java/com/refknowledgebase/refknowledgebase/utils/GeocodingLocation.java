package com.refknowledgebase.refknowledgebase.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingLocation {

    private static final String TAG = "GeocodingLocation";

    public static void getAddressFromLocation(final String locationAddress,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String result = null;
            try {
                List
                        addressList = geocoder.getFromLocationName(locationAddress, 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = (Address) addressList.get(0);
                    StringBuilder sb = new StringBuilder();
                    sb.append(address.getLatitude()).append(",");
                    sb.append(address.getLongitude());
                    result = sb.toString();
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable to connect to Geocoder", e);
            } finally {
                Message message = Message.obtain();
                message.setTarget(handler);
                if (result != null) {
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("address", result);
                    message.setData(bundle);
                } else {
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("address", result);
                    message.setData(bundle);
                }
                message.sendToTarget();
            }
            }
        };
        thread.start();
    }
}