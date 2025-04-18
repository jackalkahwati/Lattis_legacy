package cc.skylock.skylock.bluetooth;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by admin on 11/02/17.
 */

public class Accelerometer {

    String TAG = "Accelerometer";
    float xValue = 0;
    float yValue = 0;
    float zValue = 0;
    float xStdDev = 0;
    float yStdDev = 0;
    float zStdDev = 0;
    int maxPoints = 0;
    ArrayList<AccelerometerValue> accelerometerValues = new ArrayList<>();

    public void add(AccelerometerValue accelerometerValue) {
        if (maxPoints == 0) {
            return;
        }
        int accelerometerValueSize = accelerometerValues.size();
        if (accelerometerValueSize < maxPoints || accelerometerValueSize == 0) {
            accelerometerValues.add(accelerometerValue);
            return;
        }

        accelerometerValues.remove(0);
        accelerometerValues.add(accelerometerValue);

    }

    public float[] allStdDevs(){
        int accelerometerValueSize = accelerometerValues.size();
        if (accelerometerValueSize == 0 || accelerometerValueSize == 1) {
            return  new float[]  {xValue,yValue,zValue,xStdDev,yStdDev,zStdDev};
        }
        float[] averages = average();
        float xAve,yAve,zAve,xStdAve,yStdAve,zStdAve;
        try{
              xAve = averages[0];
              yAve = averages[1];
              zAve = averages[2];
              xStdAve = averages[3];
              yStdAve = averages[4];
              zStdAve = averages[5];
        }catch (Exception e){
            return  new float[] {0,0,0,0,0,0};
        }
        float xDev = 0;
        float yDev = 0;
        float zDev = 0;
        float xStdDev = 0;
        float yStdDev = 0;
        float zStdDev = 0;
        for (AccelerometerValue accelerometerValue:
                accelerometerValues) {
            xDev +=  Math.pow(accelerometerValue.x-xAve,2.0);
            yDev +=  Math.pow(accelerometerValue.y-yAve,2.0);
            zDev +=  Math.pow(accelerometerValue.z-zAve,2.0);
            xStdDev +=  Math.pow(accelerometerValue.xDev-xStdAve,2.0);
            yStdDev +=  Math.pow(accelerometerValue.yDev-yStdAve,2.0);
            zStdDev +=  Math.pow(accelerometerValue.zDev-zStdAve,2.0);
        }
        float txValue =  stdDev( xDev);
        float tyValue = stdDev( yDev);
        float tzValue = stdDev( zDev);
        float txStdDev = stdDev( xStdDev);
        float tyStdDev = stdDev( yStdDev);
        float tzStdDev = stdDev( zStdDev);
        Log.i("TAG","txValue"+txValue+"tyValue"+tyValue+"tzValue"+tzValue+"txStdDev"+txStdDev+"tyStdDev"+tyStdDev+"tzStdDev"+tzStdDev);
        return new float[]{txValue,tyValue,tzValue,txStdDev,tyStdDev,tzStdDev};
    }

    public float[] average(){
        int accelerometerValueSize = accelerometerValues.size();
        if (accelerometerValueSize == 0 ) {
            return  new float[]  {xValue,yValue,zValue,xStdDev,yStdDev,zStdDev};
        }
        float xAve = 0;
        float yAve = 0;
        float zAve = 0;
        float xStdAve = 0;
        float yStdAve = 0;
        float zStdAve = 0;

        for (AccelerometerValue accelerometerValue:
                accelerometerValues) {
            xAve += accelerometerValue.x;
            yAve += accelerometerValue.y;
            zAve += accelerometerValue.z;
            xStdAve += accelerometerValue.xDev;
            yStdAve += accelerometerValue.yDev;
            zStdAve += accelerometerValue.zDev;
        }
        return new float[]  {xAve/accelerometerValueSize,yAve/accelerometerValueSize,zAve/accelerometerValueSize,xStdAve/accelerometerValueSize,yStdAve/accelerometerValueSize,zStdAve/accelerometerValueSize};
    }

    public float stdDev(float summedValue){
        int accelerometerValueSize = accelerometerValues.size();
        if (accelerometerValueSize == 0 || accelerometerValueSize == 1) {
            return  0;
        }
        return (float) Math.sqrt(summedValue/  (accelerometerValueSize - 1));
    }

    public AccelerometerValue getLastAccValue(){
        AccelerometerValue lastValue = accelerometerValues.get(accelerometerValues.size()-1);
        if(lastValue == null){
            return new AccelerometerValue(0,0,0,0,0,0);
        }
        return lastValue;
    }

    public boolean shouldAlert(){
        return false;
    }
//    func allStdDevs() -> [Value:Float] {
//
//        var xDev:Float = 0.0
//        var yDev:Float = 0.0
//        var zDev:Float = 0.0
//        var xStdDev:Float = 0.0
//        var yStdDev:Float = 0.0
//        var zStdDev:Float = 0.0
//
//        for point in self.points {
//            xDev += powf(point.x - xAve, 2.0)
//            yDev += powf(point.y - yAve, 2.0)
//            zDev += powf(point.z - zAve, 2.0)
//            xStdDev += powf(point.xDev - xStdAve, 2.0)
//            yStdDev += powf(point.yDev - yStdAve, 2.0)
//            zStdDev += powf(point.zDev - zStdAve, 2.0)
//        }
//
//        let txValue =  stdDev(summedValue: xDev)
//        let tyValue = stdDev(summedValue: yDev)
//        let tzValue = stdDev(summedValue: zDev)
//        let txStdDev = stdDev(summedValue: xStdDev)
//        let tyStdDev = stdDev(summedValue: yStdDev)
//        let tzStdDev = stdDev(summedValue: zStdDev)
//        print(txValue, tyValue, tzValue, txStdDev, tyStdDev, tzStdDev)
//        return [
//        .xValue: stdDev(summedValue: xDev),
//        .yValue: stdDev(summedValue: yDev),
//        .zValue: stdDev(summedValue: zDev),
//        .xStdDev: stdDev(summedValue: xStdDev),
//        .yStdDev: stdDev(summedValue: yStdDev),
//        .zStdDev: stdDev(summedValue: zStdDev)
//        ]
//    }

//    func average() -> [Value:Float] {
//        if (self.points.count == 0) {
//            return [.xValue: 0.0, .yValue: 0.0, .zValue: 0.0, .xStdDev: 0.0, .yStdDev: 0.0, .zStdDev: 0.0]
//        }
//
//
//        for point in self.points {
//            xAve += point.x
//            yAve += point.y
//            zAve += point.z
//            xStdAve += point.xDev
//            yStdAve += point.yDev
//            zStdAve += point.zDev
//        }
//
//
//        return [
//        .xValue: xAve/Float(self.points.count),
//        .yValue: yAve/Float(self.points.count),
//        .zValue: zAve/Float(self.points.count),
//        .xStdDev: xStdAve/Float(self.points.count),
//        .yStdDev: yStdAve/Float(self.points.count),
//        .zStdDev: zStdAve/Float(self.points.count)
//        ]
//    }

//    func stdDev(summedValue: Float) -> Float {
//        if self.points.count == 0 || self.points.count == 1 {
//            return 0.0
//        }
//
//        return sqrtf(summedValue/Float(self.points.count - 1))
//    }

//    func getLastAccValue() -> AccelerometerValue {
//        guard let lastValue = self.points.last else {
//            return AccelerometerValue(x: 0, y: 0, z: 0, xDev: 0, yDev: 0, zDev: 0)
//        }
//
//        return lastValue
//    }
//
//    // This method should be overridden in this class' children
//    func shouldAlert() -> Bool {
//        return false
//    }

}
