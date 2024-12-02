package com.digitalcopyright.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotEmpty;

/**
 * @author Sakura
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    private String username;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码必须是6-18位")
    private String password;
    private String email;
    private String emailCode;
    private String type;
}
