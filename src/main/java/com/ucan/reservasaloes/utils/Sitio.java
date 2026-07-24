package com.ucan.reservasaloes.utils;

import com.ucan.reservasaloes.enumerable.TipoLocalidade;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Sitio implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;        
    private String pai;         
    private String avo;         
    private String nomeRua;     
    private String numero;      
    private TipoLocalidade tipo;

    // Construtores auxiliares
    public Sitio(String nome) {
        this.nome = nome;
    }

    public Sitio(String nome, String pai) {
        this.nome = nome;
        this.pai = pai;
    }

    public Sitio(String nome, String pai, String avo) {
        this.nome = nome;
        this.pai = pai;
        this.avo = avo;
    }

    public Sitio(String nome, String pai, String avo, String nomeRua, String numero) {
        this.nome = nome;
        this.pai = pai;
        this.avo = avo;
        this.nomeRua = nomeRua;
        this.numero = numero;
    }

    public Sitio(String nome, String pai, String avo, String nomeRua, String numero, TipoLocalidade tipo) {
        this.nome = nome;
        this.pai = pai;
        this.avo = avo;
        this.nomeRua = nomeRua;
        this.numero = numero;
        this.tipo = tipo;
    }
}
