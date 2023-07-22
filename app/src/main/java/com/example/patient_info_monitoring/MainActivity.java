package com.example.patient_info_monitoring;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patient_info_monitoring.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final int Request_Location=1;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    String aadhar_str;
    boolean user_existance,user_info_added;

    private DatabaseReference UsersRef,personalRef;

    private RecyclerView recyclerView;

    private ProgressDialog progressDialog;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("patient_info_app_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        aadhar_str = sharedPreferences.getString("aadhar_no", "");
        user_existance = sharedPreferences.getBoolean("exist_account", false);


        if (!user_existance || aadhar_str.equals("")) {
            sendUserToLogInActivity();
        }else{
            personalRef=FirebaseDatabase.getInstance().getReference().child("Users").child(aadhar_str);
            personalRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild("Personal_Info")){
                        mainFun();
                    }else{
                        sendUserToUserInfoActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    public void mainFun(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_Location);
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            onGPS();
        }else{
            getLocation();
        }

        recyclerView=(RecyclerView) findViewById(R.id.recyclerview_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(aadhar_str).child("Hospitals");
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Fetching");
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        FirebaseRecyclerOptions<modelMain> options=new FirebaseRecyclerOptions.Builder<modelMain>()
                .setQuery(UsersRef, modelMain.class).build();

        FirebaseRecyclerAdapter<modelMain, MainHolder> adapter =
                new FirebaseRecyclerAdapter<modelMain, MainHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MainHolder holder, int position, @NonNull modelMain model)
                    {
                        String hospital = getRef(position).getKey();
                        UsersRef.child(hospital).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.nameTxt.setText(hospital);
                                String visit=snapshot.child("Last_Visited").getValue().toString();
                                visit=visit.replace(":","-");
                                holder.lastVisitedTxt.setText("Last day visited:"+visit);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        holder.btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent mainIntent = new Intent(MainActivity.this, HospitalMedicineDetails.class);
                                mainIntent.putExtra("hospitalName",hospital);
                                mainIntent.putExtra("hospitalName",hospital);
                                startActivity(mainIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public MainHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.hospital_layout_main, viewGroup, false);
                        return new MainHolder(view);
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

        progressDialog.dismiss();
    }

    public static class MainHolder extends RecyclerView.ViewHolder
    {
        TextView nameTxt, lastVisitedTxt;
        Button btn;


        public MainHolder(@NonNull View itemView)
        {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.textView_hospital_name_mainfragment);
            lastVisitedTxt = itemView.findViewById(R.id.textView_lastVisited_mainfragment);
            btn=itemView.findViewById(R.id.button_mainpage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option)
        {
            sendUserToLogInActivity();
        }
        if (item.getItemId() == R.id.main_personal_info)
        {
            sendUserToPersonalActivity();
        }
        if (item.getItemId() == R.id.main_local_hospital)
        {
            sendUserToMapActivity();
        }
//        if (item.getItemId() == R.id.main_create_group_option)
//        {
//            RequestNewGroup();
//        }
//        if (item.getItemId() == R.id.main_find_friends_option)
//        {
//            SendUserToFindFriendsActivity();
//        }

        return true;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    private void sendUserToLogInActivity()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("patient_info_app_data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("aadhar_no",null);
        editor.putBoolean("exist_account",false);
        editor.commit();

        Intent mainIntent = new Intent(MainActivity.this, LogInActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToUserInfoActivity()
    {
        Intent mainIntent = new Intent(MainActivity.this, UserInfoActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToPersonalActivity()
    {
        Intent mainIntent = new Intent(MainActivity.this, PersonalInformationActivity.class);
        startActivity(mainIntent);
    }

    private void sendUserToMapActivity()
    {
        Intent mainIntent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(mainIntent);
    }

    public void onGPS(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void getLocation(){
        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
        ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_Location);
        }else{
            Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location!=null){
                String lat=String.valueOf(location.getLatitude());
                String longi=String.valueOf(location.getLongitude());
                Toast.makeText(getApplicationContext(),lat+" "+longi,Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"Sorry",Toast.LENGTH_SHORT).show();
            }
        }
    }

}