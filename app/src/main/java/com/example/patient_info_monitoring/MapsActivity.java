package com.example.patient_info_monitoring;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.patient_info_monitoring.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int Request_Location=1;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    LocationManager locationManager;
    Location location;

    Geocoder geocoder;
    List<Address> addressList;

    ArrayList<String> arrayList_allhospital;
    ArrayList<String> arraysList_patienthospital;

    private DatabaseReference UserRef,HosRef;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    String aadhar_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences("patient_info_app_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        aadhar_str = sharedPreferences.getString("aadhar_no", "");

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(aadhar_str);
        HosRef= FirebaseDatabase.getInstance().getReference().child("Hospitals");

        arrayList_allhospital=new ArrayList<>();
        arraysList_patienthospital=new ArrayList<>();
        geocoder=new Geocoder(this, Locale.getDefault());

        getHospitals();
    }

    private void getHospitals() {
        UserRef.child("Hospitals").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String name=dataSnapshot.getKey().toString();
                    arraysList_patienthospital.add(name);
                    try{
                        addressList=geocoder.getFromLocationName(name,5);
                        if(addressList==null){

                        }else{
                            Address locat=addressList.get(0);
                            LatLng latLng=new LatLng(locat.getLatitude(),locat.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(latLng).title(name).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12.0f));
                        }

                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        HosRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String name=dataSnapshot.getKey().toString();
                    if(!arraysList_patienthospital.contains(name)) {
                        try {
                            addressList = geocoder.getFromLocationName(name, 5);
                            if (addressList == null) {

                            } else {
                                Address locat = addressList.get(0);
                                LatLng latLng = new LatLng(locat.getLatitude(), locat.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(latLng).title(name).
                                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
        }else{
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_Location);
            }else {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                Double lat = location.getLatitude();
                Double longi = location.getLongitude();
                LatLng sydn = new LatLng(lat, longi);
                mMap.addMarker(new MarkerOptions().position(sydn).title("Your location").
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydn));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydn,12.0f));
            } else {
                Toast.makeText(getApplicationContext(), "Sorry", Toast.LENGTH_SHORT).show();
            }
            }
        }
//        Location v=mMap.getMyLocation();
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }
}