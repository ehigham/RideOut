package com.opentt.rideout;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class PlaybackActivity extends Activity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    /** Log Tag */
    private static final String TAG = "PlaybackActivity";

    /** The GoogleMap set in onMapReady */
    private GoogleMap mMap;

    /** A test marker */
    private Marker mTestMarker;

    /** Test Marker Position */
    private LatLng TestMarkerLatLng = new LatLng(51.5033630,-0.1276250);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new OverviewFragment())
                    .commit();
        }

        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map_overview);

        if ( mapFragment != null ) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Could not get map fragment");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Add lots of markers to the map.
        addRideMarkersToMap();

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

    }


    private void addRideMarkersToMap(){

        mTestMarker = mMap.addMarker(new MarkerOptions()
                .position(TestMarkerLatLng)
                .title("SimpleMarker")
                .snippet("This is a simple marker")
                .icon(BitmapDescriptorFactory.defaultMarker(1.0f)));

        /*
         * PROBABLY BEST TO DO THIS IN AN ASYNC TASK
         *
         * Open the ride_data database
         * Check if ride data is available
         * If false:
         *      Launch Dialog saying that no ride data is available
         *      exit
         * else:
         * For each ride id:
         *   Place a marker at each ride location with fields:
         *   .position(START_LATLONG)
         *   .title(RIDE_ID)
         *   .snippet(SOME_USEFUL_SUMMARY) eg. Duration/MaxSpeed
         *   .icon(BitmapDescriptorFactory.defaultMarker(SOME_COLOUR)));
         *
         * SOME_COLOUR eg (rideID * 360 / numRides)
         */

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // This causes the marker at Perth to bounce into position when it is clicked.
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

        return false;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {

        Toast.makeText(PlaybackActivity.this,"Click info window",Toast.LENGTH_SHORT).show();

        /* Launch ride data analysis - New fragment?
        *
        *  Do in Async task:
        *       Populate polyline with that ride data from database
        *       Set onBackPressedListener to new listener here (Handled by fragment)
        *
        *  Do in UI:
        *       Hide marker
        *       overlay polyline
        *       Zoom map to cover polyline bounds
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
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
    public static class OverviewFragment extends Fragment {

        public OverviewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_playback, container, false);
        }
    }
}
