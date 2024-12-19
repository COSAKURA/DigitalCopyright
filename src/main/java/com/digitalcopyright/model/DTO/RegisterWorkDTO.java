package com.digitalcopyright.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Sakura
 */
@Data
public class RegisterWorkDTO {
    @NotNull(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;

    private String title;
    private String description;

    @NotNull(message = "作品类型不能为空")
    private  String category;

    @NotNull(message = "图片文件不能为空")
    private MultipartFile img; // 图片文件

    private String privateKey; // 用户私钥
}

