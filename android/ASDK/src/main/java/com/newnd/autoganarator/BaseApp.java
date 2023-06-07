package com.newnd.autoganarator;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BaseApp extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static final String TAG = "BaseApp";
    public static BaseApp baseApp;
    public static final long COUNTER_TIME = 5;
    public static long secondsRemaining;
    public static AppOpenAdManager appOpenAdManager;
    public static Activity currentActivity;

    static List<AdModel> lessons = new ArrayList<>();
    public static AdModel adModel;
    public static AdManagerInterstitialAd mInterstitialAd;
    public static int adTime = 2;
    public static InterstitialAd admobInterstitialAd;

    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        MobileAds.initialize(
                this,
                initializationStatus -> {
                });
        baseApp = this;

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


    public static BaseApp getInstance(Activity activity) {
        currentActivity = activity;
        baseApp.getConfig();
        return baseApp;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        if (adModel != null) {
            if (!adModel.isQurekaAds)
                if (adModel.isopenmediation) {
                    com.openmediation.sdk.splash.SplashAd.showAd(adModel.getOpenmediationSplashplacementId());
                } else {
                    if (appOpenAdManager != null)

                        // Show the ad (if available) when the AdsSdk moves to foreground.
                        appOpenAdManager.showAdIfAvailable(currentActivity);
                }
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (appOpenAdManager != null)
            if (!appOpenAdManager.isShowingAd) {
                currentActivity = activity;
            }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public static void ADSinit(final Activity activity, final getDataListner myCallback1) {

        CountDownTimer countDownTimer =
                new CountDownTimer(COUNTER_TIME * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        secondsRemaining = ((millisUntilFinished / 1000) + 1);
                    }

                    @Override
                    public void onFinish() {
                        Application application = activity.getApplication();

                        // If the application is not an instance of MyApplication, log an error message and
                        // start the MainActivity without showing the AdsSdk open ad.
                        if (!(application instanceof BaseApp)) {
                            Log.e("LOG_TAG", "Failed to cast application to MyApplication.");
                            myCallback1.onsuccess();
                            return;
                        }

                        secondsRemaining = 0;
                        if (adModel != null) {
                            if (adModel.isAD()) {
                                if (adModel.isopenmediation) {
                                    com.openmediation.sdk.splash.SplashAd.showAd(adModel.getOpenmediationSplashplacementId());
                                    myCallback1.onsuccess();
                                } else {
                                    ((BaseApp) application)
                                            .showAdIfAvailable(
                                                    activity,
                                                    new OnShowAdCompleteListener() {
                                                        @Override
                                                        public void onShowAdComplete() {
                                                            myCallback1.onsuccess();
                                                        }
                                                    });
                                }
                            } else {
                                myCallback1.onsuccess();

                            }
                        }


                    }
                };
        countDownTimer.start();
    }

    void getConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(currentActivity, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();

                        lessons.clear();
                        String object = mFirebaseRemoteConfig.getString("chatgpt");
                        Gson gson = new GsonBuilder().create();
                        lessons = gson.fromJson(object, new TypeToken<List<AdModel>>() {
                        }.getType());

                        if (lessons != null) {
                            if (!lessons.isEmpty()) {
                                adModel = lessons.get(0);
                                if (adModel != null) {


                                    if (adModel.isopenmediation) {
                                        InitConfiguration configuration = new InitConfiguration.Builder()
                                                .appKey(adModel.openmediationKey)
                                                .logEnable(true)
                                                .build();
                                        OmAds.init(configuration, new InitCallback() {

                                            // Invoked when the initialization is successful.
                                            @Override
                                            public void onSuccess() {
                                                loadOpenMediation();
                                                loadOpenMediationSplash();
                                            }

                                            @Override
                                            public void onError(Error error) {

                                            }
                                        });
                                    }

                                    if (adModel.isAD()) {
                                        adTime = adModel.getAdTime();
                                        onMoveToForeground();

                                        if (!(adModel.isopenmediation)) {
                                            appOpenAdManager = new AppOpenAdManager();


                                                appOpenAdManager.showAdIfAvailable(currentActivity);
                                                admobLoadInterstitial(currentActivity);
                                                loadInterstitial(currentActivity);


                                        }
                                    }
                                }
                            }
                        }

                    }
                });
    }


    static void loadInterstitial(final Activity activity) {
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().build();
        MobileAds.setRequestConfiguration(configuration);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

        AdManagerInterstitialAd.load(activity, adModel.getInterstitial(), adRequest,
                new AdManagerInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                        // The mAdManagerInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                loadInterstitial(activity);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });

                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

    }

    static void admobLoadInterstitial(final Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(activity, adModel.getAdmobinterstitial(), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        admobInterstitialAd = interstitialAd;
                        admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                admobLoadInterstitial(activity);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                admobInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        admobInterstitialAd = null;
                    }
                });

    }

    void loadOpenMediation() {
        com.openmediation.sdk.interstitial.InterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdAvailabilityChanged(boolean available) {
                Log.e(TAG, "onInterstitialAdShowFailed: " + String.valueOf(available));

                // Change the interstitial ad state in app according to param available.
            }

            @Override
            public void onInterstitialAdShowed(Scene scene) {
                // Do not perform heavy tasks till the ad is going to be closed.
            }

            /**
             * Invoked when the Interstitial ad is closed.
             * Your activity will regain focus.
             */
            @Override
            public void onInterstitialAdClosed(Scene scene) {
                com.openmediation.sdk.interstitial.InterstitialAd.loadAd();
            }

            /**
             * Invoked when the user clicked on the Interstitial ad.
             */
            @Override
            public void onInterstitialAdClicked(Scene scene) {
            }

            /* Invoked when the Interstitial ad has showed failed
             * @param - error contains the reason for the failure:
             */
            @Override
            public void onInterstitialAdShowFailed(Scene scene, Error error) {
                Log.e(TAG, "onInterstitialAdShowFaileds: " + error.getErrorMessage());
                loadOpenMediation();
                // Interstitial ad show failed
            }
        });
        com.openmediation.sdk.interstitial.InterstitialAd.loadAd();
    }

    void loadOpenMediationSplash() {
        Log.e(TAG, "loadOpenMediationSplash: " + adModel.getOpenmediationSplashplacementId());
        com.openmediation.sdk.splash.SplashAd.setSplashAdListener(adModel.getOpenmediationSplashplacementId(), new com.openmediation.sdk.splash.SplashAdListener() {
            @Override
            public void onSplashAdLoaded(String s) {
                com.openmediation.sdk.splash.SplashAd.showAd(adModel.getOpenmediationSplashplacementId());
            }

            @Override
            public void onSplashAdFailed(String s, Error error) {

                Log.e(TAG, "onSplashAdFailed: " + error.getErrorMessage());
            }

            @Override
            public void onSplashAdClicked(String s) {

            }

            @Override
            public void onSplashAdShowed(String s) {

            }

            @Override
            public void onSplashAdShowFailed(String s, Error error) {
                Log.e(TAG, "onSplashAdFailed: " + error.getErrorMessage());
            }

            @Override
            public void onSplashAdTick(String s, long l) {

            }

            @Override
            public void onSplashAdDismissed(String s) {
                com.openmediation.sdk.splash.SplashAd.loadAd(adModel.getOpenmediationSplashplacementId());

            }
        });
        com.openmediation.sdk.splash.SplashAd.loadAd(adModel.getOpenmediationSplashplacementId());


    }

    public void showAdIfAvailable(
            @NonNull Activity activity,
            @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    /**
     * Interface definition for a callback to be invoked when an AdsSdk open ad is complete
     * (i.e. dismissed or fails to show).
     */
    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    private static class AppOpenAdManager {

        private static final String LOG_TAG = "AppOpenAdManager";
        private final String AD_UNIT_ID = adModel.isAdmobAd() ? adModel.getAdmobadOpen() : adModel.getAdOpen();

        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;

        /**
         * Keep track of the time an AdsSdk open ad is loaded to ensure you don't show an expired ad.
         */
        private long loadTime = 0;

        /**
         * Constructor.
         */
        public AppOpenAdManager() {
        }

        /**
         * Load an ad.
         *
         * @param context the context of the activity that loads the ad
         */
        private void loadAd(Context context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdManagerAdRequest request = new AdManagerAdRequest.Builder().build();
            AppOpenAd.load(
                    context,
                    AD_UNIT_ID,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        /**
                         * Called when an AdsSdk open ad has loaded.
                         *
                         * @param ad the loaded AdsSdk open ad.
                         */
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = (new Date()).getTime();

                            Log.d(LOG_TAG, "onAdLoadesssdasdd.");
                        }

                        /**
                         * Called when an AdsSdk open ad has failed to load.
                         *
                         * @param loadAdError the error.
                         */
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            isLoadingAd = false;
                            Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                        }
                    });
        }

        /**
         * Check if ad was loaded more than n hours ago.
         */
        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        /**
         * Check if ad exists and can be shown.
         */
        private boolean isAdAvailable() {
            // Ad references in the AdsSdk open beta will time out after four hours, but this time limit
            // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the AdsSdk open ad
         */
        private void showAdIfAvailable(@NonNull final Activity activity) {
            showAdIfAvailable(
                    activity,
                    new OnShowAdCompleteListener() {
                        @Override
                        public void onShowAdComplete() {
                            // Empty because the user will go back to the activity that shows the ad.
                        }
                    });
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity                 the activity that shows the AdsSdk open ad
         * @param onShowAdCompleteListener the listener to be notified when an AdsSdk open ad is complete
         */
        private void showAdIfAvailable(
                @NonNull final Activity activity,
                @NonNull final OnShowAdCompleteListener onShowAdCompleteListener) {
            // If the AdsSdk open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The AdsSdk open ad is already showing.");
                return;
            }

            // If the AdsSdk open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The AdsSdk open ad is not ready yet.");
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
                return;
            }

            Log.d(LOG_TAG, "Will show ad.");

            appOpenAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        /** Called when full screen content is dismissed. */
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                            isShowingAd = false;

                            Log.d(LOG_TAG, "onAdDismissedFullScreenContent.");

                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity);
                        }

                        /** Called when fullscreen content failed to show. */
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            appOpenAd = null;
                            isShowingAd = false;

                            Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());

                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity);
                        }

                        /** Called when fullscreen content is shown. */
                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(LOG_TAG, "onAdShowedFullScreenContent.");
                        }
                    });

            isShowingAd = true;
            appOpenAd.show(activity);
        }
    }


}
