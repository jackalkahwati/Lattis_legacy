package cc.skylock.skylock.bluetooth;

import cc.skylock.skylock.Bean.FirmwareUpdates;

/**
 * Created by Velo Labs Android on 22-09-2016.
 */
public interface SkylockBLEFirmwareUpdateStatus {
    public void doUpdateFirmware();

    public void onGetFirmwareImageData(FirmwareUpdates mFirmwareUpdates);

    public void onUpdateFirmwareImage(int mprogressStatus);

    public void oncompleteFirmwareImage();

    public void onCompleteFirmwareWithExisitingVersion();

}
