package com.ucan.reservasaloes.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FuncionalidadeDTO {

    private Integer pkFuncionalidade;

    private String descricao;

    private String designacao;

    private Integer fkTipoFuncionalidade;
    private String designacaoTipoFuncionalidade;

   
    private Integer fkFuncionalidadePai;

    private String funcionalidadesPartilhadas;
    private List<String> perfis = new ArrayList<>();
    private String url;
    private String versao;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private Boolean isHerdadaDePerfil = false;
}
