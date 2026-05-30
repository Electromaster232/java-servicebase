package me.djelectro.servicebase.web;

import io.javalin.Javalin;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.Header;

import me.djelectro.servicebase.web.Authorizer;
import me.djelectro.servicebase.web.Route;
import me.djelectro.servicebase.web.RouteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Base class that all modules that define application routes should extend.
 * See the Docs for the registerRoutes function
 * <p>
 * Explanation of class variables, most of which should be inherited and changed by the extending class:
 * URL_BASE (String) = The base path all paths defined in this module should inherit. For example, if URL_BASE is "/x" and a function defines its path as "/y", the route will be "/x/y"
 * SHOULD_ACTIVATE (Boolean) = Whether the module is considered "enabled". Can still be overridden as this check is performed as part of the bootstrap
 * AUTH_HELPER_CLASS (Class) = A reference to the class that this module should use to locate authorization functions when accessType is Method on a route. Defaults to self.
 * accessMap (HashMap<Route, Method>) = For routes that define accessType as Method, a map of the Route to authorization Method association
 * dbConn = the instance of me.djelectro.servicebase.webservice.Database the class is using. Set when registerRoutes is called, as this class has no constructor
 * jedisConn = The instance of Jedis the class is using. Set when registerRoutes is called, as this class has no constructor
 */
public abstract class Routes<T extends WebServiceParams> {

  /**
   * The basic URL for this Module
   */
  private final String urlBase;

  /**
   * Override to TRUE if the subclass should be registered by the registration handler
   */
  protected boolean shouldActivate = false;

  public boolean shouldActivate() {
    return shouldActivate;
  }

  /**
   * The map that gets created by registerRoutes when you initialize this class.
   */
  private final HashMap<Route, Method> accessMap = new HashMap<>();



  protected final Logger logger;

  /**
   * The baseclass of all me.djelectro.servicebase.webservice.web.routes.Routes in the program. Initialize this class,
   * specifying a separate class to make authentication calls
   *
   * @param urlBase         The base URL all routes will be registered against in Javalin. Ex. base = "/hello", route = "/world" would register as "/hello/world".
   */
  public Routes(String urlBase) {
    this.urlBase = urlBase;

    logger = LoggerFactory.getLogger(this.getClass());

  }


  /**
    The parameters for this instance of the class
   */
  protected T params;

  public Routes() {
    this("/");
  }



  /**
   * Purpose: Initialize the routes provided by the Module. This function is intended to be called by whatever is bootstrapping the application server
   * (normally the Main function). Keep in mind this function is intended to be inherited by (and executed only on) the subclasses that wish to define routes within the application.
   * The base class defines no routes on its own.
   * <p>
   * The function will search all methods defined in the class and determine if they have the applicable Route annotation. If they do, it will use the properties of the annotation
   * to add those routes to Javalin.
   * This function also registers the authorization middleware within Javalin.
   * <p>
   * Remember to modify URL_BASE for any submodules, this is used to build paths for all routes
   *
   * @param app       The Javalin application to add routes to
   */
  public void registerRoutes(Javalin app, T params) {
    this.params = params;
    preRegister();
    Method[] methods = this.getClass().getDeclaredMethods();

    for (Method method : methods) {
      Annotation[] annotations = method.getDeclaredAnnotations();
      for (Annotation annotation : annotations) {
        if (annotation instanceof Route) {
          if (((Route) annotation).type() == RouteType.GET) {
            app.unsafe.routes.get(urlBase + ((Route) annotation).url(), ctx -> method.invoke(this, ctx));
            accessMap.put(((Route) annotation), method);
          } else if (((Route) annotation).type() == RouteType.POST) {
            app.unsafe.routes.post(urlBase + ((Route) annotation).url(), ctx -> method.invoke(this, ctx));
            accessMap.put(((Route) annotation), method);
          } else if (((Route) annotation).type() == RouteType.PUT)
            app.unsafe.routes.put(urlBase + ((Route) annotation).url(), ctx -> method.invoke(this, ctx));
          else if (((Route) annotation).type() == RouteType.DEL)
            app.unsafe.routes.delete(urlBase + ((Route) annotation).url(), ctx -> method.invoke(this, ctx));
          else if (((Route) annotation).type() == RouteType.WS)
            app.unsafe.routes.ws(urlBase + ((Route) annotation).url(), ctx -> {
              try {
                method.invoke(this, ctx);
              } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage());
              }
            });
          /* Switch statement did not work here for some reason */
        }
      }
    }
    app.unsafe.routes.before(urlBase + "/*", ctx -> {
      if (ctx.method().toString().equals("OPTIONS")) {
        ctx.header(Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        return;
      }
      String myPath = ctx.path();

      Route routeInfo = null;
      Method routeMethod = null;
      for (Route i : accessMap.keySet()) {
        if ((urlBase + i.url()).equals(myPath)) {
          routeMethod = accessMap.get(i);
          routeInfo = i;
        }
      }
      if (routeMethod == null) {
        return;
      }

      boolean result;
      Authorizer a = (Authorizer) routeInfo.accessMethod().getConstructors()[0].newInstance();
      result = a.authorize(ctx);

      if (!result) {
        throw new ForbiddenResponse("Unauthorized");
      }
    });

    app.unsafe.routes.before(urlBase, ctx -> {
      ctx.redirect(ctx.path() + "/");
    });

    postRegister();
  }

  protected void preRegister(){}
  protected void postRegister(){}

}
