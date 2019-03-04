package com.example.chatting;

public class ImageMessage {
    public static final int TYPE_UPLOAD = 0;
    //public static final int TYPE_LOG = 1;
    //public static final int TYPE_ACTION = 2;
    //public static final int TYPE_UPLOAD = 3;

    private int mType;
    private String mMessage;
    private String mUsername;

    private ImageMessage() {}

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };


    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public ImageMessage build() {
            ImageMessage message = new ImageMessage();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            return message;
        }
    }
}
