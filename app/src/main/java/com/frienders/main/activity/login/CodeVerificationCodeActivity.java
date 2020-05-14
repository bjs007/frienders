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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class CodeVerificationCodeActivity extends AppCompatActivity {

    private Button verifyButton;
    private EditText inputVerificationCode;
    private ProgressDialog progressDialog;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verification_code);
        initializeUi();

        if(getIntent().getExtras().get(ActivityParameters.verificationcode) != null)
        {
            verificationId = getIntent().getExtras().get(ActivityParameters.verificationcode).toString();
        }

    }

    private void initializeUi()
    {
        verifyButton = findViewById(R.id.verification_code_button);
        inputVerificationCode = findViewById(R.id.verification_code);
        progressDialog  = new ProgressDialog(this);
        verifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String verificationCode = inputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(CodeVerificationCodeActivity.this, getString(R.string.writeverificationcodehere), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle(getString(R.string.verificationcodepopoptitle));
                    progressDialog.setMessage(getString(R.string.verificationcodepleasewaitmessage));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
            }
            }
        });
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
                            Toast.makeText(CodeVerificationCodeActivity.this, getString(R.string.codeverified), Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            // Sign in failed, display a message and update the UI
                            String message = task.getException().toString();
                            Toast.makeText(CodeVerificationCodeActivity.this, getString(R.string.wrongcode), Toast.LENGTH_SHORT).show();

                            Intent newLoginActivity = new Intent(CodeVerificationCodeActivity.this, NewLoginActivity.class);
                            newLoginActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(newLoginActivity);
                        }
                    }
                });
    }

    private void SendUserToMainActivity ()
    {
        Intent mainIntent = new Intent(CodeVerificationCodeActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }
}
