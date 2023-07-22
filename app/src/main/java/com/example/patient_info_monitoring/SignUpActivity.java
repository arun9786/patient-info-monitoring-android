package com.example.patient_info_monitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private EditText editText_Aadhar,editText_Phone,editText_Password,editText_Repassword,otp_edt;
    private CountryCodePicker countryCodePicker;
    private CheckBox checkBox_showpassword;
    private Button btn_signUp,verifyOTPBtn;
    private ProgressDialog loadingBar;

    private String Aadhar_str,Phone_str,Password_str,Repassword_str,Country_code_str;
    private String verificationId,Phone_no;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private LinearLayout linearLayout_signup_form,linearLayout_otp_form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editText_Aadhar=(EditText) findViewById(R.id.Signup_Aadhar_edt);
        editText_Phone=(EditText) findViewById(R.id.Signup_phone_edt);
        editText_Password=(EditText) findViewById(R.id.Signup_password_edt);
        editText_Repassword=(EditText) findViewById(R.id.Signup_re_password_edt);
        countryCodePicker=(CountryCodePicker) findViewById(R.id.countercodepicker_signup);
        checkBox_showpassword=(CheckBox) findViewById(R.id.show_password_signup_checkbox);
        otp_edt=(EditText)findViewById(R.id.Signup_otp_edt);
        btn_signUp=(Button) findViewById(R.id.signup_btn);
        verifyOTPBtn=(Button) findViewById(R.id.signup_otp_verify_btn);
        loadingBar = new ProgressDialog(this);

        linearLayout_signup_form=(LinearLayout)findViewById(R.id.linearlayout_signup_form_id);
        linearLayout_otp_form=(LinearLayout)findViewById(R.id.linearlayout_otp_verification);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Aadhar_str=editText_Aadhar.getText().toString().trim();
                Phone_str=editText_Phone.getText().toString().trim();
                Password_str=editText_Password.getText().toString().trim();
                Repassword_str=editText_Repassword.getText().toString().trim();
                Country_code_str=countryCodePicker.getFullNumberWithPlus();

                if(TextUtils.isEmpty(Aadhar_str) || Aadhar_str.length()!=12){
                    Toast.makeText(getApplicationContext(),"Enter valid Aadhar Number...",Toast.LENGTH_SHORT).show();
                    editText_Aadhar.setFocusable(true);
                    editText_Aadhar.requestFocus();
                    editText_Aadhar.setError("Enter valid Aadhar Number");
                }else if(TextUtils.isEmpty(Phone_str) || Phone_str.length()!=10){
                    Toast.makeText(getApplicationContext(),"Enter valid Phone Number...",Toast.LENGTH_SHORT).show();
                    editText_Phone.setFocusable(true);
                    editText_Phone.requestFocus();
                    editText_Phone.setError("Enter valid Phone Number");
                }else if(TextUtils.isEmpty(Password_str) || Password_str.length()<6){
                    Toast.makeText(getApplicationContext(),"Password length must be atleast 6 letters...",Toast.LENGTH_SHORT).show();
                    editText_Password.setFocusable(true);
                    editText_Password.requestFocus();
                    editText_Password.setError("Password length must be atleast 6 letters...");
                }else if(TextUtils.isEmpty(Repassword_str) || !Repassword_str.equals(Password_str)){
                    Toast.makeText(getApplicationContext(),"Passwords are not match...",Toast.LENGTH_SHORT).show();
                    editText_Repassword.setFocusable(true);
                    editText_Repassword.requestFocus();
                    editText_Repassword.setError("Passwords are not match...");
                } else{
                    UsersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(Aadhar_str)){
                                editText_Aadhar.setFocusable(true);
                                editText_Aadhar.requestFocus();
                                editText_Aadhar.setError("Your aadhar number already exists...");
                            }else{
                                Phone_no="+"+Country_code_str+Phone_str;
                                linearLayout_signup_form.setVisibility(View.GONE);
                                linearLayout_otp_form.setVisibility(View.VISIBLE);
                                sendVerificationCode(Phone_no);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(),"Error: "+error.getMessage().toString(),Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(otp_edt.getText().toString())) {
                    Toast.makeText(SignUpActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    verifyCode(otp_edt.getText().toString());
                }
            }
        });

        checkBox_showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox_showpassword.isChecked()){
                    checkBox_showpassword.setText("Hide password");
                    editText_Password.setTransformationMethod(null);
                    editText_Repassword.setTransformationMethod(null);
                }else{
                    checkBox_showpassword.setText("Show password");
                    editText_Password.setTransformationMethod(new PasswordTransformationMethod());
                    editText_Repassword.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.setTitle("Creating New Account");
                            loadingBar.setMessage("Please wait, while we are creating your new Account...");
                            loadingBar.show();
                            loadingBar.setCanceledOnTouchOutside(false);

                            String uid=mAuth.getCurrentUser().getUid();
                            HashMap userMap = new HashMap();
                            userMap.put("Aadhar", Aadhar_str);
                            userMap.put("Phone", Phone_no);
                            userMap.put("Password", Password_str);
                            userMap.put("Uid",uid);
                            UsersRef.child(Aadhar_str).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Your Account is created successfully.", Toast.LENGTH_LONG).show();
                                        SendUserInfoActivity();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SignUpActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otp_edt.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            linearLayout_otp_form.setVisibility(View.GONE);
            linearLayout_signup_form.setVisibility(View.VISIBLE);
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void SendUserInfoActivity()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("patient_info_app_data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("aadhar_no",Aadhar_str);
        editor.putBoolean("exist_account",true);
        editor.putBoolean("user_personal_info_exist",false);
        editor.commit();

        Intent mainIntent = new Intent(SignUpActivity.this, UserInfoActivity.class);
        mainIntent.putExtra("aadhar_no",Aadhar_str);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}