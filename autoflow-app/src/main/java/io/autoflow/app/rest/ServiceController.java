package io.autoflow.app.rest;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.app.model.ServiceEntity;
import io.autoflow.app.service.ServiceEntityService;
import io.autoflow.core.Linkages;
import io.autoflow.core.Options;
import io.autoflow.spi.model.Option;
import io.autoflow.spi.model.Property;
import io.ola.common.http.R;
import io.ola.common.utils.WebUtils;
import io.ola.crud.rest.BaseQueryAPI;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/27
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController implements BaseQueryAPI<ServiceEntity> {

    private final ServiceEntityService serviceEntityService;

    @GetMapping("/image/{serviceId}")
    public void svg(@PathVariable("serviceId") String serviceId, HttpServletResponse httpServletResponse) throws IOException {
        byte[] bytes = serviceEntityService.getImageBytesByServiceId(serviceId);
        if (ArrayUtil.isNotEmpty(bytes)) {
            httpServletResponse.setContentType("image/svg+xml");
            WebUtils.export(new ByteArrayInputStream(bytes), StrUtil.format("{}.svg", serviceId.replaceAll("\\.", "_")));
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpServletResponse.flushBuffer();
        }

    }

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        ServiceEntity serviceEntity = serviceEntityService.add(file);
        return R.ok(serviceEntity.getId());
    }

    @GetMapping("/properties")
    public R<List<Property>> getProperties(@RequestParam("id") String id, @RequestParam(value = "value", required = false) Object value) {
        return R.ok(Linkages.getLinkageProperties(id, value));
    }

    @GetMapping("/options")
    public R<List<Option>> getOptions(@RequestParam("id") String id) {
        return R.ok(Options.getOptions(id));
    }
}
