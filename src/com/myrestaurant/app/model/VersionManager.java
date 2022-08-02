package com.myrestaurant.app.model;

import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.myrestaurant.app.MyRestaurant;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class VersionManager {
    private static final long CHECK_DURATION = 7 * 24 * 60 * 60000;
    public static enum VersionStatus {
        CURRENT,
        NEWER_EXISTS,
        MUST_UPGRADE
    }
    
    public static void checkVersion() {
        final double currentVersion = Double.parseDouble(Display.getInstance().getProperty("AppVer", "0"));
        if(Preferences.get("unsupportedVersion", 0.0) > currentVersion) {
            Dialog.show("Unsupported Version", "This app is out of date and no longer supported, please update thru the appstore", "OK", null);
            Display.getInstance().exitApplication();
            return;
        }
        long lastCheck = Preferences.get("lastVersionCheck", (long)0);
        long t = System.currentTimeMillis();
        if(t - lastCheck > CHECK_DURATION) {
            Preferences.set("lastVersionCheck", t);
            ConnectionRequest vers = new ConnectionRequest(MyRestaurant.SERVER_URL + "ver", false) {
                boolean unsupportedVersion;
                boolean newerVersion;
                @Override
                protected void readResponse(InputStream input) throws IOException {
                    JSONParser jp = new JSONParser();
                    Map<String, Object> m = jp.parseJSON(new InputStreamReader(input, "UTF-8"));
                    Double currentVersion = (Double)m.get("currentVersion");
                    Double oldestSupportedVersion = (Double)m.get("oldestSupportedVersion");
                    if(oldestSupportedVersion > currentVersion) {
                        Preferences.set("unsupportedVersion", oldestSupportedVersion);
                        unsupportedVersion = true;
                        return;
                    }
                    if(currentVersion > currentVersion) {
                        newerVersion = true;
                        return;
                    }
                }

                @Override
                protected void postResponse() {
                    if(unsupportedVersion) {
                        Dialog.show("Unsupported Version", "This app is out of date and no longer supported, please update thru the appstore", "OK", null);
                        Display.getInstance().exitApplication();
                    }
                    if(newerVersion) {
                        ToastBar.showMessage("Newer version of this app is available in the appstore!", FontImage.MATERIAL_INFO);
                    }
                }
                
            };
            NetworkManager.getInstance().addToQueue(vers);
        }
    }
}
