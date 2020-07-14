package com.example.realchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef , Rootref;


    private byte encryptionKey[] = {9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    private Cipher decipher ;
    private SecretKeySpec secretKeySpec ;

    private String password = "vashu";


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList=userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView encryptionInfo , senderMessageText,recieverMessageText , senderMessageTime , receiverMessageTime , senderMessageImageTime , receiverMessageImageTime;
        public CircleImageView recieverProfileImage;

        public ImageView messageSenderPicture ,messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText=(TextView) itemView.findViewById(R.id.sender_message_text);
            recieverMessageText=(TextView) itemView.findViewById(R.id.receiver_message_text);
            recieverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture=(ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture=(ImageView) itemView.findViewById(R.id.message_sender_image_view);
            receiverMessageTime=(TextView) itemView.findViewById(R.id.receiver_message_text_time);

            senderMessageTime=(TextView) itemView.findViewById(R.id.sender_message_text_time);

            receiverMessageImageTime=(TextView) itemView.findViewById(R.id.receiver_message_image_time);

            senderMessageImageTime=(TextView) itemView.findViewById(R.id.sender_message_image_time);

            try {
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }

            secretKeySpec = new SecretKeySpec(encryptionKey , "AES");

        }


    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);

        mAuth=FirebaseAuth.getInstance();
        Rootref=FirebaseDatabase.getInstance().getReference();

        return new MessageViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.recieverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        holder.recieverMessageText.setVisibility(View.GONE);
        holder.recieverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.senderMessageTime.setVisibility(View.GONE);
        holder.receiverMessageTime.setVisibility(View.GONE);
        holder.senderMessageImageTime.setVisibility(View.GONE);
        holder.receiverMessageImageTime.setVisibility(View.GONE);




        if(fromMessageType.equals("text"))
        {

            if(fromUserID.equals(messageSenderID))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageTime.setVisibility(View.VISIBLE);


                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);

                String messageText = messages.getMessage() ;
                try {

                    messageText = AESDecryptionMethod(messageText);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }


                holder.senderMessageText.setText(messageText);
                holder.senderMessageTime.setText(messages.getTime());

            }
            else
            {

                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageTime.setVisibility(View.VISIBLE);


                holder.recieverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.recieverMessageText.setTextColor(Color.BLACK);


                String messageText = messages.getMessage() ;
                try {
                    messageText = AESDecryptionMethod(messageText);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                holder.recieverMessageText.setText(messageText);
                holder.receiverMessageTime.setText(messages.getTime());


            }

        }
        else if(fromMessageType.equals("image"))
        {

            if(fromUserID.equals(messageSenderID))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.senderMessageImageTime.setVisibility(View.VISIBLE);
                holder.senderMessageImageTime.setText(messages.getTime());

                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.file).into(holder.messageSenderPicture);
            }
            else
            {


                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageImageTime.setVisibility(View.VISIBLE);
                holder.receiverMessageImageTime.setText(messages.getTime());


                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);

            }

        }
        else if(fromMessageType.equals("pdf") ||fromMessageType.equals("docx") )
        {
            if(fromUserID.equals(messageSenderID))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                holder.senderMessageImageTime.setVisibility(View.VISIBLE);
                holder.senderMessageImageTime.setText(messages.getTime());



                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/realchat-54390.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=27c3b053-10e0-4474-b9b8-2bb2b380740b")
                        .into(holder.messageSenderPicture);


            }
            else
            {
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                holder.receiverMessageImageTime.setVisibility(View.VISIBLE);
                holder.receiverMessageImageTime.setText(messages.getTime());


                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/realchat-54390.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=27c3b053-10e0-4474-b9b8-2bb2b380740b").into(holder.messageReceiverPicture);


            }
        }

        if(fromUserID.equals(messageSenderID))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                  "Delete For me ",
                                  "Download and View This Document",
                                        "Cancel",
                                  "Delete For Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                               if(i==0)
                               {
                                   deleteSentMessage(position,holder);
                                   Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                   holder.itemView.getContext().startActivity(intent);
                               }
                               else if(i==1)
                               {

                                   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);
                                    Context c = holder.itemView.getContext();
                                   ((ChatActivity)c).finish();
                               }

                               else if(i==3)
                               {
                                   deleteMessageForEveryone(position,holder);
                                   Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                   holder.itemView.getContext().startActivity(intent);
                               }

                            }
                        });
                        builder.show();
                    }


                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me ",
                                        "View This Image",
                                        "Cancel",
                                        "Delete For Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i==0)
                                {
                                    deleteSentMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );

                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1)
                                {

                                    Intent intent = new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                    Context c = holder.itemView.getContext();
                                    ((ChatActivity)c).finish();

                                }

                                else if(i==3)
                                {
                                    deleteMessageForEveryone(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );

                                    holder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });
                        builder.show();
                    }
                }
            });
        }




        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me ",
                                        "Download and View This Document",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i==0)
                                {
                                    view.setVisibility(View.INVISIBLE);
                                    deleteRecievedMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);


                                }
                                else if(i==1)
                                {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);
                                    Context c = holder.itemView.getContext();
                                    ((ChatActivity)c).finish();

                                }


                            }
                        });
                        builder.show();
                    }


                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me ",
                                        "View This Image",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i==0)
                                {
                                    deleteRecievedMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1)
                                {
                                    Intent intent = new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                    Context c = holder.itemView.getContext();
                                    ((ChatActivity)c).finish();

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

        if(fromUserID.equals(messageSenderID))
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me ",
                                        "Cancel",
                                        "Delete For Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i==0)
                                {
                                    deleteSentMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if(i==2)
                                {
                                    deleteMessageForEveryone(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);


                                }


                            }
                        });
                        builder.show();
                    }

                    return true ;
                }
            });
        }
        else
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me ",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    userMessagesList.get(position).setMessage("");
                                    deleteRecievedMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    holder.itemView.getContext().startActivity(intent);

                                }


                            }
                        });
                        builder.show();
                    }
                    return true ;
                }
        });
    }
    }

    private String AESDecryptionMethod(String message) throws UnsupportedEncodingException {

        byte[] EncryptedByte = message.getBytes("ISO-8859-1");


        String decryptedString = message ;

        byte[] decryption ;

        try {
            decipher.init(Cipher.DECRYPT_MODE , secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        return decryptedString;
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
             if(task.isSuccessful())
             {
                 Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
             }
             else
             {
                 Toast.makeText(holder.itemView.getContext(), "Could not delete message", Toast.LENGTH_SHORT).show();

             }
            }
        });
    }
    private void deleteRecievedMessage(final int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Could not delete message", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position , final MessageViewHolder holder)
    {
         final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootRef.child("Message").child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted for Everyone", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Could not delete message", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }


}
