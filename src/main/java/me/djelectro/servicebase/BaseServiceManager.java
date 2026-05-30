package me.djelectro.servicebase;

import java.util.ArrayList;
import java.util.List;

public class BaseServiceManager {

    List<TypedServiceManager<?>> serviceManagers;

    public BaseServiceManager() {
        this.serviceManagers = new ArrayList<>();
    }

    public BaseServiceManager(List<TypedServiceManager<?>> services) {
        this.serviceManagers = services;
    }

    public void addServiceManager(TypedServiceManager<?> service) {
        this.serviceManagers.add(service);
    }

    public void removeServiceManager(TypedServiceManager<?> service) {
        this.serviceManagers.remove(service);
    }

    public void init(){
        for(TypedServiceManager<?> service : this.serviceManagers){
            service.initAllSubServices();
        }
    }


}
