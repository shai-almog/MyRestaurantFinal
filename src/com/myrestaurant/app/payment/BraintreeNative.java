package com.myrestaurant.app.payment;

import com.codename1.system.NativeInterface;

public interface BraintreeNative extends NativeInterface {
    public void showChargeUI(String clientToken);
}
