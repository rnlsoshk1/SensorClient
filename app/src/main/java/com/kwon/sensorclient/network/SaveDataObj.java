package com.kwon.sensorclient.network;

public class SaveDataObj {
    private double seq;
    private double[] data;

    public SaveDataObj() {
    }
    public SaveDataObj(double seq, double[] data) {
        this.seq = seq;
        this.data = data;
    }

    public double getSeq() {
        return seq;
    }

    public void setSeq(double seq) {
        this.seq = seq;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }
}
