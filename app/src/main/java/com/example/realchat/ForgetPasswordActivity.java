package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText forgetEmail ;
    private Button resetPassword;
    private FirebaseDatabase rootRef;
    private FirebaseAuth mAuth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        forgetEmail=(EditText) findViewById(R.id.forget_email);
        resetPassword=(Button) findViewById(R.id.reset_password);

        mAuth=FirebaseAuth.getInstance();



        final String email = forgetEmail.getText().toString();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.isEmpty())
                {
                       mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful())
                               {
                                   String msg = task.getException().toString();
                                   Toast.makeText(ForgetPasswordActivity.this,"Error:"+msg , Toast.LENGTH_SHORT).show();
                               }
                               else
                               {
                                   Toast.makeText(ForgetPasswordActivity.this, "Reset Password Sent to your Email", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                }
            }
        });
    }
}
