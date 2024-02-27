package io.autoflow.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Page;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Web相关操作工具
 *
 * @author yiuman
 * @date 2020/4/4
 */
@Slf4j
public final class WebUtils {

    private WebUtils() {
    }

    public static ServletRequestAttributes getServletRequestAttributes() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = getServletRequestAttributes();
        if (Objects.isNull(servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    public static String getRequestParam(String name) {
        return getRequest().getParameter(name);
    }

    public static String getRequestHeader(String name) {
        return getRequest().getHeader(name);
    }

    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }

    public static Object getAttribute(String name, int scope) {
        return getServletRequestAttributes().getAttribute(name, scope);
    }

    /**
     * 类型绑定及参数校验
     *
     * @param entityClass 当前绑定数据的类型
     * @param request     当前请求
     * @param <T>         绑定数据的类型
     * @return 已经绑定好的数据
     * @throws Exception 反射异常、参数校验异常
     */
    public static <T> T bindDataAndValidate(Class<T> entityClass, HttpServletRequest request) throws Exception {
        //构造认证模式实体
        final T supportEntity = WebUtils.requestDataBind(entityClass, request);
        //校验实体参数
        return supportEntity;

    }

    /**
     * 获取请求参数
     *
     * @param request 当前请求
     * @param name    参数名
     * @return 参数值
     */
    public static String getParameter(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    /**
     * 类型绑定请求数据
     *
     * @param objectClass 绑定的数据类型Class
     * @param request     当前请求
     * @param force       是否强制生成
     * @return 已经处理好的绑定数据
     */
    public static <T> T requestDataBind(Class<T> objectClass, HttpServletRequest request, boolean force) {
        if (objectClass == null || request == null) {
            return null;
        }
        //若是get请求或者是form-data则先检验下有没参数
        boolean hasParameterRequest = request instanceof AbstractMultipartHttpServletRequest
                || request.getMethod().equals(HttpMethod.GET.name());
        if (!force && hasParameterRequest && CollectionUtils.isEmpty(request.getParameterMap())) {
            return null;
        }
        //构造实体
        T t = BeanUtils.instantiateClass(objectClass);
        requestDataBind(t, request);
        return t;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCurrentRequestBindData(Class<T> objectClass) {
        HttpServletRequest request = WebUtils.getRequest();
        if (Objects.isNull(request)) {
            return null;
        }
        String key = objectClass.getName() + "_query__parameter";
        Object attribute = request.getAttribute(key);
        if (Objects.nonNull(attribute)) {
            return (T) attribute;
        }
        try {
            return requestDataBind(objectClass, request);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 类型绑定请求数据
     *
     * @param objectClass 绑定的数据类型Class
     * @param request     当前请求
     * @param <T>         当前绑定实例Class的类型
     * @return 已经处理好的绑定数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T requestDataBind(Class<T> objectClass, HttpServletRequest request) {
        String key = objectClass.getName() + "_query__parameter";
        Object attribute = request.getAttribute(key);
        if (Objects.nonNull(attribute)) {
            return (T) attribute;
        }
        T bindData = requestDataBind(objectClass, request, false);
        request.setAttribute(key, bindData);
        return bindData;
    }

    public static Page getPageRequest() {
        return Optional.ofNullable(WebUtils.getCurrentRequestBindData(Page.class))
                .orElse(Page.of(1, 20));
    }

    public static String getRequestURI() {
        HttpServletRequest request = getRequest();
        if (Objects.isNull(request)) {
            return null;
        }
        return request.getRequestURI();
    }

    /**
     * 实例绑定请求数据
     *
     * @param object  传入的绑定实例
     * @param request 当前请求
     * @param <T>     当前绑定实例类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void requestDataBind(T object, HttpServletRequest request) {
        if (request instanceof AbstractMultipartHttpServletRequest || request.getMethod().equals(HttpMethod.GET.name())) {
            WebRequestDataBinder dataBinder = new WebRequestDataBinder(object);
            dataBinder.registerCustomEditor(LocalDate.class, new LocalDateEditor());
            dataBinder.bind(new ServletWebRequest(request));

        } else {
            try {
                if (!(request instanceof JsonHttpServletRequestWrapper)) {
                    request = new JsonHttpServletRequestWrapper(request);
                }

                if (object.getClass().isArray()) {
                    BeanUtils.copyProperties(object, ((JsonHttpServletRequestWrapper) request).getArray());
                } else {
                    ServletInputStream inputStream = request.getInputStream();
                    if (!inputStream.isFinished()) {
                        T realT = (T) JSONUtil.toBean(IoUtil.readUtf8(inputStream), object.getClass());
                        BeanUtils.copyProperties(realT, object);
                    }

                }

            } catch (Throwable ex) {
                log.info("请求数据转换异常", ex);
            }

        }

    }

    /**
     * 获取cookie值
     *
     * @param request 当前请求
     * @param name    cookie参数名
     * @return cookie值
     */
    public static String getCookie(HttpServletRequest request, String name) {
        Optional<Cookie> optionalCookie = Arrays.stream(request.getCookies()).filter(cookie -> name.equals(cookie.getName())).findFirst();
        return optionalCookie.map(Cookie::getValue).orElse(null);
    }

    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * <p>
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * <p>
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     * <p>
     * 用户真实IP为： 192.168.1.110
     *
     * @param request 请求
     * @return IPADDRESS ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader(IpHeaders.X_FORWARDED_FOR);
        final Predicate<String> ipPredicate = ip1 -> ip1 == null || ip1.isEmpty() || IpHeaders.UNKNOWN.equalsIgnoreCase(ip1);
        if (ipPredicate.test(ip)) {
            ip = request.getHeader(IpHeaders.PROXY_CLIENT_IP);
        }
        if (ipPredicate.test(ip)) {
            ip = request.getHeader(IpHeaders.WL_PROXY_CLIENT_IP);
        }
        if (ipPredicate.test(ip)) {
            ip = request.getHeader(IpHeaders.HTTP_CLIENT_IP);
        }
        if (ipPredicate.test(ip)) {
            ip = request.getHeader(IpHeaders.HTTP_X_FORWARDED_FOR);
        }
        if (ipPredicate.test(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getClientIpAddress() {
        return getIpAddress(Objects.requireNonNull(WebUtils.getRequest()));
    }

    /**
     * 添加导出文件名的请求头
     *
     * @param response 当前请求的响应
     * @param filename 文件名
     * @throws UnsupportedEncodingException 编码异常
     */
    public static void addExportFilenameHeaders(HttpServletResponse response, String filename) throws UnsupportedEncodingException {
        int lastPointIndex = filename.lastIndexOf(".");
        addExportFilenameHeaders(response, filename.substring(0, lastPointIndex), filename.substring(lastPointIndex + 1));
    }

    /**
     * 添加导出文件名的请求头
     *
     * @param response 当前请求的响应
     * @param filename 文件名
     * @throws UnsupportedEncodingException 编码异常
     */
    public static void addExportFilenameHeaders(HttpServletResponse response, String filename, String fileType) throws UnsupportedEncodingException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s.%s", URLEncoder.encode(filename, StandardCharsets.UTF_8), fileType));
    }

    /**
     * 导出资源文件
     *
     * @param resource 资源文件
     * @throws IOException IO异常
     */
    public static void export(Resource resource) throws IOException {
        export(resource, resource.getFilename());
    }

    /**
     * 导出资源文件
     *
     * @param resource 资源文件
     * @param filename 文件名
     * @throws IOException IO异常
     */
    public static void export(Resource resource, String filename) throws IOException {
        HttpServletResponse response = getResponse();
        addExportFilenameHeaders(response, filename);
        StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
        response.flushBuffer();
    }


    /**
     * 导出资源文件
     *
     * @param stream   输入流
     * @param filename 文件名
     * @throws IOException IO异常
     */
    public static void export(InputStream stream, String filename) throws IOException {
        HttpServletResponse response = getResponse();
        addExportFilenameHeaders(response, filename);
        StreamUtils.copy(stream, response.getOutputStream());
        response.flushBuffer();
    }


    /**
     * 根据当前请求获取controller定义了的路径
     * 如：@RequestMapping('/user/{key}') 则返回/user/{key}，若没定义则返回null
     *
     * @param request 当前请求
     * @return 匹配的定义路径
     * @throws Exception 反射异常
     */
    public static String getRequestMapping(HttpServletRequest request) throws Exception {
        final String slash = "/";
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
        if (handler == null) {
            return null;
        }

        HandlerMethod handlerHandler = (HandlerMethod) handler.getHandler();

        String beanPath = null;
        String[] annotatedElementMappings = getAnnotatedElementMappings(handlerHandler.getBeanType());
        if (ArrayUtil.isNotEmpty(annotatedElementMappings)) {
            List<String> sourcePaths = Arrays.stream(annotatedElementMappings).filter(annotatedElementMapping -> request.getRequestURI().contains(annotatedElementMapping))
                    .collect(Collectors.toList());
            beanPath = CollUtil.getFirst(sourcePaths);
        }

        assert beanPath != null;
        String combinePath = requestMappingHandlerMapping
                .getPathMatcher()
                .combine(beanPath, Objects.requireNonNull(getAnnotatedElementMapping(handlerHandler.getMethod())));
        if (StringUtils.hasLength(combinePath) && !combinePath.startsWith(slash)) {
            combinePath = slash + combinePath;
        }

        return combinePath;
    }


    /**
     * 获取当前请求的MVC定义了的路径
     *
     * @return controller定义了的路径
     * @throws Exception 反射异常
     */
    public static String getCurrentRequestMapping() throws Exception {
        return getRequestMapping(getRequest());
    }

    /**
     * 根据当前请求获取当前请求的mvc处理器方法
     *
     * @param request 当前请求
     * @return mvc处理器方法
     * @throws Exception 反射异常
     */
    public static HandlerMethod getRequestHandlerMethod(HttpServletRequest request) throws Exception {
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
        if (handler == null) {
            return null;
        }

        return (HandlerMethod) handler.getHandler();
    }


    /**
     * 根据当前请求获取请求处理的方法
     *
     * @param request 当前请求
     * @return Method，处理这个请求的真正的代码方法
     * @throws Exception 反射异常
     */
    public static Method getRequestRealHandleMethod(HttpServletRequest request) throws Exception {
        HandlerMethod requestHandlerMethod = getRequestHandlerMethod(request);
        return Objects.isNull(requestHandlerMethod) ? null : requestHandlerMethod.getMethod();
    }

    /**
     * 根据当前请求获取请求处理类
     *
     * @param request 当前请求
     * @return class，处理这个请求的类
     * @throws Exception 反射异常
     */
    public static Class<?> getRequestHandlerBeanType(HttpServletRequest request) throws Exception {
        HandlerMethod requestHandlerMethod = getRequestHandlerMethod(request);
        return Objects.isNull(requestHandlerMethod) ? null : requestHandlerMethod.getBeanType();
    }


    /**
     * 获取可注解节点的Mapping值
     *
     * @param element 注解节点，如Class、Method等
     * @return requestMappingPath
     */
    public static String getAnnotatedElementMapping(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        if (requestMapping == null) {
            return null;
        }
        String[] paths = requestMapping.path();
        if (ObjectUtils.isEmpty(paths) || !StringUtils.hasLength(paths[0])) {
            return null;
        }
        return paths[0];
    }

    public static String[] getAnnotatedElementMappings(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        if (requestMapping == null) {
            return null;
        }
        return requestMapping.path();

    }


    interface IpHeaders {
        String UNKNOWN = "unknown";
        String X_FORWARDED_FOR = "x-forwarded-for";
        String PROXY_CLIENT_IP = "Proxy-Client-IP";
        String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
        String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
        String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    }

    /**
     * LocalDate表单字段编辑器
     */
    static class LocalDateEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(LocalDate.parse(text));
        }
    }
}
