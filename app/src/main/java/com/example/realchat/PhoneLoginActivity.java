package com.example.realchat;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;



public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton,verifyButton;
    private EditText inputPhoneNumber , verificationCode;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationCodeButton=(Button) findViewById(R.id.send_verification_code);
        verifyButton=(Button) findViewById(R.id.verify_button);
        inputPhoneNumber=(EditText) findViewById(R.id.phone_number_input);
        verificationCode=(EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(PhoneLoginActivity.this);



        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = inputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write a Phone number First ...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("User Verification");
                    loadingBar.setMessage("Please Wait...while we are authenticating");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    try {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber
                                , 60,
                                TimeUnit.SECONDS,
                                PhoneLoginActivity.this,
                                callbacks);
                    } catch (Exception e) {

                        Toast.makeText(PhoneLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(PhoneLoginActivity.this, "Phone Verified", Toast.LENGTH_SHORT).show();


                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String Code = verificationCode.getText().toString();
                if (TextUtils.isEmpty(Code)) {
                    Toast.makeText(PhoneLoginActivity.this, "Provide a code", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please Wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, Code);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted ( PhoneAuthCredential phoneAuthCredential)
            {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed ( FirebaseException e)
            {
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                verificationCode.setVisibility(View.INVISIBLE);

                Toast.makeText(PhoneLoginActivity.this, "Verification Failed...Please Provide correct Info\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }


            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                verificationCode.setVisibility(View.VISIBLE);


                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Code is sent to phone number", Toast.LENGTH_SHORT).show();
            }



        };


    }




    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : "  +  message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


}
