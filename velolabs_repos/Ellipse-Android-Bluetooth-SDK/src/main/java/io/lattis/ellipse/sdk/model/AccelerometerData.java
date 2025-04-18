package io.lattis.ellipse.sdk.model;

public class AccelerometerData {

    private Coordinate deviation;

    private Coordinate mav;

    private int sensitivity;

    public Coordinate getDeviation() {
        return deviation;
    }

    public void setDeviation(Coordinate deviation) {
        this.deviation = deviation;
    }

    public Coordinate getMav() {
        return mav;
    }

    public void setMav(Coordinate mav) {
        this.mav = mav;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    @Override
    public String toString() {
        return "AccelerometerData{" +
                "deviation=" + deviation +
                ", mav=" + mav +
                ", sensitivity=" + sensitivity +
                '}';
    }
}
