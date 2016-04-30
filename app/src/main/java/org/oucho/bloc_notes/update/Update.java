package org.oucho.bloc_notes.update;

import java.net.URL;

public class Update {
    private String version;
    private URL apk;

    public Update() {}

    public String getLatestVersion() {
        return version;
    }

    public void setLatestVersion(String latestVersion) {
        this.version = latestVersion;
    }

    public URL getUrlToDownload() {
        return apk;
    }

    public void setUrlToDownload(URL apk) {
        this.apk = apk;
    }
}
