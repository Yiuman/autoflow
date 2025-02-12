package io.autoflow.plugin.uncompress;

import cn.hutool.core.io.IoUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.FileData;

import java.util.ArrayList;
import java.util.List;

/**
 * 解压
 *
 * @author yiuman
 * @date 2025/2/12
 */
public class UncompressService extends BaseService<UncompressParameter, List<FileData>> {

    @Override
    public String getName() {
        return "Uncompress";
    }

    @Override
    public List<FileData> execute(UncompressParameter uncompressParameter, ExecutionContext executionContext) {
        List<FileData> fileData = new ArrayList<>();
        UncompressUtils.uncompress(
                IoUtil.toStream(uncompressParameter.getFile().getContent()),
                compressFileItem -> {
                    if (!compressFileItem.isFolder()) {
                        fileData.add(
                                new FileData(compressFileItem.getPath(), compressFileItem.getBytes())
                        );
                    }
                }
        );
        return fileData;
    }
}
