package com.ucan.reservasaloes.dto;

import com.ucan.reservasaloes.enumerable.TipoContaEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter

public class ContaDTO {

    private Integer pkConta;
    private String nomeCompleto;
    private String email;
    private Integer estado;
    private TipoContaEnum tipoConta;
    private List<PerfilDTO> perfis = new ArrayList<>();
    private List<String> funcionalidades;

    /**
     *  Conta - Perfil
     *  Funcionalidade - Perfil
     *  Conta - Perfil - Funcionalidade
     *
     *  Conta -> ContaPerfil -> Perfis -> FuncionalidadePerfil -> Funcionalidades
     *
     */
}