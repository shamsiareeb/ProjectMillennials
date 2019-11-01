package com.shayannasir.projectm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
//import android.support.v7.widget.Toolbar;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUSerName, currentDate, currentTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, "Welcome to " + currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        GetUSerInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
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

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        SendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
        userMessageInput = (EditText)findViewById(R.id.input_group_message);
        mScrollView = (ScrollView)findViewById(R.id.my_scroll_view);
        displayTextMessage = (TextView)findViewById(R.id.group_chat_display);

    }

    private void GetUSerInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUSerName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageToDatabase() {
        String message = userMessageInput.getText().toString();
        String messagekey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message))
            Toast.makeText(this, "Please write a message first", Toast.LENGTH_SHORT).show();
        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, YYYY");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messagekey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUSerName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);


        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName + " -- " + chatMessage + "    " + chatTime + " " + chatDate +"\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }



}
