package me.djelectro.servicebase.web;

import io.javalin.http.Context;

import java.lang.annotation.Annotation;
import java.util.Map;

public class BasicAuthorizers {

    public static class All implements Authorizer {
        @Override
        public boolean authorize(Context ctx, Map<String, Annotation> annotations) {
            return true;
        }
    }
}
