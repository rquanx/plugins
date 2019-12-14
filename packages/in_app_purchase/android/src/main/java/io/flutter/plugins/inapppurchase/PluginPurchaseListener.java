// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.inapppurchase;

import static io.flutter.plugins.inapppurchase.Translator.fromPurchasesList;

import androidx.annotation.Nullable;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PluginPurchaseListener implements PurchasesUpdatedListener {
  private final MethodChannel channel;

  PluginPurchaseListener(MethodChannel channel) {
    this.channel = channel;
  }

  @Override
  public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
    final Map<String, Object> callbackArgs = new HashMap<>();
    callbackArgs.put("responseCode", responseCode);
    callbackArgs.put("purchasesList", fromPurchasesList(purchases));
    channel.invokeMethod(InAppPurchasePlugin.MethodNames.ON_PURCHASES_UPDATED, callbackArgs);
  }
}
