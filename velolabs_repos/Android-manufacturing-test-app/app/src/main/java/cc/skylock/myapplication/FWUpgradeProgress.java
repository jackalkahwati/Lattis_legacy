package cc.skylock.myapplication;

public class FWUpgradeProgress {
    public int maxProgress;
    public int currentProgress;
    public FWProgress fwProgress;

    public enum FWProgress{
        FW_IN_PROGRESS,
        FW_SUCCESS,
        FW_FAIL
    }
}
