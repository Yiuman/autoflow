package io.autoflow.app.rest;

import io.autoflow.app.model.FileResource;
import io.autoflow.app.model.FileResourceStream;
import io.autoflow.app.service.FileResourceService;
import io.ola.common.http.R;
import io.ola.crud.rest.BaseRESTAPI;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for file resource management.
 * Provides file upload, download, and CRUD operations.
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileResourceController implements BaseRESTAPI<FileResource> {

    private final FileResourceService fileResourceService;

    /**
     * Upload a file and return the file ID.
     *
     * @param file the multipart file to upload
     * @return R containing the file ID
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        FileResource fileResource = fileResourceService.upload(file);
        return R.ok(fileResource.getId());
    }

    /**
     * Download a file by ID.
     *
     * @param id the file ID
     * @param response HTTP servlet response
     */
    @GetMapping("/{id}/download")
    public void downloadFile(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        FileResourceStream fileResource = fileResourceService.download(id);
        response.setContentType(fileResource.getContentType());
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileResource.getFilename() + "\"");
        byte[] bytes = fileResource.getBytes();
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
