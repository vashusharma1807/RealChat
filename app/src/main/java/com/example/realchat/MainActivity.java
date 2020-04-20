package com.example.realchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdaptor myTabsAccessorAdaptor;

    private FirebaseAuth mAuth ;
    private DatabaseReference Rootref ;
    private String currentUserID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();


        Rootref=FirebaseDatabase.getInstance().getReference();

        mtoolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("  Chat Point");

        getSupportActionBar().setIcon(R.drawable.weixing);

        myViewPager=(ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdaptor=new TabsAccessorAdaptor(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdaptor);

        myTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null) {
            sendUserToLoginActivity();

        }
            else
        {
            updateUserStatus("online");
            verifyUserExistance();
        }
        Toast.makeText(this, "Onstart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
        Toast.makeText(this, "Onstop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
        Toast.makeText(this, "OnDestroy", Toast.LENGTH_SHORT).show();
    }

    private void verifyUserExistance() {
        String currendUserID= mAuth.getCurrentUser().getUid();
        Rootref.child("Users").child(currendUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {

                    //Toast.makeText(MainActivity.this,"Welcome"+dataSnapshot.child("name").toString(),Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginActivity = new Intent(MainActivity.this,LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(loginActivity);
        finish();
    }
    private void sendUserToSettingsActivity() {
        Intent settingsActivity = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsActivity);
    }
    private void sendUserToFindFriendsActivity()
    {
        Intent loginActivity = new Intent(MainActivity.this,FindFriendsActivity.class);

        startActivity(loginActivity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
          if(item.getItemId()==R.id.main_logout_option) {
              updateUserStatus("Offline");
              mAuth.signOut();
                sendUserToLoginActivity();
          }
        if(item.getItemId()==R.id.main_settings_option) {

            //updateUserStatus("online");
            sendUserToSettingsActivity();
        }

        if(item.getItemId()==R.id.main_create_group_option)
        {
            RequestNewGroup();
        }

        if(item.getItemId()==R.id.main_find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }


        return true;
    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this , R.style.AlertDialog);
        builder.setTitle("Write Group Name:");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g. Friends");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this,"Please Write a group Name",Toast.LENGTH_SHORT).show();
                }
                else{
                    createNewGroup(groupName);

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

            }
        });

        builder.show();

    }

    private void createNewGroup(final String groupName) {

        Rootref.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete())
                {
                    Toast.makeText(MainActivity.this,groupName+" is Created Successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserStatus (String state)
    {
        String saveCurrentTime , saveCurrentDate ;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String , Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID = mAuth.getCurrentUser().getUid();

        Rootref.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }











}
