import 'dart:async';
import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_inapp_purchase/flutter_inapp_purchase.dart';

String currentPlan = "";
String notAny = "Not Subscribe Yet.";
String loading = "Loading...";
String plan = "";

showSuccessSnackBar(BuildContext context, String message) {
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      backgroundColor: Colors.transparent,
      elevation: 0,
      content: Container(
        decoration: BoxDecoration(color: Colors.black, borderRadius: BorderRadius.circular(25)),
        height: 50,
        child: Center(
          child: Text(
            "$message"  ,
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.white),
          ),
        ),
      ),
      duration: Duration(seconds: 1),
    ),
  );
}

class inAppPurchase {
  StreamSubscription? purchaseUpdatedSubscription;
  StreamSubscription? purchaseErrorSubscription;
  StreamSubscription? conectionSubscription;
  final List<String> productLists = Platform.isAndroid ? ['day_monthly'] : ['com.app.ontheroadjob.monthlySub'];

  String platformVersion = 'Unknown';
  List<IAPItem>? items = [];
  List<PurchasedItem>? purchases = [];
  List<PurchasedItem>? history = [];

  Future<void> initPlatformState(BuildContext context, Function? functionCall) async {
    var result = await FlutterInappPurchase.instance.initialize();
    print('result: $result');

    conectionSubscription = FlutterInappPurchase.connectionUpdated.listen((connected) {
      print('connected: $connected');
    });

    purchaseUpdatedSubscription = FlutterInappPurchase.purchaseUpdated.listen((productItem) async {
      print('purchase-updated: ${productItem?.productId}');

      if (Platform.isAndroid) {
        var acknowledgeAndroidReceipt =
            await FlutterInappPurchase.instance.acknowledgePurchaseAndroid(productItem!.purchaseToken!);
        print(">>> $acknowledgeAndroidReceipt <<<");
        this.getPreviousPurchases(functionCall, context);
      } else if (Platform.isIOS) {
        if (Platform.isIOS) {
          FlutterInappPurchase.instance.finishTransactionIOS(productItem!.transactionId!).then((value) {
            print("<><>> ${value}");
          }).catchError((onError) {
            print(">> ${onError.toString()}");
          });
        }
      }
    });

    purchaseErrorSubscription = FlutterInappPurchase.purchaseError.listen((purchaseError) {
      print('purchase-error: $purchaseError');
      if (purchaseError?.code == "E_USER_CANCELLED") {
        showSuccessSnackBar(context, "Process Cancel");
      }
    });

    this.getProductFromStore(functionCall);
    this.getPreviousPurchases(functionCall, context);
  }

  Future<void> buySubscription(
    IAPItem? item,
  ) async {
    // FlutterInappPurchase.instance.clearTransactionIOS().then((value) {
    //   print("dkgn $value");
    // }).catchError((onError) {
    //   print(">>${onError.toString()}");
    // });
    // return;
    if (item != null) {
      FlutterInappPurchase.instance.requestSubscription(item.productId!).then((value) {
        print(">> $value");
      }).catchError((onError) {
        print(">>> ${onError.toString()}");
      });
    }
  }

  Future getPreviousPurchases(Function? functionCall, context) async {
    currentPlan = loading;
    if (functionCall != null) functionCall.call();

    history = Platform.isIOS
        ? await FlutterInappPurchase.instance.getPurchaseHistory()
        : await FlutterInappPurchase.instance.getAvailablePurchases();

    // await FlutterInappPurchase.instance.getAvailablePurchases()

    for (var item in history!) {
      print('${item.transactionDate} : ${item.productId}');
      this.purchases!.add(item);
    }
    if (purchases!.length > 1 && purchases!.length != 0) {
      purchases!.sort((a, b) => b.transactionDate!.compareTo(a.transactionDate!));
    }
    this.purchases = history;
    if (functionCall != null) functionCall.call();

    if (purchases!.length > 0) {
      if (Platform.isIOS) {
        this
            .checkSubscribed(functionCall, purchases![0].productId!, Duration(days: 30, minutes: 0), Duration.zero)
            .then((val) {
          print(">>>> $val");
          if (!val) {
            currentPlan = notAny;
            // Navigator.pop(context);
            if (functionCall != null) functionCall.call();
          } else {
            currentPlan = purchases![0].productId!;
            currentPlan = plan;
            if (functionCall != null) functionCall.call();
          }
          print(">> $currentPlan <<");
        }).catchError((onError) {
          print(">> ${onError.toString()}");
        });
      } else {
        this
            .checkSubscribed(functionCall, purchases![0].productId!, Duration(days: 30, minutes: 0), Duration.zero)
            .then((val) {
          print(">>>> $val");
          if (!val) {
            currentPlan = notAny;
            // Navigator.pop(context);
            if (functionCall != null) functionCall.call();
          } else {
            currentPlan = purchases![0].productId!;
            currentPlan = plan;
            if (functionCall != null) functionCall.call();
          }
          print(">> $currentPlan <<");
        }).catchError((onError) {
          print(">> ${onError.toString()}");
        });
      }
    } else {
      if (Platform.isAndroid) {
        currentPlan = notAny;
        // Navigator.pop(context);
        if (functionCall != null) functionCall.call();
      } else if (Platform.isIOS) {}
    }
  }

  Future<bool> checkSubscribed(Function? functionCall, String sku,
      [Duration duration = const Duration(days: 30), Duration grace = const Duration(days: 0)]) async {
    if (Platform.isIOS) {
      if (history != null && history!.length > 1) {
        history!.sort((a, b) => b.transactionDate!.compareTo(a.transactionDate!));
      }

      for (var purchase in history!) {
        print("1111111");
        if (purchase.productId == sku) {
          Duration difference = DateTime.now().difference(purchase.transactionDate!);
          if (difference.inMinutes <= (duration + grace).inMinutes) {
            currentPlan = "${purchase.productId}";
            if (functionCall != null) functionCall.call();
            return true;
          }
        }
        break;
      }

      return false;
    } else if (Platform.isAndroid) {
      var purchases = await FlutterInappPurchase.instance.getAvailablePurchases();

      for (var purchase in purchases!) {
        if (purchase.productId == sku) return true;
      }
      return false;
    }
    throw PlatformException(code: Platform.operatingSystem, message: "platform not supported");
  }

  Future getProductFromStore(Function? functionCall) async {
    if (Platform.isAndroid) {
      await FlutterInappPurchase.instance.initialize();
    }
    List<IAPItem> items = await FlutterInappPurchase.instance.getSubscriptions(productLists);
    for (var item in items) {
      print('>> ${item.productId}');
      this.items!.add(item);
    }

    this.items = items;
    if (functionCall != null) functionCall.call();
  }
}
