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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference contactsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String userIds = getRef(position).getKey();

                        UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists())
                                {
                                    String userImage = null;
                                    if(dataSnapshot.hasChild("image"))
                                    {
                                        userImage = dataSnapshot.child("image").getValue().toString();
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(profileName);
                                        holder.userStatus.setText(profileStatus);

                                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                    }
                                    else
                                    {

                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(profileName);
                                        holder.userStatus.setText(profileStatus);
                                        holder.profileImage.setImageResource(R.drawable.profile_image);
                                    }

                                    final String finalUserImage = userImage;
                                    holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                            intent.putExtra("url", finalUserImage);
                                            holder.itemView.getContext().startActivity(intent);
                                        }
                                    });
                                    if(dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();


                                        if(state.equals("online"))
                                        {

                                            holder.userOnlineState.setVisibility(View.VISIBLE);

                                        }
                                        else
                                            holder.userOnlineState.setVisibility(View.INVISIBLE);

                                    }
                                    else
                                    {
                                        holder.userOnlineState.setVisibility(View.INVISIBLE);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
            myContactsList.setAdapter(adapter);
            adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName , userStatus;
        CircleImageView profileImage ;
        ImageView userOnlineState;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userOnlineState=itemView.findViewById(R.id.user_online_status);
        }
    }
}