package io.autoflow.app.service;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.ServiceEntity;
import io.autoflow.app.model.table.Tables;
import io.ola.crud.service.CrudService;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/5/22
 */
public interface ServiceEntityService extends CrudService<ServiceEntity> {

    byte[] getImageBytesByServiceId(String serviceId);

    default List<ServiceEntity> findAllExtensionServices() {
        return list(QueryWrapper.create().where(Tables.SERVICE_ENTITY.SYSTEM.ne(true)));
    }

}
