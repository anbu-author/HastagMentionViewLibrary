package com.hashtagmentions;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.hashtagmentions.edit.HashTagMentionEditText;
import com.hashtagmentions.model.User;
import com.hashtagmentions.text.HashTagMentionTextView;
import com.hashtagmentions.text.Parser;
import com.hashtagmentions.text.listener.OnMentionClicked;


import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = MainActivity.class.getSimpleName();
    HashTagMentionEditText hashTagMentionEditText;
    HashTagMentionTextView hashTagMentionTextView;
    Button convert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hashTagMentionEditText =findViewById(R.id.mentionEditText);
        convert =findViewById(R.id.convert);
        hashTagMentionTextView =findViewById(R.id.mentionTextView);
        loadData();
        setListeners();
    }

    private void setListeners() {
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Parser parser=new Parser(MainActivity.this, new OnMentionClicked() {
                    @Override
                    public void onClick(JSONObject jsonObject) {
                        Toast.makeText(MainActivity.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                hashTagMentionTextView.setMovementMethod(LinkMovementMethod.getInstance());
                hashTagMentionTextView.setParserConverter(parser);
                hashTagMentionTextView.setText(hashTagMentionEditText.getFormatCharSequence());
            }
        });
    }

    private void loadData() {
        ArrayList<UsersList> usersLists=new ArrayList<>();
        UsersList user1=new UsersList();
        user1.setId("1");
        user1.setName("John");
        UsersList user2=new UsersList();
        user2.setId("2");
        user2.setName("Mathew");
        usersLists.add(user1);
        usersLists.add(user2);

        CustomMentionAdapter customMentionAdapter = new CustomMentionAdapter(this,R.layout.autocomplete_item, usersLists);
        hashTagMentionEditText.setAdapter(customMentionAdapter);
        hashTagMentionEditText.setThreshold(1);

        hashTagMentionEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Object item = adapterView.getItemAtPosition(i);
                if (item instanceof UsersList){
                    UsersList itemUser=(UsersList) item;
                    User user = new User(itemUser.getId(), "@"+itemUser.getName());
//                user.setUserSex(i % 2 == 0 ? "男" : "女");
                    int pos= hashTagMentionEditText.getSelectionStart();
                    hashTagMentionEditText.getText().delete(pos-1,pos);
                    hashTagMentionEditText.insert(user);
                }
                Log.e(TAG, "onItemClick: "+hashTagMentionEditText.getFormatCharSequence() );
            }
        });

        hashTagMentionEditText.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {

            @Override
            public CharSequence terminateToken(CharSequence text) {
                int i = text.length();

                while (i > 0 && text.charAt(i - 1) == ' ') {
                    i--;
                }

                if (i > 0 && text.charAt(i - 1) == ' ') {

                    return "";
                } else {
                    if (text instanceof Spanned) {
                        SpannableString sp = new SpannableString("");
                        TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                        return sp;
                    } else {
                        return "";
                    }
                }
            }

            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                while (i > 0 && text.charAt(i - 1) != '@') {
                    i--;
                }

                //Check if token really started with @, else we don't have a valid token
                if (i < 1 || text.charAt(i - 1) != '@') {

                    return cursor;
                }

                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();

                while (i < len) {
                    if (text.charAt(i) == ' ') {
                        return i;
                    } else {
                        i++;
                    }
                }
                return len;
            }
        });

    }

}
