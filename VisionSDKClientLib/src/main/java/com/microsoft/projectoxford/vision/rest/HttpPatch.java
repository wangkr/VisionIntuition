package com.microsoft.projectoxford.vision.rest;

import org.apache.http.client.methods.HttpPost;

class HttpPatch extends HttpPost {
    private static final String METHOD_PATCH = "PATCH";

    public HttpPatch(final String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return METHOD_PATCH;
    }
}
