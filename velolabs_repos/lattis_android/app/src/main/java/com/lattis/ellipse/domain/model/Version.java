package com.lattis.ellipse.domain.model;

/**
 * Created by ssd3 on 4/26/17.
 */

public class Version {

    private int version;
    private int revision;

    public Version() {
    }

    public Version(int version, int revision) {
        this.version = version;
        this.revision = revision;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public int getVersion() {
        return version;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isInferiorTo(Version version){
        return this.version < version.getVersion() || this.revision < version.getRevision();
    }

    @Override
    public String toString() {
        return "Version{" +
                "version=" + version +
                ", revision=" + revision +
                '}';
    }

    public static class Challenge {

        private Lock lock;

        private Version latestVersion;

        private Version lockVersion;

        public Challenge(Version latestVersion, Lock lock) {
            this.latestVersion = latestVersion;
            this.lock = lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
        }

        public Lock getLock() {
            return lock;
        }

        public void setLatestVersion(Version latestVersion) {
            this.latestVersion = latestVersion;
        }

        public void setLockVersion(Version lockVersion) {
            this.lockVersion = lockVersion;
        }

        public Version getLockVersion() {
            return lockVersion;
        }

        public boolean isSuccessful() {
            return ! lockVersion.isInferiorTo(latestVersion);
        }
    }
}
