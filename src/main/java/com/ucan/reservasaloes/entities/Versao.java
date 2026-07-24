package com.ucan.reservasaloes.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

/**
 *
 * @author Sebastiao
 */

@Entity
@Getter
@Setter
@Table(name = "versao")
public class Versao {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_versao")
    private Integer pkVersao;
    
    @Column(name = "nome_tabela", nullable = false, length = 150)
    private String nomeTabela;

    @Column(name = "data", nullable = false)
    private Date data;
    
    @Column(name = "descricao", length = 255)
    private String descricao;

    public Versao() {
    }

    public Versao(String nomeTabela, Date data) {
        this.nomeTabela = nomeTabela;
        this.data = data;
    }
    
    public Versao(String nomeTabela, Date data, String descricao) {
        this.nomeTabela = nomeTabela;
        this.data = data;
        this.descricao = descricao;
    }
}
