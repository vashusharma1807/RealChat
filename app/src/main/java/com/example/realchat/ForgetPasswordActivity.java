package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText forgetEmail;
    private Button resetPassword;
    private FirebaseDatabase rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        //Toast.makeText(this, "Forget Password", Toast.LENGTH_SHORT).show();

        forgetEmail = (EditText) findViewById(R.id.forget_email);
        resetPassword = (Button) findViewById(R.id.reset_password);

        mAuth = FirebaseAuth.getInstance();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendForgetLink();
            }
        });
    }

    private void sendForgetLink() {

        final String email = forgetEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter an Email Id", Toast.LENGTH_LONG).show();
        }
        else
        {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        sendToMainActivityFun();
                        Toast.makeText(ForgetPasswordActivity.this, "Password Link Sent", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String msg = task.getException().toString();
                        Toast.makeText(ForgetPasswordActivity.this, "Error:"+msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendToMainActivityFun() {
        Intent mainActivity = new Intent(ForgetPasswordActivity.this,MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(mainActivity);
        finish();
    }


}

