package io.autoflow.spi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import io.autoflow.spi.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;


/**
 * @author yiuman
 * @date 2024/9/27
 */
@SuppressWarnings("rawtypes")
public final class Linkages {
    private static final List<LinkageProvider> LINKAGE_PROVIDERS = new ArrayList<>();

    static {
        ServiceLoader<LinkageProvider> load = ServiceLoader.load(LinkageProvider.class);
        load.forEach(LINKAGE_PROVIDERS::add);
    }

    public static LinkageProvider<?> getLinkageProvider(String id) {
        return CollUtil.findOne(LINKAGE_PROVIDERS, linkageProvider -> StrUtil.equals(id, linkageProvider.getId()));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Property> getLinkageProperties(String id, Object value) {
        LinkageProvider<T> linkageProvider = (LinkageProvider<T>) getLinkageProvider(id);
        Class<T> valueClass = (Class<T>) TypeUtil.getTypeArgument(linkageProvider.getClass());
        T convertedValue = Convert.convert(valueClass, value);
        return linkageProvider.getLinkageProperties(convertedValue);
    }
}
