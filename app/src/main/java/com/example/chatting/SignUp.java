package com.example.chatting;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.File;
import java.lang.*;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class SignUp extends Activity {
    //private EditText mUsernameView;

    //private String mUsername;
    private StorageReference mStorageRef3;
    private Socket mSocket;
    private EditText e1,e2,e3,e4;
    private ImageView imageView;
    private static final int PICK_FROM_ALBUM2 =1;
    private Cursor c2;
    String getImgURL2="";
    String getImgName2="";
    String absolutePath2;
    private String ur2;
    private String download_filename2=null;
    private Uri downloadUrl2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_layout);
        e1 = (EditText)findViewById(R.id.sign_up_id);
        e2 = (EditText)findViewById(R.id.sign_up_name);
        e3 = (EditText)findViewById(R.id.sign_up_pw);
        e4 = (EditText)findViewById(R.id.sign_up_pw2);
        imageView = (ImageView)findViewById(R.id.profil_img);
        GradientDrawable drawable= (GradientDrawable)getApplicationContext().getDrawable(R.drawable.back_round2);
        imageView.setBackground(drawable);
        imageView.setClipToOutline(true);
        mStorageRef3 = FirebaseStorage.getInstance().getReferenceFromUrl("gs://myapp-6c652.appspot.com");

        try {
            mSocket = IO.socket("https://nanatsu-sin.herokuapp.com/");
            mSocket.on("sign_up",set_Sign_Up);
            mSocket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Set up the login form.
        Button cancel_B = (Button)findViewById(R.id.sign_up_cancel);
        cancel_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Button sign_up_B = (Button)findViewById(R.id.sign_up_button2);
        sign_up_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check = check_edit();
                if(check) {
                    mSocket.emit("go sign_up",e1.getText().toString(),e3.getText().toString(),download_filename2);
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage2();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back button pressed.", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    private void getImage2(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,PICK_FROM_ALBUM2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_ALBUM2 && data!=null){
            Uri dataUri = data.getData();
            c2 = getApplicationContext().getContentResolver().query(Uri.parse(dataUri.toString()), null,null,null,null);
            c2.moveToNext();
            absolutePath2 = c2.getString(c2.getColumnIndex(MediaStore.MediaColumns.DATA));
            getImageNameToUri2(dataUri);
            if (dataUri != null) {
                //tv.setText(dataUri.toString());
                ur2=dataUri.toString();
                start_upload2();
            }
        }
    }

    public void start_upload2(){
        uploadFile(absolutePath2);
    }

    public void uploadFile(String sourceFileUri) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
        Date now = new Date();
        String filename = formatter.format(now) + getImgName2;
        download_filename2 = filename;
        Uri file = Uri.fromFile(new File(sourceFileUri));
        StorageReference riversRef = mStorageRef3.child("images/"+filename);
        //uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        riversRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get a URL to the uploaded content
                downloadUrl2 = taskSnapshot.getDownloadUrl();
                Glide.with(getApplicationContext()).load(downloadUrl2).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                // ...
            }
        });
    } // End else block

    public String getImageNameToUri2(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getApplicationContext().getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        getImgURL2 = imgPath;
        getImgName2 = imgName;

        return "success";
    }

    private Emitter.Listener set_Sign_Up = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                JSONObject data = (JSONObject)args[0];
                String sign_result;
                try {
                    sign_result = data.getString("result_message");
                    if(sign_result.equals("true")){
                        e1.setText("");
                        e2.setText("");
                        e3.setText("");
                        e4.setText("");
                        finish();
                    }else{
                        Toast.makeText(SignUp.this,
                                "이미 존재하는 아이디 입니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }

            });
        }
    };

    public void onDestroy() {
        super.onDestroy();
        //mSocket.disconnect();
        //c2.close();
        mSocket.off("sign_up",set_Sign_Up);
    }
    public boolean check_edit(){
        int check_count = 5;
        if(e1.getText().toString().trim().length()<=0){
            Toast.makeText(this.getApplicationContext(),
                    "아이디를 입력해주세요", Toast.LENGTH_LONG).show();
            check_count--;
        }
        if(e2.getText().toString().trim().length()<=0){
            Toast.makeText(this.getApplicationContext(),
                    "이름을 입력해주세요", Toast.LENGTH_LONG).show();
            check_count--;
        }
        if(e3.getText().toString().trim().length()<=0){
            Toast.makeText(this.getApplicationContext(),
                    "비밀번호를 입력해주세요", Toast.LENGTH_LONG).show();
            check_count--;
        }
        if(e4.getText().toString().trim().length()<=0){
            Toast.makeText(this.getApplicationContext(),
                    "비밀번호를 입력해주세요", Toast.LENGTH_LONG).show();
            check_count--;
        }
        if(!(e3.getText().toString().equals(e4.getText().toString()))){
            Toast.makeText(this.getApplicationContext(),
                    "두개의 비밀번호가 일치하게 입력해주세요", Toast.LENGTH_LONG).show();
            check_count--;
        }

        if(check_count==5){
            return true;
        }else{
            return false;
        }
    }
}
