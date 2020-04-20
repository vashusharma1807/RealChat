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
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private ProgressDialog loadingBar;
    private DatabaseReference Rootref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Rootref= FirebaseDatabase.getInstance().getReference();

        Initialize();
    }
    private void Initialize() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);
        loadingBar=new ProgressDialog(this);
    }

    public void SendToLoginActivity(View view) {
        SendToLoginActivityFun();
    }

    public void CreateAccount(View view) {
        CreateNewAccount();

    }
    private void CreateNewAccount()
    {
        //Toast.makeText(this,"Creating button",Toast.LENGTH_LONG).show();
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please Enter an Email Id",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please Enter Password",Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.setCancelable(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();


                        String currentUserId = mAuth.getCurrentUser().getUid();
                        Rootref.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);

                        Rootref.child("Users").child(currentUserId).setValue("");

                        SendToMainActivityFun();
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully :-)" ,Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else {
                        String error = task.getException().toString();
                        Toast.makeText( RegisterActivity.this, "Error:"+error, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            } );
        }
    }

    private void SendToLoginActivityFun() {
        Intent loginActivity = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginActivity);

    }
    private void SendToMainActivityFun() {
        Intent mainActivity = new Intent(RegisterActivity.this,MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(mainActivity);
        finish();

    }
}
