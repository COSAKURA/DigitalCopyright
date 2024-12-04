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
@AllArgsConstructor
@NoArgsConstructor
public class BindBlockchainAddressDTO {
    @NotNull(message = "用户地址不能为空")
    private String address;
    @NotNull(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式错位")
    private String email;
}
