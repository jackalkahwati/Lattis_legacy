package io.lattis.ellipse.sdk.model;

public class FirmwareUpdateProgress {

    private int total = 0;

    private int progress = 0;

    private Status status;

    public FirmwareUpdateProgress(int total, int progress) {
        this.total = total;
        this.progress = progress;
        this.status = Status.IN_PROGRESS;
    }

    public FirmwareUpdateProgress(Status status) {
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public int getProgress() {
        return progress;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        IN_PROGRESS,
        IMAGE_INVALID,
        IMAGE_VALID,
    }
}
