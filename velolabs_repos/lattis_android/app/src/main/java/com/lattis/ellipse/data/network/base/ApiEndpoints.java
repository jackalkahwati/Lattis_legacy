package com.lattis.ellipse.data.network.base;

public enum ApiEndpoints {

    STAGING("Staging", "http://lattisapp-development.lattisapi.io/api/"),
    PRODUCTION("Production", "https://lattisappv2.lattisapi.io/api/");

    public final String name;
    public final String url;

    ApiEndpoints(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ApiEndpoints from(String endpoint) {
        for (ApiEndpoints value : values()) {
            if (value.url != null && value.url.equals(endpoint)) {
                return value;
            }
        }
        return null;
    }
}
