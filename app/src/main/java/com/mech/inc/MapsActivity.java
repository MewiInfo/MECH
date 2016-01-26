package com.mech.inc;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<HighlightSite> sites = new ArrayList<HighlightSite>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Import Highlight Sites
        BufferedReader br = null;
        String line = "";

        try {

            br = new BufferedReader(new InputStreamReader(getAssets().open("HighlightSites.csv")));
            int i = 0;
            while ((line = br.readLine()) != null) {

                String[] values = line.split(";");

                String[] latlng = values[1].split(",");
                double lat = Double.parseDouble(latlng[0]);
                double lng = Double.parseDouble(latlng[1]);
                LatLng location = new LatLng(lat, lng);

                String[] tags = values[2].split(",");

                sites.add(i, new HighlightSite(values[0], location, tags, values[3]));

                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        // Add markers
        for(int i = 0; i < sites.size(); i++){
            mMap.addMarker(new MarkerOptions().position(sites.get(i).getCoordinates()).title(sites.get(i).getName()));
        }

        //Enable "My Location"
        mMap.setMyLocationEnabled(true);

        //Setting up the tracker
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new MyLocationListener();

        // Move map to current location
        Location currentLoc = locManager.getLastKnownLocation(locManager.getBestProvider(new Criteria(),true));
        LatLng location = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        //Update frequency is set to 3 seconds, minimum distance to 2 meters.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locManager.requestLocationUpdates(locManager.GPS_PROVIDER, 3000, 2, locListener);
    }

    private class MyLocationListener implements LocationListener {

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        @Override
        public void onLocationChanged(Location loc) {

            // Check for highlight sites in the proximity
            for(int i = 0; i < sites.size(); i++){

                if(!sites.get(i).isNotifiedAlready() && sites.get(i).distanceUnder(loc, 25)){

                    //Notifying the user
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder mBuilder =
                            (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Approaching " + sites.get(i).getName() + ".")
                                    .setContentText(sites.get(i).getDescription())
                                    .setSound(alarmSound)
                                    .setAutoCancel(true);

                    Intent resultIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addParentStack(MapsActivity.class);
                    stackBuilder.addNextIntent(resultIntent);

                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotificationManager.notify(i, mBuilder.build());

                    sites.get(i).setNotifiedAlready(true);
                }

                //The notification is canelled and notifiedAlready is reset to false, when the user has moved away from the site again.
                else if(sites.get(i).isNotifiedAlready() && sites.get(i).distanceOver(loc, 100)){
                    mNotificationManager.cancel(i);
                    sites.get(i).setNotifiedAlready(false);
                }
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS disabled", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }
}
