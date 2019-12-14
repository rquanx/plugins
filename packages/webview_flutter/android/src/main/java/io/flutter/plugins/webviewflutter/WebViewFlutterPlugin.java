// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import io.flutter.plugin.common.PluginRegistry;
import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import io.flutter.plugins.webviewflutter.FlutterWebView;
import android.content.Intent;
import android.net.Uri;

/**
 * Java platform implementation of the webview_flutter plugin.
 *
 * <p>Register this in an add to app scenario to gracefully handle activity and context changes.
 *
 * <p>Call {@link #registerWith(Registrar)} to use the stable {@code io.flutter.plugin.common}
 * package instead.
 */
public class WebViewFlutterPlugin implements FlutterPlugin,PluginRegistry.ActivityResultListener {

  private FlutterCookieManager flutterCookieManager;

  /** Plugin registration. */
  private Activity activity;
  private Context context;

  //add for file
  // private ValueCallback<Uri> mUploadMessage;
  // private ValueCallback<Uri[]> mUploadMessageArray;
  private final static int FILECHOOSER_RESULTCODE=1;
  // private Uri fileUri;
  // private Uri videoUri;
  private static WebViewFactory factory;

  private WebViewFactory factory2;


  /**
   * Add an instance of this to {@link io.flutter.embedding.engine.plugins.PluginRegistry} to
   * register it.
   *
   * <p>THIS PLUGIN CODE PATH DEPENDS ON A NEWER VERSION OF FLUTTER THAN THE ONE DEFINED IN THE
   * PUBSPEC.YAML. Text input will fail on some Android devices unless this is used with at least
   * flutter/flutter@1d4d63ace1f801a022ea9ec737bf8c15395588b9. Use the V1 embedding with {@link
   * #registerWith(Registrar)} to use this plugin with older Flutter versions.
   *
   * <p>Registration should eventually be handled automatically by v2 of the
   * GeneratedPluginRegistrant. https://github.com/flutter/flutter/issues/42694
   */
  public WebViewFlutterPlugin() {}

  /**
   * Registers a plugin implementation that uses the stable {@code io.flutter.plugin.common}
   * package.
   *
   * <p>Calling this automatically initializes the plugin. However plugins initialized this way
   * won't react to changes in activity or context, unlike {@link CameraPlugin}.
   */
  public static void registerWith(Registrar registrar) {
    factory = new WebViewFactory(registrar.messenger(), registrar.view());
    registrar
        .platformViewRegistry()
        .registerViewFactory(
            "plugins.flutter.io/webview",
            factory);
    final WebViewFlutterPlugin instance = new WebViewFlutterPlugin(registrar.activity(),registrar.activeContext());
    registrar.addActivityResultListener(instance);
    new FlutterCookieManager(registrar.messenger());
  }

  
  private WebViewFlutterPlugin(Activity activity, Context context){
    this.activity = activity;
    this.context = context;
  }
  @Override
    public boolean onActivityResult(int i, int i1, Intent intent) {
      
        if(factory!=null){
          factory2 = factory;
          System.out.println(factory2.getFlutterWebView() != null);
          if (factory2.getFlutterWebView() != null) {
            return factory2.getFlutterWebView().resultHandler.handleResult(i, i1, intent);
          }
          return false;
        }else{
          WebViewFactory flutterWebViewFactory = new WebViewFactory(null,null);
          if(flutterWebViewFactory!=null){
            return flutterWebViewFactory.getFlutterWebView().resultHandler.handleResult(i, i1, intent);
          }
          return false;
        }
    }

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    BinaryMessenger messenger = binding.getFlutterEngine().getDartExecutor();
    binding
        .getFlutterEngine()
        .getPlatformViewsController()
        .getRegistry()
        .registerViewFactory(
            "plugins.flutter.io/webview", new WebViewFactory(messenger, /*containerView=*/ null));
    flutterCookieManager = new FlutterCookieManager(messenger);
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    if (flutterCookieManager == null) {
      return;
    }

    flutterCookieManager.dispose();
    flutterCookieManager = null;
  }
}
