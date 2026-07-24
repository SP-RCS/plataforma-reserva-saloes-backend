package com.ucan.reservasaloes.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncionalidadePerfilDTO {

    private int fkFuncionalidade;
    private int fkPerfil;
    private String detalhe;
    private String nomePerfil;
    private String nomeFuncionalidade;
    private String tipoFuncionalidade;
    private String detalhePerfil;
    private String detalheFuncionalidade;
    private String paiFuncionalidade;
    //private String detalhe;  
      private String estadoPerfil;
        private Boolean isHerdadaDePerfil = false;

}
