package com.lattis.ellipse.domain.model;

public class TermsAndConditions {

    private String version;
    private String content;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TermsAndConditions{" +
                "version='" + version + "'" +
                ", content='" + content + "'}";
    }

}
