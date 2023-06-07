
import 'package:flutter/material.dart';
import 'package:oktoast/oktoast.dart';

warningToast({Color? color, required String toast}) {
  final Widget widget = Container(
    padding: EdgeInsets.all(10),
    margin: EdgeInsets.symmetric(horizontal: 10),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(10),
    ),
    child: Text(
      toast,
      textAlign: TextAlign.center,
      style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black, fontSize: 20),
    ),
  );

  final ToastFuture toastFuture = showToastWidget(
    widget,
    duration: Duration(seconds: 5),
    position: ToastPosition.center,
    onDismiss: () {
      debugPrint('Toast has been dismissed.');
    },
  );
}
