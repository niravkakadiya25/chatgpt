import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

class MyButton extends StatelessWidget {
  double? width;
  double? height;
  Color? buttonClr;
  Color? textClr;
  BorderRadiusGeometry? borderRadius;
  EdgeInsetsGeometry? padding;
  final String title;
  double? fontSize;
  BoxBorder? border;
  final Function onClick;
  TextStyle? textStyle;

  MyButton({
    Key? key,
    this.width,
    this.height,
    this.buttonClr,
    this.textClr,
    this.borderRadius,
    required this.title,
    this.fontSize,
    this.padding,
    this.border,
    this.textStyle,
    required this.onClick,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      width: width,
      height: height ?? 10.w,
      padding: padding ?? EdgeInsets.all(1.w),
      decoration: BoxDecoration(
          color: buttonClr ?? Colors.black, borderRadius: borderRadius ?? BorderRadius.circular(5), border: border),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () async {
            FocusScope.of(context).requestFocus(FocusNode());
            onClick();
          },
          child: Center(
            child: Text(
              title,
              style: textStyle ??
                  TextStyle(
                    letterSpacing: 0.2.w,
                    color: textClr ?? Colors.white,
                    fontSize: fontSize ?? 13.sp,
                    // fontWeight: FontWeight.w500,
                  ),
            ),
          ),
        ),
      ),
    );
  }
}
