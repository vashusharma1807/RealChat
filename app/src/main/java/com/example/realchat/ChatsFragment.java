package com.example.realchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView ;
    private RecyclerView chatsList ;
    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        PrivateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chat_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");



        return  PrivateChatsView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new  FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
            {

                final String userIds = getRef(position).getKey();
                final String[] userImage={"Default Image"};

                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            final String profileName = dataSnapshot.child("name").getValue().toString();
                            final String profileStatus = dataSnapshot.child("status").getValue().toString();
                            if(dataSnapshot.hasChild("image"))
                            {
                                 userImage[0] = dataSnapshot.child("image").getValue().toString();

                                holder.userName.setText(profileName);

                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            }
                            else
                            {

                                holder.userName.setText(profileName);

                                holder.profileImage.setImageResource(R.drawable.profile_image);
                            }
                            holder.userOnlineState.setVisibility(View.INVISIBLE);
                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online"))
                                {
                                    holder.userStatus.setText("Online");
                                    holder.userOnlineState.setVisibility(View.VISIBLE);

                                }
                                else
                                {
                                    holder.userOnlineState.setVisibility(View.INVISIBLE);
                                    holder.userStatus.setText("Last Seen: "+date+" "+time);
                                }
                            }
                            else
                            {
                                holder.userOnlineState.setVisibility(View.INVISIBLE);
                                holder.userStatus.setText("offline");
                            }



                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",userIds);
                                    chatIntent.putExtra("visit_user_name",profileName);
                                    chatIntent.putExtra("visit_user_image",userImage[0]);

                                    startActivity(chatIntent);
                                }
                            });
                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();

    }




    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        TextView userName , userStatus;
        CircleImageView profileImage ;
        ImageView userOnlineState;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userOnlineState=itemView.findViewById(R.id.user_online_status);
        }
    }
}
