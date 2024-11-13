package io.autoflow.plugin.http;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/2/29
 */
public final class Constants {

    public static final String UNKNOWN = "UNKNOWN";

    public static final List<String> BINARY_CONTENT_TYPES = List.of(
            "image/",
            "audio/",
            "video/",
            "application/octet-stream",
            "application/gzip",
            "application/zip",
            "application/vnd.rar",
            "application/epub+zip",
            "application/x-bzip",
            "application/x-bzip2",
            "application/x-cdf",
            "application/vnd.amazon.ebook",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-fontobject",
            "application/vnd.oasis.opendocument.presentation",
            "application/pdf",
            "application/x-tar",
            "application/vnd.visio",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/x-7z-compressed"
    );

    private Constants() {
    }
}
