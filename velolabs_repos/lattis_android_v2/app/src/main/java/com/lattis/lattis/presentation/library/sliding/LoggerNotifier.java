package com.lattis.lattis.presentation.library.sliding;

/**
 * @author pa.gulko zTrap (05.07.2017)
 */
interface LoggerNotifier {
    
    void notifyPercentChanged(float percent);
    
    void notifyVisibilityChanged(int visibility);
}
