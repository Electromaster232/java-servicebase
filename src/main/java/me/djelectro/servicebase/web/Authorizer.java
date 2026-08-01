package me.djelectro.servicebase.web;

import io.javalin.http.Context;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface Authorizer {

   boolean authorize(Context ctx, Map<String, Annotation> annotations);


}
