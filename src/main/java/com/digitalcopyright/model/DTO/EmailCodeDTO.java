package com.digitalcopyright.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sakura
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCodeDTO {
    private String emailCode;
    @NotNull(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式错位")
    private String email;
    private int times;
    private long timestamp;

}

