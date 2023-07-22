package com.example.patient_info_monitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalInformationActivity extends AppCompatActivity {

    private TextView textViewName,textViewId,textViewAge,textViewGender,textViewMobile,textViewAddress;
    private DatabaseReference UserRef;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    String aadhar_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesonal_information);

        sharedPreferences = getSharedPreferences("patient_info_app_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        aadhar_str = sharedPreferences.getString("aadhar_no", "");

        textViewName=(TextView) findViewById(R.id.name_userpersonalActivity);
        textViewId=(TextView) findViewById(R.id.id_userpersonalActivity);
        textViewAge=(TextView) findViewById(R.id.age_userpersonalActivity);
        textViewGender=(TextView) findViewById(R.id.gender_userpersonalActivity);
        textViewMobile=(TextView) findViewById(R.id.mobile_userpersonalActivity);
        textViewAddress=(TextView) findViewById(R.id.address_userpersonalActivity);

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(aadhar_str);

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String phone=snapshot.child("Phone").getValue().toString();
                textViewMobile.setText(phone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UserRef.child("Personal_Info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.child("Name").getValue().toString();
                String fatherName=snapshot.child("Father_Name").getValue().toString();
                String age=snapshot.child("Age").getValue().toString();
                String gender=snapshot.child("Gender").getValue().toString();

                String district=snapshot.child("District").getValue().toString();//5
                String country=snapshot.child("Country").getValue().toString();//7
                String flatno=snapshot.child("Flat_No").getValue().toString();//1
                String pincode=snapshot.child("Pincode").getValue().toString();//8
                String place=snapshot.child("Place").getValue().toString();//3
                String post=snapshot.child("Post_Office").getValue().toString();//4
                String state=snapshot.child("State").getValue().toString();//6
                String street=snapshot.child("Street").getValue().toString();//2
                String Taluk=snapshot.child("Taluk").getValue().toString();//5

                String address="";
                if(!flatno.equals("")){
                    address+=flatno+", ";
                }
                if(!street.equals("")){
                    address+=street+", ";
                }
                if(!place.equals("")){
                    address+=place+", ";
                }
                if(!post.equals("")){
                    address+=post+"(PO), ";
                }
                if(!Taluk.equals("")){
                    address+=Taluk+"(Tk),";
                }
                if(!district.equals("")){
                    address+=district+"(Dt), ";
                }
                if(!state.equals("")){
                    address+=state+",";
                }
                if(!pincode.equals("")){
                    address+=pincode+", ";
                }
                if(!country.equals("")){
                    address+=country+".";
                }

                textViewId.setText(aadhar_str);
                textViewName.setText(name+" "+fatherName);
                textViewAge.setText(age+" years");
                textViewGender.setText(gender);
                textViewAddress.setText(address);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}