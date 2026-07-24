package com.ucan.reservasaloes.controllers;

import com.ucan.reservasaloes.dto.LoginDTO;
import com.ucan.reservasaloes.dto.MenuDTO;
import com.ucan.reservasaloes.entities.Conta;
import com.ucan.reservasaloes.entities.ContaPerfil;
import com.ucan.reservasaloes.entities.Funcionalidade;
import com.ucan.reservasaloes.entities.Perfil;
import com.ucan.reservasaloes.repositories.ContaPerfilRepository;
import com.ucan.reservasaloes.repositories.ContaRepository;
import com.ucan.reservasaloes.repositories.FuncionalidadePerfilRepository;
import com.ucan.reservasaloes.services.MenuDTOService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController 
@RequestMapping("/api/menu") 
public class MenuController
{

@Autowired private MenuDTOService menuService;

@GetMapping("/{pkPerfil}") 
public List getMenu(@PathVariable Integer pkPerfil){ 
    return menuService.getMenu(pkPerfil); }


/*@GetMapping("/menu_usuario")
public ResponseEntity<?> getMenuUsuario(@RequestParam String email, @RequestParam String perfil) {
    // ROOT vê tudo
    if ("ROOT".equals(perfil)) {
        return ResponseEntity.ok(Map.of("sucesso", true, "menus", getAllMenus()));
    }
    
    // Buscar funcionalidades do perfil do usuário
    List<Funcionalidade> funcs = findFuncionalidadesByUserEmail(email);
    
    // Agrupar por menu pai
    List<MenuDTO> menus = groupByParent(funcs);
    
    return ResponseEntity.ok(Map.of("sucesso", true, "menus", menus));
}*/

}
