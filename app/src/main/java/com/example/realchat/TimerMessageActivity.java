package com.example.realchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimerMessageActivity extends AppCompatActivity {




    private EditText title  , message , minDist ;
    private String date,time;
    private Button addWork,selectDate , selectTime ;
    private FirebaseUser currUser;
    private String recieverUser , messagePushId;
    private FirebaseAuth mAuth ;
    private DatabaseReference RootRef ;
    private Toolbar timerToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_message);

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        recieverUser = getIntent().getStringExtra("Receiver");


        date="00-00-0000";
        time = "00:00:00";

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

                    String messageSenderRef ="Timer Message/"+currUser.getUid()+"/"+recieverUser;
                    String messageRecieverRef ="Timer Message/"+recieverUser+"/"+currUser.getUid();

                    DatabaseReference userMessageKeyRef = RootRef.child("Timer Message").child(currUser.getUid()).child(recieverUser).push();
                    messagePushId= userMessageKeyRef.getKey();



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
                                addAlarm();
                                //Intent intent = new Intent(TimerMessageActivity.this , ChatActivity.class);
                                //startActivity(intent);
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

    private void addAlarm() {

        Toast.makeText(this, "Alarm Added", Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        String saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm aa");
        String saveCurrentTime=currentTime.format(calendar.getTime());


        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        long diff=0;
        String k = saveCurrentTime.substring(saveCurrentTime.length() - 2);
        if(k.equals("pm"))
        {
            diff = -43200000;

        }

        Date d1 = null;
        Date d2 = null;

        try {
            d1=format.parse(date+" "+time);
            d2=format.parse(saveCurrentDate+" "+saveCurrentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        diff += (d1.getTime() - d2.getTime());

        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.putExtra("Sender",currUser.getUid());
        intent.putExtra("Receiver",recieverUser);
        intent.putExtra("Push Id",messagePushId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + diff, pendingIntent);
        Toast.makeText(this, "Message set in " + diff/1000 + " seconds",Toast.LENGTH_LONG).show();



        //PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        Toast.makeText(this, "Alarm will ring after every 15 minutes interval",Toast.LENGTH_LONG).show();

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

        timerToolBar = (Toolbar) findViewById(R.id.timer_message_bar);
        setSupportActionBar(timerToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Timer Messages");

    }

}