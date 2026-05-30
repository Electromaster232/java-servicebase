package me.djelectro.servicebase;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/*
Services will register themselves with this class. All services must implement ServiceBase which is a generic requiring an instance of ServiceParams that matches this class
Then, you can call start/stop/restart/etc... on your services.

You can call init() and pass it an instance of your ServiceParams and this will automatically start all services and call their init
BaseServiceManager is responsible for organizing all ServiceManagers
*/
public class TypedServiceManager<T extends ServiceParams> {

    T myParams;
    List<SubService<T>> mySubServices;

    public TypedServiceManager(T myParams) {
        this.myParams = myParams;
        mySubServices = new ArrayList<>();
    }

    public void addService(SubService<T> newService){
        mySubServices.add(newService);
    }

    public void changeParams(T newParams){
        this.myParams = newParams;
    }

    public void initAllSubServices() {
        for (SubService<T> subService : mySubServices) {
            try {
                subService.init(myParams);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
