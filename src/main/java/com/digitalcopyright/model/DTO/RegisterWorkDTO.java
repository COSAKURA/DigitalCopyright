package com.digitalcopyright.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Sakura
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterWorkDTO {
    @NotNull(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式错位")
    private String  email;
    private String title;
    private String description;
    private MultipartFile img;
    private String privateKey;


}
