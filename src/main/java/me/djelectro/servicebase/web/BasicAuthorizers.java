package me.djelectro.servicebase.web;

import io.javalin.http.Context;

public class BasicAuthorizers {

    public static class All implements Authorizer {
        @Override
        public boolean authorize(Context ctx) {
            return true;
        }
    }
}
