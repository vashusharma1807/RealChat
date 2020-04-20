package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView displayTextMessages;
    private ImageButton sendMessageButton;
    private ScrollView mScrollView;
    private EditText userMessageInput;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef , groupNameRef , groupMessageKeyRef;

    private String currentGroupName,currenUserId,currentUserName , currentDate , currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();

        mAuth=FirebaseAuth.getInstance();
        currenUserId=mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);



        initialise();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                saveMessageInfoToDatabase();

                userMessageInput.setText(null);

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessages = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            displayTextMessages.append(chatName+" :\n"+chatMessages+"\n"+chatDate+"   "+chatTime+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

    private void getUserInfo() {
        UsersRef.child(currenUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void initialise() {
        mToolbar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);


        sendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        displayTextMessages=(TextView) findViewById(R.id.group_chat_text_display);
        mScrollView=(ScrollView) findViewById(R.id.my_scroll_view);
        userMessageInput=(EditText) findViewById(R.id.input_group);
    }


    private void saveMessageInfoToDatabase() {

        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(GroupChatActivity.this, "Please Enter a message..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar ccalForDate= Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy ");
            currentDate=currentDateFormat.format(ccalForDate.getTime());

            Calendar ccalForTime= Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm:ss a");
            currentTime=currentTimeFormat.format(ccalForTime.getTime());

            HashMap<String , Object > groupMessageKey = new HashMap<>();
             groupNameRef.updateChildren(groupMessageKey);

             groupMessageKeyRef = groupNameRef.child(messageKey);

             HashMap<String ,Object > messageInfoMap = new HashMap<>();
             messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
             groupMessageKeyRef.updateChildren(messageInfoMap);
        }


    }
}
