package com.frienders.main.activity.group;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.frienders.main.R;
import com.frienders.main.handlers.GroupHandler;
import com.frienders.main.model.GroupCreationRequest;

import java.util.ArrayList;
import java.util.List;

public class GroupCreationActivity extends AppCompatActivity {

    LinearLayout innerlayout, innerlayout1, innerlayout2, innerlayout3;
    final List<EditText> groupDetail = new ArrayList<>();
    final List<GroupCreationRequest> levelOneRequest = new ArrayList<>();
    Button button, submit;
    EditText levelNameEng, levelNameHin, levelNameEngDec, levelNameHinDec;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);


        final List<GroupCreationRequest> list = new ArrayList<>();

        final LinearLayout layout = findViewById(R.id.group_create);
        layout.setOrientation(LinearLayout.VERTICAL);
         innerlayout = new LinearLayout(this);
         innerlayout1 = new LinearLayout(this);
         innerlayout2 = new LinearLayout(this);
         innerlayout3 = new LinearLayout(this);


        innerlayout1.setOrientation(LinearLayout.HORIZONTAL);
        innerlayout.setOrientation(LinearLayout.HORIZONTAL);
        innerlayout2.setOrientation(LinearLayout.HORIZONTAL);
        innerlayout3.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout.LayoutParams viewLayout = new LinearLayout.LayoutParams(
                400, ViewGroup.LayoutParams.WRAP_CONTENT);







        //FirstButton at the top
        button = new Button(this);
        button.setText("Add level");
        button.setLayoutParams(viewLayout);

        //Level number
        TextView textView = new TextView(this);
        textView.setText(" Level: 0");
        textView.setTextColor(Color.RED);
        innerlayout2.addView(textView);


        levelNameEng = new EditText(this);
        groupDetail.add(levelNameEng);
        levelNameEng.setHint("level name");

        levelNameEng.setLayoutParams(viewLayout);

        levelNameHin = new EditText(this);
        groupDetail.add(levelNameHin);
        levelNameHin.setHint("स्तर का नाम");
        levelNameHin.setLayoutParams(viewLayout);


        levelNameEngDec = new EditText(this);
        groupDetail.add(levelNameEngDec);
        levelNameEngDec.setHint("level description");
        levelNameEngDec.setLayoutParams(viewLayout);

        levelNameHinDec = new EditText(this);
        groupDetail.add(levelNameHinDec);
        levelNameHinDec.setHint(
               " स्तर का विवरण");
        levelNameHinDec.setLayoutParams(viewLayout);


        submit = new Button(this);
        submit.setText("submit");
        submit.setLayoutParams(viewLayout);



        innerlayout.addView(button);
        innerlayout.addView(submit);


        innerlayout1.addView(levelNameEng);
        innerlayout1.addView(levelNameHin);
        innerlayout3.addView(levelNameEngDec);
        innerlayout3.addView(levelNameHinDec);



        layout.addView(innerlayout);
        layout.addView(innerlayout2);
        layout.addView(innerlayout1);
        layout.addView(innerlayout3);

        final int[] size = new int[]{0};


        final String[] moreLevel = new String[]{"No"};
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                size[0]++;
                LinearLayout linearLayout = new LinearLayout(GroupCreationActivity.this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout linearLayout1= new LinearLayout(GroupCreationActivity.this);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout linearLayoutLevelIndex= new LinearLayout(GroupCreationActivity.this);
                linearLayoutLevelIndex.setOrientation(LinearLayout.HORIZONTAL);


                final TextView levelNumber = new TextView(GroupCreationActivity.this);
                levelNumber.setText("\n level " + size[0]);
                levelNumber.setTextColor(Color.RED);
                levelNumber.setLayoutParams(viewLayout);
                linearLayoutLevelIndex.addView(levelNumber);

                final EditText editText = getEditText();
                editText.setHint("level name");
                editText.setLayoutParams(viewLayout);
                linearLayout.addView(editText);


                final EditText editText1 = getEditText();
                editText1.setHint("स्तर का नाम");
                editText1.setLayoutParams(viewLayout);
                linearLayout.addView(editText1);


                final EditText editTextEngDesc = getEditText();
                editTextEngDesc.setHint("level description");
                editTextEngDesc.setLayoutParams(viewLayout);
                linearLayout1.addView(editTextEngDesc);


                final EditText editText1HindDesc = getEditText();
                editText1HindDesc.setHint("स्तर का विवरण");
                editText1HindDesc.setLayoutParams(viewLayout);
                linearLayout1.addView(editText1HindDesc);


                layout.addView(linearLayoutLevelIndex);

                layout.addView(linearLayout);
                layout.addView(linearLayout1);

                groupDetail.add(editText);
                groupDetail.add(editText1);
                groupDetail.add(editTextEngDesc);
                groupDetail.add(editText1HindDesc);

            }
        });

        submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                final List<GroupCreationRequest> requests = new ArrayList<>();
                GroupCreationRequest groupCreationRequest = new GroupCreationRequest();

                GroupHandler groupHandler = GroupHandler.getGroupHandler();


//                groupCreationRequest.setGroupNameInEng(levelNameEng.getText().toString());
//                groupCreationRequest.setGroupDescInEng(levelNameEngDec.getText().toString());
//
//                groupCreationRequest.setGroupNameInHin(levelNameHin.getText().toString());
//                groupCreationRequest.setGroupDescInHin(levelNameHinDec.getText().toString());

//                requests.add(groupCreationRequest);

                for (int i = 0; i < groupDetail.size(); i = i + 4) {
                    groupCreationRequest = new GroupCreationRequest();
                    groupCreationRequest.setGroupNameInEng(groupDetail.get(i).getText().toString());
                    groupCreationRequest.setGroupDescInEng(groupDetail.get(i + 2).getText().toString());
                    groupCreationRequest.setGroupNameInHin(groupDetail.get(i + 1).getText().toString());
                    groupCreationRequest.setGroupDescInHin(groupDetail.get(i + 3).getText().toString());
                    requests.add(groupCreationRequest);
                }


                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GroupHandler groupHandler = GroupHandler.getGroupHandler();
                        try {
                            groupHandler.createGroup(requests);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

                t1.start();

            }
        });


    }

    private EditText getEditText()
    {
        EditText levelName = new EditText(this);
        levelName.setHint("level name");
        return  levelName;
    }
}
