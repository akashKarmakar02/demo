package com.lazycoder;

import com.amberj.net.http.HttpRequest;
import com.amberj.net.http.HttpResponse;
import com.amberj.net.httpserver.HttpHandler;

public class IndexHandler implements HttpHandler {

    @Override
    public void get(HttpRequest request, HttpResponse response) {
        response.render("index");
    }
    
}
