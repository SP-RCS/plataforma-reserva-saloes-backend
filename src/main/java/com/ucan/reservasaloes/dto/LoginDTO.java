package com.ucan.reservasaloes.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginDTO {

    private String username;
    private String password;
    private List<MenuDTO> menu; 

}
