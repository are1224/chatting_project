package com.example.chatting;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.lang.Runnable;
import android.database.Cursor;

/**
 * A chat fragment containing messages view and input form.
 */
public class MainFragment extends Fragment {
    private StorageReference mStorageRef;
    private static final String TAG = "MainFragment";
    private Uri downloadUrl;
    private static final int REQUEST_LOGIN = 0;

    private static final int TYPING_TIMER_LENGTH = 600;

    private RecyclerView mMessagesView;
    private RecyclerView MUploadView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private List<ImageMessage> imMessages = new ArrayList<ImageMessage>();
    private RecyclerView.Adapter imAdapter;
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private Socket mSocket;
    private String myname;
    private ImageButton plusB;
    private Boolean isConnected = true;
    String absolutePath;
    private ProgressDialog progressBar;
    ProgressDialog dialog = null;
    private String usersrc;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String upLoadServerUri="https://nanatsu-sin.herokuapp.com/upload";
    private static final int PICK_FROM_ALBUM =1;
    int serverResponseCode = 0;
    private String ur;
    private String download_filename;
    private Cursor c;
    String getImgURL="";
    String getImgName="";
    public MainFragment() {
        super();
    }


    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter = new MessageAdapter(context, mMessages);
        imAdapter = new ImageAdapter(context,imMessages);
        if (context instanceof Activity){
            //this.listener = (MainActivity) context;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://myapp-6c652.appspot.com");
        try {
            mSocket = IO.socket("https://nanatsu-sin.herokuapp.com/");
            mSocket.on(Socket.EVENT_CONNECT,onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on("new message", onNewMessage);
            mSocket.on("user joined", onUserJoined);
            mSocket.on("user left", onUserLeft);
            mSocket.on("typing", onTyping);
            mSocket.on("stop typing", onStopTyping);
            mSocket.on("new dataUpload",onUploaddata);
            mSocket.on("send src",onSendsrc);
            mSocket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        startSignIn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(c!=null) {
            c.close();
        }
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        mSocket.off("new dataUpload",onUploaddata);
        mSocket.off("send src",onSendsrc);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        //MUploadView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        progressBar=new ProgressDialog(getContext());
        progressBar.setMessage("다운로드중");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == 100 || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
        plusB=(ImageButton)view.findViewById(R.id.plus_button);
        plusB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getImage();
                //addImageMessage(mUsername, "fsf");
            }
        });
        verifyStoragePermissions(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_ALBUM && data!=null){
            Uri dataUri = data.getData();
            c = getContext().getContentResolver().query(Uri.parse(dataUri.toString()), null,null,null,null);
            c.moveToNext();
            absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            getImageNameToUri(dataUri);
            if (dataUri != null) {
                //tv.setText(dataUri.toString());
                ur=dataUri.toString();
                start_upload();
            }
        }else{
            if (Activity.RESULT_OK != resultCode) {
                getActivity().finish();
                return;
            }

            mUsername = data.getStringExtra("username");
            int numUsers = data.getIntExtra("numUsers", 1);
            usersrc = data.getStringExtra("usersrc");
            addLog(getResources().getString(R.string.message_welcome));
            addParticipantsLog(numUsers);
        }
    }

    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContext().getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        getImgURL = imgPath;
        getImgName = imgName;

        return "success";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leave) {
            leave();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void getImage(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }
    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void addMessage(String username, String message,String usersrc3) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).usersrc(usersrc3).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addImageMessage(String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_UPLOAD)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message,usersrc);

        // perform the sending message attempt.
        mSocket.emit("new message", mUsername,message,usersrc);
    }

    private void startSignIn() {
        mUsername = null;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void leave() {
        mUsername = null;
        mSocket.disconnect();
        mSocket.connect();
        startSignIn();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(() -> {
                if(!isConnected) {
                    if(null!=mUsername)
                        mSocket.emit("add user_second", mUsername);
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.connect, Toast.LENGTH_LONG).show();
                    isConnected = true;
                }
            });
        }
    };

    private Emitter.Listener onUploaddata = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String username2;
                String uploaddata;
                try {
                    username2 = data.getString("username");
                    uploaddata = data.getString("uploaddata");
                    addImageMessage(username2, uploaddata);

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
            });
        }
    };

    private Emitter.Listener onSendsrc = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                try {
                    usersrc = data.getString("userSrc");

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(() -> {
                Log.i(TAG, "diconnected");
                isConnected = false;
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.disconnect, Toast.LENGTH_LONG).show();
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(() -> {
                Log.e(TAG, "Error connecting");
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.error_connect, Toast.LENGTH_LONG).show();
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String username;
                String message;
                String usersrc2;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                    usersrc2 = data.getString("usersrc");
                    addMessage(username, message,usersrc2);
                    removeTyping(username);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String username;
                int numUsers;
                try {
                    username = data.getString("username");
                    myname = username;
                    numUsers = data.getInt("numUsers");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }

                addLog(getResources().getString(R.string.message_user_joined, username));
                addParticipantsLog(numUsers);
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String username;
                int numUsers;
                try {
                    username = data.getString("username");
                    numUsers = data.getInt("numUsers");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }

                addLog(getResources().getString(R.string.message_user_left, username));
                addParticipantsLog(numUsers);
                removeTyping(username);
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String username;
                try {
                    username = data.getString("username");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                addTyping(username);
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String username;
                try {
                    username = data.getString("username");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                removeTyping(username);
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    public void start_upload(){
        dialog = ProgressDialog.show(getContext(), "", "Uploading file...", true);
        uploadFile(absolutePath);
    }

    public void uploadFile(String sourceFileUri) {

        dialog.dismiss();
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("업로드중...");
        progressDialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
        Date now = new Date();
        String filename = formatter.format(now) + getImgName;
        download_filename = filename;
        Uri file = Uri.fromFile(new File(sourceFileUri));
        StorageReference riversRef = mStorageRef.child("images/"+filename);
        //uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        riversRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get a URL to the uploaded content
                downloadUrl = taskSnapshot.getDownloadUrl();
                addImageMessage(mUsername, download_filename);
                mSocket.emit("new uploaddata", mUsername,download_filename);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                // ...
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                        double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                //dialog에 진행률을 퍼센트로 출력해 준다
                progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
            }
        });
    } // End else block

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}