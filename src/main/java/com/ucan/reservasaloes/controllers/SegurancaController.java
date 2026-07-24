package com.ucan.reservasaloes.controllers;

import com.ucan.reservasaloes.config.Defs;
import com.ucan.reservasaloes.config.FuncionsHelper;
import com.ucan.reservasaloes.dto.ContaPerfilDTO;
import com.ucan.reservasaloes.dto.FuncionalidadeDTO;
import com.ucan.reservasaloes.dto.FuncionalidadePerfilDTO;
import com.ucan.reservasaloes.dto.PerfilDTO;
import com.ucan.reservasaloes.entities.*;
import com.ucan.reservasaloes.enumerable.TipoContaEnum;
import com.ucan.reservasaloes.enumerable.TipoLocalidade;
import com.ucan.reservasaloes.enumerable.TipoTelefoneEnum;
import com.ucan.reservasaloes.initializer.TipoFuncionalidadeLoader;
import com.ucan.reservasaloes.repositories.*;
import com.ucan.reservasaloes.services.VersaoService;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/seguranca")
@CrossOrigin(origins = "*")
public class SegurancaController {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private ContaPerfilRepository contaPerfilRepository;

    @Autowired
    private FuncionalidadeRepository funcionalidadeRepository;

    @Autowired
    private FuncionalidadePerfilRepository funcionalidadePerfilRepository;

    @Autowired
    private TipoFuncionalidadeRepository tipoFuncionalidadeRepository;

    @Autowired
    private VersaoService versaoService;

    @Autowired
    private LocalidadeRepository localidadeRepository; // ADICIONADO: Para salvar o endereço

    // Expressões regulares para validação
    private static final Pattern DESIGNACAO_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']+$");
    private static final Pattern DESCRICAO_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ0-9\\s\\-',.!?]*$");

    // Método auxiliar para converter estado inteiro em string
    private String converterEstadoPerfil(Integer estado) {
        if (estado == null) {
            return "ATIVO"; // Valor padrão
        }
        switch (estado) {
            case 1:
                return "ATIVO";
            case 0:
                return "INATIVO";
            case 2:
                return "SUSPENSO";
            default:
                return "DESCONHECIDO";
        }
    }

    @GetMapping("/")
    public String index() {
        return "Segurança ON";
    }

    @GetMapping("/funcionalidade_listar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarFuncionalidade() {
        try {
            List<Funcionalidade> funcionalidades = funcionalidadeRepository.findAll();

            if (funcionalidades == null || funcionalidades.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", true);
                response.put("mensagem", "Nenhuma funcionalidade encontrada");
                response.put("dados", new ArrayList<>());
                response.put("total", 0);
                return ResponseEntity.ok(response);
            }

            List<FuncionalidadeDTO> listaFuncionalidade = new ArrayList<>();

            for (Funcionalidade funcionalidade : funcionalidades) {
                try {
                    FuncionalidadeDTO funcionalidadeDTO = new FuncionalidadeDTO();
                    funcionalidadeDTO.setPkFuncionalidade(funcionalidade.getPkFuncionalidade());
                    funcionalidadeDTO.setDesignacao(funcionalidade.getDesignacao() != null ? funcionalidade.getDesignacao() : "");
                    funcionalidadeDTO.setDescricao(funcionalidade.getDescricao() != null ? funcionalidade.getDescricao() : "");
                    //  funcionalidadeDTO.setCreatedAt(funcionalidade.getCreatedAt());
                    //   funcionalidadeDTO.setUpdatedAt(funcionalidade.getUpdatedAt());
                    funcionalidadeDTO.setUrl(funcionalidade.getUrl() != null ? funcionalidade.getUrl() : "");

                 

                    if (funcionalidade.getFuncionalidadesPartilhadas() != null) {
                        funcionalidadeDTO.setFuncionalidadesPartilhadas(funcionalidade.getFuncionalidadesPartilhadas());
                    }

                    if (funcionalidade.getFkTipoFuncionalidade() != null) {
                        funcionalidadeDTO.setFkTipoFuncionalidade(funcionalidade.getFkTipoFuncionalidade().getPkTipoFuncionalidade());
                        funcionalidadeDTO.setDesignacaoTipoFuncionalidade(
                                funcionalidade.getFkTipoFuncionalidade().getDesignacao() != null
                                        ? funcionalidade.getFkTipoFuncionalidade().getDesignacao() : ""
                        );
                    } else {
                        funcionalidadeDTO.setDesignacaoTipoFuncionalidade("Sem tipo");
                    }

                    if (funcionalidade.getFkFuncionalidadePai() != null) {
                        funcionalidadeDTO.setFkFuncionalidadePai(funcionalidade.getFkFuncionalidadePai().getPkFuncionalidade());
                    }

                    listaFuncionalidade.add(funcionalidadeDTO);
                } catch (Exception e) {
                    System.err.println("Erro ao processar funcionalidade ID " + funcionalidade.getPkFuncionalidade() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Funcionalidades listadas com sucesso");
            response.put("dados", listaFuncionalidade);
            response.put("total", listaFuncionalidade.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Erro grave em funcionalidade_listar: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro interno ao listar funcionalidades");
            response.put("erroDetalhado", e.getMessage());
            response.put("erroTipo", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/tipos_funcionalidade_listar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarTiposFuncionalidade() {
        try {
            List<TipoFuncionalidade> tipos = tipoFuncionalidadeRepository.findAll();

            List<Map<String, Object>> tiposList = tipos.stream()
                    .map(tipo -> {
                        Map<String, Object> tipoMap = new HashMap<>();
                        tipoMap.put("pkTipoFuncionalidade", tipo.getPkTipoFuncionalidade());
                        tipoMap.put("designacao", tipo.getDesignacao());
                        tipoMap.put("descricao", tipo.getDescricao());
                        if (tipo.getFkTipoFuncionalidadePai() != null) {
                            tipoMap.put("fkTipoFuncionalidadePai",
                                    tipo.getFkTipoFuncionalidadePai().getPkTipoFuncionalidade());
                        } else {
                            tipoMap.put("fkTipoFuncionalidadePai", null);
                        }
                        return tipoMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("dados", tiposList);
            response.put("total", tiposList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao listar tipos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/conta_listar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarConta() {
        try {
            List<Conta> contas = contaRepository.findAll();

            List<Map<String, Object>> contasCompletas = new ArrayList<>();

            for (Conta conta : contas) {
                Map<String, Object> contaMap = new HashMap<>();

                contaMap.put("pkConta", conta.getPkConta());
                contaMap.put("email", conta.getEmail());
                contaMap.put("tipoConta", conta.getTipoConta() != null ? conta.getTipoConta().name() : null);

                Integer estadoConta = conta.getEstado();
                contaMap.put("estado", estadoConta != null ? estadoConta : 1);

                contaMap.put("createdAt", conta.getCreatedAt());
                contaMap.put("updatedAt", conta.getUpdatedAt());

                if (conta.getFkPessoa() != null) {
                    Pessoa pessoa = conta.getFkPessoa();
                    contaMap.put("nomeCompleto", pessoa.getNome());
                    contaMap.put("identificacao", pessoa.getIdentificacao());

                    if (pessoa.getTelefones() != null && !pessoa.getTelefones().isEmpty()) {
                        Telefone telefonePrincipal = pessoa.getTelefones().stream()
                                .findFirst()
                                .orElse(pessoa.getTelefones().get(0));
                        contaMap.put("telefone", telefonePrincipal.getNumero());
                    }

                    contaMap.put("fkGenero", pessoa.getFkGenero() != null ? pessoa.getFkGenero().getPkGenero() : null);
                    contaMap.put("fkEstadoCivil", pessoa.getFkEstadoCivil() != null ? pessoa.getFkEstadoCivil().getPkEstadoCivil() : null);
                    contaMap.put("dataNascimento", pessoa.getDataNascimento());

                    // MELHORADO: Incluir informações de endereço/localidade com campos separados
                    if (pessoa.getLocalidade() != null) {
                        Localidade localidade = pessoa.getLocalidade();
                        contaMap.put("localidade", localidade.getNome());
                        contaMap.put("nomeRua", localidade.getNomeRua());

                        // ADICIONADO: Campos separados para província, município, bairro
                        // Extrair da estrutura hierárquica de Localidade
                        String provincia = extrairProvincia(localidade);
                        String municipio = extrairMunicipio(localidade);
                        String bairro = extrairBairro(localidade);

                        contaMap.put("provincia", provincia);
                        contaMap.put("municipio", municipio);
                        contaMap.put("bairro", bairro);

                        System.out.println("Endereço extraído para conta " + conta.getPkConta() + ": " +
                                "Província=" + provincia + ", Município=" + municipio + ", Bairro=" + bairro);
                    } else {
                        // Se não houver localidade, definir campos como nulo
                        contaMap.put("provincia", null);
                        contaMap.put("municipio", null);
                        contaMap.put("bairro", null);
                        contaMap.put("nomeRua", null);
                        contaMap.put("localidade", null);
                    }
                } else {
                    contaMap.put("nomeCompleto", null);
                    contaMap.put("identificacao", null);
                    contaMap.put("telefone", null);
                    contaMap.put("fkGenero", null);
                    contaMap.put("fkEstadoCivil", null);
                    contaMap.put("dataNascimento", null);
                    contaMap.put("provincia", null);
                    contaMap.put("municipio", null);
                    contaMap.put("bairro", null);
                    contaMap.put("nomeRua", null);
                    contaMap.put("localidade", null);
                }

                contasCompletas.add(contaMap);
            }

            return ResponseEntity.ok(contasCompletas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao listar contas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ADICIONADO: Métodos auxiliares para extrair informações de endereço
    private String extrairProvincia(Localidade localidade) {
        try {
            // Se a localidade for do tipo PROVINCIA, retornar o nome
            if (localidade.getTipo() == TipoLocalidade.PROVINCIA) {
                return localidade.getNome();
            }

            // Se tiver localidade pai, navegar até a província
            Localidade atual = localidade;
            while (atual != null) {
                if (atual.getTipo() == TipoLocalidade.PROVINCIA) {
                    return atual.getNome();
                }
                atual = atual.getLocalidadePai();
            }

            // Tentar extrair do nome se tiver formato conhecido
            String nome = localidade.getNome();
            if (nome != null && nome.contains(",")) {
                String[] partes = nome.split(",");
                for (String parte : partes) {
                    String parteTrim = parte.trim();
                    // Verificar se parece ser uma província (heuristicamente)
                    if (parteTrim.matches(".*(nda|ela|mbo|nza)$")) {
                        return parteTrim;
                    }
                }
            }

            return nome != null ? nome : "Não informado";
        } catch (Exception e) {
            return "Não informado";
        }
    }

    private String extrairMunicipio(Localidade localidade) {
        try {
            // Se a localidade for do tipo MUNICIPIO, retornar o nome
            if (localidade.getTipo() == TipoLocalidade.MUNICIPIO) {
                return localidade.getNome();
            }

            // Se for bairro, buscar o município pai
            if (localidade.getTipo() == TipoLocalidade.BAIRRO && localidade.getLocalidadePai() != null) {
                Localidade pai = localidade.getLocalidadePai();
                if (pai.getTipo() == TipoLocalidade.MUNICIPIO) {
                    return pai.getNome();
                }
            }

            // Tentar extrair do nome se tiver formato conhecido
            String nome = localidade.getNome();
            if (nome != null && nome.contains(",")) {
                String[] partes = nome.split(",");
                if (partes.length >= 2) {
                    return partes[1].trim();
                }
            }

            return "Não informado";
        } catch (Exception e) {
            return "Não informado";
        }
    }

    private String extrairBairro(Localidade localidade) {
        try {
            // Se a localidade for do tipo BAIRRO, retornar o nome
            if (localidade.getTipo() == TipoLocalidade.BAIRRO) {
                return localidade.getNome();
            }

            // Tentar extrair do nome se tiver formato conhecido
            String nome = localidade.getNome();
            if (nome != null && nome.contains(",")) {
                String[] partes = nome.split(",");
                if (partes.length >= 3) {
                    return partes[2].trim();
                } else if (partes.length == 2) {
                    // Se só tiver 2 partes, a segunda pode ser o bairro
                    return partes[1].trim();
                }
            }

            return nome != null ? nome : "Não informado";
        } catch (Exception e) {
            return "Não informado";
        }
    }

    @GetMapping("/perfil_listar")
@Transactional(readOnly = true)
public ResponseEntity<?> listarPerfil() {
    try {
        List<Perfil> perfis = perfilRepository.findAll();

        // ⭐ Converter para DTO para evitar Lazy Loading
        List<Map<String, Object>> listaPerfil = perfis.stream().map(perfil -> {
            Map<String, Object> perfilDTO = new HashMap<>();
            perfilDTO.put("pkPerfil", perfil.getPkPerfil());
            perfilDTO.put("designacao", perfil.getDesignacao());
            perfilDTO.put("descricao", perfil.getDescricao());
            perfilDTO.put("estado", perfil.getEstado());
            perfilDTO.put("createdAt", perfil.getCreatedAt());
            perfilDTO.put("updatedAt", perfil.getUpdatedAt());
            // NÃO incluir funcionalidades aqui!
            return perfilDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(listaPerfil);

    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", false);
        response.put("mensagem", "Erro ao listar perfis: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

    @GetMapping("/conta_perfil_listar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarContaPerfil() {
        try {
            List<ContaPerfil> contaPerfis = contaPerfilRepository.findAll();
            List<ContaPerfilDTO> listContaPerfil = new ArrayList<>();

            for (ContaPerfil contaPerfil : contaPerfis) {
                ContaPerfilDTO contaPerfilDTO = new ContaPerfilDTO();

                contaPerfilDTO.setFkPerfil(contaPerfil.getFkPerfil().getPkPerfil());
                contaPerfilDTO.setFkConta(contaPerfil.getFkConta().getPkConta());

                contaPerfilDTO.setEmail(contaPerfil.getFkConta().getEmail());
                contaPerfilDTO.setTipoConta(contaPerfil.getFkConta().getTipoConta().name());
                contaPerfilDTO.setEstado(contaPerfil.getEstado());

                listContaPerfil.add(contaPerfilDTO);
            }

            return ResponseEntity.ok(listContaPerfil);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao listar conta-perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/funcionalidade_perfil_listar")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listarFuncionalidadePerfil() {
        try {
            List<FuncionalidadePerfil> funcionalidadePerfis = funcionalidadePerfilRepository.findAll();
            List<FuncionalidadePerfilDTO> listFuncionalidadePerfil = new ArrayList<>();

            for (FuncionalidadePerfil funcionalidadePerfilModel : funcionalidadePerfis) {
                FuncionalidadePerfilDTO funcionalidadePerfilDTO = new FuncionalidadePerfilDTO();
                funcionalidadePerfilDTO.setNomePerfil(funcionalidadePerfilModel.getFkPerfil().getDesignacao());
                funcionalidadePerfilDTO.setNomeFuncionalidade(funcionalidadePerfilModel.getFkFuncionalidade().getDescricao());

                funcionalidadePerfilDTO.setFkPerfil(funcionalidadePerfilModel.getFkPerfil().getPkPerfil());
                funcionalidadePerfilDTO.setFkFuncionalidade(funcionalidadePerfilModel.getFkFuncionalidade().getPkFuncionalidade());

                if (funcionalidadePerfilModel.getFkFuncionalidade().getFkTipoFuncionalidade() != null) {
                    funcionalidadePerfilDTO.setTipoFuncionalidade(funcionalidadePerfilModel.getFkFuncionalidade().getFkTipoFuncionalidade().getDesignacao());
                }

                funcionalidadePerfilDTO.setDetalhePerfil(funcionalidadePerfilModel.getFkPerfil().getDescricao());
                funcionalidadePerfilDTO.setDetalheFuncionalidade(funcionalidadePerfilModel.getFkFuncionalidade().getDescricao());

                funcionalidadePerfilDTO.setPaiFuncionalidade(funcionalidadePerfilModel.getFkPerfil().getDesignacao());

                Integer estadoInt = funcionalidadePerfilModel.getFkPerfil().getEstado();
                String estadoStr = converterEstadoPerfil(estadoInt);
                funcionalidadePerfilDTO.setEstadoPerfil(estadoStr);

                listFuncionalidadePerfil.add(funcionalidadePerfilDTO);
            }

            return ResponseEntity.ok(listFuncionalidadePerfil);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao listar funcionalidade-perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/conta_cadastrar")
    @Transactional
    public ResponseEntity<?> cadastrarConta(@RequestBody ContaPerfilDTO contaPerfilDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("Dados recebidos para cadastro: " + contaPerfilDTO.toString());

            // ========== VALIDAÇÃO: VERIFICAR SE IDENTIFICAÇÃO JÁ EXISTE ==========
            if (contaPerfilDTO.getIdentificacao() != null && !contaPerfilDTO.getIdentificacao().trim().isEmpty()) {
                String identificacao = contaPerfilDTO.getIdentificacao().trim();

                Optional<Pessoa> pessoaExistente = pessoaRepository.findByIdentificacao(identificacao);

                if (pessoaExistente.isPresent()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Esta identificação já está em uso por outra pessoa");
                    response.put("campoErro", "identificacao");
                    return ResponseEntity.ok(response); // Mudar para .ok() para manter consistência
                }
            }

            // ========== VALIDAÇÃO: VERIFICAR SE EMAIL JÁ EXISTE ==========
            if (contaPerfilDTO.getEmail() != null && !contaPerfilDTO.getEmail().trim().isEmpty()) {
                String email = contaPerfilDTO.getEmail().trim().toLowerCase();

                Optional<Conta> contaExistente = contaRepository.findByEmail(email);

                if (contaExistente.isPresent()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Este email já está em uso por outra conta");
                    response.put("campoErro", "email");
                    return ResponseEntity.ok(response); // Mudar para .ok() para manter consistência
                }
            }

            // ========== VALIDAÇÃO: VERIFICAR SE PERFIL É VÁLIDO ==========
            int fkPerfil = contaPerfilDTO.getFkPerfil();
            if (fkPerfil <= 0) {
                response.put("sucesso", false);
                response.put("mensagem", "Perfil é obrigatório");
                response.put("campoErro", "fkPerfil");
                return ResponseEntity.ok(response);
            }

            Perfil perfilModel = perfilRepository.findByPkPerfil(fkPerfil);
            if (perfilModel == null) {
                response.put("sucesso", false);
                response.put("mensagem", "Perfil não encontrado");
                response.put("campoErro", "fkPerfil");
                return ResponseEntity.ok(response);
            }

            // ========== FIM DA VALIDAÇÃO ==========

            Pessoa pessoaModel = new Pessoa();
            Conta contaModel = new Conta();
            ContaPerfil contaPerfil = new ContaPerfil();

            // 1. PROCESSAR ENDEREÇO/LOCALIDADE COM TRATAMENTO DE ERRO ATUALIZADO
            Localidade localidadePessoa = null;
            try {
                localidadePessoa = processarEnderecoComNomes(contaPerfilDTO);
            } catch (Exception e) {
                System.err.println("Erro ao processar endereço, criando localidade padrão...");
                localidadePessoa = criarLocalidadeBasica();
            }

            // 2. CONFIGURAR PESSOA COM ENDEREÇO
            pessoaModel.setNome(contaPerfilDTO.getNomeCompleto());
            pessoaModel.setIdentificacao(contaPerfilDTO.getIdentificacao() != null ?
                    contaPerfilDTO.getIdentificacao().trim() : null);

            // Configurar gênero se fornecido
            int fkGenero = contaPerfilDTO.getFkGenero();
            if (fkGenero > 0) {
                pessoaModel.setFkGenero(new Genero(fkGenero));
            }

            // Configurar estado civil se fornecido
            int fkEstadoCivil = contaPerfilDTO.getFkEstadoCivil();
            if (fkEstadoCivil > 0) {
                pessoaModel.setFkEstadoCivil(new EstadoCivil(fkEstadoCivil));
            }

            // Definir data de nascimento
            if (contaPerfilDTO.getDataNascimento() != null && !contaPerfilDTO.getDataNascimento().isEmpty()) {
                try {
                    String[] dataArray = contaPerfilDTO.getDataNascimento().split("-");
                    LocalDate localDate = LocalDate.of(
                            Integer.parseInt(dataArray[0]),
                            Integer.parseInt(dataArray[1]),
                            Integer.parseInt(dataArray[2]));
                    pessoaModel.setDataNascimento(localDate);
                } catch (Exception e) {
                    System.err.println("Erro ao converter data de nascimento: " + e.getMessage());
                    pessoaModel.setDataNascimento(LocalDate.now().minusYears(18));
                }
            }

            // Associar localidade à pessoa SOMENTE se não for null
            if (localidadePessoa != null) {
                pessoaModel.setLocalidade(localidadePessoa);
            }

            // 3. SALVAR TELEFONE
            if (contaPerfilDTO.getTelefone() != null && !contaPerfilDTO.getTelefone().trim().isEmpty()) {
                Telefone telefone = new Telefone();
                telefone.setNumero(contaPerfilDTO.getTelefone().trim());
                telefone.setPrincipal(true);
                telefone.setTipo(TipoTelefoneEnum.CELULAR);
                telefone.setFkPessoa(pessoaModel);

                if (pessoaModel.getTelefones() == null) {
                    pessoaModel.setTelefones(new ArrayList<>());
                }
                pessoaModel.getTelefones().add(telefone);
            }

            // 4. SALVAR PESSOA
            pessoaModel = pessoaRepository.save(pessoaModel);

            // 5. SALVAR CONTA
            contaModel.setEmail(contaPerfilDTO.getEmail() != null ?
                    contaPerfilDTO.getEmail().trim().toLowerCase() : null);
            contaModel.setPasswordHash(contaPerfilDTO.getPasswordHash());

            // Validar e converter tipo de conta
            try {
                if (contaPerfilDTO.getTipoConta() != null && !contaPerfilDTO.getTipoConta().isEmpty()) {
                    // Se ADMINISTRADOR não existir no enum, use ADMIN como fallback
                    try {
                        contaModel.setTipoConta(TipoContaEnum.valueOf(contaPerfilDTO.getTipoConta()));
                    } catch (IllegalArgumentException e) {
                        // Se o valor for ADMINISTRADOR, converta para ADMIN
                        if ("ADMINISTRADOR".equalsIgnoreCase(contaPerfilDTO.getTipoConta())) {
                            contaModel.setTipoConta(TipoContaEnum.ADMIN);
                        } else {
                            // Para outros valores inválidos, use ADMIN como padrão
                            contaModel.setTipoConta(TipoContaEnum.ADMIN);
                        }
                    }
                } else {
                    contaModel.setTipoConta(TipoContaEnum.ADMIN);
                }
            } catch (Exception e) {
                System.err.println("Erro ao definir tipo de conta: " + e.getMessage());
                contaModel.setTipoConta(TipoContaEnum.ADMIN);
            }

            // Configurar estado
            int estadoConta = contaPerfilDTO.getEstado();
            if (estadoConta < 0 || estadoConta > 1) {
                estadoConta = 1; // Valor padrão se inválido
            }
            contaModel.setEstado(estadoConta);

            contaModel.setFkPessoa(pessoaModel);
            contaModel = contaRepository.save(contaModel);

            // 6. SALVAR CONTA-PERFIL
            contaPerfil.setFkConta(contaModel);
            contaPerfil.setFkPerfil(perfilModel);

            int estadoContaPerfil = contaPerfilDTO.getEstado();
            if (estadoContaPerfil < 0 || estadoContaPerfil > 1) {
                estadoContaPerfil = 1;
            }
            contaPerfil.setEstado(estadoContaPerfil);

            contaPerfilRepository.save(contaPerfil);

            response.put("sucesso", true);
            response.put("mensagem", "Conta cadastrada com sucesso!");
            response.put("contaId", contaModel.getPkConta());
            response.put("pessoaId", pessoaModel.getPkPessoa());

            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            String mensagemErro = "Erro de integridade de dados";

            if (e.getMessage() != null) {
                if (e.getMessage().contains("pessoa_identificacao_key")) {
                    mensagemErro = "Esta identificação já está em uso por outra pessoa";
                } else if (e.getMessage().contains("conta_email_key")) {
                    mensagemErro = "Este email já está em uso por outra conta";
                } else if (e.getMessage().contains("duplicate key")) {
                    mensagemErro = "Já existe um registro com estes dados";
                }
            }

            response.put("sucesso", false);
            response.put("mensagem", mensagemErro);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao cadastrar conta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(response);
        }
    }

    // MÉTODO AUXILIAR ATUALIZADO PARA PROCESSAR ENDEREÇO COM NOMES
    private Localidade processarEnderecoComNomes(ContaPerfilDTO contaPerfilDTO) {
        try {
            System.out.println("=== PROCESSAR ENDEREÇO COM NOMES ===");
            System.out.println("Dados recebidos:");
            System.out.println("Província: " + contaPerfilDTO.getProvincia());
            System.out.println("Município: " + contaPerfilDTO.getMunicipio());
            System.out.println("Bairro: " + contaPerfilDTO.getBairro());
            System.out.println("Nome Rua: " + contaPerfilDTO.getNomeRua());

            // Usar diretamente os nomes recebidos para criar a localidade
            return criarLocalidadeComNomes(contaPerfilDTO);

        } catch (Exception e) {
            System.err.println("Erro em processarEnderecoComNomes: " + e.getMessage());
            e.printStackTrace();
            return criarLocalidadeBasica();
        }
    }

    private Localidade criarLocalidadeComNomes(ContaPerfilDTO contaPerfilDTO) {
        Localidade localidade = new Localidade();

        // Montar nome completo da localidade
        StringBuilder nomeBuilder = new StringBuilder();

        if (contaPerfilDTO.getBairro() != null && !contaPerfilDTO.getBairro().trim().isEmpty()) {
            nomeBuilder.append(contaPerfilDTO.getBairro().trim());
            localidade.setTipo(TipoLocalidade.BAIRRO);
        } else if (contaPerfilDTO.getMunicipio() != null && !contaPerfilDTO.getMunicipio().trim().isEmpty()) {
            nomeBuilder.append(contaPerfilDTO.getMunicipio().trim());
            localidade.setTipo(TipoLocalidade.MUNICIPIO);
        } else if (contaPerfilDTO.getProvincia() != null && !contaPerfilDTO.getProvincia().trim().isEmpty()) {
            nomeBuilder.append(contaPerfilDTO.getProvincia().trim());
            localidade.setTipo(TipoLocalidade.PROVINCIA);
        } else {
            nomeBuilder.append("Endereço não especificado");
            localidade.setTipo(TipoLocalidade.BAIRRO);
        }

        localidade.setNome(nomeBuilder.toString());

        // Adicionar nome da rua se existir
        if (contaPerfilDTO.getNomeRua() != null && !contaPerfilDTO.getNomeRua().trim().isEmpty()) {
            localidade.setNomeRua(contaPerfilDTO.getNomeRua().trim());
            System.out.println("Nome da rua definido: " + contaPerfilDTO.getNomeRua().trim());
        }

        System.out.println("Criando nova localidade com nomes: " + localidade.getNome() + " (Tipo: " + localidade.getTipo() + ")");

        // Buscar localidade pai (município) se existir
        if (contaPerfilDTO.getMunicipio() != null && !contaPerfilDTO.getMunicipio().trim().isEmpty() &&
                localidade.getTipo() == TipoLocalidade.BAIRRO) {
            try {
                // Buscar município pelo nome
                Optional<Localidade> municipioOpt = localidadeRepository.findByNome(contaPerfilDTO.getMunicipio().trim());

                if (municipioOpt.isPresent()) {
                    localidade.setLocalidadePai(municipioOpt.get());
                    System.out.println("Município pai encontrado pelo nome: " + municipioOpt.get().getNome());
                } else {
                    // Se não encontrar, criar novo município
                    Localidade novoMunicipio = new Localidade();
                    novoMunicipio.setNome(contaPerfilDTO.getMunicipio().trim());
                    novoMunicipio.setTipo(TipoLocalidade.MUNICIPIO);

                    // Buscar província pai se existir
                    if (contaPerfilDTO.getProvincia() != null && !contaPerfilDTO.getProvincia().trim().isEmpty()) {
                        Optional<Localidade> provinciaOpt = localidadeRepository.findByNome(contaPerfilDTO.getProvincia().trim());
                        if (provinciaOpt.isPresent()) {
                            novoMunicipio.setLocalidadePai(provinciaOpt.get());
                        }
                    }

                    novoMunicipio = localidadeRepository.save(novoMunicipio);
                    localidade.setLocalidadePai(novoMunicipio);
                    System.out.println("Novo município criado: " + novoMunicipio.getNome());
                }
            } catch (Exception e) {
                System.err.println("Erro ao buscar ou criar município pai: " + e.getMessage());
            }
        }

        return localidadeRepository.save(localidade);
    }

    // MÉTODO PARA CRIAR LOCALIDADE BÁSICA EM CASO DE ERRO
    private Localidade criarLocalidadeBasica() {
        try {
            Localidade localidadeBasica = new Localidade();
            localidadeBasica.setNome("Endereço padrão");

            try {
                localidadeBasica.setTipo(TipoLocalidade.BAIRRO);
            } catch (Exception e) {
                // Se não conseguir definir tipo, não definir
                System.err.println("Não foi possível definir tipo para localidade básica");
            }

            return localidadeRepository.save(localidadeBasica);
        } catch (Exception ex) {
            System.err.println("Erro ao criar localidade básica: " + ex.getMessage());

            // Criar objeto mínimo sem salvar no banco
            Localidade localidade = new Localidade();
            localidade.setNome("Endereço");
            return localidade;
        }
    }

@PostMapping("/perfil_cadastrar")
public ResponseEntity<?> cadastrarPerfil(@RequestBody PerfilDTO dto) {
    try {
        // 1. Salvar o novo perfil
        Perfil novoPerfil = new Perfil();
        novoPerfil.setDesignacao(dto.getDesignacao());
        novoPerfil.setEstado(dto.getEstado());
        novoPerfil.setDescricao(dto.getDescricao());
        Perfil salvo = perfilRepository.save(novoPerfil);
        
        // 2. Herdar funcionalidades dos perfis selecionados
        if (dto.getHerdarDePerfis() != null && !dto.getHerdarDePerfis().isEmpty()) {
            for (Integer perfilOrigemId : dto.getHerdarDePerfis()) {
                // Usando o método correto
                List<FuncionalidadePerfil> funcs = funcionalidadePerfilRepository
                    .findByFkPerfil_PkPerfil(perfilOrigemId.intValue());
                
                for (FuncionalidadePerfil fp : funcs) {
                    // Verificar se já existe
                    boolean existe = funcionalidadePerfilRepository
                        .existsByFkPerfil_PkPerfilAndFkFuncionalidade_PkFuncionalidade(
                            salvo.getPkPerfil(), 
                            fp.getFkFuncionalidade().getPkFuncionalidade()
                        );
                    
                    if (!existe) {
                        FuncionalidadePerfil nova = new FuncionalidadePerfil();
                        nova.setFkPerfil(salvo);
                        nova.setFkFuncionalidade(fp.getFkFuncionalidade());
                        nova.setDetalhe(fp.getDetalhe());
                        funcionalidadePerfilRepository.save(nova);
                    }
                }
            }
        }
        
        return ResponseEntity.ok(Map.of("sucesso", true, 
            "mensagem", "Perfil criado com herança de " + dto.getHerdarDePerfis().size() + " perfil(is)"));
        
    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body(Map.of("sucesso", false, "mensagem", e.getMessage()));
    }
}

    @PostMapping("/conta_perfil_cadastrar")
    public ResponseEntity<?> cadastrarContaPerfil(@RequestBody ContaPerfilDTO contaPerfilDTO) {
        try {
            Perfil perfilModel = new Perfil();
            Conta contaModel = new Conta();
            ContaPerfil contaPerfilModel = new ContaPerfil();

            perfilModel.setPkPerfil(contaPerfilDTO.getFkPerfil());
            contaModel.setPkConta(contaPerfilDTO.getFkConta());
            contaModel.setEmail(contaPerfilDTO.getEmail());

            contaPerfilModel.setFkPerfil(perfilModel);
            contaPerfilModel.setFkConta(contaModel);
            contaPerfilModel.setEstado(contaPerfilDTO.getEstado());

            ContaPerfil contaPerfilModelAux = contaPerfilRepository.save(contaPerfilModel);

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Conta-Perfil cadastrado com sucesso");
            response.put("contaPerfil", contaPerfilModelAux);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao cadastrar conta-perfil: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/funcionalidade_cadastrar")
    @Transactional
    public ResponseEntity<?> cadastrarFuncionalidade(@RequestBody Funcionalidade funcionalidade) {
        try {
            Map<String, Object> response = new HashMap<>();

            // VALIDAÇÃO 1: Campos obrigatórios
            if (funcionalidade.getDesignacao() == null || funcionalidade.getDesignacao().trim().isEmpty()) {
                response.put("sucesso", false);
                response.put("mensagem", "Designação é obrigatória");
                response.put("campoErro", "designacao");
                return ResponseEntity.ok(response);
            }

            if (funcionalidade.getDescricao() == null || funcionalidade.getDescricao().trim().isEmpty()) {
                response.put("sucesso", false);
                response.put("mensagem", "Descrição é obrigatória");
                response.put("campoErro", "descricao");
                return ResponseEntity.ok(response);
            }

            // VALIDAÇÃO 2: Verificar duplicidade designação + mesmo pai
            Integer paiId = (funcionalidade.getFkFuncionalidadePai() != null &&
                    funcionalidade.getFkFuncionalidadePai().getPkFuncionalidade() != null) ?
                    funcionalidade.getFkFuncionalidadePai().getPkFuncionalidade() : null;

            Map<String, Object> validacaoDuplicidade = validarDuplicidadeDesignacaoPai(
                    funcionalidade.getDesignacao(),
                    paiId,
                    null // null porque é novo cadastro
            );

            if (!(Boolean) validacaoDuplicidade.get("valido")) {
                response.put("sucesso", false);
                response.put("mensagem", validacaoDuplicidade.get("mensagem"));
                response.put("campoErro", "designacao");
                response.put("detalhes", validacaoDuplicidade.get("duplicatas"));
                return ResponseEntity.ok(response);
            }

            // VALIDAÇÃO 3: Verificar se PK já existe (se fornecida)
            if (funcionalidade.getPkFuncionalidade() != null) {
                Optional<Funcionalidade> funcExistente = funcionalidadeRepository
                        .findById(funcionalidade.getPkFuncionalidade());
                if (funcExistente.isPresent()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Já existe uma funcionalidade com PK " +
                            funcionalidade.getPkFuncionalidade());
                    response.put("campoErro", "pkFuncionalidade");
                    return ResponseEntity.ok(response);
                }
            }

            // VALIDAÇÃO 4: Verificar se o pai existe (se fornecido)
            if (paiId != null) {
                Optional<Funcionalidade> paiExistente = funcionalidadeRepository.findById(paiId);
                if (!paiExistente.isPresent()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Funcionalidade pai não encontrada (ID: " + paiId + ")");
                    response.put("campoErro", "fkFuncionalidadePai");
                    return ResponseEntity.ok(response);
                }
            }

            // Salvar se todas as validações passarem
            Funcionalidade funcionalidadeModel = funcionalidadeRepository.save(funcionalidade);

            response.put("sucesso", true);
            response.put("mensagem", "Funcionalidade cadastrada com sucesso");
            response.put("funcionalidade", funcionalidadeModel);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao cadastrar funcionalidade: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

@PostMapping("/funcionalidade_perfil_cadastrar")
    public ResponseEntity<?> cadastrarFuncionalidadePerfil(@RequestBody FuncionalidadePerfilDTO funcionalidadePerfilDTO) {

        try {
            System.out.println("=== CADASTRAR FUNCIONALIDADE PERFIL ===");
            System.out.println("Dados recebidos: " + funcionalidadePerfilDTO.toString());

            Integer fkFuncionalidade = funcionalidadePerfilDTO.getFkFuncionalidade();
            Integer fkPerfil = funcionalidadePerfilDTO.getFkPerfil();

            System.out.println("FK Funcionalidade: " + fkFuncionalidade);
            System.out.println("FK Perfil: " + fkPerfil);

            // VALIDAÇÃO, Verificar os dados obrigatórios 
            if (fkFuncionalidade == null || fkFuncionalidade <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Funcionalidade é obrigatória");
                response.put("campoErro", "fkFuncionalidade");
                return ResponseEntity.ok(response);
            }

            if (fkPerfil == null || fkPerfil <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Perfil é obrigatório");
                response.put("campoErro", "fkPerfil");
                return ResponseEntity.ok(response);
            }

            Optional<Funcionalidade> funcionalidadeOpt = funcionalidadeRepository.findById(fkFuncionalidade);
            if (!funcionalidadeOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Funcionalidade não encontrada (ID: " + fkFuncionalidade + ")");
                response.put("campoErro", "fkFuncionalidade");
                return ResponseEntity.ok(response);
            }

            Optional<Perfil> perfilOpt = perfilRepository.findById(fkPerfil);
            if (!perfilOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Perfil não encontrado (ID: " + fkPerfil + ")");
                response.put("campoErro", "fkPerfil");
                return ResponseEntity.ok(response);
            }

            // VERIFICAR SE ASSOCIAÇÃO JÁ EXISTE
            Optional<FuncionalidadePerfil> associacaoExistente =
                    funcionalidadePerfilRepository.findByFkPerfilPkPerfilAndFkFuncionalidadePkFuncionalidade(
                            fkPerfil, fkFuncionalidade);

            if (associacaoExistente.isPresent()) {
                Funcionalidade func = funcionalidadeOpt.get();
                Perfil perfil = perfilOpt.get();

                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "A funcionalidade \"" + func.getDesignacao() +
                        "\" já está atribuída ao perfil \"" + perfil.getDesignacao() + "\"");
                response.put("campoErro", "duplicidade");
                return ResponseEntity.ok(response);
            }

            // Criar nova associação
            FuncionalidadePerfil funcionalidadePerfilModel = new FuncionalidadePerfil();
            funcionalidadePerfilModel.setFkFuncionalidade(funcionalidadeOpt.get());
            funcionalidadePerfilModel.setFkPerfil(perfilOpt.get());

            // Detalhe é opcional
            if (funcionalidadePerfilDTO.getDetalhe() != null && !funcionalidadePerfilDTO.getDetalhe().isEmpty()) {
                funcionalidadePerfilModel.setDetalhe(funcionalidadePerfilDTO.getDetalhe().trim());
            }

            // Salvar associação
            FuncionalidadePerfil associacaoSalva = funcionalidadePerfilRepository.save(funcionalidadePerfilModel);

            // Preparar resposta de sucesso
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Funcionalidade atribuída ao perfil com sucesso");
            response.put("associacao",
                    Map.of(
                            "id", associacaoSalva.getPkFuncionalidadePerfil(),
                            "funcionalidade", funcionalidadeOpt.get().getDesignacao(),
                            "perfil", perfilOpt.get().getDesignacao()
                    )
            );

            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            System.err.println("Erro de integridade em cadastrarFuncionalidadePerfil: " + e.getMessage());

            // Tratar violação de constraints
            String mensagemErro = "Erro de integridade de dados";

            if (e.getMostSpecificCause() != null) {
                String causa = e.getMostSpecificCause().getMessage();
                if (causa.contains("duplicate key") || causa.contains("unique constraint")) {
                    mensagemErro = "Esta associação já existe no sistema";
                } else if (causa.contains("foreign key constraint")) {
                    mensagemErro = "Funcionalidade ou perfil inválido";
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", mensagemErro);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Erro em cadastrarFuncionalidadePerfil: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro interno ao cadastrar associação");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    @PutMapping("/funcionalidade_editar/{id}")
    @Transactional
    public ResponseEntity<?> editarFuncionalidade(@PathVariable int id, @RequestBody Funcionalidade funcionalidade) {
        try {
            Map<String, Object> response = new HashMap<>();

            // NOVA VALIDAÇÃO: Verificar duplicidade ao editar
            if (funcionalidade.getDescricao() != null && funcionalidade.getDesignacao() != null) {
                List<Funcionalidade> funcionalidadesExistentes;

                Integer paiId = (funcionalidade.getFkFuncionalidadePai() != null &&
                        funcionalidade.getFkFuncionalidadePai().getPkFuncionalidade() != null) ?
                        funcionalidade.getFkFuncionalidadePai().getPkFuncionalidade() : null;

                if (paiId != null) {
                    // 🔍 CORREÇÃO: Usar o método existente 'findByDescricaoAndDesignacaoAnd...'
                    funcionalidadesExistentes = funcionalidadeRepository
                            .findByDescricaoAndDesignacaoAndFkFuncionalidadePaiPkFuncionalidade(
                                    funcionalidade.getDescricao(),   // Parâmetro 1: Descricao
                                    funcionalidade.getDesignacao(),  // Parâmetro 2: Designacao
                                    paiId                           // Parâmetro 3: ID do pai
                            );
                } else {
                    // 🔍 CORREÇÃO: Usar o método existente 'findByDescricaoAndDesignacaoAnd...IsNull'
                    funcionalidadesExistentes = funcionalidadeRepository
                            .findByDescricaoAndDesignacaoAndFkFuncionalidadePaiIsNull(
                                    funcionalidade.getDescricao(),   // Parâmetro 1: Descricao
                                    funcionalidade.getDesignacao()   // Parâmetro 2: Designacao
                            );
                }

                // Filtrar para excluir a própria funcionalidade que está sendo editada
                final Integer finalId = id;
                funcionalidadesExistentes = funcionalidadesExistentes.stream()
                        .filter(f -> !f.getPkFuncionalidade().equals(finalId))
                        .collect(Collectors.toList());

                if (!funcionalidadesExistentes.isEmpty()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Já existe uma funcionalidade com esta designação, descrição e mesmo nível hierárquico");
                    response.put("campoErro", "duplicidade");
                    return ResponseEntity.ok(response);
                }
            }

            funcionalidade.setPkFuncionalidade(id);
            Funcionalidade funcionalidadeAtualizada = funcionalidadeRepository.save(funcionalidade);

            response.put("sucesso", true);
            response.put("mensagem", "Funcionalidade editada com sucesso");
            response.put("funcionalidade", funcionalidadeAtualizada);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao editar funcionalidade: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

  /*  @PostMapping("/funcionalidade_importar")
    @Transactional
    public ResponseEntity<?> importar(@RequestParam("file") MultipartFile file) {
        System.out.println("=== INICIANDO IMPORTAÇÃO DE DUAS FOLHAS ===");

        try {
            Map<String, Object> resultado = TipoFuncionalidadeLoader.insertBothSheets(
                    file, tipoFuncionalidadeRepository, funcionalidadeRepository, versaoService);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "❌ Erro durante a importação: " + e.getMessage());
            erro.put("sucesso", false);
            return ResponseEntity.badRequest().body(erro);
        }
    }*/

    @GetMapping("/versoes_atuais")
    @Transactional(readOnly = true)
    public ResponseEntity<?> obterVersoesAtuais() {
        Map<String, Object> resultado = new HashMap<>();

        Versao versaoTipos = versaoService.obterVersao(Defs.TIPO_FUNCIONALIDADE);
        Versao versaoFunc = versaoService.obterVersao(Defs.FUNCIONALIDADE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (versaoTipos != null) {
            resultado.put("tipos_funcionalidade", sdf.format(versaoTipos.getData()));
        } else {
            resultado.put("tipos_funcionalidade", "Nunca importado");
        }

        if (versaoFunc != null) {
            resultado.put("funcionalidades", sdf.format(versaoFunc.getData()));
        } else {
            resultado.put("funcionalidades", "Nunca importado");
        }

        return ResponseEntity.ok(resultado);
    }

   @PutMapping("/perfil_editar/{id}")
@Transactional
public ResponseEntity<?> editarPerfil(@PathVariable Integer id, @RequestBody Perfil perfil) {

    Map<String, String> erros = new HashMap<>();

    Optional<Perfil> perfilExistente = perfilRepository.findById(id);
    if (!perfilExistente.isPresent()) {
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", false);
        response.put("mensagem", "Perfil não encontrado");
        return ResponseEntity.badRequest().body(response);
    }

    if (perfil.getDesignacao() == null || perfil.getDesignacao().trim().isEmpty()) {
        erros.put("designacao", "A designação é obrigatória");
    } else {
        String designacao = perfil.getDesignacao().trim();

        if (designacao.length() < 3) {
            erros.put("designacao", "A designação deve ter no mínimo 3 caracteres");
        } else if (designacao.length() > 50) {
            erros.put("designacao", "A designação não pode exceder 50 caracteres");
        } else if (!DESIGNACAO_PATTERN.matcher(designacao).matches()) {
            erros.put("designacao", "A designação não pode conter números ou caracteres especiais");
        } else if (perfilRepository.existsByDesignacaoIgnoreCaseAndPkPerfilNot(designacao, id)) {
            erros.put("designacao", "Esta designação já está em uso por outro perfil");
        }
    }

    if (perfil.getDescricao() != null && !perfil.getDescricao().trim().isEmpty()) {
        String descricao = perfil.getDescricao().trim();

        if (descricao.length() > 200) {
            erros.put("descricao", "A descrição não pode exceder 200 caracteres");
        } else if (!DESCRICAO_PATTERN.matcher(descricao).matches()) {
            erros.put("descricao", "A descrição contém caracteres inválidos");
        }
    }

    // Validação do estado
    if (perfil.getEstado() == null) {
        erros.put("estado", "O estado é obrigatório");
    } else if (perfil.getEstado() != 0 && perfil.getEstado() != 1) {
        erros.put("estado", "Estado inválido. Deve ser 0 (INATIVO) ou 1 (ATIVO)");
    }

    if (!erros.isEmpty()) {
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", false);
        response.put("mensagem", "Erro de validação");
        response.put("erros", erros);
        return ResponseEntity.badRequest().body(response);
    }

    try {
        Perfil perfilAtual = perfilExistente.get();
        perfilAtual.setDesignacao(perfil.getDesignacao().trim());
        perfilAtual.setDescricao(perfil.getDescricao() != null ? perfil.getDescricao().trim() : null);
        perfilAtual.setEstado(perfil.getEstado());

        Perfil perfilSalvo = perfilRepository.save(perfilAtual);

        // ⭐ IMPORTANTE: Criar um DTO para retornar
        Map<String, Object> perfilDTO = new HashMap<>();
        perfilDTO.put("pkPerfil", perfilSalvo.getPkPerfil());
        perfilDTO.put("designacao", perfilSalvo.getDesignacao());
        perfilDTO.put("descricao", perfilSalvo.getDescricao());
        perfilDTO.put("estado", perfilSalvo.getEstado());
        perfilDTO.put("createdAt", perfilSalvo.getCreatedAt());
        perfilDTO.put("updatedAt", perfilSalvo.getUpdatedAt());
        // NÃO incluir funcionalidades aqui!

        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("mensagem", "Perfil atualizado com sucesso");
        response.put("perfil", perfilDTO);  // ← Retornar o DTO, não a entidade

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        e.printStackTrace(); // Log do erro no console do backend
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", false);
        response.put("mensagem", "Erro ao atualizar perfil: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

    @GetMapping("/perfil_buscar/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarPerfil(@PathVariable Integer id) {
        try {
            Optional<Perfil> perfil = perfilRepository.findById(id);

            if (perfil.isPresent()) {
                return ResponseEntity.ok(perfil.get());
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", false);
                response.put("mensagem", "Perfil não encontrado");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao buscar perfil: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/perfil_historico/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> historicoPerfil(@PathVariable Integer id) {
        try {
            List<Map<String, Object>> historico = new ArrayList<>();

            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 1);
            item1.put("acao", "Cadastro");
            item1.put("usuario", "admin");
            item1.put("data", LocalDateTime.now().minusDays(2));
            item1.put("detalhes", "Perfil criado inicialmente");
            historico.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("id", 2);
            item2.put("acao", "Atualização");
            item2.put("usuario", "admin");
            item2.put("data", LocalDateTime.now().minusDays(1));
            item2.put("detalhes", "Estado alterado para ATIVO");
            historico.add(item2);

            Map<String, Object> item3 = new HashMap<>();
            item3.put("id", 3);
            item3.put("acao", "Atualização");
            item3.put("usuario", "moderador");
            item3.put("data", LocalDateTime.now());
            item3.put("detalhes", "Descrição atualizada");
            historico.add(item3);

            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao carregar histórico");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/perfil_excluir/{id}")
    @Transactional
    public ResponseEntity<?> excluirPerfil(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Perfil> perfilExistente = perfilRepository.findById(id);

            if (!perfilExistente.isPresent()) {
                response.put("sucesso", false);
                response.put("mensagem", "Perfil não encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            Perfil perfil = perfilExistente.get();

            Long countContas = contaPerfilRepository.countByFkPerfil(perfil);
            if (countContas != null && countContas > 0) {
                response.put("sucesso", false);
                response.put("mensagem", "Não é possível excluir o perfil pois está associado a "
                        + countContas + " conta(s)");
                return ResponseEntity.badRequest().body(response);
            }

            Long countFuncionalidades = funcionalidadePerfilRepository.countByFkPerfil(perfil);
            if (countFuncionalidades != null && countFuncionalidades > 0) {
                response.put("sucesso", false);
                response.put("mensagem", "Não é possível excluir o perfil pois está associado a "
                        + countFuncionalidades + " funcionalidade(s)");
                return ResponseEntity.badRequest().body(response);
            }

            perfilRepository.deleteById(id);

            response.put("sucesso", true);
            response.put("mensagem", "Perfil excluído com sucesso");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao excluir perfil: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/funcionalidade_perfil_excluir")
    @Transactional
    public ResponseEntity<?> excluirFuncionalidadePerfil(@RequestBody FuncionalidadePerfilDTO funcionalidadePerfilDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Perfil> perfilOpt = perfilRepository.findById(funcionalidadePerfilDTO.getFkPerfil());
            Optional<Funcionalidade> funcionalidadeOpt = funcionalidadeRepository.findById(funcionalidadePerfilDTO.getFkFuncionalidade());

            if (!perfilOpt.isPresent() || !funcionalidadeOpt.isPresent()) {
                response.put("sucesso", false);
                response.put("mensagem", "Perfil ou funcionalidade não encontrados");
                return ResponseEntity.badRequest().body(response);
            }

            Perfil perfil = perfilOpt.get();
            Funcionalidade funcionalidade = funcionalidadeOpt.get();

            List<FuncionalidadePerfil> associacoes = funcionalidadePerfilRepository.findByFkPerfil(perfil);

            FuncionalidadePerfil associacaoParaExcluir = null;
            for (FuncionalidadePerfil fp : associacoes) {
                if (fp.getFkFuncionalidade().getPkFuncionalidade().equals(funcionalidade.getPkFuncionalidade())) {
                    associacaoParaExcluir = fp;
                    break;
                }
            }

            if (associacaoParaExcluir == null) {
                response.put("sucesso", false);
                response.put("mensagem", "Associação não encontrada");
                return ResponseEntity.badRequest().body(response);
            }

            funcionalidadePerfilRepository.delete(associacaoParaExcluir);

            response.put("sucesso", true);
            response.put("mensagem", "Funcionalidade removida do perfil com sucesso");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao remover funcionalidade do perfil: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verificar_estrutura_arquivo")
    public ResponseEntity<?> verificarEstruturaArquivo(@RequestParam("file") MultipartFile file) {
        Map<String, Object> resultado = new HashMap<>();

        System.out.println("@PostMapping( verificar_estrutura_arquivo )");

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

            resultado.put("total_folhas", workbook.getNumberOfSheets());

            List<Map<String, Object>> infoFolhas = new ArrayList<>();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Map<String, Object> infoFolha = new HashMap<>();
                infoFolha.put("indice", i);
                infoFolha.put("nome", workbook.getSheetName(i));
                infoFolha.put("total_linhas", sheet.getLastRowNum() + 1);

                List<List<String>> primeirasLinhas = new ArrayList<>();
                for (int j = 0; j < Math.min(5, sheet.getLastRowNum() + 1); j++) {
                    Row row = sheet.getRow(j);
                    List<String> linha = new ArrayList<>();
                    if (row != null) {
                        for (int k = 0; k < Math.min(10, row.getLastCellNum()); k++) {
                            Cell cell = row.getCell(k);
                            linha.add(FuncionsHelper.getCellAsString(cell));
                        }
                    }
                    primeirasLinhas.add(linha);
                }
                infoFolha.put("primeiras_linhas", primeirasLinhas);

                infoFolhas.add(infoFolha);
            }

            resultado.put("folhas", infoFolhas);
            resultado.put("sucesso", true);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            resultado.put("erro", "Erro ao verificar arquivo: " + e.getMessage());
            resultado.put("sucesso", false);
            return ResponseEntity.badRequest().body(resultado);
        }
    }


    @PutMapping("/conta_atualizar/{id}")
    @Transactional
    public ResponseEntity<?> atualizarConta(@PathVariable Integer id, @RequestBody ContaPerfilDTO contaPerfilDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== INICIANDO ATUALIZAÇÃO DE CONTA ID: " + id + " ===");
            System.out.println("Dados recebidos: " + contaPerfilDTO.toString());

            // Buscar conta existente
            Optional<Conta> contaExistenteOpt = contaRepository.findById(id);
            if (!contaExistenteOpt.isPresent()) {
                response.put("sucesso", false);
                response.put("mensagem", "Conta não encontrada");
                return ResponseEntity.ok(response);
            }

            Conta contaExistente = contaExistenteOpt.get();
            Pessoa pessoaExistente = contaExistente.getFkPessoa();

            if (pessoaExistente == null) {
                response.put("sucesso", false);
                response.put("mensagem", "Pessoa associada à conta não encontrada");
                return ResponseEntity.ok(response);
            }

            // VALIDAÇÃO: Verificar se email mudou e se novo email já existe
            String novoEmail = contaPerfilDTO.getEmail() != null ?
                    contaPerfilDTO.getEmail().trim().toLowerCase() : null;
            String emailAtual = contaExistente.getEmail();

            if (novoEmail != null && !novoEmail.isEmpty() && !novoEmail.equals(emailAtual)) {
                Optional<Conta> contaComMesmoEmail = contaRepository.findByEmail(novoEmail);
                if (contaComMesmoEmail.isPresent()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Este email já está em uso por outra conta");
                    response.put("campoErro", "email");
                    return ResponseEntity.ok(response);
                }
                System.out.println("Email atualizado de '" + emailAtual + "' para '" + novoEmail + "'");
            }

            // VALIDAÇÃO: Verificar se identificação mudou e se nova identificação já existe
            String novaIdentificacao = contaPerfilDTO.getIdentificacao() != null ?
                    contaPerfilDTO.getIdentificacao().trim() : null;
            String identificacaoAtual = pessoaExistente.getIdentificacao();

            if (novaIdentificacao != null && !novaIdentificacao.isEmpty() &&
                    !novaIdentificacao.equals(identificacaoAtual)) {
                Optional<Pessoa> pessoaComMesmoBI = pessoaRepository.findByIdentificacao(novaIdentificacao);
                if (pessoaComMesmoBI.isPresent()) {
                    response.put("sucesso", false);
                    response.put("mensagem", "Esta identificação já está em uso por outra pessoa");
                    response.put("campoErro", "identificacao");
                    return ResponseEntity.ok(response);
                }
                System.out.println("Identificação atualizada de '" + identificacaoAtual + "' para '" + novaIdentificacao + "'");
            }

            // ========== ATUALIZAR PESSOA ==========
            // Nome
            if (contaPerfilDTO.getNomeCompleto() != null && !contaPerfilDTO.getNomeCompleto().trim().isEmpty()) {
                pessoaExistente.setNome(contaPerfilDTO.getNomeCompleto().trim());
            }

            // Identificação
            if (novaIdentificacao != null && !novaIdentificacao.isEmpty()) {
                pessoaExistente.setIdentificacao(novaIdentificacao);
            }

            // Gênero
            int fkGenero = contaPerfilDTO.getFkGenero();
            if (fkGenero > 0) {
                pessoaExistente.setFkGenero(new Genero(fkGenero));
            } else {
                pessoaExistente.setFkGenero(null);
            }

            // Estado Civil
            int fkEstadoCivil = contaPerfilDTO.getFkEstadoCivil();
            if (fkEstadoCivil > 0) {
                pessoaExistente.setFkEstadoCivil(new EstadoCivil(fkEstadoCivil));
            } else {
                pessoaExistente.setFkEstadoCivil(null);
            }

            // Data de Nascimento
            if (contaPerfilDTO.getDataNascimento() != null && !contaPerfilDTO.getDataNascimento().isEmpty()) {
                try {
                    String dataStr = contaPerfilDTO.getDataNascimento();
                    // Aceitar formatos diferentes
                    if (dataStr.contains("-")) {
                        String[] dataArray = dataStr.split("-");
                        if (dataArray.length >= 3) {
                            int ano = Integer.parseInt(dataArray[0]);
                            int mes = Integer.parseInt(dataArray[1]);
                            int dia = Integer.parseInt(dataArray[2]);
                            LocalDate localDate = LocalDate.of(ano, mes, dia);
                            pessoaExistente.setDataNascimento(localDate);
                            System.out.println("Data de nascimento atualizada: " + localDate);
                        }
                    } else if (dataStr.contains("/")) {
                        String[] dataArray = dataStr.split("/");
                        if (dataArray.length >= 3) {
                            int dia = Integer.parseInt(dataArray[0]);
                            int mes = Integer.parseInt(dataArray[1]);
                            int ano = Integer.parseInt(dataArray[2]);
                            LocalDate localDate = LocalDate.of(ano, mes, dia);
                            pessoaExistente.setDataNascimento(localDate);
                            System.out.println("Data de nascimento atualizada: " + localDate);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao converter data de nascimento: " + e.getMessage());
                }
            }

            // ATUALIZAR ENDEREÇO
            Localidade novaLocalidade = null;
            try {
                novaLocalidade = processarEnderecoComNomes(contaPerfilDTO);
                if (novaLocalidade != null) {
                    pessoaExistente.setLocalidade(novaLocalidade);
                    System.out.println("Endereço atualizado para localidade ID: " + novaLocalidade.getPkLocalidade());
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar endereço: " + e.getMessage());
                // Mantém a localidade atual em caso de erro
            }

            // ATUALIZAR TELEFONE
            if (contaPerfilDTO.getTelefone() != null && !contaPerfilDTO.getTelefone().trim().isEmpty()) {
                String novoTelefone = contaPerfilDTO.getTelefone().trim();

                if (pessoaExistente.getTelefones() == null || pessoaExistente.getTelefones().isEmpty()) {
                    // Criar novo telefone
                    Telefone telefone = new Telefone();
                    telefone.setNumero(novoTelefone);
                    telefone.setPrincipal(true);
                    telefone.setTipo(TipoTelefoneEnum.CELULAR);
                    telefone.setFkPessoa(pessoaExistente);

                    List<Telefone> telefones = new ArrayList<>();
                    telefones.add(telefone);
                    pessoaExistente.setTelefones(telefones);
                    System.out.println("Novo telefone criado: " + novoTelefone);
                } else {
                    // Atualizar telefone principal existente
                    Telefone telefonePrincipal = pessoaExistente.getTelefones().get(0);
                    String telefoneAnterior = telefonePrincipal.getNumero();
                    telefonePrincipal.setNumero(novoTelefone);
                    System.out.println("Telefone atualizado de '" + telefoneAnterior + "' para '" + novoTelefone + "'");
                }
            }

            // Salvar pessoa
            pessoaExistente = pessoaRepository.save(pessoaExistente);
            System.out.println("Pessoa ID " + pessoaExistente.getPkPessoa() + " atualizada");

            // ========== ATUALIZAR CONTA ==========
            // Email
            if (novoEmail != null && !novoEmail.isEmpty()) {
                contaExistente.setEmail(novoEmail);
            }

            // Senha (apenas se fornecida e não estiver vazia)
            if (contaPerfilDTO.getPasswordHash() != null &&
                    !contaPerfilDTO.getPasswordHash().isEmpty() &&
                    !contaPerfilDTO.getPasswordHash().equals("[PROTEGIDO]")) {
                contaExistente.setPasswordHash(contaPerfilDTO.getPasswordHash());
                System.out.println("Senha atualizada");
            }

            // Tipo de conta
            if (contaPerfilDTO.getTipoConta() != null && !contaPerfilDTO.getTipoConta().isEmpty()) {
                try {
                    contaExistente.setTipoConta(TipoContaEnum.valueOf(contaPerfilDTO.getTipoConta()));
                } catch (IllegalArgumentException e) {
                    if ("ADMINISTRADOR".equalsIgnoreCase(contaPerfilDTO.getTipoConta())) {
                        contaExistente.setTipoConta(TipoContaEnum.ADMIN);
                    } else {
                        contaExistente.setTipoConta(TipoContaEnum.ADMIN);
                    }
                }
            }

            // Estado
            int estadoConta = contaPerfilDTO.getEstado();
            if (estadoConta >= 0 && estadoConta <= 2) { // Aceita 0, 1, 2
                contaExistente.setEstado(estadoConta);
                System.out.println("Estado da conta definido como: " + estadoConta);
            }

            contaExistente.setFkPessoa(pessoaExistente);
            contaExistente = contaRepository.save(contaExistente);
            System.out.println("Conta ID " + contaExistente.getPkConta() + " atualizada");

            // ========== ATUALIZAR CONTA-PERFIL ==========
            int fkPerfil = contaPerfilDTO.getFkPerfil();
            if (fkPerfil > 0) {
                Optional<ContaPerfil> contaPerfilExistenteOpt = contaPerfilRepository.findByFkConta(contaExistente);
                Perfil novoPerfil = perfilRepository.findByPkPerfil(fkPerfil);

                if (novoPerfil != null) {
                    if (contaPerfilExistenteOpt.isPresent()) {
                        // Atualizar associação existente
                        ContaPerfil contaPerfilExistente = contaPerfilExistenteOpt.get();
                        contaPerfilExistente.setFkPerfil(novoPerfil);
                        contaPerfilExistente.setEstado(estadoConta);
                        contaPerfilRepository.save(contaPerfilExistente);
                        System.out.println("Associação conta-perfil atualizada para perfil ID: " + fkPerfil);
                    } else {
                        // Criar nova associação
                        ContaPerfil novaContaPerfil = new ContaPerfil();
                        novaContaPerfil.setFkConta(contaExistente);
                        novaContaPerfil.setFkPerfil(novoPerfil);
                        novaContaPerfil.setEstado(estadoConta);
                        contaPerfilRepository.save(novaContaPerfil);
                        System.out.println("Nova associação conta-perfil criada para perfil ID: " + fkPerfil);
                    }
                } else {
                    System.err.println("Perfil ID " + fkPerfil + " não encontrado");
                }
            }

            response.put("sucesso", true);
            response.put("mensagem", "Conta atualizada com sucesso!");
            response.put("contaId", contaExistente.getPkConta());
            response.put("pessoaId", pessoaExistente.getPkPessoa());

            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            String mensagemErro = "Erro de integridade de dados";
            String detalheErro = "";

            if (e.getMessage() != null) {
                if (e.getMessage().contains("pessoa_identificacao_key")) {
                    mensagemErro = "Esta identificação já está em uso por outra pessoa";
                    detalheErro = "identificacao_duplicada";
                } else if (e.getMessage().contains("conta_email_key")) {
                    mensagemErro = "Este email já está em uso por outra conta";
                    detalheErro = "email_duplicado";
                } else if (e.getMessage().contains("duplicate key")) {
                    mensagemErro = "Já existe um registro com estes dados";
                    detalheErro = "registro_duplicado";
                }
            }

            System.err.println("DataIntegrityViolationException: " + e.getMessage());
            e.printStackTrace();

            response.put("sucesso", false);
            response.put("mensagem", mensagemErro);
            response.put("erroDetalhe", detalheErro);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Erro ao atualizar conta: " + e.getMessage());
            e.printStackTrace();

            response.put("sucesso", false);
            response.put("mensagem", "Erro ao atualizar conta: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/conta_excluir/{id}")
    @Transactional
    public ResponseEntity<?> excluirConta(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Conta> contaExistenteOpt = contaRepository.findById(id);

            if (!contaExistenteOpt.isPresent()) {
                response.put("sucesso", false);
                response.put("mensagem", "Conta não encontrada");
                return ResponseEntity.ok(response);
            }

            Conta conta = contaExistenteOpt.get();
            Pessoa pessoa = conta.getFkPessoa();

            // Verificar se a conta está ativa (opcional, para prevenção)
            if (conta.getEstado() != null && conta.getEstado() == 1) {
                response.put("sucesso", false);
                response.put("mensagem", "Não é possível excluir uma conta ativa. Desative-a primeiro.");
                return ResponseEntity.ok(response);
            }

            // 1. Excluir todas as associações conta-perfil
            List<ContaPerfil> associacoesContaPerfil = contaPerfilRepository.findAllByFkConta(conta);
            if (associacoesContaPerfil != null && !associacoesContaPerfil.isEmpty()) {
                contaPerfilRepository.deleteAll(associacoesContaPerfil);
                System.out.println("Excluídas " + associacoesContaPerfil.size() + " associações conta-perfil");
            }

            // 2. Excluir a conta
            contaRepository.delete(conta);
            System.out.println("Conta ID " + id + " excluída");

            // 3. Verificar se deve excluir a pessoa (apenas se não estiver vinculada a outras contas)
            if (pessoa != null) {
                Long contasDaPessoa = contaRepository.countByFkPessoa(pessoa);
                if (contasDaPessoa == null || contasDaPessoa == 0) {
                    // Verificar se a pessoa tem telefones
                    if (pessoa.getTelefones() != null && !pessoa.getTelefones().isEmpty()) {
                        // Poderia excluir telefones aqui se necessário
                        System.out.println("Pessoa tem " + pessoa.getTelefones().size() + " telefones");
                    }

                    pessoaRepository.delete(pessoa);
                    System.out.println("Pessoa ID " + pessoa.getPkPessoa() + " excluída");
                } else {
                    System.out.println("Pessoa mantida pois está vinculada a " + contasDaPessoa + " outras contas");
                }
            }

            response.put("sucesso", true);
            response.put("mensagem", "Conta excluída com sucesso");
            response.put("contaId", id);

            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            System.err.println("Erro de integridade ao excluir conta: " + e.getMessage());
            e.printStackTrace();

            String mensagemErro = "Não é possível excluir a conta pois está associada a outros registros no sistema";

            // Análise mais detalhada do erro
            if (e.getMessage() != null) {
                if (e.getMessage().contains("foreign key constraint")) {
                    mensagemErro = "Esta conta está vinculada a outros registros do sistema. "
                            + "Desvincule todas as associações antes de excluir.";
                }
            }

            response.put("sucesso", false);
            response.put("mensagem", mensagemErro);
            response.put("erroTecnico", e.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Erro ao excluir conta: " + e.getMessage());
            e.printStackTrace();

            response.put("sucesso", false);
            response.put("mensagem", "Erro ao excluir conta: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/conta_alternar_estado/{id}")
    @Transactional
    public ResponseEntity<?> alternarEstadoConta(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Conta> contaExistenteOpt = contaRepository.findById(id);

            if (!contaExistenteOpt.isPresent()) {
                response.put("sucesso", false);
                response.put("mensagem", "Conta não encontrada");
                return ResponseEntity.ok(response);
            }

            Conta conta = contaExistenteOpt.get();

            // Alternar estado (0 = inativo, 1 = ativo)
            Integer estadoAtual = conta.getEstado();
            Integer novoEstado = (estadoAtual == 1) ? 0 : 1;

            conta.setEstado(novoEstado);
            contaRepository.save(conta);

            // Atualizar também nas associações conta-perfil
            List<ContaPerfil> contaPerfis = contaPerfilRepository.findAllByFkConta(conta);
            for (ContaPerfil cp : contaPerfis) {
                cp.setEstado(novoEstado);
                contaPerfilRepository.save(cp);
            }

            String estadoStr = (novoEstado == 1) ? "ativada" : "desativada";

            response.put("sucesso", true);
            response.put("mensagem", "Conta " + estadoStr + " com sucesso");
            response.put("estadoAnterior", estadoAtual);
            response.put("novoEstado", novoEstado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Erro ao alternar estado da conta: " + e.getMessage());
            e.printStackTrace();

            response.put("sucesso", false);
            response.put("mensagem", "Erro ao alterar estado da conta: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/conta_por_email/{email}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarContaPorEmail(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Conta> contaOpt = contaRepository.findByEmail(email.toLowerCase());

            if (!contaOpt.isPresent()) {
                response.put("sucesso", true);
                response.put("existe", false);
                response.put("mensagem", "Email disponível");
                return ResponseEntity.ok(response);
            }

            Conta conta = contaOpt.get();

            Map<String, Object> dadosConta = new HashMap<>();
            dadosConta.put("pkConta", conta.getPkConta());
            dadosConta.put("email", conta.getEmail());
            dadosConta.put("estado", conta.getEstado());

            if (conta.getFkPessoa() != null) {
                dadosConta.put("nomePessoa", conta.getFkPessoa().getNome());
            }

            response.put("sucesso", true);
            response.put("existe", true);
            response.put("dados", dadosConta);
            response.put("mensagem", "Email já está em uso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao verificar email: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/conta_buscar/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscarContaPorId(@PathVariable Integer id) {
        System.out.println("=== BUSCANDO CONTA ID: " + id + " ===");

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Conta> contaOpt = contaRepository.findById(id);

            if (!contaOpt.isPresent()) {
                System.out.println("❌ Conta não encontrada: ID " + id);
                response.put("sucesso", false);
                response.put("mensagem", "Conta não encontrada");
                return ResponseEntity.ok(response);
            }

            Conta conta = contaOpt.get();
            System.out.println("✅ Conta encontrada: " + conta.getEmail());

            Map<String, Object> contaMap = new HashMap<>();

            // Dados básicos da conta
            contaMap.put("pkConta", conta.getPkConta());
            contaMap.put("email", conta.getEmail());
            contaMap.put("tipoConta", conta.getTipoConta() != null ? conta.getTipoConta().name() : null);
            contaMap.put("estado", conta.getEstado() != null ? conta.getEstado() : 1);

            if (conta.getFkPessoa() != null) {
                Pessoa pessoa = conta.getFkPessoa();
                System.out.println("Pessoa associada: " + pessoa.getNome());

                contaMap.put("nomeCompleto", pessoa.getNome());
                contaMap.put("identificacao", pessoa.getIdentificacao());

                // Telefone
                if (pessoa.getTelefones() != null && !pessoa.getTelefones().isEmpty()) {
                    Telefone telefonePrincipal = pessoa.getTelefones().get(0);
                    contaMap.put("telefone", telefonePrincipal.getNumero());
                    System.out.println("Telefone: " + telefonePrincipal.getNumero());
                }

                // Gênero
                if (pessoa.getFkGenero() != null) {
                    contaMap.put("fkGenero", pessoa.getFkGenero().getPkGenero());
                    System.out.println("Gênero ID: " + pessoa.getFkGenero().getPkGenero());
                }

                // Estado Civil
                if (pessoa.getFkEstadoCivil() != null) {
                    contaMap.put("fkEstadoCivil", pessoa.getFkEstadoCivil().getPkEstadoCivil());
                    System.out.println("Estado Civil ID: " + pessoa.getFkEstadoCivil().getPkEstadoCivil());
                }

                // Data de Nascimento
                if (pessoa.getDataNascimento() != null) {
                    contaMap.put("dataNascimento", pessoa.getDataNascimento().toString());
                    System.out.println("Data Nascimento: " + pessoa.getDataNascimento().toString());
                }

                // Informações de endereço
                if (pessoa.getLocalidade() != null) {
                    Localidade localidade = pessoa.getLocalidade();
                    System.out.println("Localidade encontrada: " + localidade.getNome());

                    contaMap.put("nomeRua", localidade.getNomeRua());

                    String provincia = extrairProvincia(localidade);
                    String municipio = extrairMunicipio(localidade);
                    String bairro = extrairBairro(localidade);

                    contaMap.put("provincia", provincia);
                    contaMap.put("municipio", municipio);
                    contaMap.put("bairro", bairro);

                    System.out.println("Endereço extraído: Província=" + provincia +
                            ", Município=" + municipio + ", Bairro=" + bairro);
                } else {
                    System.out.println("⚠️ Nenhuma localidade associada à pessoa");
                }
            } else {
                System.out.println("⚠️ Nenhuma pessoa associada à conta");
            }

            // Buscar associação conta-perfil
            Optional<ContaPerfil> contaPerfilOpt = contaPerfilRepository.findByFkConta(conta);
            if (contaPerfilOpt.isPresent()) {
                ContaPerfil contaPerfil = contaPerfilOpt.get();
                contaMap.put("fkPerfil", contaPerfil.getFkPerfil().getPkPerfil());
                System.out.println("Perfil associado: ID " + contaPerfil.getFkPerfil().getPkPerfil());
            } else {
                System.out.println("ℹ️ Nenhuma associação conta-perfil encontrada");
            }

            System.out.println("📊 Dados retornados pela API: " + contaMap.toString());

            response.put("sucesso", true);
            response.put("mensagem", "Conta encontrada");
            response.put("conta", contaMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ ERRO em conta_buscar: " + e.getMessage());
            e.printStackTrace();

            response.put("sucesso", false);
            response.put("mensagem", "Erro ao buscar conta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/tipo_funcionalidade_importar")
    @Transactional
    public ResponseEntity<?> importarTipoFuncionalidade(@RequestParam("file") MultipartFile file) {
        System.out.println("=== INICIANDO IMPORTAÇÃO APENAS DE TIPO FUNCIONALIDADE ===");

        try {
            // Processar apenas a folha de tipos (folha 2)
            Map<String, Object> resultado = TipoFuncionalidadeLoader.insertOnlyTypeSheet(
                    file, tipoFuncionalidadeRepository, versaoService);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "❌ Erro durante a importação: " + e.getMessage());
            erro.put("sucesso", false);
            return ResponseEntity.badRequest().body(erro);
        }
    }

    @PostMapping("/funcionalidade_apenas_importar")
    @Transactional
    public ResponseEntity<?> importarApenasFuncionalidade(@RequestParam("file") MultipartFile file) {

        System.out.println("=== INICIANDO IMPORTAÇÃO APENAS DE FUNCIONALIDADE ===");

        try {

            // Processar apenas a folha de funcionalidades (folha 1)
            Map<String, Object> resultado = TipoFuncionalidadeLoader.insertOnlyFuncionalidadeSheet(
                    file, funcionalidadeRepository, tipoFuncionalidadeRepository, versaoService);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "❌ Erro durante a importação: " + e.getMessage());
            erro.put("sucesso", false);
            return ResponseEntity.badRequest().body(erro);
        }

    }

/*@GetMapping("/tipo_funcionalidade_listar")
@Transactional(readOnly = true)
public ResponseEntity<?> listarTipoFuncionalidade() {
    try {
        List<TipoFuncionalidade> tipos = FuncionalidadeRepository.findAllByOrderByPkTipoFuncionalidadeAsc();
        
        List<Map<String, Object>> tiposFormatados = new ArrayList<>();
        for (TipoFuncionalidade tipo : tipos) {
            Map<String, Object> tipoMap = new HashMap<>();
            tipoMap.put("pkTipoFuncionalidade", tipo.getPkTipoFuncionalidade());
            tipoMap.put("designacao", tipo.getDesignacao());
            tipoMap.put("createdAt", tipo.getCreatedAt());
            tipoMap.put("updatedAt", tipo.getUpdatedAt());
            tiposFormatados.add(tipoMap);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("mensagem", "Tipos de funcionalidade listados com sucesso");
        response.put("dados", tiposFormatados);
        response.put("total", tiposFormatados.size());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", false);
        response.put("mensagem", "Erro ao listar tipos: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
*/


   
    
/**
 * Listar funcionalidades do perfil de uma conta
 *
 */
@GetMapping("/funcionalidade_perfil_listar_por_conta/{contaId}")
@Transactional(readOnly = true)
public ResponseEntity<?> listarFuncionalidadesPorConta(@PathVariable Integer contaId) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        System.out.println("=== BUSCANDO FUNCIONALIDADES PARA CONTA ID: " + contaId);
        
        Optional<Conta> contaOpt = contaRepository.findById(contaId);
        if (!contaOpt.isPresent()) {
            response.put("sucesso", false);
            response.put("mensagem", "Conta não encontrada");
            return ResponseEntity.badRequest().body(response);
        }
        
        Conta conta = contaOpt.get();
        System.out.println("Conta encontrada: " + conta.getEmail());
        
        // Buscar perfil da conta
        Optional<ContaPerfil> contaPerfilOpt = contaPerfilRepository.findByFkConta(conta);
        if (!contaPerfilOpt.isPresent()) {
            System.out.println("⚠️ NENHUM PERFIL associado a esta conta!");
            response.put("sucesso", true);
            response.put("funcionalidades", new ArrayList<>());
            response.put("acessoTotal", false);
            return ResponseEntity.ok(response);
        }
        
        Perfil perfil = contaPerfilOpt.get().getFkPerfil();
        System.out.println("Perfil encontrado: " + perfil.getDesignacao() + " (ID: " + perfil.getPkPerfil() + ")");
        
        // Buscar TODAS as funcionalidades do perfil
        List<FuncionalidadePerfil> associacoes = funcionalidadePerfilRepository.findByFkPerfil(perfil);
        System.out.println("🔍 Total de funcionalidades associadas a este perfil: " + associacoes.size());
        
        if (associacoes.isEmpty()) {
            System.out.println("⚠️ NENHUMA FUNCIONALIDADE associada a este perfil!");
            response.put("sucesso", true);
            response.put("funcionalidades", new ArrayList<>());
            response.put("acessoTotal", false);
            return ResponseEntity.ok(response);
        }
        
        // Converter para DTO
        List<FuncionalidadeDTO> funcionalidadesPermitidas = new ArrayList<>();
        
        for (FuncionalidadePerfil fp : associacoes) {
            Funcionalidade func = fp.getFkFuncionalidade();
            FuncionalidadeDTO dto = converterParaFuncionalidadeDTO(func);
            dto.setIsHerdadaDePerfil(fp.getIsHerdadaDePerfil() != null ? fp.getIsHerdadaDePerfil() : false);
            funcionalidadesPermitidas.add(dto);
            
            System.out.println("  → Adicionando: " + func.getDesignacao() + 
                               " (ID: " + func.getPkFuncionalidade() + 
                               ", Herdada: " + dto.getIsHerdadaDePerfil() + ")");
        }
        
        // Verificar se tem acesso total (se tem a funcionalidade raiz ID=1)
        boolean temAcessoTotal = funcionalidadesPermitidas.stream()
            .anyMatch(f -> f.getPkFuncionalidade() == 1);
        
        response.put("sucesso", true);
        response.put("funcionalidades", funcionalidadesPermitidas);
        response.put("acessoTotal", temAcessoTotal);
        response.put("perfilId", perfil.getPkPerfil());
        
        System.out.println("📤 TOTAL enviado: " + funcionalidadesPermitidas.size() + " funcionalidades");
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        e.printStackTrace();
        response.put("sucesso", false);
        response.put("mensagem", "Erro ao listar funcionalidades: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


/**
 * Método auxiliar para converter Funcionalidade para DTO
 */
private FuncionalidadeDTO converterParaFuncionalidadeDTO(Funcionalidade func) {
    FuncionalidadeDTO dto = new FuncionalidadeDTO();
    dto.setPkFuncionalidade(func.getPkFuncionalidade());
    dto.setDesignacao(func.getDesignacao());
    dto.setDescricao(func.getDescricao());
    dto.setUrl(func.getUrl());
    
    if (func.getFkTipoFuncionalidade() != null) {
        dto.setFkTipoFuncionalidade(func.getFkTipoFuncionalidade().getPkTipoFuncionalidade());
        dto.setDesignacaoTipoFuncionalidade(func.getFkTipoFuncionalidade().getDesignacao());
    }
    
    if (func.getFkFuncionalidadePai() != null) {
        dto.setFkFuncionalidadePai(func.getFkFuncionalidadePai().getPkFuncionalidade());
    }
    
    return dto;
}


 
    /**
     * Valida se já existe uma funcionalidade com mesma designação e mesmo pai
     * 
     */
    private Map<String, Object> validarDuplicidadeDesignacaoPai(
            String designacao,
            Integer fkPaiId,
            Integer pkFuncionalidadeAtual) {

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("valido", true);

        // Se designação for nula ou vazia, não valida
        if (designacao == null || designacao.trim().isEmpty()) {
            return resultado;
        }

        designacao = designacao.trim();
        List<Funcionalidade> duplicatas;

        if (fkPaiId != null && fkPaiId > 0) {
            if (pkFuncionalidadeAtual != null) {
                // Validação para edição (exclui o próprio ID)
                duplicatas = funcionalidadeRepository
                        .findByDesignacaoAndFkFuncionalidadePaiPkFuncionalidadeAndPkFuncionalidadeNot(
                                designacao, fkPaiId, pkFuncionalidadeAtual);
            } else {
                // Validação para cadastro
                duplicatas = funcionalidadeRepository
                        .findByDesignacaoAndFkFuncionalidadePaiPkFuncionalidade(designacao, fkPaiId);
            }
        } else {
            // Pai é null (funcionalidade raiz)
            if (pkFuncionalidadeAtual != null) {
                // Validação para edição (exclui o próprio ID)
                duplicatas = funcionalidadeRepository
                        .findByDesignacaoAndFkFuncionalidadePaiIsNullAndPkFuncionalidadeNot(
                                designacao, pkFuncionalidadeAtual);
            } else {
                // Validação para cadastro
                duplicatas = funcionalidadeRepository
                        .findByDesignacaoAndFkFuncionalidadePaiIsNull(designacao);
            }
        }

        if (!duplicatas.isEmpty()) {
            resultado.put("valido", false);

            String mensagemPai = (fkPaiId != null && fkPaiId > 0) ?
                    " com o pai ID " + fkPaiId : " no nível raiz";

            resultado.put("mensagem", String.format(
                    "Já existe uma funcionalidade com a designação '%s'%s. " +
                            "Designação deve ser única para cada pai.",
                    designacao, mensagemPai
            ));

            resultado.put("duplicatas", duplicatas.stream()
                    .map(f -> {
                        Map<String, Object> duplicataInfo = new HashMap<>();
                        duplicataInfo.put("pkFuncionalidade", f.getPkFuncionalidade());
                        duplicataInfo.put("designacao", f.getDesignacao());
                        duplicataInfo.put("descricao", f.getDescricao());
                        duplicataInfo.put("paiId", (f.getFkFuncionalidadePai() != null) ?
                                f.getFkFuncionalidadePai().getPkFuncionalidade() : null);
                        duplicataInfo.put("paiDesignacao", (f.getFkFuncionalidadePai() != null) ?
                                f.getFkFuncionalidadePai().getDesignacao() : "Raiz");
                        return duplicataInfo;
                    })
                    .collect(Collectors.toList()));
        }
        
        

        return resultado;
    }

}
