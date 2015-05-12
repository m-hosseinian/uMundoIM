package de.tudarmstadt.tk.umundoim.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.tudarmstadt.tk.umundoim.R;
import de.tudarmstadt.tk.umundoim.constant.Constants;

public class UserNameActivity extends ActionBarActivity {

    TextView userNameEditText;
    Button userNameButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        userNameEditText = (EditText) findViewById(R.id.editTextUserName);
        userNameButton = (Button) findViewById(R.id.buttonUserName);

        userNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userNameEditText.getText() != null && userNameEditText.getText().length() != 0) {
                    Constants.userName = userNameEditText.getText().toString();

                    new Thread(){
                        @Override
                        public void run(){
                            startActivity(new Intent(UserNameActivity.this, ChatMainActivity.class));
                        }
                    }.run();
                    UserNameActivity.this.finish();
                } else {
                    Toast.makeText(UserNameActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
