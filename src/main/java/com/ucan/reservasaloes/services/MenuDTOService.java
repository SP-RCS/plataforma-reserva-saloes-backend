package com.ucan.reservasaloes.services;

import com.ucan.reservasaloes.dto.MenuDTO;
import com.ucan.reservasaloes.entities.Funcionalidade;
import com.ucan.reservasaloes.entities.FuncionalidadePerfil;
import com.ucan.reservasaloes.repositories.FuncionalidadePerfilRepository;
import com.ucan.reservasaloes.repositories.FuncionalidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuDTOService {

    @Autowired
    private FuncionalidadePerfilRepository funcionalidadePerfilRepository;
    
    @Autowired
    private FuncionalidadeRepository funcionalidadeRepository;

    /**
     * Retorna TODAS as funcionalidades que o perfil tem acesso
     * Se tiver a funcionalidade raiz (ID=1), retorna TODAS as funcionalidades do sistema
     */
    public List<MenuDTO> getMenu(Integer perfilId) {
        System.out.println("=== BUSCANDO MENU PARA PERFIL ID: " + perfilId + " ===");
        
        // 1. Buscar todas as associações do perfil
        List<FuncionalidadePerfil> associacoes = funcionalidadePerfilRepository
            .findByFkPerfil_PkPerfil(perfilId);
        
        if (associacoes == null || associacoes.isEmpty()) {
            System.out.println("Nenhuma funcionalidade associada ao perfil");
            return new ArrayList<>();
        }
        
        // 2. Extrair IDs das funcionalidades
        List<Integer> idsFuncionalidades = associacoes.stream()
            .map(fp -> fp.getFkFuncionalidade().getPkFuncionalidade())
            .collect(Collectors.toList());
        
        System.out.println("IDs de funcionalidades associadas: " + idsFuncionalidades);
        
        // 3. 🔥 CORREÇÃO CRÍTICA: Se tem a raiz (ID=1), retorna TODAS as funcionalidades
        if (idsFuncionalidades.contains(1)) {
            System.out.println("⚠️ PERFIL TEM ACESSO TOTAL (RAIZ)! Retornando todas as funcionalidades...");
            
            List<Funcionalidade> todasFuncionalidades = funcionalidadeRepository.findAll();
            
            List<MenuDTO> todosMenus = todasFuncionalidades.stream()
                .map(func -> new MenuDTO(
                    func.getPkFuncionalidade(),
                    func.getDesignacao(),
                    func.getUrl() != null ? func.getUrl() : "",
                    func.getFkFuncionalidadePai() != null ? 
                        func.getFkFuncionalidadePai().getPkFuncionalidade() : null
                ))
                .collect(Collectors.toList());
            
            System.out.println("Retornando " + todosMenus.size() + " funcionalidades (ACESSO TOTAL)");
            return todosMenus;
        }
        
        // 4. Se não tem raiz, retorna apenas as associadas
        List<MenuDTO> menus = associacoes.stream()
            .map(fp -> {
                Funcionalidade func = fp.getFkFuncionalidade();
                return new MenuDTO(
                    func.getPkFuncionalidade(),
                    func.getDesignacao(),
                    func.getUrl() != null ? func.getUrl() : "",
                    func.getFkFuncionalidadePai() != null ? 
                        func.getFkFuncionalidadePai().getPkFuncionalidade() : null
                );
            })
            .collect(Collectors.toList());
        
        System.out.println("Retornando " + menus.size() + " funcionalidades específicas");
        return menus;
    }
    
    
    
    // No MenuDTOService.java, adicione este método:
public List<MenuDTO> getAllFuncionalidades() {
    List<Funcionalidade> todas = funcionalidadeRepository.findAll();
    
    return todas.stream()
        .map(func -> new MenuDTO(
            func.getPkFuncionalidade(),
            func.getDesignacao(),
            func.getUrl() != null ? func.getUrl() : "",
            func.getFkFuncionalidadePai() != null ? 
                func.getFkFuncionalidadePai().getPkFuncionalidade() : null
        ))
        .collect(Collectors.toList());
}
    
    
       
    
    /**
     * Constrói uma árvore hierárquica de menus (para Header)
     */
    public List<MenuDTO> getMenuHierarquico(Integer perfilId) {
        List<MenuDTO> todosMenus = getMenu(perfilId);
        return construirArvore(todosMenus);
    }
    
    private List<MenuDTO> construirArvore(List<MenuDTO> menus) {
        Map<Integer, MenuDTO> map = new HashMap<>();
        List<MenuDTO> raiz = new ArrayList<>();
        
        // Primeiro, mapear todos por ID
        for (MenuDTO menu : menus) {
            map.put(menu.getPkFuncionalidade(), menu);
            menu.setFilhos(new ArrayList<>());
        }
        
        // Depois, construir hierarquia
        for (MenuDTO menu : menus) {
            Integer paiId = menu.getFkFuncionalidadePai();
            if (paiId != null && map.containsKey(paiId)) {
                MenuDTO pai = map.get(paiId);
                pai.getFilhos().add(menu);
            } else {
                raiz.add(menu);
            }
        }
        
        return raiz;
    }
}
