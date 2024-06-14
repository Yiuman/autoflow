package io.autoflow.app.service;

import io.autoflow.app.model.FileResource;
import io.autoflow.app.model.FileResourceStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yiuman
 * @date 2024/6/14
 */
public interface FileResourceService {

    FileResource upload(MultipartFile file);

    FileResourceStream download(String id);

    void remove(String id);
}