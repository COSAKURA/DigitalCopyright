package com.digitalcopyright.model.DTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


/**
 * @author Sakura
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    @NotNull(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式错位")
    private String email;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码必须是6-18位")
    private String password;
}
