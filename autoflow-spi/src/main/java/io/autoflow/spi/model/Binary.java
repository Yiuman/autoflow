package io.autoflow.spi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author yiuman
 * @date 2023/7/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Binary {
    private String filename;
    private String base64;
}
