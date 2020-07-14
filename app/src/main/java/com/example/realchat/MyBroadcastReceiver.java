package com.example.realchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MyBroadcastReceiver  extends BroadcastReceiver {
    MediaPlayer mp;
    String recieverUser,senderUser,pushId;
    private DatabaseReference RootRef;
    private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher  ;
    private SecretKeySpec secretKeySpec ;



    @Override
    public void onReceive(Context context, Intent intent) {

        RootRef = FirebaseDatabase.getInstance().getReference();

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey , "AES");


        mp = MediaPlayer.create(context, R.raw.diamond);
        mp.start();
        Toast.makeText(context, "Alarm....", Toast.LENGTH_LONG).show();

            recieverUser = intent.getStringExtra("Receiver");
            senderUser = intent.getStringExtra("Sender");
            pushId = intent.getStringExtra("Push Id");

            retrieveMessages(context);
    }


    private void retrieveMessages(final Context context) {

        Toast.makeText(context, pushId, Toast.LENGTH_SHORT).show();

        RootRef.child("Timer Message").child(senderUser).child(recieverUser)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        TimerMessages messages = dataSnapshot.getValue(TimerMessages.class);

                        //Toast.makeText(context, messages.getMessage(), Toast.LENGTH_SHORT).show();

                        if(messages.getMessageID().equals(pushId))
                        {
                            Toast.makeText(context, messages.getMessageID(), Toast.LENGTH_SHORT).show();
                            SendMessage(messages);
                        }


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


    private void SendMessage(TimerMessages messages)
    {
        String messageText = messages.getMessage();


        if(!TextUtils.isEmpty(messageText))
        {
            String messageSenderRef ="Message/"+messages.getFrom()+"/"+messages.getTo();
            String messageRecieverRef ="Message/"+messages.getTo()+"/"+messages.getFrom();

            DatabaseReference userMessageKeyRef=RootRef.child("Message").child(messages.getFrom()).child(messages.getTo()).push();

            String messagePushId= messages.getMessageID();

            messageText = AESEncryptionMethod(messages.getMessage());


            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messages.getFrom());
            messageTextBody.put("to",messages.getTo());
            messageTextBody.put("messageID",messages.getMessageID());
            messageTextBody.put("time",messages.getTime());
            messageTextBody.put("date",messages.getDate());



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
                        //Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private String AESEncryptionMethod(String string) {

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
            //Toast.makeText(this, returnString, Toast.LENGTH_SHORT).show();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

}
