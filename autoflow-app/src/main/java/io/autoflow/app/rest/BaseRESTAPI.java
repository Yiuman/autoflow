package io.autoflow.app.rest;

import cn.hutool.db.PageResult;
import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import io.autoflow.app.common.R;
import org.springframework.web.bind.annotation.*;


/**
 * @author yiuman
 * @date 2023/7/25
 */
public interface BaseRESTAPI<ENTITY> {

    IService<ENTITY> getService();

    @GetMapping
    default PageResult<ENTITY> page(HttpServerRequest request) {
        return null;
    }

    default Wrapper<ENTITY> buildWrapper(HttpServerRequest request) {
        return Wrappers.emptyWrapper();
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