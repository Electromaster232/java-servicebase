package me.djelectro.servicebase.web;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be placed on all routes within the app
 * Placing this annotation is all that is necessary for the Route to be included in the application
 * This is accomplished via the registerRoutes function in the Routes base class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {

    /** The URL of the route, dependent on the URL_BASE of the class */
    String url();

    /** The RouteType according to the RouteType enum (GET, POST, WS, etc..) */
    RouteType type();
    /** The way the application will determine who has access to the Route */

    Class<? extends Authorizer> accessMethod();


}


