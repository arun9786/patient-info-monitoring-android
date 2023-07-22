package com.example.patient_info_monitoring;

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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity
{
    private EditText UserAadhar, UserPassword, UserConfirmPassword;
    private Button SignInButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private LinearLayout linearLayout_register;
    private RadioButton radioButton_create_account;
    private CheckBox show_password_checkbox;
    private TextView textView_forgot_password;

    String aadhar,password;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getCurrentLocation();


        linearLayout_register=(LinearLayout)findViewById(R.id.linearlayout_register_next_page);
        radioButton_create_account=(RadioButton)findViewById(R.id.radiobutton_register_create_account);
        show_password_checkbox=(CheckBox)findViewById(R.id.show_password_signin_checkbox);
        textView_forgot_password=(TextView)findViewById(R.id.textView_forgot_password_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        UserAadhar = (EditText) findViewById(R.id.signin_aadhar);
        UserPassword = (EditText) findViewById(R.id.register_password);
        SignInButton = (Button) findViewById(R.id.register_create_account);
        loadingBar = new ProgressDialog(this);

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CheckExcistingAccount();
            }
        });

        radioButton_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setupIntent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(setupIntent);
            }
        });

        linearLayout_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButton_create_account.setChecked(true);
                Intent setupIntent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(setupIntent);
            }
        });

        show_password_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(show_password_checkbox.isChecked()){
                    show_password_checkbox.setText("Hide password");
                    UserPassword.setTransformationMethod(null);
                }else{
                    show_password_checkbox.setText("Show password");
                    UserPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        textView_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LogInActivity.this,ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        radioButton_create_account.setChecked(false);
    }

    private void CheckExcistingAccount()
    {
        aadhar = UserAadhar.getText().toString().trim();
        password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(aadhar) || aadhar.length()!=12) {
            Toast.makeText(this, "Enter valid Aadhar number...", Toast.LENGTH_SHORT).show();
            UserAadhar.setError("Enter valid Aadhar number...");
            UserAadhar.requestFocus();
            UserAadhar.setFocusable(true);
        }
        else if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
            UserPassword.setError("Enter valid Password ...");
            UserPassword.setFocusable(true);
            UserPassword.requestFocus();
        }
        else {
            loadingBar.setTitle("Verifying Account");
            loadingBar.setMessage("Please wait, while we are verifying your new Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(aadhar)){
                        String pass_cld=snapshot.child(aadhar).child("Password").getValue().toString();
                        if(pass_cld.equals(password)){
                            loadingBar.dismiss();
                            Toast.makeText(getApplicationContext(),"Welcome to Mr.Patient!",Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }else{
                            Toast.makeText(getApplicationContext(),"Aadhar number or password was wrong!",Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(),"Are you new user?. Then create a new Account.",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Aadhar number or password was wrong!",Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),"Are you new user?. Then create a new Account.",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingBar.dismiss();
                    Toast.makeText(getApplicationContext(),"Error: "+error.getMessage().toString()+" Try Again!",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void SendUserToMainActivity()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("patient_info_app_data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("aadhar_no",aadhar);
        editor.putBoolean("exist_account",true);
        editor.commit();

        Intent mainIntent = new Intent(LogInActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
