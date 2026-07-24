package com.ucan.reservasaloes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContaPerfilDTO {
    private Integer fkConta;  
    private String tipoConta;
    private String nomeCompleto;
    private String dataNascimento;
    private int fkGenero;
    private int fkEstadoCivil;
    private String identificacao;
    private String telefone;  
    private String email;
    private String passwordHash;
    
    // Perfil
    private int fkPerfil;
    private int estado = 1;
    
    // Endereço - ADICIONAR ESTES CAMPOS
    private String provincia;
    private String municipio;
    private String bairro;
    private String nomeRua;  


    @Override
    public String toString() {
        return "ContaPerfilDTO{" +
                "tipoConta='" + tipoConta + '\'' +
                ", nomeCompleto='" + nomeCompleto + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                ", fkGenero=" + fkGenero +
                ", fkEstadoCivil=" + fkEstadoCivil +
                ", identificacao='" + identificacao + '\'' +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", fkPerfil=" + fkPerfil +
                ", estado=" + estado +
                ", provincia='" + provincia + '\'' +
                ", municipio='" + municipio + '\'' +
                ", bairro='" + bairro + '\'' +
                ", nomeRua='" + nomeRua + '\'' +
                '}';
    }
}
