package com.example.chatting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


/**
 * A login screen that offers login via username.
 */
public class LoginActivity extends Activity {

    private EditText mUsernameView, mUserpwView;

    private String mUsername;

    private Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        try {
            mSocket = IO.socket("https://nanatsu-sin.herokuapp.com/");
            mSocket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mUserpwView = (EditText) findViewById(R.id.username_pw);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == 101 || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button signupButton = (Button)findViewById(R.id.sign_up_button);
        signupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signup = new Intent(LoginActivity.this,SignUp.class);
                startActivity(signup);
            }
        });

        mSocket.on("login", onLogin);
        mSocket.on("result message", result_message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mSocket.disconnect();
        mSocket.off("login", onLogin);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String userpw = mUserpwView.getText().toString();
        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        mUsername = username;

        // perform the user login attempt.
        mSocket.emit("add user", username,userpw);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            String usersrc;
            try {
                numUsers = data.getInt("numUsers");
                usersrc=data.getString("usersrc");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent(LoginActivity.this,MainFragment.class);
            //Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            intent.putExtra("usersrc", usersrc);
            setResult(RESULT_OK, intent);
            //startActivityForResult(intent,RESULT_OK);
            finish();
        }
    };
    private Emitter.Listener result_message = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this,
                        "로그인에 실패하셨습니다.", Toast.LENGTH_LONG).show();
            });
        }
    };
}
