package com.gkzxhn.gkprison.prisonport.http;

import org.apache.http.client.methods.HttpPut;

/**
 * Created by Administrator on 2016/1/29.
 */
public class HttpPatch extends HttpPut {

    public HttpPatch(String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return "PATCH";
    }
}
