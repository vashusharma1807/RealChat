package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    
    private String recieverUserId, senderUserID , current_stat ;
    private CircleImageView userProfileImage ;
    private TextView userProfilename , userProfileStatus;
    private Button SendMessageRequestButton  ,DeclineMessageRequestButton;

    private FirebaseAuth mAuth ;
    private String userImage;

    private DatabaseReference UserRef , ChatRequestRef , ContactsRef , NotificationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        
        recieverUserId=getIntent().getExtras().get("visit_user_id").toString();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        //Toast.makeText(ProfileActivity.this,"User Id"+ recieverUserId , Toast.LENGTH_SHORT).show();

        userProfilename=(TextView) findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView) findViewById(R.id.visit_user_status);
        userProfileImage=(CircleImageView) findViewById(R.id.visit_profile_image);
        SendMessageRequestButton=(Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=(Button) findViewById(R.id.decline_message_request_button);

        current_stat = "new";
        senderUserID= mAuth.getCurrentUser().getUid();

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this , ImageViewerActivity.class);
                intent.putExtra("url",userImage);
                startActivity(intent);
            }
        });


    }

    private void RetrieveUserInfo() {

        UserRef.child(recieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    userImage = dataSnapshot.child("image").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String username = dataSnapshot.child("name").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfilename.setText(username);
                    userProfileStatus.setText(userStatus);


                    ManageChatRequests();
                }
                else
                {

                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String username = dataSnapshot.child("name").getValue().toString();


                    userProfilename.setText(username);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

/*
    private void ManageChatRequests() {



    }*/

    private void ManageChatRequests() {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(recieverUserId)) {
                            String requestType = dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();
                            if (requestType.equals("sent")) {
                                current_stat = "request_sent";
                                SendMessageRequestButton.setText("Cancel Request");
                            } else if (requestType.equals("recieved")) {
                                current_stat = "request_recieved";
                                SendMessageRequestButton.setText("Accept Chat Request");
                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);
                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });


                            }
                        } else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(recieverUserId)) {
                                                current_stat = "friends";
                                                SendMessageRequestButton.setText("Remove This Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(recieverUserId)) {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessageRequestButton.setEnabled(false);
                    if (current_stat.equals("new")) {
                        SendChatRequest();
                    }
                    if (current_stat.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (current_stat.equals("request_recieved")) {
                        AcceptChatRequest();
                    }
                    if (current_stat.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }


                }
            });
        } else {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }
    private void RemoveSpecificContact()
    {
        ContactsRef.child(senderUserID).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(recieverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                current_stat = "new";
                                                SendMessageRequestButton.setText("Send Message Request");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void AcceptChatRequest() {

        ContactsRef.child(senderUserID).child(recieverUserId)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContactsRef.child(senderUserID).child(recieverUserId)
                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ChatRequestRef.child(senderUserID).child(recieverUserId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    ContactsRef.child(senderUserID).child(recieverUserId)
                                                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                ChatRequestRef.child(senderUserID).child(recieverUserId).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    SendMessageRequestButton.setEnabled(true);
                                                                                    current_stat = "friends";
                                                                                    SendMessageRequestButton.setText("Remove this Contact");
                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            }
                                                                        });


                                                            }
                                                        }
                                                    });

                                                }

                                            }
                                        });
                            }

                        }
                    });
                }
            }
        });
    }


    private void CancelChatRequest() {

        ChatRequestRef.child(senderUserID).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            ChatRequestRef.child(recieverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            SendMessageRequestButton.setEnabled(true);
                                            current_stat="new";
                                            SendMessageRequestButton.setText("Send Message Request");


                                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });




    }



    public void SendChatRequest()
    {

        ChatRequestRef.child(senderUserID).child(recieverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(recieverUserId).child(senderUserID).child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        HashMap<String,String > chatNotificationName = new HashMap<>();
                                        chatNotificationName.put("from",senderUserID);
                                        chatNotificationName.put("type","request");


                                        NotificationRef.child(recieverUserId).push().setValue(chatNotificationName)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            SendMessageRequestButton.setEnabled(true);
                                                            current_stat="request_sent";
                                                            SendMessageRequestButton.setText("Cancel Chat Request");
                                                        }
                                                    }
                                                });

                                        SendMessageRequestButton.setEnabled(true);
                                        current_stat="request_sent";
                                        SendMessageRequestButton.setText("Cancel Request");
                                    }
                                }
                            });
                        }
                    }
                });
    }
}
