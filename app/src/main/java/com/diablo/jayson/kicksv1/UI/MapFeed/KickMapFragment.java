package com.diablo.jayson.kicksv1.UI.MapFeed;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.diablo.jayson.kicksv1.Models.Activity;
import com.diablo.jayson.kicksv1.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class KickMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = KickMapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private Location mLastLocation;
    private LatLng mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private float ZOOM = 17f;
    private double CBD_LAT = -1.28333;
    private double CBD_LONG = 36.81667;
    private LatLng CBD = new LatLng(CBD_LAT, CBD_LONG);

    private ArrayList<Activity> allActivities;
    private Marker Marker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation();
                } else {
                    Toast.makeText(getActivity(), R.string.location_permission_denied,
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.kick_map_fragment, container, false);
        loadActiveActivitiesFromDb();

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        return view;
    }

    private void loadActiveActivitiesFromDb() {

        allActivities = new ArrayList<Activity>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                allActivities.add(new Activity(snapshot.toObject(Activity.class).getHost(),
                                        snapshot.toObject(Activity.class).getActivityTitle(),
                                        snapshot.toObject(Activity.class).getActivityStartTime(),
                                        snapshot.toObject(Activity.class).getActivityEndTime(),
                                        snapshot.toObject(Activity.class).getActivityDate(),
                                        snapshot.toObject(Activity.class).getActivityLocationName(),
                                        snapshot.toObject(Activity.class).getActivityLocationCoordinates(),
                                        snapshot.toObject(Activity.class).getActivityMinRequiredPeople(),
                                        snapshot.toObject(Activity.class).getActivityMaxRequiredPeople(),
                                        snapshot.toObject(Activity.class).getActivityMinAge(),
                                        snapshot.toObject(Activity.class).getActivityMaxAge(),
                                        snapshot.toObject(Activity.class).getImageUrl(),
                                        snapshot.toObject(Activity.class).getActivityUploaderId(),
                                        snapshot.toObject(Activity.class).getActivityId(),
                                        snapshot.toObject(Activity.class).getActivityCost(),
                                        snapshot.toObject(Activity.class).getActivityUploadedTime(),
                                        snapshot.toObject(Activity.class).getTags(),
                                        snapshot.toObject(Activity.class).getActivityTag(),
                                        snapshot.toObject(Activity.class).getActivityAttendees(),
                                        snapshot.toObject(Activity.class).isActivityPrivate()));
                            }
                        }
                    }
                });
    }

    public void enableLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mLastLocation = location;

                                mCurrentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        enableLocation();

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        allActivities = new ArrayList<Activity>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            allActivities.add(new Activity(snapshot.toObject(Activity.class).getHost(),
                                    snapshot.toObject(Activity.class).getActivityTitle(),
                                    snapshot.toObject(Activity.class).getActivityStartTime(),
                                    snapshot.toObject(Activity.class).getActivityEndTime(),
                                    snapshot.toObject(Activity.class).getActivityDate(),
                                    snapshot.toObject(Activity.class).getActivityLocationName(),
                                    snapshot.toObject(Activity.class).getActivityLocationCoordinates(),
                                    snapshot.toObject(Activity.class).getActivityMinRequiredPeople(),
                                    snapshot.toObject(Activity.class).getActivityMaxRequiredPeople(),
                                    snapshot.toObject(Activity.class).getActivityMinAge(),
                                    snapshot.toObject(Activity.class).getActivityMaxAge(),
                                    snapshot.toObject(Activity.class).getImageUrl(),
                                    snapshot.toObject(Activity.class).getActivityUploaderId(),
                                    snapshot.toObject(Activity.class).getActivityId(),
                                    snapshot.toObject(Activity.class).getActivityCost(),
                                    snapshot.toObject(Activity.class).getActivityUploadedTime(),
                                    snapshot.toObject(Activity.class).getTags(),
                                    snapshot.toObject(Activity.class).getActivityTag(),
                                    snapshot.toObject(Activity.class).getActivityAttendees(),
                                    snapshot.toObject(Activity.class).isActivityPrivate()));
                        }
                        mMap = googleMap;
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(
                                new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 12));
                                        } else {
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CBD, 12));
                                        }
                                    }
                                }
                        );
//                        Log.e(TAG, String.valueOf(allActivities.get(0).getActivityLocationCoordinates()));
                        for (int i = 0; i < allActivities.size(); i++) {


                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(allActivities.get(i).getActivityLocationCoordinates().getLatitude(), allActivities.get(i).getActivityLocationCoordinates().getLongitude()))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                    .title(allActivities.get(i).getActivityTag().getTagName()));
                            marker.setTag(allActivities.get(i).getActivityId());
                            Glide.with(getContext())
                                    .asBitmap()
                                    .load(allActivities.get(i).getActivityTag().getTagIconUrl())
                                    .into(new SimpleTarget<Bitmap>(20, 20) {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
                                        }
                                    });
                        }
                        mMap.setOnMarkerClickListener(this::onMarkerClick);

                    }

                });


//        for (Activity activity : allActivities) {
//            Marker = mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(activity.getKickLocationCordinates().getLatitude(), activity.getKickLocationCordinates().getLongitude()))
//                    .title(activity.getTag().getTagName()));
//            Marker.setTag(activity.getActivityId());
//        }


    }

    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
        String activityId = marker.getTag().toString();
        Toast.makeText(getContext(), activityId, Toast.LENGTH_LONG).show();
        return false;
    }


    public static class DownloadFilesTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            Bitmap networkBitmap = null;


            String networkUrl = urls[0];//Load the first element
            URL url = null;
            try {
                url = new URL(networkUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                networkBitmap = BitmapFactory.decodeStream(
                        url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return networkBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }

    AsyncTask<String, Void, Bitmap> loadImageTask = new AsyncTask<String, Void, Bitmap>() {
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bmImg = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
                bmImg = null;
            }

            return bmImg;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            // TODO: do what you need with resulting bitmap - add marker to map


        }
    };


}
