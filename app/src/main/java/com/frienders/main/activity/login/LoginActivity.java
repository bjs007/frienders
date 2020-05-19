package com.frienders.main.activity.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.activity.profile.SettingActivity;
import com.frienders.main.config.UsersFirebaseFields;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.utility.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity
{

    private EditText userEmail, userPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeUi();
    }


    private void initializeUi()
    {
        Button loginButton = findViewById(R.id.login_button);
        Button phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        TextView needNewAccountLink = findViewById(R.id.need_new_account_link);
        progressDialog = new ProgressDialog(this);
        needNewAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                allowUsersToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserSettingActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, SettingActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(registerIntent);
    }

    private void allowUsersToLogin()
    {
        final String email = userEmail.getText().toString();
        final String password = userPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, getString(R.string.enteremailmessage), Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, getString(R.string.enterpasswordmessage), Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setTitle(getString(R.string.pleasewait));
            progressDialog.setCanceledOnTouchOutside(true);
            FirebaseAuthProvider.getFirebaseAuth().signInWithEmailAndPassword(email, password)
               .addOnCompleteListener(new OnCompleteListener<AuthResult>()
               {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task)
                   {
                       if(task.isSuccessful()){

                           FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId()).addValueEventListener(new ValueEventListener()
                           {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                               {
                                   if(!(dataSnapshot.child(UsersFirebaseFields.name).exists()))
                                   {
                                       sendUserSettingActivity();
                                   }
                                   else
                                   {
                                       sendUserToMainActivity();
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
                           final String message = task.getException().getMessage();
                           Toast.makeText(LoginActivity.this, getString(R.string.loginerror) + message, Toast.LENGTH_SHORT);
                       }
                       progressDialog.dismiss();
                   }
               });
        }
    }
}
