// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.webkit.WebStorage;
import android.webkit.WebViewClient;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.platform.PlatformView;
import java.util.Collections;
import java.util.List;
import java.util.Map;


// import android.hardware.display.DisplayManager;
// import android.webkit.WebStorage;
// import android.webkit.WebViewClient;
// import io.flutter.plugin.common.BinaryMessenger;
// import io.flutter.plugin.common.MethodCall;
// import io.flutter.plugin.common.MethodChannel;
// import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
// import io.flutter.plugin.common.MethodChannel.Result;
// import io.flutter.plugin.platform.PlatformView;

// import android.content.Intent;
// import android.net.Uri;
// import android.annotation.TargetApi;
// import android.app.Activity;
// import android.content.Context;
// import android.os.Build;
// import android.os.Handler;
// import android.view.KeyEvent;
// import android.view.View;
// import android.view.ViewGroup;
// import android.webkit.ValueCallback;
// import android.webkit.WebChromeClient;
// import android.webkit.WebSettings;
// import android.webkit.WebView;
// import android.widget.FrameLayout;
// import android.provider.MediaStore;
// import androidx.core.content.FileProvider;
// import android.database.Cursor;
// import android.provider.OpenableColumns;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Map;
// import java.io.File;
// import java.util.Date;
// import java.io.IOException;
// import java.text.SimpleDateFormat;
// import java.util.Collections;
// import static android.app.Activity.RESULT_OK;

public class FlutterWebView implements PlatformView, MethodCallHandler {
  private static final String JS_CHANNEL_NAMES_FIELD = "javascriptChannelNames";
  private final InputAwareWebView webView;
  private final MethodChannel methodChannel;
  private final FlutterWebViewClient flutterWebViewClient;
  private final Handler platformThreadHandler;

  // private ValueCallback<Uri> mUploadMessage;
  // private ValueCallback<Uri[]> mUploadMessageArray;
  // private final static int FILECHOOSER_RESULTCODE = 1;
  // private Uri fileUri;
  // private Uri videoUri;
  // private ValueCallback<Uri> mUploadCallBack;
  // private ValueCallback<Uri[]> mUploadCallBackAboveL;

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  @SuppressWarnings("unchecked")
  FlutterWebView(
      final Context context,
      BinaryMessenger messenger,
      int id,
      Map<String, Object> params,
      View containerView) {

    DisplayListenerProxy displayListenerProxy = new DisplayListenerProxy();
    DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    displayListenerProxy.onPreWebViewInitialization(displayManager);
    webView = new InputAwareWebView(context, containerView);
    displayListenerProxy.onPostWebViewInitialization(displayManager);

    platformThreadHandler = new Handler(context.getMainLooper());
    // Allow local storage.
    webView.getSettings().setDomStorageEnabled(true);

    methodChannel = new MethodChannel(messenger, "plugins.flutter.io/webview_" + id);
    methodChannel.setMethodCallHandler(this);

    flutterWebViewClient = new FlutterWebViewClient(methodChannel);
    applySettings((Map<String, Object>) params.get("settings"));

    if (params.containsKey(JS_CHANNEL_NAMES_FIELD)) {
      registerJavaScriptChannelNames((List<String>) params.get(JS_CHANNEL_NAMES_FIELD));
    }

    // webView.setWebChromeClient(new WebChromeClient() {
    //   // The undocumented magic method override
    //   // Eclipse will swear at you if you try to put @Override here
    //   // For Android 3.0+
    //   public void openFileChooser(ValueCallback<Uri> uploadMsg) {

    //     mUploadMessage = uploadMsg;
    //     Intent i = new Intent(Intent.ACTION_GET_CONTENT);
    //     i.addCategory(Intent.CATEGORY_OPENABLE);
    //     i.setType("image/*");
    //     startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

    //   }

    //   // For Android 3.0+
    //   public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
    //     mUploadMessage = uploadMsg;
    //     Intent i = new Intent(Intent.ACTION_GET_CONTENT);
    //     i.addCategory(Intent.CATEGORY_OPENABLE);
    //     i.setType("*/*");
    //     startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
    //   }

    //   // For Android 4.1
    //   public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
    //     mUploadMessage = uploadMsg;
    //     Intent i = new Intent(Intent.ACTION_GET_CONTENT);
    //     i.addCategory(Intent.CATEGORY_OPENABLE);
    //     i.setType("image/*");
    //     startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

    //   }

    //   // For Android 5.0+
    //   public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
    //       FileChooserParams fileChooserParams) {
    //     if (mUploadMessageArray != null) {
    //       mUploadMessageArray.onReceiveValue(null);
    //     }
    //     mUploadMessageArray = filePathCallback;

    //     final String[] acceptTypes = getSafeAcceptedTypes(fileChooserParams);
    //     List<Intent> intentList = new ArrayList<Intent>();
    //     fileUri = null;
    //     videoUri = null;
    //     if (acceptsImages(acceptTypes)) {
    //       Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //       fileUri = getOutputFilename(MediaStore.ACTION_IMAGE_CAPTURE);
    //       takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
    //       intentList.add(takePhotoIntent);
    //     }
    //     if (acceptsVideo(acceptTypes)) {
    //       Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    //       videoUri = getOutputFilename(MediaStore.ACTION_VIDEO_CAPTURE);
    //       takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
    //       intentList.add(takeVideoIntent);
    //     }
    //     Intent contentSelectionIntent;
    //     if (Build.VERSION.SDK_INT >= 21) {
    //       final boolean allowMultiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
    //       contentSelectionIntent = fileChooserParams.createIntent();
    //       contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
    //     } else {
    //       contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
    //       contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
    //       contentSelectionIntent.setType("*/*");
    //     }
    //     Intent[] intentArray = intentList.toArray(new Intent[intentList.size()]);

    //     Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
    //     chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
    //     chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
    //     startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    //     return true;
    //   }
    // });

    updateAutoMediaPlaybackPolicy((Integer) params.get("autoMediaPlaybackPolicy"));
    if (params.containsKey("userAgent")) {
      String userAgent = (String) params.get("userAgent");
      updateUserAgent(userAgent);
    }
    if (params.containsKey("initialUrl")) {
      String url = (String) params.get("initialUrl");
      webView.loadUrl(url);
    }
  }

  // private Uri getOutputFilename(String intentType) {
  //   String prefix = "";
  //   String suffix = "";

  //   if (intentType == MediaStore.ACTION_IMAGE_CAPTURE) {
  //     prefix = "image-";
  //     suffix = ".jpg";
  //   } else if (intentType == MediaStore.ACTION_VIDEO_CAPTURE) {
  //     prefix = "video-";
  //     suffix = ".mp4";
  //   }

  //   String packageName = context.getPackageName();
  //   File capturedFile = null;
  //   try {
  //     capturedFile = createCapturedFile(prefix, suffix);
  //   } catch (IOException e) {
  //     e.printStackTrace();
  //   }
  //   return FileProvider.getUriForFile(context, packageName + ".fileprovider", capturedFile);
  // }

  // private File createCapturedFile(String prefix, String suffix) throws IOException {
  //   String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
  //   String imageFileName = prefix + "_" + timeStamp;
  //   File storageDir = context.getExternalFilesDir(null);
  //   return File.createTempFile(imageFileName, suffix, storageDir);
  // }

  // private Boolean acceptsImages(String[] types) {
  //   return isArrayEmpty(types) || arrayContainsString(types, "image");
  // }

  // private Boolean acceptsVideo(String[] types) {
  //   return isArrayEmpty(types) || arrayContainsString(types, "video");
  // }

  // private Boolean arrayContainsString(String[] array, String pattern) {
  //   for (String content : array) {
  //     if (content.contains(pattern)) {
  //       return true;
  //     }
  //   }
  //   return false;
  // }

  // private Boolean isArrayEmpty(String[] arr) {
  //   // when our array returned from getAcceptTypes() has no values set from the
  //   // webview
  //   // i.e. <input type="file" />, without any "accept" attr
  //   // will be an array with one empty string element, afaik
  //   return arr.length == 0 || (arr.length == 1 && arr[0].length() == 0);
  // }

  // private String[] getSafeAcceptedTypes(WebChromeClient.FileChooserParams params) {

  //   // the getAcceptTypes() is available only in api 21+
  //   // for lower level, we ignore it
  //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
  //     return params.getAcceptTypes();
  //   }

  //   final String[] EMPTY = {};
  //   return EMPTY;
  // }

  // @Override
  // protected void onActivityResult(int requestCode, int resultCode, @Nullable
  // Intent data) {
  // super.onActivityResult(requestCode, resultCode, data);
  // if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
  // Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
  // if (result == null && !TextUtils.isEmpty(mCameraFilePath)) {
  // // 看是否从相机返回
  // File cameraFile = new File(mCameraFilePath);
  // if (cameraFile.exists()) {
  // result = Uri.fromFile(cameraFile);
  // sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
  // }
  // }
  // if (result != null) {
  // String path = FileUtils.getPath(this, result);
  // if (!TextUtils.isEmpty(path)) {
  // File f = new File(path);
  // if (f.exists() && f.isFile()) {
  // Uri newUri = Uri.fromFile(f);
  // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
  // if (mUploadCallBackAboveL != null) {
  // if (newUri != null) {
  // mUploadCallBackAboveL.onReceiveValue(new Uri[] { newUri });
  // mUploadCallBackAboveL = null;
  // return;
  // }
  // }
  // } else if (mUploadCallBack != null) {
  // if (newUri != null) {
  // mUploadCallBack.onReceiveValue(newUri);
  // mUploadCallBack = null;
  // return;
  // }
  // }
  // }
  // }
  // }
  // clearUploadMessage();
  // return;
  // }
  // }

  // /**
  // * 打开选择文件/相机
  // */
  // private void showFileChooser() {

  // // Intent intent1 = new Intent(Intent.ACTION_PICK, null);
  // // intent1.setDataAndType(
  // // MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
  // Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
  // intent1.addCategory(Intent.CATEGORY_OPENABLE);
  // intent1.setType("*/*");

  // Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  // mCameraFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
  // + File.separator
  // + System.currentTimeMillis() + ".jpg";
  // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
  // // android7.0注意uri的获取方式改变
  // Uri photoOutputUri = FileProvider.getUriForFile(MainActivity.this,
  // BuildConfig.APPLICATION_ID + ".fileProvider",
  // new File(mCameraFilePath));
  // intent2.putExtra(MediaStore.EXTRA_OUTPUT, photoOutputUri);
  // } else {
  // intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new
  // File(mCameraFilePath)));
  // }

  // Intent chooser = new Intent(Intent.ACTION_CHOOSER);
  // chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
  // chooser.putExtra(Intent.EXTRA_INTENT, intent1);
  // chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent2 });
  // startActivityForResult(chooser, REQUEST_CODE_FILE_CHOOSER);
  // }

  // /**
  // * webview没有选择文件也要传null，防止下次无法执行
  // */
  // private void clearUploadMessage() {
  // if (mUploadCallBackAboveL != null) {
  // mUploadCallBackAboveL.onReceiveValue(null);
  // mUploadCallBackAboveL = null;
  // }
  // if (mUploadCallBack != null) {
  // mUploadCallBack.onReceiveValue(null);
  // mUploadCallBack = null;
  // }
  // }

  @Override
  public View getView() {
    return webView;
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet.
  // Including the
  // annotation would cause compile time failures in versions of Flutter too old
  // to include the new
  // method. However leaving it raw like this means that the method will be
  // ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once flutter/engine#9727 rolls to
  // stable.
  public void onInputConnectionUnlocked() {
    webView.unlockInputConnection();
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet.
  // Including the
  // annotation would cause compile time failures in versions of Flutter too old
  // to include the new
  // method. However leaving it raw like this means that the method will be
  // ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once flutter/engine#9727 rolls to
  // stable.
  public void onInputConnectionLocked() {
    webView.lockInputConnection();
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet. Including the
  // annotation would cause compile time failures in versions of Flutter too old to include the new
  // method. However leaving it raw like this means that the method will be ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once stable passes v1.10.9.
  public void onFlutterViewAttached(View flutterView) {
    webView.setContainerView(flutterView);
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet. Including the
  // annotation would cause compile time failures in versions of Flutter too old to include the new
  // method. However leaving it raw like this means that the method will be ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once stable passes v1.10.9.
  public void onFlutterViewDetached() {
    webView.setContainerView(null);
  }

  @Override
  public void onMethodCall(MethodCall methodCall, Result result) {
    switch (methodCall.method) {
    case "loadUrl":
      loadUrl(methodCall, result);
      break;
    case "updateSettings":
      updateSettings(methodCall, result);
      break;
    case "canGoBack":
      canGoBack(result);
      break;
    case "canGoForward":
      canGoForward(result);
      break;
    case "goBack":
      goBack(result);
      break;
    case "goForward":
      goForward(result);
      break;
    case "reload":
      reload(result);
      break;
    case "currentUrl":
      currentUrl(result);
      break;
    case "evaluateJavascript":
      evaluateJavaScript(methodCall, result);
      break;
    case "addJavascriptChannels":
      addJavaScriptChannels(methodCall, result);
      break;
    case "removeJavascriptChannels":
      removeJavaScriptChannels(methodCall, result);
      break;
    case "clearCache":
      clearCache(result);
      break;
    case "getTitle":
      getTitle(result);
      break;
    default:
      result.notImplemented();
    }
  }

  @SuppressWarnings("unchecked")
  private void loadUrl(MethodCall methodCall, Result result) {
    Map<String, Object> request = (Map<String, Object>) methodCall.arguments;
    String url = (String) request.get("url");
    Map<String, String> headers = (Map<String, String>) request.get("headers");
    if (headers == null) {
      headers = Collections.emptyMap();
    }
    webView.loadUrl(url, headers);
    result.success(null);
  }

  private void canGoBack(Result result) {
    result.success(webView.canGoBack());
  }

  private void canGoForward(Result result) {
    result.success(webView.canGoForward());
  }

  private void goBack(Result result) {
    if (webView.canGoBack()) {
      webView.goBack();
    }
    result.success(null);
  }

  private void goForward(Result result) {
    if (webView.canGoForward()) {
      webView.goForward();
    }
    result.success(null);
  }

  private void reload(Result result) {
    webView.reload();
    result.success(null);
  }

  private void currentUrl(Result result) {
    result.success(webView.getUrl());
  }

  @SuppressWarnings("unchecked")
  private void updateSettings(MethodCall methodCall, Result result) {
    applySettings((Map<String, Object>) methodCall.arguments);
    result.success(null);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void evaluateJavaScript(MethodCall methodCall, final Result result) {
    String jsString = (String) methodCall.arguments;
    if (jsString == null) {
      throw new UnsupportedOperationException("JavaScript string cannot be null");
    }
    webView.evaluateJavascript(jsString, new android.webkit.ValueCallback<String>() {
      @Override
      public void onReceiveValue(String value) {
        result.success(value);
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void addJavaScriptChannels(MethodCall methodCall, Result result) {
    List<String> channelNames = (List<String>) methodCall.arguments;
    registerJavaScriptChannelNames(channelNames);
    result.success(null);
  }

  @SuppressWarnings("unchecked")
  private void removeJavaScriptChannels(MethodCall methodCall, Result result) {
    List<String> channelNames = (List<String>) methodCall.arguments;
    for (String channelName : channelNames) {
      webView.removeJavascriptInterface(channelName);
    }
    result.success(null);
  }

  private void clearCache(Result result) {
    webView.clearCache(true);
    WebStorage.getInstance().deleteAllData();
    result.success(null);
  }

  private void getTitle(Result result) {
    result.success(webView.getTitle());
  }

  private void applySettings(Map<String, Object> settings) {
    for (String key : settings.keySet()) {
      switch (key) {
        case "jsMode":
          updateJsMode((Integer) settings.get(key));
          break;
        case "hasNavigationDelegate":
          final boolean hasNavigationDelegate = (boolean) settings.get(key);

          final WebViewClient webViewClient =
              flutterWebViewClient.createWebViewClient(hasNavigationDelegate);

          webView.setWebViewClient(webViewClient);
          break;
        case "debuggingEnabled":
          final boolean debuggingEnabled = (boolean) settings.get(key);

          webView.setWebContentsDebuggingEnabled(debuggingEnabled);
          break;
        case "gestureNavigationEnabled":
          break;
        case "userAgent":
          updateUserAgent((String) settings.get(key));
          break;
        default:
          throw new IllegalArgumentException("Unknown WebView setting: " + key);
      }
    }
  }

  private void updateJsMode(int mode) {
    switch (mode) {
    case 0: // disabled
      webView.getSettings().setJavaScriptEnabled(false);
      break;
    case 1: // unrestricted
      webView.getSettings().setJavaScriptEnabled(true);
      break;
    default:
      throw new IllegalArgumentException("Trying to set unknown JavaScript mode: " + mode);
    }
  }

  private void updateAutoMediaPlaybackPolicy(int mode) {
    // This is the index of the AutoMediaPlaybackPolicy enum, index 1 is
    // always_allow, for all
    // other values we require a user gesture.
    boolean requireUserGesture = mode != 1;
    webView.getSettings().setMediaPlaybackRequiresUserGesture(requireUserGesture);
  }

  private void registerJavaScriptChannelNames(List<String> channelNames) {
    for (String channelName : channelNames) {
      webView.addJavascriptInterface(new JavaScriptChannel(methodChannel, channelName, platformThreadHandler),
          channelName);
    }
  }

  private void updateUserAgent(String userAgent) {
    webView.getSettings().setUserAgentString(userAgent);
  }

  @Override
  public void dispose() {
    methodChannel.setMethodCallHandler(null);
    webView.dispose();
    webView.destroy();
  }
}
