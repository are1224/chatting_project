package com.example.chatting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<ImageMessage> mMessages2;
    private int[] mUsernameColors;
    private View imageV;
    private ViewGroup hereview;
    public ImageAdapter(Context context, List<ImageMessage> messages) {
        mMessages2 = messages;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout2 = -1;
        //layout2 = R.layout.uploaded_data;
        hereview = parent;
        switch (viewType) {
            case ImageMessage.TYPE_UPLOAD:
                layout2 = R.layout.item_message;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ImageMessage message = mMessages2.get(position);
        viewHolder.setMessage(message.getMessage());
        viewHolder.setUsername(message.getUsername());
    }

    @Override
    public int getItemCount() {
        return mMessages2.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages2.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private ImageView mImageView;
        private View herelayout;
        public ViewHolder(View itemView) {
            super(itemView);
            //herelayout = itemView;
            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mImageView = (ImageView) itemView.findViewById(R.id.UploadedImage);
        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
            mUsernameView.setTextColor(getUsernameColor(username));
            Log.i("imadani","gazeasss");
        }

        public void setMessage(String message) {
            if (null == mImageView) return;
            Log.i("imadani","gsssssssssssssssssssssss");
            //mImageView.setText(message);
            String url = "https://nanatsu-sin.herokuapp.com/download/"+message;
            //Toast.makeText(hereview.getContext(), ""+url, Toast.LENGTH_SHORT).show();
            //Glide.with(hereview.getContext()).load(url).into(mImageView);
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
