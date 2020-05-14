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
import android.widget.Toast;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.config.ActivityParameters;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class NewLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton;
    private EditText inputPhoneNumber;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String verificationId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);
        initializeUiAndCallBacks();
    }

    private void initializeUiAndCallBacks()
    {
        sendVerificationCodeButton = findViewById(R.id.send_verification_code);
        inputPhoneNumber = findViewById(R.id.phone_number_input_new_login);
        sendVerificationCodeButton.setVisibility(View.VISIBLE);
        progressDialog = new ProgressDialog(this);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = inputPhoneNumber.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(NewLoginActivity.this, getString(R.string.phonenumberrequired), Toast.LENGTH_SHORT).show();
                }

                else if(inputPhoneNumber.length() != 10)
                {
                    Toast.makeText(NewLoginActivity.this, getString(R.string.pleaseenterphonenumber), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    phoneNumber = "+91" + phoneNumber;
                    progressDialog.setMessage(getString(R.string.sendingverificationcode));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            NewLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                progressDialog.dismiss();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                progressDialog.dismiss();
                Toast.makeText(NewLoginActivity.this, getString(R.string.entervalidphonenumber), Toast.LENGTH_SHORT).show();
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later

                NewLoginActivity.this.verificationId = verificationId;
                progressDialog.dismiss();

                Toast.makeText(NewLoginActivity.this, getString(R.string.codesent), Toast.LENGTH_SHORT).show();
                Intent codeVerificationIntent = new Intent(NewLoginActivity.this, CodeVerificationCodeActivity.class);
                codeVerificationIntent.putExtra(ActivityParameters.verificationcode, NewLoginActivity.this.verificationId);
                codeVerificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(codeVerificationIntent);
            }
        };
    }

    private void signInWithPhoneAuthCredential (PhoneAuthCredential credential)
    {
        FirebaseAuthProvider.getFirebaseAuth().signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        progressDialog.dismiss();
                        SendUserToMainActivity();
                    }
                    else
                    {
                        // Sign in failed, display a message and update the UI
                        String message = task.getException().toString();
                        progressDialog.dismiss();
                        Toast.makeText(NewLoginActivity.this, getString(R.string.loginerror) + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void SendUserToMainActivity ()
    {
        progressDialog.dismiss();
        Intent mainIntent = new Intent(NewLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }
}
