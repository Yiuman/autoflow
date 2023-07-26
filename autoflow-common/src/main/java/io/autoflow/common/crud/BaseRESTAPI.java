package io.autoflow.common.crud;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import io.autoflow.common.http.R;
import io.autoflow.common.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


/**
 * @author yiuman
 * @date 2023/7/25
 */
public interface BaseRESTAPI<ENTITY> {

    default IService<ENTITY> getService() {
        //noinspection Convert2Diamond
        return SpringUtil.getBean(new TypeReference<IService<ENTITY>>() {
        });
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