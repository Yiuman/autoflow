package io.autoflow.app.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.exceptions.MessageException;
import io.autoflow.app.model.FileResource;
import io.autoflow.app.model.ServiceEntity;
import io.autoflow.app.service.FileResourceService;
import io.autoflow.app.service.ServiceEntityService;
import io.autoflow.core.Services;
import io.autoflow.spi.I18n;
import io.autoflow.spi.utils.I18nUtils;
import io.ola.crud.service.impl.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2024/5/22
 */
@SuppressWarnings("rawtypes")
@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceEntityServiceImpl extends BaseService<ServiceEntity> implements ServiceEntityService, ApplicationRunner {
    private static final Map<String, byte[]> SERVICE_SVG_CACHE = new ConcurrentHashMap<>();
    private final FileResourceService fileResourceService;

    public void init() {
        addOrUpdateSystemServices();
        loadExtensionServices();
    }

    private void addOrUpdateSystemServices() {
        List<io.autoflow.spi.Service> serviceList = Services.getServiceList();
        List<ServiceEntity> systemServices = serviceList.stream().map(this::convert).toList();
         saveAll(systemServices);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ServiceEntity> List<T> list(QueryWrapper queryWrapper) {
        List<ServiceEntity> list = super.list(queryWrapper);
        for (ServiceEntity serviceEntity : list) {
            serviceEntity.setI18n(I18nUtils.getI18n(serviceEntity.getId()));
        }
        return (List<T>) list;
    }

    private ServiceEntity convert(io.autoflow.spi.Service<?> service) {
        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setId(service.getId());
        serviceEntity.setName(service.getName());
        serviceEntity.setProperties(service.getProperties());
        serviceEntity.setOutputProperties(service.getOutputProperties());
        if (service instanceof I18n i18n) {
            serviceEntity.setI18n(i18n.getI18n());
        }

        serviceEntity.setSystem(true);
        return serviceEntity;
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
        try {
            FileResource fileResource = fileResourceService.upload(file);
            io.autoflow.spi.Service service = Services.load(fileResource.getPath());
            Assert.notNull(service, () -> new MessageException("cannot found plugin service class"));
            Assert.isFalse(Services.exists(service), () -> new MessageException("already exists"));
            Services.add(service);
            ServiceEntity serviceEntity = convert(service);
            serviceEntity.setJarFileId(fileResource.getId());
            save(serviceEntity);
            return serviceEntity;
        } catch (MessageException messageException) {
            throw messageException;
        } catch (Throwable throwable) {
            log.error("load jar service happen error", throwable);
            throw new MessageException("add plugin happen error");
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        init();
    }
}
