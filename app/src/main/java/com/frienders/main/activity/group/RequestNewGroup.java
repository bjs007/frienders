package com.frienders.main.activity.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.frienders.main.R;
import com.frienders.main.activity.MainActivity;
import com.frienders.main.activity.profile.NewSetting;
import com.frienders.main.config.GroupFirebaseFields;
import com.frienders.main.db.refs.FirebaseAuthProvider;
import com.frienders.main.db.refs.FirebasePaths;
import com.frienders.main.utility.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RequestNewGroup extends AppCompatActivity {

    private ImageButton requestGroup, cancelRequestGroup;
    private EditText newGroupDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_new_group);
        initializeUi();
        requestGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newGroupDescription.getText() != null && !Utility.isEmpty(newGroupDescription.getText().toString())) {
                     if(Utility.getNumberOfWords(newGroupDescription.getText().toString()) < 5) {
                         Toast.makeText(RequestNewGroup.this, getString(R.string.write_at_least_5_words), Toast.LENGTH_SHORT).show();

                     }else {
                         HashMap<String, Object> suggestion = new HashMap<>();
                         suggestion.put(GroupFirebaseFields.groupDetails, newGroupDescription.getText().toString());

                         FirebasePaths.firebaseGroupSuggestionDbRef()
                                 .child(FirebaseAuthProvider.getCurrentUserId())
                                 .updateChildren(suggestion)
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful()) {
                                             Toast.makeText(RequestNewGroup.this, getString(R.string.new_group_request_registered), Toast.LENGTH_SHORT).show();
                                             SendUserToMainActivity();
                                         }else {
                                             Toast.makeText(RequestNewGroup.this, getString(R.string.new_group_request_registered_failed), Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 Toast.makeText(RequestNewGroup.this, getString(R.string.new_group_request_registered_failed), Toast.LENGTH_SHORT).show();

                             }
                         });
                     }

                        }else {
                            Toast.makeText(RequestNewGroup.this, getString(R.string.write_something_about_new_group_message), Toast.LENGTH_SHORT).show();
                        }
                    }
        });
        cancelRequestGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMainActivity();
            }
        });
    }
    private void initializeUi() {
        requestGroup = findViewById(R.id.request_new_group);
        cancelRequestGroup = findViewById(R.id.cancel_request_new_group);
        newGroupDescription = findViewById(R.id.new_group_description);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RequestNewGroup.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);
        finish();
    }

}
