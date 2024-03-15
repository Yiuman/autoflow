package io.autoflow.common.crud;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import io.autoflow.common.http.R;
import io.autoflow.common.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @param <ENTITY> 实体类型
 * @author yiuman
 * @date 2023/7/25
 */
public interface BaseRESTAPI<ENTITY> {
    Map<Class<?>, IService<?>> SERVICE_MAP = new HashMap<>();

    @SuppressWarnings("unchecked")
    default IService<ENTITY> getService() {
        IService<ENTITY> iService = (IService<ENTITY>) SERVICE_MAP.get(getClass());
        if (Objects.nonNull(iService)) {
            return iService;
        }
        TypeReference<IService<ENTITY>> reference = new TypeReference<>() {
        };
        Type typeArgument = TypeUtil.getTypeArgument(getClass(), 0);
        final ParameterizedType parameterizedType = (ParameterizedType) reference.getType();
        final Class<IService<ENTITY>> rawType = (Class<IService<ENTITY>>) parameterizedType.getRawType();
        final Class<?>[] genericTypes = new Class[]{(Class<?>) typeArgument};
        final String[] beanNames = SpringUtil.getBeanFactory().getBeanNamesForType(ResolvableType.forClassWithGenerics(rawType, genericTypes));
        iService = SpringUtil.getBean(beanNames[0], rawType);
        SERVICE_MAP.put(getClass(), iService);
        return iService;
    }

    @GetMapping
    default R<Page<ENTITY>> page(HttpServletRequest request) {
        cn.hutool.db.Page pageRequest = WebUtils.getPageRequest();
        Page<ENTITY> mfPage = Page.of(pageRequest.getPageNumber(), pageRequest.getPageSize());
        return R.ok(getService().page(mfPage, buildWrapper(request)));
    }

    default QueryWrapper buildWrapper(HttpServletRequest request) {
        Query query = AnnotationUtil.getAnnotation(getClass(), Query.class);
        if (Objects.isNull(query)) {
            return QueryWrapper.create();
        }
        Object queryObject = WebUtils.requestDataBind(query.value(), request);
        return QueryHelper.build(queryObject);
    }

    @GetMapping("/{id}")
    default R<ENTITY> get(@PathVariable String id) {
        return R.ok(getService().getById(id));
    }

    @PostMapping
    default R<ENTITY> post(@RequestBody ENTITY entity) {
        getService().saveOrUpdate(entity);
        return R.ok(entity);
    }

    @DeleteMapping("/{id}")
    default R<Void> delete(@PathVariable String id) {
        getService().removeById(id);
        return R.ok();
    }
}