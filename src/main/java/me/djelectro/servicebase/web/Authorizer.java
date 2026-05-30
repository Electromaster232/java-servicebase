package me.djelectro.servicebase.web;

import io.javalin.http.Context;

public interface Authorizer {

   boolean authorize(Context ctx);


}
