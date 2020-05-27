package com.frienders.main.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.frienders.main.activity.profile.NewSetting;
import com.frienders.main.activity.login.NewLoginActivity;
import com.frienders.main.config.UsersFirebaseFields;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.frienders.main.R;
import com.frienders.main.adapter.TabsAccessorAdapter;
import com.frienders.main.config.Configuration;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUi();
    }

    private void initializeUi()
    {
        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        viewPager = findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(tabsAccessorAdapter);
        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuthProvider.getCurrentUser();
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            verifyUserExistence();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(Configuration.default_channel_id, Configuration.default_channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(Configuration.default_channel_desc);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    private void verifyUserExistence()
    {
        final String currentUserId = FirebaseAuthProvider.getCurrentUserId();

        FirebasePaths.firebaseUserRef(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!(dataSnapshot.child(UsersFirebaseFields.name).exists()))
                {
                    FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists() && !dataSnapshot.hasChild(UsersFirebaseFields.device_token))
                            {
                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    final String deviceToken = task.getResult().getToken();
                                                    FirebasePaths.firebaseUserRef(currentUserId).child(UsersFirebaseFields.device_token)
                                                            .setValue(deviceToken)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        sendUserToSettingActivity();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }

                else
                {
                    FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists() && !dataSnapshot.hasChild(UsersFirebaseFields.device_token))
                            {
                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    final String deviceToken = task.getResult().getToken();
                                                    FirebasePaths.firebaseUserRef(currentUserId).child(UsersFirebaseFields.device_token)
                                                            .setValue(deviceToken)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {

                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });


    }

    @Override
    public void onBackPressed()
    {

    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, NewLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option)
        {
            FirebaseAuthProvider.logout();
            sendUserToLoginActivity();
        }

        if(item.getItemId() == R.id.main_settings_option)
        {
            sendUserToSettingActivity();
        }

        return true;
    }

    private void sendUserToSettingActivity()
    {
        Intent settingIntent = new Intent(MainActivity.this, NewSetting.class);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }

}
