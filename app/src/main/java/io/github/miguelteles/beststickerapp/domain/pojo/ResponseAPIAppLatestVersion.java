package io.github.miguelteles.beststickerapp.domain.pojo;

public class ResponseAPIAppLatestVersion extends ResponseAPIBase {

    private Version version;

    public Version getVersion() {
        return version;
    }

    public static class Version {
        private String latestVersion;
        private boolean isUpdateOptional;
        private String[] changes;

        public String getLatestVersion() {
            return latestVersion;
        }

        public void setLatestVersion(String latestVersion) {
            this.latestVersion = latestVersion;
        }

        public boolean isUpdateOptional() {
            return isUpdateOptional;
        }

        public void setUpdateOptional(boolean updateOptional) {
            isUpdateOptional = updateOptional;
        }

        public String[] getChanges() {
            return changes;
        }

        public void setChanges(String[] changes) {
            this.changes = changes;
        }
    }
}
