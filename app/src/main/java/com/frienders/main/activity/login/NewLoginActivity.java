package com.frienders.main.activity.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class NewLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);

        mAuth = FirebaseAuth.getInstance();


        SendVerificationCodeButton = (Button) findViewById(R.id.send_verification_code);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input_new_login);
        SendVerificationCodeButton.setVisibility(View.VISIBLE);

        progressDialog = new ProgressDialog(this);


        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = InputPhoneNumber.getText().toString().trim();


                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(NewLoginActivity.this, "Phone number is required", Toast.LENGTH_SHORT).show();
                }

                else if(InputPhoneNumber.length() != 10)
                {
                    Toast.makeText(NewLoginActivity.this, "Please enter 10 digits indian mobile number e.g. 9876543210", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    phoneNumber = "+91" + phoneNumber;
//                    progressDialog.setTitle("Phone verification");
                    progressDialog.setMessage("We are sending verification code. Please wait!");
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
                Toast.makeText(NewLoginActivity.this, "Invalid Phone number, please enter 10 digits indian mobile number e.g. 9876543210", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

//                VerifyButton.setVisibility(View.INVISIBLE);
//                InputVerificationCode.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {

                // Save verification ID and resending token so we can use them later

                mVerificationId = verificationId;
                mResendToken = token;
                progressDialog.dismiss();

                Toast.makeText(NewLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
                Intent codeVerificationIntent = new Intent(NewLoginActivity.this, CodeVerificationCodeActivity.class);
                codeVerificationIntent.putExtra("verificationcode", mVerificationId);
                startActivity(codeVerificationIntent);


            }
        };
    }
        private void signInWithPhoneAuthCredential (PhoneAuthCredential credential)
        {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                // Sign in success, update UI with the signed-in user's information
                                progressDialog.dismiss();
                                Toast.makeText(NewLoginActivity.this, "Congratulations", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                            else
                            {
                                // Sign in failed, display a message and update the UI
                                String message = task.getException().toString();
                                progressDialog.dismiss();
                                Toast.makeText(NewLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();


                            }
                        }
                    });
        }

        private void SendUserToMainActivity ()
        {
            progressDialog.dismiss();
            Intent mainIntent = new Intent(NewLoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();

        }
}
