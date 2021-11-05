# Android10 ANR解决



## 问题描述

部分Android 10手机，比如小米的安卓10系统，会有该问题：

- 当识别 “表情符号字符串” 替换成 “表情图片” 显示在TextView上，如果表情符号过多，会报ANR异常。



问题代码如下：

```java
// 识别表情，将表情字符串替换成表情图片，以富文本的形式显示在TextView中
public static void identifyFaceExpression(TextView textView, CharSequence value, int align, float scale) {
    SpannableString spannableString = replaceEmoticons(textView.getContext(), value, scale, align);
    // android 10手机上具有很多ReplacementSpans时，在view.onMeasure中花费很多时间，因此发生了ANR
    // https://www.soinside.com/question/aw6fPiL6ecbEoeNRdkQtcm
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textView.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);
    }
    textView.setText(spannableString);
}

private static SpannableString replaceEmoticons(Context context, CharSequence value, float scale, int align) {
    if (TextUtils.isEmpty(value)) {
        value = "";
    }
    SpannableString spannableString = new SpannableString(value);
    Matcher matcher = EmojiManager.getPattern().matcher(value);
    while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        String emot = value.toString().substring(start, end);
        Drawable d = getEmotDrawable(context, emot, scale);
        if (d != null) {
            ImageSpan span = new ImageSpan(d, align);
            spannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    return spannableString;
}
```



## 解决过程

首先想到导出anr日志，查看找出原因。



1、导出anr日志方法如下：

https://blog.csdn.net/xjz696/article/details/97958441?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase



2、如何根据anr日志定位问题：

https://www.jianshu.com/p/545e5e7bbf94

发现由于 android.graphics.text.LineBreaker.nComputeLineBreaks 异常。



3、解决问题方法：

https://www.soinside.com/question/aw6fPiL6ecbEoeNRdkQtcm

进一步得出 textview 设置的文本在android 10手机上具有很多ReplacementSpans时，在`view.onMeasure`中花费很多时间，因此发生了ANR。



解决代码如下：

```java
// android 10手机上具有很多ReplacementSpans时，在view.onMeasure中花费很多时间，因此发生了ANR
// https://www.soinside.com/question/aw6fPiL6ecbEoeNRdkQtcm
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    textView.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);
}
```





