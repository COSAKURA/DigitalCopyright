package com.digitalcopyright.model.DTO;

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
    private String username;
    private String email;
    private int times;
    private long timestamp;

}

