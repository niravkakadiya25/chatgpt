package com.example.chat_gpt_demo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.newnd.autoganarator.ads;

import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

class NativeView implements PlatformView {
    private final FrameLayout frameLayout;

    NativeView(Context context,Activity activity, int id, Map<String, Object> creationParams) {
        frameLayout = new FrameLayout(context);
        new ads().nativeAds(activity,frameLayout,adLoaded -> {});
    }

    @Override
    public View getView() {
        return frameLayout;
    }

    @Override
    public void dispose() {}
}

class BannerView implements PlatformView {
    private final FrameLayout frameLayout;

    BannerView(Context context,Activity activity, int id, Map<String, Object> creationParams) {
        frameLayout = new FrameLayout(context);
        new ads().bannerAds(activity,frameLayout, adLoaded -> {});
    }

    @Override
    public View getView() {
        return frameLayout;
    }

    @Override
    public void dispose() {}
}
