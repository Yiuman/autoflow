package io.autoflow.app.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/12/10
 */
@Data
public class ID {
    @NotBlank
    private String id;
}
