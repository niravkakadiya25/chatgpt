import 'dart:async';
import 'dart:io';

import 'package:bubble/bubble.dart';
import 'package:chat_gpt_demo/ads/banner_view.dart';
import 'package:chat_gpt_demo/ads/call_ads.dart';
import 'package:chat_gpt_demo/main.dart';
import 'package:chat_gpt_demo/subscription/inAppPurchase.dart';
import 'package:chat_gpt_demo/toast.dart';
import 'package:chat_gpt_sdk/chat_gpt_sdk.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_share/flutter_share.dart';
import 'package:keyboard_dismisser/keyboard_dismisser.dart';
import 'package:lottie/lottie.dart';
import 'package:url_launcher/url_launcher.dart';

class ChatBotScreen extends StatefulWidget {
  @override
  _ChatBotScreenState createState() => _ChatBotScreenState();
}

class _ChatBotScreenState extends State<ChatBotScreen> {
  final messageInsert = TextEditingController();
  CompleteRes? _response;
  StreamSubscription? subscription;
  final api = ChatGPT.instance;
  List messsages = [];

  int? click;
  var skId;
  bool inApp = false;
  var listing;
  var Id;
  bool? isClicked;
  bool done = false;
  FirebaseFirestore type = FirebaseFirestore.instance;
  inAppPurchase iap = inAppPurchase();

  Widget chat(Map messageList) {
    return Padding(
      padding: EdgeInsets.all(10.0),
      child: GestureDetector(
        onLongPress: () {
          Clipboard.setData(new ClipboardData(text: messageList["data"].toString().replaceAll('\n', '')));
          var snackBar = SnackBar(content: Text('Copied to Clipboard'));
          ScaffoldMessenger.of(context).showSnackBar(snackBar);
        },
        child: Bubble(
          radius: Radius.circular(15.0),
          color: messageList["itsMe"] ? Colors.deepPurple[300] : Colors.white,
          elevation: 0.0,
          alignment: messageList["itsMe"] ? Alignment.topRight : Alignment.topLeft,
          nip: messageList["itsMe"] ? BubbleNip.rightTop : BubbleNip.leftTop,
          child: Padding(
            padding: EdgeInsets.all(2.0),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                CircleAvatar(
                  backgroundImage: AssetImage(messageList["itsMe"] ? "assets/user.jpg" : "assets/bot.jpg"),
                ),
                SizedBox(
                  width: 10.0,
                ),
                Flexible(
                  child: Text(
                    messageList["data"].toString().replaceAll('\n', ''),
                    style: TextStyle(
                        color: messageList["itsMe"] ? Colors.white : Colors.black, fontWeight: FontWeight.bold),
                  ),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    subscription?.cancel();
    api.close();
    super.dispose();
  }

  @override
  void initState() {
    iap.initPlatformState(context, () {
      setState(() {});
    });
    getPrevious();

    connect();

    messsages.clear();
    click = (pref!.getInt('tapCounter') ?? 0);
    isClicked = (pref!.getBool('isClicked') ?? false);
    Id = FirebaseFirestore.instance.collection('Question').doc().id;
    wait();


    WidgetsBinding.instance.addObserver(LifecycleEventHandler(
        resumeCallBack: () async => setState(() {
          if(isAd) {
            CallAds().callInterstitialAds();
          }
          }),
        suspendingCallBack: () async {}));
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return KeyboardDismisser(
      gestures: [GestureType.onTap, GestureType.onPanUpdateDownDirection],
      child: Scaffold(
        backgroundColor: Colors.grey[900],
        appBar: AppBar(
          centerTitle: true,
          // backgroundColor: Colors.deepPurple[500],
          backgroundColor: Colors.blueGrey[600],
          elevation: 1,
          title: Text("AI Chat GPT"),
          actions: [
            inApp
                ? InkWell(
                    onTap: () async {
                      if (iap.items!.isNotEmpty) {
                        iap.buySubscription(
                          iap.items![0],
                        );
                      }
                    },
                    child: Lottie.asset(
                      'assets/inAppPurchase.json',
                      width: 50,
                      height: 50,
                      fit: BoxFit.fill,
                    ),
                  )
                : SizedBox(),
          ],
        ),
        body: Container(
          child: Column(
            children: [
              Expanded(
                flex: 9,
                child: ListView.builder(
                  reverse: true,
                  shrinkWrap: true,
                  itemCount: messsages.length,
                  itemBuilder: (context, index) {
                    List messageList = messsages.reversed.toList();
                    return chat(messageList[index]);
                  },
                ),
              ),
              isAd
                  ? Expanded(
                      flex: 1,
                      child: Container(

                        child: BlurViewBannerWidget(
                          onBlurBannerViewWidgetCreated: _onBlurBannerViewWidgetCreated,
                        ),
                      ),
                    )
                  : SizedBox(),
              Divider(
                height: 5,
              ),
              Container(
                decoration: BoxDecoration(color: Colors.grey[300], borderRadius: BorderRadius.circular(10)),
                padding: EdgeInsets.only(left: 15.0, right: 15.0, bottom: 10, top: 10),
                margin: EdgeInsets.symmetric(horizontal: 8.0),
                child: Row(
                  children: <Widget>[
                    Flexible(
                      child: TextField(
                        controller: messageInsert,
                        decoration: InputDecoration.collapsed(
                            hintText: "Send your message",
                            hintStyle: TextStyle(fontWeight: FontWeight.bold, fontSize: 18.0)),
                      ),
                    ),
                    InkWell(
                      onTap: () async {
                        FocusManager.instance.primaryFocus?.unfocus();
                        if (messageInsert.text.isEmpty) {
                          print("empty message");
                        }
                        if (inApp) {
                          isClicked = await (pref!.getBool("isClicked") ?? false);
                          int tap = await (pref!.getInt("tapCounter") ?? 0);
                          tap++;
                          await pref!.setInt("tapCounter", tap);
                          if (tap > 3) {
                            setState(
                              () {
                                isClicked = true;
                              },
                            );
                            await pref!.setBool('isClicked', isClicked!);
                            await iap.getPreviousPurchases(() {}, context);
                            if (currentPlan.isNotEmpty && currentPlan != notAny) {
                              await sendMessage();
                            } else if (iap.items!.isNotEmpty) {
                              iap.buySubscription(
                                iap.items![0],
                              );
                            }
                          } else {
                            await sendMessage();
                          }
                        }
                        else {
                          await sendMessage();
                        }
                      },
                      child: Container(
                        margin: EdgeInsets.only(right: 10),
                        child: done
                            ? Lottie.asset(
                                'assets/loader.json',
                                width: 50,
                                height: 50,
                                fit: BoxFit.cover,
                              )
                            : Icon(
                                Icons.send,
                                size: 30.0,
                              ),
                      ),
                    )
                  ],
                ),
              ),
              SizedBox(
                height: 15.0,
              )
            ],
          ),
        ),
      ),
    );
  }

  sendMessage() async {
    done = true;
    await addData();
    if (messsages.isEmpty) {
      await _translateEngToThai();
    }
    Future.delayed(Duration(seconds: 1), () async {
      await _translateEngToThai();
      messageInsert.clear();
    });

    setState(() {
      messsages.add(
        {"data": messageInsert.text, "itsMe": true},
      );
    });
  }

  void _onBlurBannerViewWidgetCreated(BlurBannerViewWidgetController controller) {
    if (isAd) {
      setState(() {});
    }
    setState(() {});
  }

  _translateEngToThai() {
    final request = CompleteReq(
      prompt: messageInsert.text,
      model: kTranslateModelV3,
      max_tokens: 100,
    );
    subscription = ChatGPT.instance
        .builder(skId, baseOption: HttpSetup(receiveTimeout: 5000))
        .onCompleteStream(request: request)
        .asBroadcastStream()
        .listen(
      (res) {
        setState(
          () {
            _response = res;
            messsages.add(
              {"data": _response?.choices.first.text, "itsMe": false},
            );
            done = false;
          },
        );
      },
    );
  }

  Future<void> addData() async {
    return await type
        .collection("Question")
        .doc(Id)
        .collection("Questions")
        .doc()
        .set(
          {
            // 'id': Id,
            'question': messageInsert.text,
            'createTime': DateTime.now().microsecondsSinceEpoch,
          },
        )
        .then(
          (value) {},
        )
        .catchError(
          (error) => print("Failed to update user: $error"),
        );
  }

  getList() async {
    await FirebaseFirestore.instance.collection("Credential").get().then((value) {
      setState(() {
        listing = value.docs.first.data();
        var purchase = value.docs.last.data();
        skId = listing["skId"];
        inApp = purchase["inApp"];
      });
    });
    return listing;
  }

  wait() async {
    await getList();
  }

  connect() async {
    try {
      final result = await InternetAddress.lookup('example.com');
      if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {}
    } on SocketException catch (_) {
      warningToast(
        toast: "InterNet Not Connected,Please Connect Internet ",
      );
    }
  }


  getPrevious() async {
    await iap.getPreviousPurchases(() {}, context);
    currentPlan.isNotEmpty && currentPlan != notAny ? isAd = false : true;
    print("==================${isAd}");
    if(isAd){
      CallAds().callInterstitialAds();
    }
    setState(() {});
  }
}

class LifecycleEventHandler extends WidgetsBindingObserver {
  final AsyncCallback resumeCallBack;
  final AsyncCallback suspendingCallBack;

  LifecycleEventHandler({
    required this.resumeCallBack,
    required this.suspendingCallBack,
  });

  @override
  Future<void> didChangeAppLifecycleState(AppLifecycleState state) async {
    switch (state) {
      case AppLifecycleState.resumed:
        await resumeCallBack();
        break;
      case AppLifecycleState.inactive:
      case AppLifecycleState.paused:
      case AppLifecycleState.detached:
        await suspendingCallBack();
        break;
    }
  }
}
