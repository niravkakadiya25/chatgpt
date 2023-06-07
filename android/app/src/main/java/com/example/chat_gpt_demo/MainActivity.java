package com.example.chat_gpt_demo;


import static com.newnd.autoganarator.BaseApp.adModel;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.newnd.autoganarator.BaseApp;
import com.newnd.autoganarator.ads;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private String CHANNEL = "ads";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        BaseApp.getInstance(this);
        flutterEngine
                .getPlatformViewsController()
                .getRegistry()
                .registerViewFactory("plugins/blur_view_widget", new NativeViewFactory(getActivity()));
        flutterEngine
                .getPlatformViewsController()
                .getRegistry()
                .registerViewFactory("plugins/blur_view_banner_widget", new BannerViewFactory(getActivity()));

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                Log.e("TAG", "onMethodCall: " + call.method);
                if (call.method.equals("ADOpen")) {
                    Application application = getActivity().getApplication();
                    ((BaseApp) application)
                            .showAdIfAvailable(
                                    getActivity(),
                                    new BaseApp.OnShowAdCompleteListener() {
                                        @Override
                                        public void onShowAdComplete() {
                                            result.success(true);
                                                                                    }
                                    });

                }
                if (call.method.equals("Interstitial")) {
                    new ads().showIntAds();
                }
                if (call.method.equals("CloseInterstitial")) {
                    new ads().CloseActivityWithAds(getActivity());
                }
                if (call.method.equals("quiz")) {
                   result.success(adModel.getQlink());
                }
                if (call.method.equals("isAD")) {
                   result.success(adModel.isAD());
                }if (call.method.equals("oneSignalKey")) {
                    result.success(adModel.getOneSignalKey());
                }
            }
        });
    }
}
