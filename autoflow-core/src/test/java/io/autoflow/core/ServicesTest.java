package io.autoflow.core;

import cn.hutool.core.collection.CollUtil;
import io.autoflow.spi.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}