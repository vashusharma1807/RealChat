package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TimerMessageActivity extends AppCompatActivity {




    private EditText title  , message , minDist ;
    private String date,time;
    private Button addWork,selectDate , selectTime ;
    private FirebaseUser currUser;
    private String recieverUser;
    private FirebaseAuth mAuth ;
    private DatabaseReference RootRef ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_message);

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        recieverUser = getIntent().getStringExtra("Receiver");


        date="00-00-0000";
        time = "00:00";

        initiate();

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeadlineClick();
            }
        });

        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTimeClick();
            }
        });


        addWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message= title.getText().toString();


                String deadlineV = date+" "+time;

                if(TextUtils.isEmpty(message))
                {
                    Toast.makeText(TimerMessageActivity.this, "Please Enter a message", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(deadlineV))
                {
                    Toast.makeText(TimerMessageActivity.this, "Please Enter Time and Date ", Toast.LENGTH_SHORT).show();
                }
                else {

                    String messageSenderRef ="Message/"+currUser.getUid()+"/"+recieverUser;
                    String messageRecieverRef ="Message/"+recieverUser+"/"+currUser.getUid();

                    DatabaseReference userMessageKeyRef = RootRef.child("Timer Message").child(currUser.getUid()).child(recieverUser).push();
                    String messagePushId= userMessageKeyRef.getKey();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message",message);
                    messageTextBody.put("type","text");
                    messageTextBody.put("from",currUser.getUid());
                    messageTextBody.put("to",recieverUser);
                    messageTextBody.put("messageID",messagePushId);
                    messageTextBody.put("time",time);
                    messageTextBody.put("date",date);


                    Map messageTextDetails = new HashMap();
                    messageTextDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                    messageTextDetails.put(messageRecieverRef+"/"+messagePushId,messageTextBody);

                    RootRef.updateChildren(messageTextDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(TimerMessageActivity.this, "Message Added", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(TimerMessageActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }


            }
        });



    }


    public void onDeadlineClick() {

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        date=(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void onTimeClick()
    {

        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        time=(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();

    }


    private void initiate() {
        title = (EditText) findViewById(R.id.edit_text1);
        addWork = (Button) findViewById(R.id.add_job);
        selectDate=(Button) findViewById(R.id.select_date);
        selectTime=(Button) findViewById(R.id.select_time);

    }

}