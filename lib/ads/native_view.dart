import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef BlurViewWidgetCreatedCallback = void Function(
    BlurViewWidgetController controller);


class BlurViewWidget extends StatefulWidget {
  final BlurViewWidgetCreatedCallback onBlurViewWidgetCreated;

  const BlurViewWidget({
    Key? key,
    required this.onBlurViewWidgetCreated,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => _BlurViewWidgetState();
}

class _BlurViewWidgetState extends State<BlurViewWidget> {
  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'plugins/blur_view_widget',
        onPlatformViewCreated: _onPlatformViewCreated,
      );
    }

    return const Text('iOS platform version is not implemented yet.');
  }

  void _onPlatformViewCreated(int id) {
    if (widget.onBlurViewWidgetCreated == null) {
      return;
    }
    widget.onBlurViewWidgetCreated(BlurViewWidgetController._(id));
  }
}

class BlurViewWidgetController {
  BlurViewWidgetController._(int id)
      : _channel = MethodChannel('plugins/blur_view_widget_$id');

  final MethodChannel _channel;
}
