package com.example.chatting;
public class Message {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;
    public static final int TYPE_UPLOAD = 3;

    private int mType;
    private String mMessage;
    private String mUsername;
    private String mUsersrc;

    private Message() {}

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };

    public String getUsersrc() {
        return mUsersrc;
    };

    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private String mUsersrc;

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
        public Builder usersrc(String usersrc) {
            mUsersrc = usersrc;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            message.mUsersrc = mUsersrc;
            return message;
        }
    }
}