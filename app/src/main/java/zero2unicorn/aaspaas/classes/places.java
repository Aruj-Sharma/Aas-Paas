package zero2unicorn.aaspaas.classes;

import android.graphics.Bitmap;

public class places {
    private String name,address,isopen,image,placeId;
    private double distance;

    public places() { }

    public places(String image,String name, String address, String isopen,double distance,String placeId) {
        this.image=image;
        this.name = name;
        this.address = address;
        this.isopen = isopen;
        this.distance = distance;
        this.placeId=placeId;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsopen() {
        return isopen;
    }

    public void setIsopen(String isopen) {
        this.isopen = isopen;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
