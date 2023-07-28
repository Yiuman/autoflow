package io.autoflow.common.crud;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author yiuman
 * @date 2023/7/25
 */
@Data
public class BaseEntity<KEY> {
    @TableId
    private KEY id;
    private String creator;
    private String lastModifier;
    private Date createTime;
    private Date updateTime;
}
