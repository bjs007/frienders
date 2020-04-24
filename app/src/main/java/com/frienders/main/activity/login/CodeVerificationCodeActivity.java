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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class CodeVerificationCodeActivity extends AppCompatActivity {

    private Button VerifyButton;
    private EditText InputVerificationCode;
    private ProgressDialog progressDialog;
    private String mVerificationId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verification_code);
        VerifyButton = findViewById(R.id.verification_code_button);
        InputVerificationCode = findViewById(R.id.verification_code);
        mAuth = FirebaseAuth.getInstance();
        if(getIntent().getExtras().get("verificationcode") != null)
        {
            mVerificationId  = getIntent().getExtras().get("verificationcode").toString();
        }

        progressDialog  = new ProgressDialog(this);



        VerifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                String verificationCode = InputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(CodeVerificationCodeActivity.this, "Please write verification code first", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("Please wait, while we are verifying verification code");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
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
                            Toast.makeText(CodeVerificationCodeActivity.this, "Code verified", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            // Sign in failed, display a message and update the UI
                            String message = task.getException().toString();
                            Toast.makeText(CodeVerificationCodeActivity.this, "Wrong code! Try again!", Toast.LENGTH_SHORT).show();

                            Intent newLoginActivity = new Intent(CodeVerificationCodeActivity.this, NewLoginActivity.class);
                            startActivity(newLoginActivity);
                        }
                    }
                });
    }

    private void SendUserToMainActivity ()
    {
        Intent mainIntent = new Intent(CodeVerificationCodeActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
