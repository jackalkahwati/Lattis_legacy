package cc.skylock.skylock.bluetooth;

/**
 * Created by admin on 11/02/17.
 */

public class AccelerometerValue {

    float x;
    float y;
    float z;
    float xDev;
    float yDev;
    float zDev;

    public AccelerometerValue(float x,float y,float z,float xDev,float yDev,float zDev){
        this.x = x;
        this.y = y;
        this.z = z;
        this.xDev = xDev;
        this.yDev = yDev;
        this.zDev = zDev;
    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getxDev() {
        return xDev;
    }

    public void setxDev(float xDev) {
        this.xDev = xDev;
    }

    public float getyDev() {
        return yDev;
    }

    public void setyDev(float yDev) {
        this.yDev = yDev;
    }

    public float getzDev() {
        return zDev;
    }

    public void setzDev(float zDev) {
        this.zDev = zDev;
    }
}
