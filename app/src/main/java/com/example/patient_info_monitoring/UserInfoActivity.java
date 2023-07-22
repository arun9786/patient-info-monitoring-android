package com.example.patient_info_monitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInfoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private RadioGroup radioGroup_gender;
    private RadioButton radioButton_gender;
    private EditText editText_name,editText_fathername,editText_age,editText_flatno,editText_street,editText_place,
            editText_pincode,editText_taluk,editText_district,editText_state,editText_country;
    private Spinner spinner_post_office;
    private String name_str,father_name_str,age_str,gender_str,flatno_str,street_str,place_str,pincode_str,
                    postoffice_str,taluk_str,district_str,state_str,country_str,aadhar_str;
    private Button save_btn;
    ProgressDialog progressDialog;
    RequestQueue requestQueue;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private ArrayList<String> arrayList_postoffice,arrayList_taluk,arrayList_district,arrayList_state,arrayList_country;
    private ArrayAdapter<String> arrayAdapter_postoffice;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        editText_name=(EditText)findViewById(R.id.Name_user_info_activity_edt);
        editText_fathername=(EditText)findViewById(R.id.Father_Name_user_info_activity_edt);
        editText_age=(EditText)findViewById(R.id.Age_user_info_activity_edt);
        editText_flatno=(EditText)findViewById(R.id.Flatno_user_info_activity_edt);
        editText_street=(EditText)findViewById(R.id.Street_user_info_activity_edt);
        editText_place=(EditText)findViewById(R.id.Place_user_info_activity_edt);
        editText_pincode=(EditText)findViewById(R.id.Pincode_user_info_activity_edt);
        editText_taluk=(EditText)findViewById(R.id.Taluk_user_info_activity_edt);
        editText_district=(EditText)findViewById(R.id.District_user_info_activity_edt);
        editText_state=(EditText)findViewById(R.id.State_user_info_activity_edt);
        editText_country=(EditText)findViewById(R.id.Country_user_info_activity_edt);

        spinner_post_office=(Spinner)findViewById(R.id.spinner_post_office_user_info_activity);
        spinner_post_office.setOnItemSelectedListener(this);
        spinner_post_office.setEnabled(false);
        save_btn=(Button)findViewById(R.id.save_user_info_btn);
        radioGroup_gender=(RadioGroup) findViewById(R.id.radioGroup_gender_user_info_activity);
        progressDialog = new ProgressDialog(this);

        arrayList_postoffice=new ArrayList<String>();
        arrayList_taluk=new ArrayList<String>();
        arrayList_district=new ArrayList<String>();
        arrayList_state=new ArrayList<String>();
        arrayList_country=new ArrayList<String>();

        requestQueue= Volley.newRequestQueue(this);
        progressDialog=new ProgressDialog(this);

        sharedPreferences=getSharedPreferences("patient_info_app_data",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        aadhar_str=sharedPreferences.getString("aadhar_no","");
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        editText_pincode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayList_postoffice.clear();
                arrayList_taluk.clear();
                arrayList_district.clear();
                arrayList_state.clear();
                arrayList_district.clear();
                if(s.length()==6){
                    getAddressforPincode(s);
                }else{
                    spinner_post_office.setEnabled(false);
                    editText_taluk.setText(null);
                    editText_district.setText(null);
                    editText_state.setText(null);
                    editText_country.setText(null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name_str=editText_name.getText().toString().trim();
                father_name_str=editText_fathername.getText().toString().trim();
                age_str=editText_age.getText().toString().trim();
                flatno_str=editText_flatno.getText().toString().trim();
                street_str=editText_street.getText().toString().trim();
                place_str=editText_place.getText().toString().trim();
                pincode_str=editText_pincode.getText().toString().trim();
                taluk_str=editText_taluk.getText().toString().trim();
                int selected_gender_id=radioGroup_gender.getCheckedRadioButtonId();
                radioButton_gender=(RadioButton) findViewById(selected_gender_id);
                gender_str=radioButton_gender.getText().toString();

                if(TextUtils.isEmpty(name_str)) {
                    Toast.makeText(getApplicationContext(), "Enter valid Name...", Toast.LENGTH_SHORT).show();
                    editText_name.setError("Enter valid Name...");
                    editText_name.requestFocus();
                    editText_name.setFocusable(true);
                }else if(TextUtils.isEmpty(father_name_str)) {
                    Toast.makeText(getApplicationContext(), "Enter valid Father name...", Toast.LENGTH_SHORT).show();
                    editText_fathername.setError("Enter valid Father name...");
                    editText_fathername.requestFocus();
                    editText_fathername.setFocusable(true);
                }else if(TextUtils.isEmpty(age_str) || Integer.parseInt(age_str)<1 || Integer.parseInt(age_str)>200) {
                    Toast.makeText(getApplicationContext(), "Enter valid Age...", Toast.LENGTH_SHORT).show();
                    editText_age.setError("Enter valid Age...");
                    editText_age.requestFocus();
                    editText_age.setFocusable(true);
                }else if(TextUtils.isEmpty(gender_str) ) {
                    Toast.makeText(getApplicationContext(), "Select valid gender...", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(flatno_str)) {
                    Toast.makeText(getApplicationContext(), "Enter valid Falt no...", Toast.LENGTH_SHORT).show();
                    editText_flatno.setError("Enter valid Flat no...");
                    editText_flatno.requestFocus();
                    editText_flatno.setFocusable(true);
                }else if(TextUtils.isEmpty(street_str)) {
                    Toast.makeText(getApplicationContext(), "Enter valid Street...", Toast.LENGTH_SHORT).show();
                    editText_street.setError("Enter valid Street...");
                    editText_street.requestFocus();
                    editText_street.setFocusable(true);
                }else if(TextUtils.isEmpty(place_str)) {
                    Toast.makeText(getApplicationContext(), "Enter valid Place...", Toast.LENGTH_SHORT).show();
                    editText_place.setError("Enter valid Place...");
                    editText_place.requestFocus();
                    editText_place.setFocusable(true);
                }else if(TextUtils.isEmpty(pincode_str) || pincode_str.length()!=6) {
                    Toast.makeText(getApplicationContext(), "Enter valid Pincode.Pincode length must be 6.", Toast.LENGTH_SHORT).show();
                    editText_pincode.setError("Enter valid Pincode...");
                    editText_pincode.requestFocus();
                    editText_pincode.setFocusable(true);
                }else if(arrayList_postoffice.size()==0) {
                    Toast.makeText(getApplicationContext(), "Enter valid Pincode.", Toast.LENGTH_SHORT).show();
                    editText_pincode.setError("Enter valid Pincode...");
                    editText_pincode.requestFocus();
                    editText_pincode.setFocusable(true);
                }else if(TextUtils.isEmpty(postoffice_str) || !(arrayList_postoffice.contains(postoffice_str))) {
                    Toast.makeText(getApplicationContext(), "Select your Post Office...", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(taluk_str)) {
                    Toast.makeText(getApplicationContext(), "Select your Post Office...", Toast.LENGTH_SHORT).show();
                }
                else{
                    district_str=editText_district.getText().toString();
                    state_str=editText_state.getText().toString();
                    country_str=editText_country.getText().toString();
                    progressDialog.setTitle("Updating your information");
                    progressDialog.setMessage("Please wait, while we are updating your information...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);

                    HashMap userMap = new HashMap();
                    userMap.put("Name", name_str);
                    userMap.put("Father_Name", father_name_str);
                    userMap.put("Age", age_str);
                    userMap.put("Gender",gender_str);
                    userMap.put("Flat_No",flatno_str);
                    userMap.put("Street",street_str);
                    userMap.put("Place",place_str);
                    userMap.put("Pincode",pincode_str);
                    userMap.put("Post_Office",postoffice_str);
                    userMap.put("Taluk",taluk_str);
                    userMap.put("District",district_str);
                    userMap.put("State",state_str);
                    userMap.put("Country",country_str);
                    UsersRef.child(aadhar_str).child("Personal_Info").updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                finish();
                                Toast.makeText(UserInfoActivity.this, "your information updated Successfully.", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(UserInfoActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    private void getAddressforPincode(CharSequence s) {
        String url="http://postalpincode.in/api/pincode/"+s;
        progressDialog.setTitle("Verifying Pincode");
        progressDialog.setMessage("Please wait while verifying your pincode...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("PostOffice");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject=jsonArray.getJSONObject(i);
                                String name=jsonObject.getString("Name");
                                String taluk=jsonObject.getString("Taluk");
                                String district=jsonObject.getString("District");
                                String state=jsonObject.getString("State");
                                String country=jsonObject.getString("Country");
                                arrayList_postoffice.add(name);
                                arrayList_taluk.add(taluk);
                                arrayList_district.add(district);
                                arrayList_state.add(state);
                                arrayList_country.add(country);
                            }
                            progressDialog.dismiss();
                            spinner_post_office.setEnabled(true);
                            arrayAdapter_postoffice=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_item,arrayList_postoffice);
                            arrayAdapter_postoffice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner_post_office.setAdapter(arrayAdapter_postoffice);
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"Enter valid Pincode",Toast.LENGTH_SHORT).show();
                            editText_pincode.setError("Enter valid Pincode...");
                            editText_pincode.requestFocus();
                            editText_pincode.setFocusable(true);
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error: "+error.getMessage().toString(),Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        postoffice_str=arrayList_postoffice.get(position).toString();
        editText_taluk.setText(arrayList_taluk.get(position));
        editText_district.setText(arrayList_district.get(position));
        editText_state.setText(arrayList_state.get(position));
        editText_country.setText(arrayList_country.get(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void SendUserToMainActivity()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("patient_info_app_data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("aadhar_no",aadhar_str);
        editor.putBoolean("exist_account",true);
        editor.putBoolean("user_personal_info_exist",true);
        editor.commit();

        Intent mainIntent = new Intent(UserInfoActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}