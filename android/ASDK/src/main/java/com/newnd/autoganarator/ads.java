package com.newnd.autoganarator;

import static android.content.ContentValues.TAG;
import static com.newnd.autoganarator.BaseApp.adModel;
import static com.newnd.autoganarator.BaseApp.adTime;
import static com.newnd.autoganarator.BaseApp.admobInterstitialAd;
import static com.newnd.autoganarator.BaseApp.baseApp;
import static com.newnd.autoganarator.BaseApp.currentActivity;
import static com.newnd.autoganarator.BaseApp.mInterstitialAd;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.openmediation.sdk.banner.BannerAd;
import com.openmediation.sdk.banner.BannerAdListener;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.NativeAdListener;
import com.openmediation.sdk.utils.error.Error;

import java.util.List;
import java.util.Random;

public class ads {
    public void openPlayGames(Context context) {
        String urlString = adModel.getQlink();
        Log.e(TAG, "onClick: " + urlString);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            context.startActivity(intent);
        }
    }

    public void showIntAds() {
        if (adModel != null) {
            if (adModel.isAD()) {
                if (adModel.isBothAds) {
                    baseApp.showAdIfAvailable(currentActivity, new BaseApp.OnShowAdCompleteListener() {
                        @Override
                        public void onShowAdComplete() {
                            new ads().showints();
                        }
                    });
                } else {
                    new ads().showints();
                }
            }
        }
    }

    void showints() {
        if (adModel != null) {
            if (adModel.isAD()) {
                if (adModel.isEveryAds) {
                    showint(currentActivity);
                } else if (adTime == adModel.getAdTime()) {
                    showint(currentActivity);
                } else {
                    adTime++;
                }
            }
        }
    }

    void showint(Activity activity) {
        if (adModel.isopenmediation) {
            if (com.openmediation.sdk.interstitial.InterstitialAd.isReady()) {
                adTime = 1;
                com.openmediation.sdk.interstitial.InterstitialAd.showAd();
            } else {
                com.openmediation.sdk.interstitial.InterstitialAd.loadAd();

                adTime = 1;
            }
        } else if (adModel.isAdmobAd) {
            if (admobInterstitialAd != null) {
                adTime = 1;
                admobInterstitialAd.show(activity);
            } else {
                adTime = 1;
                Log.d("TAG", "The interstitial ad wasn't ready yet.");

            }
        }  else if (adModel.isQurekaAds()) {

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();

            Intent intent = new Intent(String.valueOf(Intent.FLAG_ACTIVITY_NO_USER_ACTION));
            intent.setPackage("com.android.chrome");
            PendingIntent pendingIntent = PendingIntent.getActivity(activity,
                    100,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE);

            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_ad);
            builder.setActionButton(bitmap, "Ad", pendingIntent);
            customTabsIntent.launchUrl(activity, Uri.parse(adModel.getQlink()));

        } else {
            if (mInterstitialAd != null) {
                adTime = 1;
                mInterstitialAd.show(activity);
            } else {
                adTime = 1;
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }
        }
    }

    public void bannerAds(Activity activity,ViewGroup linearLayout, final isAdLoaded isAdLoadeda) {
        if (adModel != null) {
            if (adModel.isAD()) {
                if (adModel.isopenmediation) {
                    BannerAd bannerAd = new BannerAd(adModel.getOpenmediationbannerplacementId(), new BannerAdListener() {
                        @Override
                        public void onBannerAdLoaded(String placementId, View view) {
                            // bannerAd is load success
                            if (null != view.getParent()) {
                                ((ViewGroup) view.getParent()).removeView(view);
                            }
                            linearLayout.removeAllViews();
                            linearLayout.addView(view);
                            isAdLoadeda.callback(true);
                        }

                        @Override
                        public void onBannerAdLoadFailed(String s, Error error) {
                            isAdLoadeda.callback(true);
                        }

                        @Override
                        public void onBannerAdClicked(String placementId) {
                            // bannerAd click
                        }
                    });
                    bannerAd.setAdSize(com.openmediation.sdk.banner.AdSize.BANNER);
                    bannerAd.loadAd();

                }
                else if (adModel.isAdmobAd) {
                    AdView adView = new AdView(currentActivity.getApplicationContext());
                    adView.setAdUnitId(adModel.getAdmobbannerAd());
                    adView.setAdSize(AdSize.BANNER);
                    linearLayout.addView(adView);
                    AdRequest build = new AdRequest.Builder().build();
                    adView.loadAd(build);
                    isAdLoadeda.callback(true);
                }else if(adModel.isQurekaAds){
                    RelativeLayout adView = (RelativeLayout) ((Activity) activity).getLayoutInflater().inflate(R.layout.layout_qureka_bannerads, null);

                    ImageView bannerimage = adView.findViewById(R.id.bannerimage);
                    int[] bannerarray = new int[]{R.drawable.banner1, R.drawable.banner2, R.drawable.banner3, R.drawable.banner4};
                    int random = new Random().nextInt(5 - 1);

                    try {
                        bannerimage.setImageDrawable(activity.getResources().getDrawable(bannerarray[random]));
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        bannerimage.setImageDrawable(activity.getResources().getDrawable(R.drawable.banner1));
                    }

                    adView.findViewById(R.id.clickQureka).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        openPlayGames(activity.getApplicationContext());
                        }
                    });

                    linearLayout.removeAllViews();
                    linearLayout.addView(adView);
                }
                else {
                    AdManagerAdView adView = new AdManagerAdView(currentActivity.getApplicationContext());
                    adView.setAdSizes(AdSize.BANNER);
                    adView.setAdUnitId(adModel.getBannerAd());
                    AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
                    adView.loadAd(adRequest);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            // Code to be executed when an ad finishes loading.
                            Log.e(TAG, "onAdFailedToLoadss: ");
                            isAdLoadeda.callback(true);


                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            // Code to be executed when an ad request fails.
                            isAdLoadeda.callback(true);
                            Log.e(TAG, "onAdFailedToLoad: " + adError.getMessage());
                        }

                        @Override
                        public void onAdOpened() {
                            // Code to be executed when an ad opens an overlay that
                            // covers the screen.

                        }

                        @Override
                        public void onAdClicked() {
                            // Code to be executed when the user clicks on an ad.
                        }

                        @Override
                        public void onAdClosed() {
                            // Code to be executed when the user is about to return
                            // to the AdsSdk after tapping on an ad.
                        }
                    });
                    linearLayout.addView(adView);
                }
            } else
                isAdLoadeda.callback(true);
        } else {
            isAdLoadeda.callback(true);
        }

    }


    public void nativeAds(Activity activity, final ViewGroup frameLayout, final isAdLoaded isAdLoadeda) {
        if (adModel != null) {
            if (adModel.isAD()) {
                if (adModel.isopenmediation) {
                    openMediationListener(frameLayout, activity, isAdLoadeda);
                }
                else if (adModel.isAdmobAd) {
                    admobNativeAds(activity, frameLayout, isAdLoadeda);
                }else if(adModel.isQurekaAds){
                    RelativeLayout adView = (RelativeLayout) (activity).getLayoutInflater().inflate(R.layout.layout_qureka_nativeads, null);

                    ImageView nativeimages = adView.findViewById(R.id.nativeimages);
                    int[] nativearray = new int[]{R.drawable.native_banner_1, R.drawable.native_banner_2, R.drawable.native_banner_3, R.drawable.native_banner_4};
                    int random = new Random().nextInt(5 - 1);

                    try {
                        nativeimages.setImageDrawable(activity.getResources().getDrawable(nativearray[random]));
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        nativeimages.setImageDrawable(activity.getResources().getDrawable(R.drawable.native_banner_1));
                    }

                    adView.findViewById(R.id.clickQureka).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openPlayGames(activity.getApplicationContext());
                        }
                    });

                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
                else {
                    final NativeAd[] nativeAds = {null};

                    AdLoader.Builder builder = new AdLoader.Builder(activity, adModel.getNativeAd());

                    NativeAdOptions adOptions = new NativeAdOptions.Builder().build();
                    builder.withNativeAdOptions(adOptions);


                    builder.forNativeAd(
                            new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(NativeAd nativeAd) {
                                    // If this callback occurs after the activity is destroyed, you must call
                                    // destroy and return or you may get a memory leak.
                                    boolean isDestroyed = false;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        isDestroyed = activity.isDestroyed();
                                    }
                                    if (isDestroyed || activity.isFinishing() || activity.isChangingConfigurations()) {
                                        nativeAd.destroy();
                                        return;
                                    }
                                    // You must call destroy on old ads when you are done with them,
                                    // otherwise you will have a memory leak.
                                    if (nativeAds[0] != null) {
                                        nativeAds[0].destroy();
                                    }
                                    nativeAds[0] = nativeAd;
                                    NativeAdView adView;
                                    if (adModel.isCustomeAds) {
                                        adView = (NativeAdView) activity.getLayoutInflater()
                                                .inflate(R.layout.ad_unified, null);
                                    } else {
                                        adView = (NativeAdView) activity.getLayoutInflater()
                                                .inflate(R.layout.ad_unified1, null);
                                    }
                                    populateNativeAdView(nativeAd, adView);
                                    frameLayout.removeAllViews();
                                    frameLayout.addView(adView);
                                }
                            });
                    AdLoader adLoader =
                            builder
                                    .withAdListener(
                                            new AdListener() {
                                                @Override
                                                public void onAdFailedToLoad(LoadAdError loadAdError) {
                                                    String error =
                                                            String.format(
                                                                    "domain: %s, code: %d, message: %s",
                                                                    loadAdError.getDomain(),
                                                                    loadAdError.getCode(),
                                                                    loadAdError.getMessage());
                                                    isAdLoadeda.callback(true);

                                                }

                                                @Override
                                                public void onAdLoaded() {
                                                    super.onAdLoaded();
                                                    isAdLoadeda.callback(true);

                                                }
                                            })
                                    .build();
                    adLoader.loadAd(new AdManagerAdRequest.Builder().build());
                }
            } else {
                isAdLoadeda.callback(true);
            }
        } else {
            isAdLoadeda.callback(true);
        }
    }

    public void admobNativeAds(final Activity activity, final ViewGroup frameLayout, final isAdLoaded isAdLoadeda) {
        if (adModel != null) {
            if (adModel.isAD()) {
                AdLoader adLoader = new AdLoader.Builder(activity, adModel.getAdmobnativeAd())
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                            @Override
                            public void onNativeAdLoaded(NativeAd nativeAd) {

                                NativeAdView adView;
                                // Assumes that your ad layout is in a file call native_ad_layout.xml
                                // in the res/layout folder
                                if (adModel.isCustomeAds) {
                                    adView = (NativeAdView) activity.getLayoutInflater()
                                            .inflate(R.layout.ad_unified, null);
                                } else {
                                    adView = (NativeAdView) activity.getLayoutInflater()
                                            .inflate(R.layout.ad_unified1, null);
                                }
                                // This method sets the text, images and the native ad, etc into the ad
                                // view.
                                populateNativeAdView(nativeAd, adView);
                                frameLayout.removeAllViews();
                                frameLayout.addView(adView);

                                // Show the ad.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    if (activity.isDestroyed()) {
                                        nativeAd.destroy();
                                        return;
                                    }
                                }

                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError adError) {
                                isAdLoadeda.callback(true);
                                // Handle the failure by logging, altering the UI, and so on.
                            }

                            @Override
                            public void onAdLoaded() {

                                isAdLoadeda.callback(true);

                                super.onAdLoaded();

                            }
                        })
                        .withNativeAdOptions(new NativeAdOptions.Builder()
                                // Methods in the NativeAdOptions.Builder class can be
                                // used here to specify individual options settings.
                                .build())
                        .build();
                adLoader.loadAds(new AdRequest.Builder().build(), 3);
            }
        }
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            if (adModel.isCustomeAds) {
                ((AppCompatButton) adView.getCallToActionView()).setText(adModel.customAdText == null ? "OPEN" : adModel.customAdText);

            } else {
                ((AppCompatButton) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

    }


    public void CloseActivityWithAds(Activity activity) {
        if (adModel != null) {
            if (adModel.isAD) {
                if (adModel.isBackGroundAds) {
                    activity.finish();
                    if (adModel.isBackBothAds) {
                        baseApp.showAdIfAvailable(currentActivity, () -> {
                            activity.finish();
                            new ads().showints();
                        });
                    } else {
                        activity.finish();
                        new ads().showints();
                    }
                } else {
                    activity.finish();
                }
            } else {
                activity.finish();
            }
        } else {
            activity.finish();
        }
    }

    private com.openmediation.sdk.nativead.NativeAdView nativeAdView;

    void openMediationListener(ViewGroup adContainer, Activity activity, final isAdLoaded isAdLoadeda) {
        final AdInfo[] adInfo = new AdInfo[1];
        NativeAdListener listener = new NativeAdListener() {
            /**
             * Invoked when Native Ad are available.
             * You can then show the Ad by calling nativeAd.showAd().
             */
            @Override
            public void onNativeAdLoaded(String placementId, AdInfo info) {
                //native ad load success

                adContainer.removeAllViews();
                adInfo[0] = info;
                View adView;

                if (info.isTemplateRender()) {
                    adContainer.addView(info.getView());
                } else {
                    adView = LayoutInflater.from(activity).inflate(R.layout.native_ad_layout, null);
                    TextView title = adView.findViewById(R.id.ad_title);
                    title.setText(info.getTitle());
                    AppCompatButton btn = adView.findViewById(R.id.ad_btn);
                    if (!adModel.isCustomeAds()) {
                        btn.setText(info.getCallToActionText());
                    }
                    if (adModel.isCustomeAds) {
                        btn.setBackgroundResource(R.drawable.mybutton);
                        btn.setText(adModel.customAdText == null ? "OPEN" : adModel.customAdText);
                    } else {
                        btn.setBackgroundColor(activity.getResources().getColor(android.R.color.darker_gray));
                    }
                    com.openmediation.sdk.nativead.MediaView mediaView = adView.findViewById(R.id.ad_media);
                    nativeAdView = new com.openmediation.sdk.nativead.NativeAdView(activity);
                    AdIconView adIconView = adView.findViewById(R.id.ad_icon_media);
                    nativeAdView.addView(adView);
                    nativeAdView.setTitleView(title);
                    nativeAdView.setAdIconView(adIconView);
                    nativeAdView.setCallToActionView(btn);

                    nativeAdView.setMediaView(mediaView);

                    com.openmediation.sdk.nativead.NativeAd.registerNativeAdView(placementId, nativeAdView, info);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    adContainer.addView(nativeAdView, layoutParams);
                }
                isAdLoadeda.callback(true);
            }


            /**
             * Invoked when the call to load a Native Ad has failed
             * String error contains the reason for the failure.
             */
            @Override
            public void onNativeAdLoadFailed(String placementId, Error error) {
                isAdLoadeda.callback(true);
                //native ad load failed
            }

            /**
             * Invoked when an impression is recorded for NativeAd.
             */
            @Override
            public void onNativeAdImpression(String placementId, AdInfo info) {
                //native ad impression
            }

            @Override
            public void onNativeAdClicked(String s, AdInfo adInfo) {

            }

        };
        com.openmediation.sdk.nativead.NativeAd.addAdListener(adModel.getOpenmediationNativeplacementId(), listener);
        com.openmediation.sdk.nativead.NativeAd.loadAd(adModel.getOpenmediationNativeplacementId());
    }
}
