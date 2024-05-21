package io.autoflow.core;

import io.autoflow.spi.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
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
    private static Map<String, Service> SERVICE_MAP;

    static {
        ServiceLoader<Service> load = ServiceLoader.load(Service.class);
        load.forEach(SERVICE_LIST::add);
        refreshMap();
    }

    private static void refreshMap() {
        SERVICE_MAP = SERVICE_LIST.stream().collect(Collectors.toMap(Service::getId, service -> service));
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

    public static void add(Path path) throws IOException {
        try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{path.toUri().toURL()})) {
            ServiceLoader<Service> load = ServiceLoader.load(Service.class, urlClassLoader);
            load.findFirst().ifPresent(SERVICE_LIST::add);
            refreshMap();
        }
    }


    public static void add(String path) throws IOException {
        add(Path.of(path));
    }
}
