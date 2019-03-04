package com.example.chatting;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.logging.Logger;

public class MyAppGildeModule extends AppGlideModule
{
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry)
    {
        //Logger.d("");
        registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
    }
}

