package com.kwon.sensorclient;

public class MyGlobals {
    private double x, y, z;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    private static MyGlobals instance = null;
    public static synchronized MyGlobals getInstance(){
        if(instance == null){
            instance = new MyGlobals();
        }
        return instance;
    }
}
