package io.lattis.ellipse.sdk.util;

import android.util.Log;

public class Logger {

    public enum Level {
        NONE,
        BLUETOOTH,
        ELLIPSE,
        FULL,
    }

    private Level currentLevel;

    public Logger(Level level) {
        this.currentLevel = level;
    }

    public void d(Level level, String tag, String message){
        if(currentLevel == level){
            Log.d(tag,message);
        }
    }

    public void e(Level level, String tag, String message){
        if(currentLevel == level){
            Log.e(tag,message);
        }
    }

    public void i(Level level, String tag, String message){
        if(currentLevel == level){
            Log.i(tag,message);
        }
    }

    public void v(Level level, String tag, String message){
        if(currentLevel == level){
            Log.v(tag,message);
        }
    }

    public void w(Level level, String tag, String message){
        if(currentLevel == level){
            Log.w(tag,message);
        }
    }

    public void wtf(Level level, String tag, String message){
        if(currentLevel == level){
            Log.wtf(tag,message);
        }
    }
}
