package com.myrestaurant.app.payment;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Component;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.FlowLayout;
import com.myrestaurant.app.MyRestaurant;
import com.myrestaurant.app.model.Order;
import com.myrestaurant.app.model.Restaurant;
import com.myrestaurant.app.ui.BaseForm;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

/**
 * This class receives callback from native off of the EDT!
 */
public class BraintreePaymentCallback {
    public static void onPurchaseSuccess(String nonce) {
        Display.getInstance().callSerially(() -> {
            Order o = Restaurant.getInstance().cart.get();
            o.nounce.set(nonce);
            o.auth.set(MyRestaurant.APP_AUTH);
            
            ToastBar.showMessage("Sending purchase request", FontImage.MATERIAL_SHOPPING_CART);

            // send order to the server
            ConnectionRequest cr = new ConnectionRequest(MyRestaurant.SERVER_URL + "braintree", true) {
                private Map<String, Object> m;

                @Override
                protected void buildRequestBody(OutputStream os) throws IOException {
                    os.write(o.toJSON().getBytes("UTF-8"));
                }
                
                
                @Override
                protected void readResponse(InputStream input) throws IOException {
                    JSONParser p  = new JSONParser();
                    m = p.parseJSON(new InputStreamReader(input, "UTF-8"));
                }
                
                @Override
                protected void postResponse() {
                    if(m.get("isSuccess").equals("true")) {
                        Dialog.show("Success", "Your meal is on it's way!", "YUM", null);
                        BaseForm.showMainMenuForm();
                    } else {
                        ToastBar.showErrorMessage("Purchase Error: " + m.get("message"));
                    }
                }
            };
            cr.setContentType("application/json");
            cr.setFailSilently(true);
            cr.setReadResponseForErrors(true);
            NetworkManager.getInstance().addToQueue(cr);        
        });
    }

    public static void onPurchaseFail(String errorMessage) {
        Display.getInstance().callSerially(() -> ToastBar.showErrorMessage("Error during purchase: " + errorMessage));
    }

    public static void onPurchaseCancel() {
    }
}
