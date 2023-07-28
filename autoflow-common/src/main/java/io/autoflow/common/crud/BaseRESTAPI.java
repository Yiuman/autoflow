package io.autoflow.common.crud;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
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
    default PageResult<ENTITY> page(HttpServletRequest request) {
        Page pageRequest = WebUtils.getPageRequest();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ENTITY> mpPage
                = com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageRequest.getPageNumber(), pageRequest.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ENTITY> mpPageResult
                = getService().page(mpPage, buildWrapper(request));
        PageResult<ENTITY> pageResult = new PageResult<>(
                (int) mpPageResult.getCurrent(),
                (int) mpPageResult.getSize(),
                (int) mpPageResult.getTotal()
        );
        pageResult.addAll(mpPageResult.getRecords());
        return pageResult;
    }

    @SuppressWarnings("unchecked")
    default Wrapper<ENTITY> buildWrapper(HttpServletRequest request) {
        Query query = AnnotationUtil.getAnnotation(getClass(), Query.class);
        if (Objects.isNull(query)) {
            return Wrappers.emptyWrapper();
        }
        Object queryObject = WebUtils.requestDataBind(query.value(), request);
        return (Wrapper<ENTITY>) QueryHelper.build(queryObject);
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