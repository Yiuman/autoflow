package io.autoflow.common.crud;

import com.mybatisflex.annotation.Id;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @param <KEY> 主键类型
 * @author yiuman
 * @date 2023/7/25
 */
@Data
public class BaseEntity<KEY> {
    @Id
    private KEY id;
    private String creator;
    private String lastModifier;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
