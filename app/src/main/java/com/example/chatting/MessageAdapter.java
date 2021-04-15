package com.example.chatting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private int[] mUsernameColors;
    private View hereView;
    private ViewGroup getP;
    private StorageReference mStorageRef2;
    private ImageView profil_view;
    public MessageAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        //hereView = context;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        mStorageRef2 = FirebaseStorage.getInstance().getReferenceFromUrl("");

        switch (viewType) {
            case Message.TYPE_MESSAGE:
                layout = R.layout.item_message;
                break;
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_ACTION:
                layout = R.layout.item_action;
                break;
            case Message.TYPE_UPLOAD:
                layout = R.layout.uploaded_data;
                break;
        }
        getP = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        viewHolder.setUsersrc(message.getUsersrc());
        viewHolder.setMessage(message.getMessage());
        viewHolder.setUsername(message.getUsername());
        viewHolder.setUPloadImage(message.getMessage());

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private TextView mMessageView;
        private ImageView UploadV;

        public ViewHolder(View itemView) {
            super(itemView);
            hereView = itemView;
            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            UploadV = (ImageView)itemView.findViewById(R.id.UploadedImage);
            profil_view = (ImageView)itemView.findViewById(R.id.profil_image);

        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
            mUsernameView.setTextColor(getUsernameColor(username));
        }

        public void setUsersrc(String usersrc) {
            if (null == profil_view) return;

            mStorageRef2.child("images/"+usersrc).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Glide.with(hereView.getContext()).load(uri).into(profil_view);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

            GradientDrawable drawable2= (GradientDrawable)hereView.getContext().getDrawable(R.drawable.back_round2);
            profil_view.setBackground(drawable2);
            profil_view.setClipToOutline(true);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
            mMessageView.setBackground(hereView.getContext().getResources().getDrawable( (R.drawable.perso)));
            //mMessageView.setTextColor(Color.parseColor("#000000"));
        }
        public void setUPloadImage(String message) {
            if (null ==UploadV) return;
            if (message == null) return;
            //String url = "https://nanatsu-sin.herokuapp.com/download/"+message;
            mStorageRef2.child("images/"+message).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Glide.with(hereView.getContext()).load(uri).into(UploadV);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

            UploadV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent=new Intent(getP.getContext(),bigImage.class);

                    intent.putExtra("Imagesrc",message);
                    getP.getContext().startActivity(intent);
                }
            });
        }
        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }
    }
}
