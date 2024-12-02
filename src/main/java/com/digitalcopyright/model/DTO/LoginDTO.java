package com.digitalcopyright.model.DTO;
import jakarta.validation.constraints.NotEmpty;
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
    private String email;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码必须是6-18位")
    private String password;
    private String code;
}
