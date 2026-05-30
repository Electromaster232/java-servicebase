package me.djelectro.servicebase.web;

import io.javalin.http.Handler;
import me.djelectro.servicebase.ServiceParams;

public class WebServiceParams implements ServiceParams {

    int port;
    boolean devMode;
    Handler rootUrlHandler;
    String[] staticFilesUrls;
    String packageClasspathRoot;

    public WebServiceParams(int port, boolean devMode, Handler rootUrlHandler, String[] staticFilesUrls, String packageClasspathRoot) {
        this.port = port;
        this.devMode = devMode;
        this.rootUrlHandler = rootUrlHandler;
        this.staticFilesUrls = staticFilesUrls;
        this.packageClasspathRoot = packageClasspathRoot;
    }


}
