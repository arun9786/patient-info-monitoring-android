package com.example.patient_info_monitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HospitalMedicineDetails extends AppCompatActivity {

    LinearLayout linearLayoutMedicine,linearLayoutReport;
    TextView textView,medicinePlus,reportPlus;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    String aadhar_str;

    private DatabaseReference UsersRef;

    private ArrayList<String> medicineDate;
    private ArrayList<String> reportDate;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_medicine_details);

        String hosName=getIntent().getStringExtra("hospitalName").toString();
        sharedPreferences = getSharedPreferences("patient_info_app_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        aadhar_str = sharedPreferences.getString("aadhar_no", "");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(aadhar_str);

        linearLayoutMedicine=(LinearLayout) findViewById(R.id.linearLayout_medicineDetails);
        linearLayoutReport=(LinearLayout)findViewById(R.id.linearLayout_reportDetails);
        textView=(TextView) findViewById(R.id.hospitalname_medicineDetails);
        textView.setText(hosName);
        medicinePlus=(TextView)findViewById(R.id.show_medicinedetails_plus);
        reportPlus=(TextView)findViewById(R.id.show_reportdetails_plus);

        medicineDate=new ArrayList<>();
        reportDate=new ArrayList<>();
        progressDialog=new ProgressDialog(this);

        TextView textViewMedicine=new TextView(this);
        textViewMedicine.setText("Medicine Details");

        progressDialog.setTitle("Fetching");
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        UsersRef.child("Medicine").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UsersRef.child("Medicine").child(hosName).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    String date=dataSnapshot.getKey().toString();

                                    UsersRef.child("Medicine").child(hosName).child(date).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                for(DataSnapshot dataSnapshot1:snapshot.getChildren()){
                                                    String time=dataSnapshot1.getKey().toString();

                                                    UsersRef.child("Medicine").child(hosName).child(date).child(time).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            int i=1;
                                                            if(!medicineDate.contains(date)){
                                                                TextView textView=new TextView(getApplicationContext());
                                                                textView.setText(date.replace(":","-"));
                                                                textView.setTextColor(Color.parseColor("#E2AB07"));
                                                                textView.setTextSize(23);
                                                                textView.setTypeface(null, Typeface.BOLD);
                                                                textView.setGravity(Gravity.CENTER);
                                                                linearLayoutMedicine.addView(textView);
                                                                medicineDate.add(date);
                                                            }


                                                            TextView textView1=new TextView(getApplicationContext());
                                                            String s[]=time.split(":");
                                                            String timef="";
                                                            if(Integer.parseInt(s[0]+"")<12){
                                                                timef=time+" AM";
                                                            }else{
                                                                timef=(Integer.parseInt(s[0]+"")-12)+":"+s[1]+" PM";
                                                            }
                                                            textView1.setText(timef);
                                                            textView1.setTextColor(Color.parseColor("#068CF7"));
                                                            textView1.setTextSize(22);
                                                            textView1.setTypeface(null, Typeface.BOLD);
                                                            textView1.setGravity(Gravity.CENTER);
                                                            linearLayoutMedicine.addView(textView1);

                                                            for(DataSnapshot dataSnapshot2:snapshot.getChildren()){

                                                                String mediName=dataSnapshot2.getKey().toString();
                                                                mediName=mediName.replace("{}",".");
                                                                String days=dataSnapshot2.child("No_of_Days").getValue().toString();
                                                                TextView textView2=new TextView(getApplicationContext());
                                                                if(Integer.parseInt(days)==1){
                                                                    textView2.setText(i + "." + mediName + " - " + days + " Day");
                                                                }else {
                                                                    textView2.setText(i + "." + mediName + " - " + days + " Days");
                                                                }
                                                                textView2.setTextColor(Color.parseColor("#04B90B"));
                                                                textView2.setTextSize(18);
                                                                textView2.setGravity(Gravity.START);
                                                                linearLayoutMedicine.addView(textView2);
                                                                i++;
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }else{
                                TextView textView=new TextView(getApplicationContext());
                                textView.setText("No medicine found...");
                                textView.setTextColor(Color.parseColor("#F80606"));
                                textView.setTextSize(20);
                                textView.setGravity(Gravity.CENTER);
                                linearLayoutMedicine.addView(textView);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
                else{
                    TextView textView=new TextView(getApplicationContext());
                    textView.setText("No medicine found...");
                    textView.setTextColor(Color.parseColor("#F80606"));
                    textView.setTextSize(20);
                    textView.setGravity(Gravity.CENTER);
                    linearLayoutMedicine.addView(textView);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UsersRef.child("Reports").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UsersRef.child("Reports").child(hosName).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    String date=dataSnapshot.getKey().toString();

                                    UsersRef.child("Reports").child(hosName).child(date).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                for(DataSnapshot dataSnapshot1:snapshot.getChildren()){
                                                    String time=dataSnapshot1.getKey().toString();

                                                    UsersRef.child("Reports").child(hosName).child(date).child(time).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            int i=1;
                                                            if(!reportDate.contains(date)){
                                                                TextView textView=new TextView(getApplicationContext());
                                                                textView.setText(date.replace(":","-"));
                                                                textView.setTextColor(Color.parseColor("#E2AB07"));
                                                                textView.setTextSize(23);
                                                                textView.setTypeface(null, Typeface.BOLD);
                                                                textView.setGravity(Gravity.CENTER);
                                                                linearLayoutReport.addView(textView);
                                                                reportDate.add(date);
                                                            }

                                                            TextView textView1=new TextView(getApplicationContext());
                                                            String s[]=time.split(":");
                                                            String timef="";
                                                            if(Integer.parseInt(s[0]+"")<12){
                                                                timef=time+" AM";
                                                            }else{
                                                                timef=(Integer.parseInt(s[0]+"")-12)+":"+s[1]+" PM";
                                                            }
                                                            textView1.setText(timef);
                                                            textView1.setTextColor(Color.parseColor("#068CF7"));
                                                            textView1.setTextSize(22);
                                                            textView1.setTypeface(null, Typeface.BOLD);
                                                            textView1.setGravity(Gravity.CENTER);
                                                            linearLayoutReport.addView(textView1);

                                                            for(DataSnapshot dataSnapshot2:snapshot.getChildren()){

                                                                String testName=dataSnapshot2.getKey().toString();
                                                                testName=testName.replace("{}",".");
                                                                TextView textView2=new TextView(getApplicationContext());
                                                                textView2.setText(i+"."+testName);
                                                                textView2.setTextColor(Color.parseColor("#9303AC"));
                                                                textView2.setTextSize(22);
                                                                textView2.setGravity(Gravity.START);
                                                                linearLayoutReport.addView(textView2);
                                                                i++;
                                                                if(dataSnapshot2.child("URL").exists()){
                                                                    String URL=dataSnapshot2.child("URL").getValue().toString();
                                                                    Button btn=new Button(getApplicationContext());
                                                                    btn.setText("See Report");
                                                                    btn.setBackgroundColor(Color.parseColor("#FAE73C"));
                                                                    btn.setTextColor(Color.parseColor("#FF3700B3"));
                                                                    btn.setTextSize(20);
                                                                    btn.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                                                                            startActivity(intent);
                                                                        }
                                                                    });
                                                                    linearLayoutReport.addView(btn);
                                                                }else{
                                                                    String val=dataSnapshot2.child("Reading_Value").getValue().toString();
                                                                    String unit=dataSnapshot2.child("Reading_Unit").getValue().toString();
                                                                    TextView textView3=new TextView(getApplicationContext());
                                                                    textView3.setText("Reading: "+val+" "+unit);
                                                                    textView3.setTextColor(Color.parseColor("#F31404"));
                                                                    textView3.setTextSize(20);
                                                                    textView3.setTypeface(null, Typeface.BOLD);
                                                                    textView3.setGravity(Gravity.CENTER);
                                                                    linearLayoutReport.addView(textView3);
                                                                }

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                progressDialog.dismiss();
                            }else{
                                TextView textView=new TextView(getApplicationContext());
                                textView.setText("No reports found...");
                                textView.setTextColor(Color.parseColor("#F80606"));
                                textView.setTextSize(20);
                                textView.setGravity(Gravity.CENTER);
                                linearLayoutReport.addView(textView);
                                progressDialog.dismiss();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
                else{
                    TextView textView=new TextView(getApplicationContext());
                    textView.setText("No reports found...");
                    textView.setTextColor(Color.parseColor("#F80606"));
                    textView.setTextSize(20);
                    textView.setGravity(Gravity.CENTER);
                    linearLayoutReport.addView(textView);

                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        medicinePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(medicinePlus.getText().equals("+")){
                    medicinePlus.setText("-");
                    linearLayoutMedicine.setVisibility(View.VISIBLE);
                }else{
                    medicinePlus.setText("+");
                    linearLayoutMedicine.setVisibility(View.GONE);
                }
            }
        });

        reportPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reportPlus.getText().equals("+")){
                    reportPlus.setText("-");
                    linearLayoutReport.setVisibility(View.VISIBLE);
                }else{
                    reportPlus.setText("+");
                    linearLayoutReport.setVisibility(View.GONE);
                }
            }
        });

    }
}