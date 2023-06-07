import 'dart:async';

import 'package:chat_gpt_demo/ads/call_ads.dart';
import 'package:chat_gpt_demo/chatbot_screen.dart';
import 'package:chat_gpt_demo/start_screen.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';

class SplashScreenPage extends StatefulWidget {
  const SplashScreenPage({Key? key}) : super(key: key);

  @override
  _SplashScreenPageState createState() => _SplashScreenPageState();
}

class _SplashScreenPageState extends State<SplashScreenPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Center(
        child: Container(
          margin: EdgeInsets.all(100),
          child: Image.asset('assets/logo.png'),
        ),
      ),
    );
  }

  @override
  void initState() {
    navigateToOtherScreen();
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  navigateToOtherScreen() async {
    await Future.delayed(Duration(seconds: 1), () async {
      await CallAds().callAppOpenAds();
      await CallAds().adsONorOFf();
      await CallAds().getOneSignalKey();
      initPlatformState();
    });
    startTimer();
  }

  Future<void> startTimer() async {
    navigateUser();
  }

  navigateUser() async {
    Future.delayed(Duration(seconds: 3), () async {
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => StartScreen()));
    });
  }

  Future<void> initPlatformState() async {
    OneSignal.shared.setLogLevel(OSLogLevel.verbose, OSLogLevel.none);
    OneSignal.shared.setAppId(oneSignalKey);
  }
}
