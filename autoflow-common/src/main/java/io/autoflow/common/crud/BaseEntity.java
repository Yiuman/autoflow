package io.autoflow.common.crud;

import com.mybatisflex.annotation.Id;
import lombok.Data;

import java.util.Date;

/**
 * @author yiuman
 * @date 2023/7/25
 */
@Data
public class BaseEntity<KEY> {
    @Id
    private KEY id;
    private String creator;
    private String lastModifier;
    private Date createTime;
    private Date updateTime;
}
