import 'package:chat_gpt_demo/ads/banner_view.dart';
import 'package:chat_gpt_demo/ads/call_ads.dart';
import 'package:chat_gpt_demo/ads/native_view.dart';
import 'package:chat_gpt_demo/button.dart';
import 'package:chat_gpt_demo/chatbot_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_share/flutter_share.dart';
import 'package:sizer/sizer.dart';
import 'package:url_launcher/url_launcher.dart';

class StartScreen extends StatefulWidget {
  const StartScreen({Key? key}) : super(key: key);

  @override
  State<StartScreen> createState() => _StartScreenState();
}

class _StartScreenState extends State<StartScreen> {
  double he = 10;
  final url = Uri.parse(
      'https://play.google.com/store/apps/details?id=com.app_chats.gpts');

  Future<bool> exitPopUp(BuildContext context) async {
    return await showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          backgroundColor: Colors.black,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(2.w)),
          title: Column(
            children: [
              Text(
                "Do you want to exit an App?",
                textAlign: TextAlign.center,
                style: TextStyle(
                    color: Colors.white,
                    fontSize: 15.sp,
                    height: 0.3.w,
                    fontWeight: FontWeight.w500),
              ),
            ],
          ),
          actions: [
            Padding(
              padding: EdgeInsets.only(bottom: 3.w),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  MyButton(
                    height: 9.w,
                    width: 25.w,
                    buttonClr: Colors.green,
                    title: "Yes",
                    onClick: () async {
                      SystemNavigator.pop();
                    },
                  ),
                  SizedBox(width: 4.w),
                  MyButton(
                    height: 9.w,
                    width: 25.w,
                    buttonClr: Colors.red,
                    title: "No",
                    onClick: () {
                      Navigator.of(context).pop(false);
                    },
                  ),
                ],
              ),
            )
          ],
        );
      },
    );
  }

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () => exitPopUp(context),
      child: Scaffold(
        backgroundColor: Colors.grey[900],
        appBar: AppBar(
          title: Text("Chat Gpt"),
          backgroundColor: Colors.blueGrey[600],
          centerTitle: true,
          elevation: 1,
        ),
        drawer: Drawer(
          child: ListView(
            // Important: Remove any padding from the ListView.
            padding: EdgeInsets.zero,
            children: <Widget>[
              Container(
                padding:
                    EdgeInsets.only(top: 50, bottom: 30, right: 10, left: 10),
                color: Colors.blueGrey[700],
                child: CircleAvatar(
                  // radius: 100,
                  backgroundColor: Colors.blueGrey[700],
                  radius: 50,
                  child: Image.asset("assets/logo.png"),
                ),
              ),
              ListTile(
                leading: Icon(Icons.share),
                title: Text("Share App"),
                onTap: () {
                  Navigator.pop(context);
                  share();
                },
              ),
              ListTile(
                leading: Icon(Icons.privacy_tip_outlined),
                title: Text("Privacy Policy"),
                onTap: () async {
                  Navigator.pop(context);
                  if (!await launchUrl(
                    Uri.parse(
                        "https://daytodayexpenses.blogspot.com/2023/01/chatgpt-chat-gpt-ai.html"),
                    mode: LaunchMode.externalNonBrowserApplication,
                  )) {
                    throw 'Could not launch $url';
                  }
                },
              ),
              ListTile(
                leading: Icon(Icons.star_rate_rounded),
                title: Text("Rate Us"),
                onTap: () {
                  Navigator.pop(context);
                  _launchInWebViewOrVC();
                },
              ),
            ],
          ),
        ),
        body: Column(
          children: [
            Expanded(
              flex: 9,
              child: SingleChildScrollView(
                child: Padding(
                  padding: EdgeInsets.all(10),
                  child: Column(
                    children: [
                      SizedBox(
                        height: he,
                        child: BlurViewWidget(
                          onBlurViewWidgetCreated: _onBlurViewWidgetCreated,
                        ),
                      ),
                      GestureDetector(
                        onTap: () {
                          Navigator.push(context, MaterialPageRoute(
                            builder: (context) {
                              return ChatBotScreen();
                            },
                          ));
                        },
                        child: Card(
                          elevation: 10,
                          shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10)),
                          child: Container(
                            alignment: Alignment.center,
                            width: MediaQuery.of(context).size.width,
                            height: 60,
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(10),
                              gradient: LinearGradient(
                                begin: Alignment.topCenter,
                                end: Alignment.bottomCenter,
                                stops: [
                                  0.1,
                                  0.3,
                                  0.5,
                                  0.9,
                                ],
                                colors: [
                                  Colors.blueGrey[100]!,
                                  Colors.blueGrey[200]!,
                                  Colors.blueGrey[300]!,
                                  Colors.blueGrey[500]!,
                                ],
                              ),
                            ),
                            child: Text(
                              "Start",
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 20,
                                // fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            isAd != false
                ? Expanded(
                    flex: 1,
                    child: SizedBox(
                      child: BlurViewBannerWidget(
                        onBlurBannerViewWidgetCreated:
                            _onBlurBannerViewWidgetCreated,
                      ),
                    ),
                  )
                : SizedBox(),
          ],
        ),
      ),
    );
  }

  void _onBlurViewWidgetCreated(BlurViewWidgetController controller) {
    if (isAd) {
      he = 350;
      setState(() {});
    }
    setState(() {});
  }

  void _onBlurBannerViewWidgetCreated(
      BlurBannerViewWidgetController controller) {
    if (isAd) {
      setState(() {});
    }
    setState(() {});
  }

  Future<void> share() async {
    await FlutterShare.share(
      title: 'Chat GPT',
      text: 'Check out our new AI Chat Application',
      linkUrl:
          'https://play.google.com/store/apps/details?id=com.app_chats.gpts',
    );
  }

  Future<void> _launchInWebViewOrVC() async {
    if (!await launchUrl(
      url,
      mode: LaunchMode.externalNonBrowserApplication,
    )) {
      throw 'Could not launch $url';
    }
  }
}
