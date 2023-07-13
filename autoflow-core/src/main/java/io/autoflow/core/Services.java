package io.autoflow.core;

import io.autoflow.spi.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2023/7/13
 */
public class Services {
    private static final List<Service> SERVICE_LIST = new ArrayList<>();
    private static final Map<String, Service> SERVICE_MAP;

    static {
        ServiceLoader<Service> load = ServiceLoader.load(Service.class);
        load.forEach(SERVICE_LIST::add);
        SERVICE_MAP = SERVICE_LIST.stream().collect(Collectors.toMap(Service::getName, service -> service));
    }

    public static List<Service> getServiceList() {
        return SERVICE_LIST;
    }

    public static Map<String, Service> getServiceMap() {
        return SERVICE_MAP;
    }

    public static Service getService(String name) {
        return SERVICE_MAP.get(name);
    }
}
