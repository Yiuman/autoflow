package io.autoflow.plugin.uncompress;


import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * @author yiuman
 * @date 2023/5/17
 */
@Slf4j
public class ExtractCallback implements IArchiveExtractCallback {

    private final IInArchive inArchive;
    private final Consumer<CompressFileItem> consumer;

    public ExtractCallback(IInArchive inArchive, Consumer<CompressFileItem> consumer) {
        this.inArchive = inArchive;
        this.consumer = consumer;
    }

    public static boolean save2File(File file, byte[] msg) {
        OutputStream fos = null;
        try {
            File parent = file.getParentFile();
            if ((!parent.exists()) && (!parent.mkdirs())) {
                return false;
            }
            fos = new FileOutputStream(file, true);
            fos.write(msg);
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (null != fos) {
                    fos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setCompleted(long arg0) {
    }

    @Override
    public void setTotal(long arg0) {
    }

    @Override
    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
        final String path = (String) inArchive.getProperty(index, PropID.PATH);
        final boolean isFolder = (boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
        return data -> {
            try {
                CompressFileItem compressFileItem = new CompressFileItem();
                compressFileItem.setPath(path);
                compressFileItem.setFolder(isFolder);
                compressFileItem.setBytes(data);
                consumer.accept(compressFileItem);
            } catch (Exception e) {
                log.error("rar getStream happen error", e);
            }
            return data.length;
        };


    }

    @Override
    public void prepareOperation(ExtractAskMode arg0) {
    }

    @Override
    public void setOperationResult(ExtractOperationResult extractOperationResult) {

    }

}