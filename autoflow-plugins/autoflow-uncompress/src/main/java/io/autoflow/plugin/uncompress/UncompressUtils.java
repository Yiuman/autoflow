package io.autoflow.plugin.uncompress;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author yiuman
 * @date 2025/2/12
 */
@Slf4j
public final class UncompressUtils {
    public static void uncompress(InputStream inputStream, Consumer<CompressFileItem> consumer) {
        try {
            File tempFile = FileUtil.createTempFile();
            FileUtil.writeFromStream(inputStream, tempFile, true);
            Charset charset;
            try (BufferedInputStream fileInputStream = FileUtil.getInputStream(tempFile)) {
                charset = Optional.ofNullable(CharsetUtil.defaultCharset(fileInputStream))
                        .orElse(Charset.defaultCharset());
            } catch (Throwable throwable) {
                charset = StandardCharsets.UTF_8;
            }

            try (BufferedInputStream zipStream = FileUtil.getInputStream(tempFile);
                 ArchiveInputStream<?> archiveInputStream = new ArchiveStreamFactory(charset.name())
                         .createArchiveInputStream(zipStream)) {
                ArchiveEntry entry;
                while ((entry = archiveInputStream.getNextEntry()) != null) {
                    if (!archiveInputStream.canReadEntryData(entry)) {
                        // handle error
                        continue;
                    }

                    //gitee issue #I4ZDQI
                    String path = entry.getName();
                    if (FileUtil.isWindows()) {
                        // Win系统下
                        path = StrUtil.replace(path, "*", "_");
                    }
                    path = path.replaceAll("\\uFEFF", "");
                    if (FileUtil.getName(path).startsWith(".")) {
                        continue;
                    }

                    if (entry.isDirectory()) {
                        CompressFileItem compressFileItem = new CompressFileItem();
                        compressFileItem.setFolder(true);
                        compressFileItem.setPath(path);
                        consumer.accept(compressFileItem);
                        continue;
                    }

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((bytesRead = archiveInputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    CompressFileItem compressFileItem = new CompressFileItem();
                    compressFileItem.setFolder(false);
                    compressFileItem.setPath(path);
                    compressFileItem.setBytes(baos.toByteArray());
                    consumer.accept(compressFileItem);

                }
            } catch (Throwable throwable) {
                throw new IORuntimeException(throwable);
            }
        } catch (Throwable throwable) {
            throw new IORuntimeException(throwable);
        }

    }

    public static void uncompressZip(ZipInputStream zipInputStream, Consumer<CompressFileItem> consumer) {
        try (zipInputStream) {
            ZipEntry zipEntry;
            while (null != (zipEntry = zipInputStream.getNextEntry())) {
                //gitee issue #I4ZDQI
                String path = zipEntry.getName();
                if (FileUtil.isWindows()) {
                    // Win系统下
                    path = StrUtil.replace(path, "*", "_");
                }
                path = path.replaceAll("\\uFEFF", "");
                if (FileUtil.getName(path).startsWith(".")) {
                    continue;
                }

                if (zipEntry.isDirectory()) {
                    CompressFileItem compressFileItem = new CompressFileItem();
                    compressFileItem.setFolder(true);
                    compressFileItem.setPath(path);
                    consumer.accept(compressFileItem);
                    continue;
                }


                byte[] buffer = new byte[1024];
                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                CompressFileItem compressFileItem = new CompressFileItem();
                compressFileItem.setFolder(false);
                compressFileItem.setPath(path);
                compressFileItem.setBytes(baos.toByteArray());
                consumer.accept(compressFileItem);
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    public static void uncompress(InputStream stream, Type type, Consumer<CompressFileItem> consumer) throws IOException {
        //先写入临时文件，方便重复读
        File tempFile = FileUtil.createTempFile();
        FileUtil.writeBytes(stream.readAllBytes(), tempFile);
        StopWatch stopWatch = new StopWatch("【解压耗时】");
        stopWatch.start();
        if (Type.rar.equals(type)) {
            try {
                uncompressRar(FileUtil.getInputStream(tempFile), consumer);
            } catch (Throwable throwable) {
                throw new RuntimeException("解压rar发生错误", throwable);
            }

        } else {
            try {
                try {
                    Charset charset = Optional
                            .ofNullable(CharsetUtil.defaultCharset(FileUtil.getInputStream(tempFile)))
                            .orElse(Charset.defaultCharset());
                    uncompressZip(new ZipInputStream(FileUtil.getInputStream(tempFile), charset), consumer);
                } catch (Throwable ignore) {
                    uncompressZip(new ZipInputStream(FileUtil.getInputStream(tempFile), Charset.defaultCharset()), consumer);
                }
            } catch (Throwable throwable) {
                throw new RuntimeException("解压zip发生错误", throwable);
            }
        }

        stopWatch.stop();
        log.debug("解压耗时: {}s", stopWatch.getTotalTimeSeconds());
    }

    public static void uncompressRar(InputStream inputStream, Consumer<CompressFileItem> consumer) throws IOException {
        IInArchive archive = null;
        ByteArrayStream byteArrayStream = new ByteArrayStream(inputStream.readAllBytes(), false);
        try {
            archive = SevenZip.openInArchive(null, byteArrayStream);
            int[] in = new int[archive.getNumberOfItems()];
            for (int i = 0; i < in.length; i++) {
                in[i] = i;
            }
            archive.extract(in, false, new ExtractCallback(archive, consumer));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != archive) {
                    archive.close();
                }
                if (Objects.nonNull(byteArrayStream)) {
                    byteArrayStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("RAR文件解压成功");
        }
    }

    public enum Type {
        rar,
        zip
    }
}
