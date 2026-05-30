package me.djelectro.servicebase.web;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.jetty.JettyPrecompressingResourceHandler;
import io.javalin.plugin.bundled.CorsPluginConfig;
import me.djelectro.servicebase.SubService;
import me.djelectro.servicebase.utils.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class WebService<T extends WebServiceParams> implements SubService<T> {

    private static final Logger logger = LoggerFactory.getLogger(WebService.class);
    Javalin app;
    boolean enabled = false;
    T params;

    @Override
    public void init(T params) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Javalin app = null;
        boolean devMode = params.devMode;
        int port = params.port;

        try {
            // Increase the file precompression limit to 10 MB
            //JettyPrecompressingResourceHandler.resourceMaxSize = 10 * 1024 * 1024;
            // Check for dev mode
                app = Javalin.create(Jconfig ->
                {
                    for (String x : params.staticFilesUrls) {
                        String updatesAbsolutePath = new File(x).getAbsolutePath();
                        Jconfig.staticFiles.add(staticFileConfig ->
                        {
                            staticFileConfig.hostedPath = "/" + x;
                            staticFileConfig.directory = updatesAbsolutePath;
                            staticFileConfig.location = Location.EXTERNAL;
                            //staticFileConfig.precompress = true;
                        });
                    }
                    Jconfig.bundledPlugins.enableCors(cors -> {
                        //replacement for enableCorsForAllOrigins()
                        cors.addRule(CorsPluginConfig.CorsRule::anyHost);
                    });
                    if(devMode){
                        logger.warn("CAUTION: Development mode enabled. This will result in the use and creation of INSECURE TOKENS.\nDO NOT USE IN PRODUCTION!");
                        Jconfig.bundledPlugins.enableDevLogging();
                    }
                }).start(port);
        } catch (io.javalin.util.JavalinBindException e) {
            if (devMode) {
                logger.error("Port " + port + " appears to already be in use, and development mode is enabled. Assuming you are already running the backend elsewhere. Exiting gracefully.");
                System.exit(0);
            } else {
                logger.error("Port " + port + " appears to already be in use. Program exiting. Please check configuration.");
                System.exit(1);
            }
        }
        // This should never be needed but is here just in case we somehow get through the catch above.
        if (app == null)
            throw new InstantiationException();

        // Start loading tasks
        app.unsafe.routes.get("/", params.rootUrlHandler);

        this.app = app;
        this.params = params;
        enabled = true;

        for(String pkg : packages){
            enableRoutePackage(pkg);
        }

        for(Routes<T> route : routes){
            enableRoute(route);
        }


    }

    List<String> packages = new ArrayList<>();
    List<Routes<T>> routes = new ArrayList<>();

    public void registerRoutePackage(String packageName) {
        packages.add(packageName);

        if(enabled){
            try {
                enableRoutePackage(packageName);
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void enableRoutePackage(String packageName) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Set<Class<?>> classes = ClassFinder.findAllClassesInPackage(params.packageClasspathRoot + "." + packageName);
        logger.info("Found {} classes under package {}", classes.size(), params.packageClasspathRoot);
        for (Class<?> theClass : classes) {
            logger.info("Enabling route package {}", theClass.getSimpleName());
            if (theClass.getSuperclass() == Routes.class) {
                Routes<T> r = (Routes<T>) theClass.getDeclaredConstructor().newInstance();
                enableRoute(r);
            }
        }
    }

    public void registerRoute(Routes<T> routes) {
        this.routes.add(routes);

        if(enabled){
            enableRoute(routes);
        }
    }

    private void enableRoute(Routes<T> r) {
        if (r.shouldActivate()) {
            r.registerRoutes(app, params);
            logger.info("Activated " + r);
        }
    }
}
