package com.example.patient_info_monitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText phoneNo_edt,Otp_edt,Aadhar_no_edt,new_password_edt,new_repassword_edt;
    private Button Verify_phone_btn,Verify_otp_btn,Save_change_btn;
    private ProgressDialog loadingBar;
    private CountryCodePicker countryCodePicker;
    private CheckBox show_password_checkbox;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private LinearLayout linearLayout_phone_form,linearLayout_otp_form,linearLayout_change_password_form;

    private String verificationId,Phone_no,Phone_str,Country_code_str,Aadhar_str,new_password_str,new_repassword_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        linearLayout_phone_form=(LinearLayout)findViewById(R.id.linearlayout_forgot_password_mobile_no_form);
        linearLayout_otp_form=(LinearLayout)findViewById(R.id.linearlayout_forgot_password_otp_form);
        linearLayout_change_password_form=(LinearLayout)findViewById(R.id.linearlayout_forgot_password_new_password_form);

        Verify_phone_btn=(Button)findViewById(R.id.forgot_password_btn);
        Verify_otp_btn=(Button)findViewById(R.id.forgot_password_otp_verify_btn);
        Save_change_btn=(Button)findViewById(R.id.forgot_password_new_password_change_btn);

        Aadhar_no_edt=(EditText)findViewById(R.id.Forgot_password_Aadhar_no_edt);
        phoneNo_edt=(EditText)findViewById(R.id.forgot_password_mobile_no_edt);
        Otp_edt=(EditText)findViewById(R.id.forgot_password_otp_edt);
        new_password_edt=(EditText)findViewById(R.id.forgot_password_new_password_edt);
        new_repassword_edt=(EditText)findViewById(R.id.forgot_password_new_repassword_edt);

        countryCodePicker=(CountryCodePicker) findViewById(R.id.countercodepicker_forgot_password);
        show_password_checkbox=(CheckBox)findViewById(R.id.show_password_forgot_password_checkbox);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Verify_phone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckExcistingAccount();
            }
        });

        Verify_otp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(Otp_edt.getText().toString())) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    verifyCode(Otp_edt.getText().toString());
                }
            }
        });

        Save_change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_password_str=new_password_edt.getText().toString().trim();
                new_repassword_str=new_repassword_edt.getText().toString().trim();
                if(TextUtils.isEmpty(new_password_str) || new_password_str.length()<6){
                    Toast.makeText(getApplicationContext(),"Password length must be atleast 6 letters...",Toast.LENGTH_SHORT).show();
                    new_password_edt.setFocusable(true);
                    new_password_edt.requestFocus();
                    new_password_edt.setError("Password length must be atleast 6 letters...");
                }else if(TextUtils.isEmpty(new_repassword_str) || !new_repassword_str.equals(new_password_str)){
                    Toast.makeText(getApplicationContext(),"Passwords are not match...",Toast.LENGTH_SHORT).show();
                    new_repassword_edt.setFocusable(true);
                    new_repassword_edt.requestFocus();
                    new_repassword_edt.setError("Passwords are not match...");
                }else{
                    loadingBar.setTitle("Updating Password");
                    loadingBar.setMessage("Please wait, while we are updating your account password...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);

                    HashMap userMap = new HashMap();
                    userMap.put("Password", new_password_str);
                    UsersRef.child(Aadhar_str).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                finish();
                                loadingBar.dismiss();
                                Toast.makeText(ForgotPasswordActivity.this, "Your new password updated successfully.", Toast.LENGTH_LONG).show();
                            } else {
                                loadingBar.dismiss();
                                String message = task.getException().getMessage();
                                Toast.makeText(ForgotPasswordActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        show_password_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(show_password_checkbox.isChecked()){
                    show_password_checkbox.setText("Hide password");
                    new_password_edt.setTransformationMethod(null);
                    new_repassword_edt.setTransformationMethod(null);
                }else{
                    show_password_checkbox.setText("Show password");
                    new_password_edt.setTransformationMethod(new PasswordTransformationMethod());
                    new_repassword_edt.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
    }

    private void CheckExcistingAccount() {
        Aadhar_str=Aadhar_no_edt.getText().toString().trim();
        Phone_str=phoneNo_edt.getText().toString().trim();
        Country_code_str=countryCodePicker.getFullNumberWithPlus();
        if(TextUtils.isEmpty(Aadhar_str) || Aadhar_str.length()!=12){
            Toast.makeText(getApplicationContext(),"Enter valid Aadhar Number...",Toast.LENGTH_SHORT).show();
            Aadhar_no_edt.setFocusable(true);
            Aadhar_no_edt.requestFocus();
            Aadhar_no_edt.setError("Enter valid Aadhar Number");
        }else if(TextUtils.isEmpty(Phone_str) || Phone_str.length()!=10){
            Toast.makeText(getApplicationContext(),"Enter valid Phone Number...",Toast.LENGTH_SHORT).show();
            phoneNo_edt.setFocusable(true);
            phoneNo_edt.requestFocus();
            phoneNo_edt.setError("Enter valid Phone Number");
        }else{
            Phone_no="+"+Country_code_str+Phone_str;
            loadingBar.setTitle("Checking Your Account...");
            loadingBar.setMessage("Please wait, while we are checking your Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(Aadhar_str)){
                        UsersRef.child(Aadhar_str).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String phone=snapshot.child("Phone").getValue().toString();
                                if(phone.equalsIgnoreCase(Phone_no)){
                                    linearLayout_phone_form.setVisibility(View.GONE);
                                    linearLayout_change_password_form.setVisibility(View.GONE);
                                    linearLayout_otp_form.setVisibility(View.VISIBLE);
                                    sendVerificationCode(Phone_no);
                                    loadingBar.dismiss();
                                }else{
                                    Toast.makeText(getApplicationContext(), "You mobile number is wrong. Please enter your valid mobile number combine with this aadhar no. Otherwise create new account.", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        });
                    }else{
                        Toast.makeText(getApplicationContext(), "You are not existing user. Please check your Aadhar no. Otherwise create new account.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
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
                Otp_edt.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            linearLayout_otp_form.setVisibility(View.GONE);
            linearLayout_change_password_form.setVisibility(View.GONE);
            linearLayout_phone_form.setVisibility(View.VISIBLE);
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            linearLayout_otp_form.setVisibility(View.GONE);
                            linearLayout_change_password_form.setVisibility(View.VISIBLE);
                            linearLayout_phone_form.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}