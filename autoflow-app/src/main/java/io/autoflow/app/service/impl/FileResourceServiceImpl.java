package io.autoflow.app.service.impl;

import cn.hutool.json.JSONUtil;
import io.autoflow.app.mapper.FlowMapper;
import io.autoflow.app.model.FileResource;
import io.autoflow.app.model.FileResourceStream;
import io.autoflow.app.service.FileResourceService;
import io.ola.crud.service.impl.BaseService;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.Downloader;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yiuman
 * @date 2024/6/14
 */
@Service
@RequiredArgsConstructor
public class FileResourceServiceImpl extends BaseService<FileResource> implements FileResourceService {
    private final FileStorageService fileStorageService;

    @Override
    public FileResource upload(MultipartFile file) {
        FileInfo fileInfo = fileStorageService.of(file).upload();
        return save(toFileResource(fileInfo));
    }

    @Override
    public FileResourceStream download(String id) {
        FileResource fileResource = get(id);
        Downloader download = fileStorageService.download(new FileInfo()
                .setPlatform(fileResource.getPlatform())
                .setUrl(fileResource.getPath()));
        FileResourceStream fileResourceStream = FlowMapper.INSTANCE.toFileResourceStream(fileResource);
        fileResourceStream.setBytes(download.bytes());
        return fileResourceStream;
    }

    @Override
    public void remove(String id) {
        FileResource fileResource = get(id);
        fileStorageService.delete(new FileInfo()
                .setPlatform(fileResource.getPlatform())
                .setUrl(fileResource.getPath()));
        delete(id);
    }

    private FileResource toFileResource(FileInfo fileInfo) {
        FileResource fileResource = new FileResource();
        fileResource.setId(fileInfo.getId());
        fileResource.setFilename(fileInfo.getFilename());
        fileResource.setSize(fileInfo.getSize());
        fileResource.setPath(fileInfo.getUrl());
        fileResource.setMetadata(JSONUtil.toJsonStr(fileInfo.getMetadata()));
        fileResource.setPlatform(fileInfo.getPlatform());
        fileResource.setContentType(fileInfo.getContentType());
        return fileResource;
    }
}
