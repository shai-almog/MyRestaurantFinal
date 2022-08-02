package com.myrestaurant.app.model;

import com.codename1.io.Log;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Image;
import com.codename1.ui.URLImage;
import com.codename1.ui.util.Resources;
import com.myrestaurant.app.MyRestaurant;
import com.myrestaurant.app.ui.MaskManager;
import java.io.IOException;

/**
 * Abstraction of a generic dish not of an order 
 */
public class Dish implements PropertyBusinessObject {
    private static EncodedImage fullSizePlaceholder;
    private static EncodedImage thumbPlaceholder;
    public final Property<String, Dish> id = new Property<>("id");
    public final Property<Double, Dish> price = new Property<>("price");
    public final Property<String, Dish> name = new Property<>("name");
    public final Property<String, Dish> description = new Property<>("description");
    public final Property<String, Dish> category = new Property<>("category");
    public final Property<String, Dish> imageName = new Property<>("imageName");
    
    private Image thumbnail;
    public Image getThumbnail() {
        if(thumbnail == null) {
            if(thumbPlaceholder == null) {
                thumbPlaceholder = EncodedImage.createFromImage(
                        MaskManager.maskWithRoundRect(
                                Resources.getGlobalResources().getImage("round-rect-mask.png")), false);
            }
            thumbnail = URLImage.createToStorage(thumbPlaceholder, "dishThumbImage" + id.get(), 
                    MyRestaurant.SERVER_URL + "images/dish/" + id.get(), URLImage.createMaskAdapter(thumbPlaceholder));
        }
        return thumbnail;
    }

    private Image fullSize;
    public Image getFullSize() {
        if(fullSize == null) {
            if(fullSizePlaceholder == null) {
                int size = Math.min(Display.getInstance().getDisplayWidth(), 800);
                fullSizePlaceholder = EncodedImage.createFromImage(Image.createImage(size, size), true);
            }
            fullSize = URLImage.createCachedImage("dishImage" + id.get(), 
                    MyRestaurant.SERVER_URL + "images/dish/" + id.get(), fullSizePlaceholder, 
                    URLImage.FLAG_RESIZE_SCALE_TO_FILL);
        }
        return fullSize;
    }
    
    private final PropertyIndex idx = new PropertyIndex(this, "Dish", 
            id, price, name, description, category, imageName);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    @Override
    public boolean equals(Object obj) {
        return name.get().equals(((Dish)obj).name.get());
    }

    @Override
    public int hashCode() {
        return name.get().hashCode();
    }
}
