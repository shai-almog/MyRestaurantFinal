package com.myrestaurant.app.model;

import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.processing.Result;
import com.myrestaurant.app.MyRestaurant;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Fetches an up to date view of the dishes
 */
public class DishService {
    public static boolean hasCachedDishList() {
        return Storage.getInstance().exists("DishListCache");
    }
    
    public static void loadDishesFromCache() {
        JSONParser p = new JSONParser();
        try {
            Map<String, Object> m = p.parseJSON(new InputStreamReader(Storage.getInstance().createInputStream("DishListCache"), "UTF-8"));
            List<Object> l = (List<Object>)m.get("dishes");
            Restaurant.getInstance().menu.get().dishes.clear();
            for(Object o : l) {
                Dish d = new Dish();
                d.getPropertyIndex().populateFromMap((Map)o);
                Restaurant.getInstance().menu.get().dishes.add(d);
            }

            List<String> cats = (List<String>)m.get("categories");
            Restaurant.getInstance().menu.get().categories.clear();
            for(String str : cats) {
                Restaurant.getInstance().menu.get().categories.add(str);
            }
            
            // do this only the first time around, other times are probably "pull-to-refresh"
            if(!Restaurant.getInstance().menu.get().dishDownloadFinished.get()) { 
                Restaurant.getInstance().menu.get().dishDownloadFinished.set(Boolean.TRUE);
            }
        } catch(IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error loading list of dishes: " + err);
            Log.sendLog();
        }
    }
    
    public static void updateDishesFromServerSync() {
        NetworkManager.getInstance().addToQueueAndWait(updateDishesFromServer());
    }
    
    public static void updateDishesFromServerAsync() {
        NetworkManager.getInstance().addToQueue(updateDishesFromServer());
    }

    private static ConnectionRequest updateDishesFromServer() {
        ConnectionRequest cr = new ConnectionRequest(MyRestaurant.SERVER_URL + "dish", false) {
            private boolean autoLoad;
            @Override
            protected void readResponse(InputStream input) throws IOException {
                JSONParser p = new JSONParser();
                Map<String, Object> m = p.parseJSON(new InputStreamReader(input, "UTF-8"));
                String stamp = (String)m.get("stamp");
                if(stamp != null) {
                    Preferences.set("lastTimeStamp", stamp);
                    OutputStream o = Storage.getInstance().createOutputStream("DishListCache");
                    o.write(Result.fromContent(m).toString().getBytes());
                    o.close();
                    autoLoad = true;
                }
            }

            @Override
            protected void postResponse() {
                if(autoLoad) {
                    loadDishesFromCache();
                }
            }
        };
        cr.addArgument("stamp", Preferences.get("lastTimeStamp", "0"));
        cr.addArgument("appId", MyRestaurant.APP_AUTH);
        return cr;
    }
}
