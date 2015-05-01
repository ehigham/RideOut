package com.opentt.rideout;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;


public class PlaybackActivity extends Activity
        implements PlaybackOverviewFragment.OnMarkerWindowClickListener {

    /** Log Tag */
    private static final String TAG = "PlaybackActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaybackOverviewFragment())
                    .commit();
        }
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
     * Called by PlaybackOverviewFragment
     * Launches new fragment containing polyline for that marker
     */
    @Override
    public void onMarkerWindowClick(final Marker marker){
        Toast.makeText(PlaybackActivity.this,"Received " + marker.getId(), Toast.LENGTH_SHORT ).show();

    }
}
