package io.autoflow.core;

import cn.hutool.core.collection.CollUtil;
import io.autoflow.spi.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/13
 */
class ServicesTest {

    @Test
    public void testLoadServices() {
        List<Service> serviceList = Services.getServiceList();
        Assertions.assertTrue(CollUtil.isNotEmpty(serviceList));
    }

    @Test
    public void testAddService() throws IOException {
        List<Service> serviceList = Services.getServiceList();
        int size = serviceList.size();
        Services.add("/Users/yiumankam/tools/maven-repository/io/autoflow/autoflow-http-plugin/1.0-SNAPSHOT/autoflow-http-plugin-1.0-SNAPSHOT.jar");
        Assertions.assertTrue(Services.getServiceList().size() > size);
    }
}