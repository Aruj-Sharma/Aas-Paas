package zero2unicorn.aaspaas.classes;

import android.graphics.Bitmap;

public class Item {
    String image,title;

    public Item(String image, String title) {
        super();
        this.image = image;
        this.title = title;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
