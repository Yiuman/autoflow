package io.autoflow.app.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.app.model.ServiceEntity;
import io.autoflow.app.service.ServiceEntityService;
import io.autoflow.core.Services;
import io.ola.crud.service.impl.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2024/5/22
 */
@Service
@Slf4j
public class ServiceEntityServiceImpl extends BaseService<ServiceEntity> implements ServiceEntityService {
    private static final Map<String, byte[]> SERVICE_SVG_CACHE = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        addOrUpdateSystemServices();
        loadExtensionServices();
    }

    private void addOrUpdateSystemServices() {
        List<io.autoflow.spi.Service> serviceList = Services.getServiceList();
        List<ServiceEntity> systemServices = serviceList.stream().map(service -> {
            ServiceEntity serviceEntity = new ServiceEntity();
            serviceEntity.setId(service.getId());
            serviceEntity.setName(service.getName());
            serviceEntity.setDescription(service.getDescription());
            serviceEntity.setSystem(true);
            return serviceEntity;
        }).toList();
        saveAll(systemServices);
    }


    private void loadExtensionServices() {
        List<ServiceEntity> extensionServices = findAllExtensionServices();
        for (ServiceEntity extensionService : extensionServices) {
            if (StrUtil.isEmpty(extensionService.getJarFileId())) {
                continue;
            }
            try {
                Services.add(extensionService.getJarFileId());
            } catch (IOException e) {
                log.warn("【Load extension service jar happen error】", e);
            }
        }
    }

    @Override
    public byte[] getImageBytesByServiceId(String serviceId) {
        if (!SERVICE_SVG_CACHE.containsKey(serviceId)) {
            try {
                SERVICE_SVG_CACHE.put(serviceId, ResourceUtil.readBytes(StrUtil.format("{}.svg", serviceId)));
            } catch (Throwable throwable) {
                SERVICE_SVG_CACHE.put(serviceId, new byte[]{});
            }

        }

        return SERVICE_SVG_CACHE.get(serviceId);
    }

    @Override
    public ServiceEntity add(MultipartFile file) {
        //todo
        return null;
    }
}
