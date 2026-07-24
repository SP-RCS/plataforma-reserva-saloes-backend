package com.ucan.reservasaloes.dto;

import java.util.ArrayList;
import java.util.List;

public class MenuDTO {
    private Integer pkFuncionalidade;
    private String name;
    private String path;
    private Integer fkFuncionalidadePai;
    private List<MenuDTO> filhos;
    
    // Construtor completo
    public MenuDTO(Integer pkFuncionalidade, String name, String path, Integer fkFuncionalidadePai) {
        this.pkFuncionalidade = pkFuncionalidade;
        this.name = name;
        this.path = path;
        this.fkFuncionalidadePai = fkFuncionalidadePai;
        this.filhos = new ArrayList<>();
    }
    
    // Construtor simplificado (para compatibilidade)
    public MenuDTO(Integer pkFuncionalidade, String name, String path) {
        this(pkFuncionalidade, name, path, null);
    }
    
    // Getters e Setters
    public Integer getPkFuncionalidade() { return pkFuncionalidade; }
    public void setPkFuncionalidade(Integer pkFuncionalidade) { this.pkFuncionalidade = pkFuncionalidade; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public Integer getFkFuncionalidadePai() { return fkFuncionalidadePai; }
    public void setFkFuncionalidadePai(Integer fkFuncionalidadePai) { this.fkFuncionalidadePai = fkFuncionalidadePai; }
    
    public List<MenuDTO> getFilhos() { return filhos; }
    public void setFilhos(List<MenuDTO> filhos) { this.filhos = filhos; }
}
