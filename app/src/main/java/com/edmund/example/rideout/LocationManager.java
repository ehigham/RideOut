package com.edmund.example.rideout;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.location.LocationListener;

public class LocationManager implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private static GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public LocationManager(Context context) {
        mContext = context;

        if (GooglePlayServicesAvailable()) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else {
            //Use Android Location Services
            //TODO:
            //ATM, you're fucked...
        }
    }

    private boolean GooglePlayServicesAvailable(){
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (errorCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(errorCode, (MainActivity) mContext, 0).show();
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(mContext, "suspended", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(mContext, connectionResult.toString(), Toast.LENGTH_LONG).show();
    }

    protected Location getCoarseLocation(){
        if (mLastLocation != null){
            return mLastLocation;
        } else return null;
    }
}
