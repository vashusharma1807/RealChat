package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;
    private TextView userName, userLastSeen;
    String saveCurrentTime , saveCurrentDate ;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private StorageTask uploadTask;
    private String checker = "",myUrl="";
    private Uri fileUri ;

    private String currentUserID ;

    private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher  ;
    private SecretKeySpec secretKeySpec ;

    private String password = "vashu";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();


        IntializeControllers();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        userName.setText(messageReceiverName);

        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"

                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if (i == 1) {

                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);


                        }
                        if (i == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);

                        }

                    }
                });
                builder.show();
            }
        });

    }

    private void IntializeControllers() {

        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_layout, null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);



        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        loadingBar = new ProgressDialog(this);


        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey , "AES");


    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Message").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        /*updateUserStatus("online");*/

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Work in Progress...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri=data.getData();

            if(!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderRef ="Message/"+messageSenderId+"/"+messageReceiverId;
                final String messageRecieverRef ="Message/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef=RootRef.child("Message").child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId= userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+"."+checker);

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageID",messagePushId);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);



                            Map messageTextDetails = new HashMap();
                            messageTextDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageTextDetails.put(messageRecieverRef+"/"+messagePushId,messageTextBody);

                            RootRef.updateChildren(messageTextDetails);
                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                         double p = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                         loadingBar.setMessage((int) p+ "% Uploading");
                    }
                });
            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef ="Message/"+messageSenderId+"/"+messageReceiverId;
                final String messageRecieverRef ="Message/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef=RootRef.child("Message").child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId= userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+".jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageID",messagePushId);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);



                            Map messageTextDetails = new HashMap();
                            messageTextDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageTextDetails.put(messageRecieverRef+"/"+messagePushId,messageTextBody);

                            RootRef.updateChildren(messageTextDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        //Toast.makeText(ChatActivity.this, "Image Sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });

                        }
                    }
                });

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p+ "% Uploading");
                    }
                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "E:Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("userState").hasChild("state"))
                {
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online"))
                    {
                        userLastSeen.setText("Online");
                    }
                    else
                    {
                        userLastSeen.setText("Last Seen: "+date+" "+time);
                    }
                }
                else
                {
                    userLastSeen.setText("offline");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();


        if(!TextUtils.isEmpty(messageText))
        {
            String messageSenderRef ="Message/"+messageSenderId+"/"+messageReceiverId;
            String messageRecieverRef ="Message/"+messageReceiverId+"/"+messageSenderId;

            DatabaseReference userMessageKeyRef=RootRef.child("Message").child(messageSenderId).child(messageReceiverId).push();

            String messagePushId= userMessageKeyRef.getKey();


            messageText = AESEncryptionMethod(messageText);


            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageID",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);



            Map messageTextDetails = new HashMap();
            messageTextDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageTextDetails.put(messageRecieverRef+"/"+messagePushId,messageTextBody);

            RootRef.updateChildren(messageTextDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {

                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }


    private String AESEncryptionMethod(String string){

        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;

        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
            Toast.makeText(this, returnString, Toast.LENGTH_SHORT).show();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_option,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.chat_info_option) {
            userProfile();
        }
        if(item.getItemId()==R.id.chat_clearChat_option) {
            ClearChat();
        }

        if(item.getItemId()==R.id.chat_timer_option)
        {
            AddTimerMessage();
        }


        return true;
    }

    private void ClearChat() {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(messageSenderId)
                .child(messageReceiverId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(ChatActivity.this, "Chat Cleared", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Could not delete messages", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void userProfile() {

        Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
        profileIntent.putExtra("visit_user_id",messageReceiverId);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(profileIntent);
    }

    private void AddTimerMessage() {
        Intent intent = new Intent(ChatActivity.this,TimerMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(intent);
    }



}
