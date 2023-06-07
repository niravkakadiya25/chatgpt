import 'package:chat_gpt_demo/main.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

bool isAd = false;
String oneSignalKey = "";

class CallAds {
  Future callAppOpenAds() async {
    try {
      await platform.invokeMethod('ADOpen');
      return true;
    } on PlatformException catch (e) {
      callInterstitialAds();
      if (kDebugMode) {
        print("Failed to get battery level: '${e.message}'.");
      }
      return true;
    }
  }

  Future callInterstitialAds() async {
    try {
      await platform.invokeMethod('Interstitial');
      return true;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print("Failed to get battery level: '${e.message}'.");
      }
      return true;
    }
  }

  Future callCloseInterstitialAds() async {
    try {
      await platform.invokeMethod('CloseInterstitial');
      return true;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print("Failed to get battery level: '${e.message}'.");
      }
      return true;
    }
  }

  adsONorOFf() async {
    try {
      var result = await platform.invokeMethod('isAD');
      isAd = result;
    } on PlatformException catch (e) {
      isAd = false;
    }
  }
  getOneSignalKey() async {
    try {
      var result = await platform.invokeMethod('oneSignalKey');
      oneSignalKey = result;
    } on PlatformException catch (e) {
      oneSignalKey = "";
    }
  }
}
