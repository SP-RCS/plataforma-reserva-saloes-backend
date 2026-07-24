package com.ucan.reservasaloes.services;

import com.ucan.reservasaloes.entities.Localidade;
import com.ucan.reservasaloes.enumerable.TipoLocalidade;
import com.ucan.reservasaloes.repositories.LocalidadeRepository;
import com.ucan.reservasaloes.utils.Sitio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LocalidadeService {

    public static boolean localidadesInitialized = false;
    private static HashMap<Integer, Localidade> localidadesByPkLocalidadeCache;
    private static List<Localidade> localidades;
    private static List<Localidade> localidadesAngolanas = new ArrayList<>();
    private static Comparator<Localidade> comparadorLocalidades, comparadorLocalidadesIndefinidas;

    private static final Logger logger = LoggerFactory.getLogger(LocalidadeService.class);

    public final String INDEFINIDO = "Indefinido";
    public final String INDEFINIDA = "Indefinida";

    @Autowired
    private LocalidadeRepository localidadeRepository;

    public LocalidadeService(LocalidadeRepository localidadeRepository) {
        this.localidadeRepository = localidadeRepository;
    }

    @PostConstruct
    public void init() {
        try {
            // Verificar se já há localidades no banco
            long count = localidadeRepository.count();
            logger.info("Total de localidades no banco: {}", count);
            
            if (count == 0) {
                initializeLocalidades();
            } else {
                initLocalidadesCache();
                criarLocalidadesAngolanas();
            }
            
            localidadesInitialized = true;
            logger.info("Localidades inicializadas com sucesso. Total: {}", 
                       localidades != null ? localidades.size() : 0);
        } catch (Exception e) {
            logger.error("Erro ao inicializar localidades", e);
        }
    }

    private void initializeLocalidades() {
        logger.info("Inicializando localidades...");
        
        // Primeiro, garantir que Angola existe
        Localidade angola = localidadeRepository.findByNome("Angola")
            .orElseGet(() -> {
                logger.info("Criando localidade Angola...");
                Localidade novaAngola = new Localidade();
                novaAngola.setNome("Angola");
                novaAngola.setTipo(TipoLocalidade.PAIS);
                Localidade salva = localidadeRepository.save(novaAngola);
                logger.info("Angola criada com ID: {}", salva.getPkLocalidade());
                return salva;
            });
        
        logger.info("Angola: ID={}, Nome={}", angola.getPkLocalidade(), angola.getNome());

        // Criar Luanda (província)
        Localidade luanda = localidadeRepository.findByNomeAndLocalidadePai("Luanda", angola)
            .orElseGet(() -> {
                logger.info("Criando província Luanda...");
                Localidade provLuanda = new Localidade();
                provLuanda.setNome("Luanda");
                provLuanda.setTipo(TipoLocalidade.PROVINCIA);
                provLuanda.setLocalidadePai(angola);
                Localidade salva = localidadeRepository.save(provLuanda);
                logger.info("Luanda (província) criada com ID: {}", salva.getPkLocalidade());
                return salva;
            });

        List<Sitio> sitios = Arrays.asList(
                new Sitio("Belas", luanda.getNome(), angola.getNome(), "Rua Principal", "45"),
                new Sitio("Cacuaco", luanda.getNome(), angola.getNome(), "Avenida Central", "120"),
                new Sitio("Cazenga", luanda.getNome(), angola.getNome(), null, null),
                new Sitio("Talatona", luanda.getNome(), angola.getNome(), "Rua do Mercado", "78"),
                new Sitio("KilambaKiaxi", luanda.getNome(), angola.getNome(), null, null),
                new Sitio("Viana", luanda.getNome(), angola.getNome(), null, null)
        );

        saveAll(sitios);
    }

    private void saveAll(List<Sitio> sitios) {
        for (Sitio s : sitios) {
            try {
                Localidade loc = generateLocalidade(s);
                if (loc != null) {
                    Localidade salva = this.localidadeRepository.save(loc);
                    logger.info("Localidade salva: {} (ID: {})", 
                               salva.getNome(), salva.getPkLocalidade());
                }
            } catch (Exception e) {
                logger.error("Erro ao salvar localidade {}: {}", s.getNome(), e.getMessage());
            }
        }
        initLocalidadesCache();
        criarLocalidadesAngolanas();
    }

    public Localidade generateLocalidade(Sitio s) {
        try {
            Localidade loc = new Localidade();
            loc.setNome(s.getNome());
            loc.setNomeRua(s.getNomeRua());
//            loc.setNumero(s.getNumero());

            String pai = s.getPai();
            String avo = s.getAvo();

            Localidade paiLocal = null;

            if (pai != null) {
                if (avo == null) {
                    // Buscar apenas pelo nome do pai
                    paiLocal = localidadeRepository.findByNome(pai).orElse(null);
                } else {
                    // Buscar pai com avô específico
                    paiLocal = findLocalidadeByNomeAndAvo(pai, avo);
                }
                
                if (paiLocal == null) {
                    logger.warn("Localidade pai não encontrada: {} (avô: {})", pai, avo);
                    return null;
                }
            }

            loc.setLocalidadePai(paiLocal);

            // 🔹 DEFINIÇÃO DO TIPO
            if (paiLocal == null) {
                loc.setTipo(TipoLocalidade.PAIS);
            } else if ("Angola".equalsIgnoreCase(paiLocal.getNome())) {
                loc.setTipo(TipoLocalidade.PROVINCIA);
            } else if (paiLocal.getLocalidadePai() != null &&
                       "Angola".equalsIgnoreCase(paiLocal.getLocalidadePai().getNome())) {
                loc.setTipo(TipoLocalidade.MUNICIPIO);
            } else {
                loc.setTipo(TipoLocalidade.BAIRRO);
            }

            logger.debug("Localidade gerada: {} -> Tipo: {}", loc.getNome(), loc.getTipo());
            return loc;
            
        } catch (Exception e) {
            logger.error("Erro ao gerar localidade para {}: {}", s.getNome(), e.getMessage());
            return null;
        }
    }

    // Método auxiliar para buscar localidade por nome e avô
    private Localidade findLocalidadeByNomeAndAvo(String nome, String avoNome) {
        try {
            // Buscar todas as localidades com o nome especificado
            List<Localidade> localidadesComNome = localidadeRepository.findAllByNome(nome);
            
            if (localidadesComNome.isEmpty()) {
                logger.warn("Nenhuma localidade encontrada com nome: {}", nome);
                return null;
            }
            
            // Filtrar aquelas cujo pai (avô) tem o nome especificado
            for (Localidade loc : localidadesComNome) {
                if (loc.getLocalidadePai() != null && 
                    avoNome != null && 
                    avoNome.equalsIgnoreCase(loc.getLocalidadePai().getNome())) {
                    logger.debug("Localidade encontrada: {} (ID: {}) com pai {}", 
                               loc.getNome(), loc.getPkLocalidade(), loc.getLocalidadePai().getNome());
                    return loc;
                }
            }
            
            logger.warn("Localidade {} não encontrada com avô {}", nome, avoNome);
            return null;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar localidade {} com avô {}: {}", 
                        nome, avoNome, e.getMessage());
            return null;
        }
    }

    private void initLocalidadesCache() {
        logger.info("Inicializando cache de localidades...");
        
        localidadesByPkLocalidadeCache = new HashMap<>();
        localidades = this.localidadeRepository.findAll();

        logger.info("Total de localidades carregadas: {}", localidades.size());
        
        for (Localidade l : localidades) {
            localidadesByPkLocalidadeCache.put(l.getPkLocalidade(), l);
        }

        comparadorLocalidades = Comparator.comparing(Localidade::getNome, String.CASE_INSENSITIVE_ORDER);

        comparadorLocalidadesIndefinidas = (s1, s2) -> {
            if (s1.getLocalidadePai() == null) return 1;
            if (s2.getLocalidadePai() == null) return -1;
            return s1.getLocalidadePai().getNome().compareToIgnoreCase(s2.getLocalidadePai().getNome());
        };

        if (localidades != null && !localidades.isEmpty()) {
            Collections.sort(localidades, comparadorLocalidades);
            logger.info("Cache de localidades inicializado com {} registros", localidades.size());
        }
    }

    private void criarLocalidadesAngolanas() {
        if (localidades == null || localidades.isEmpty()) {
            logger.warn("Nenhuma localidade para criar lista angolana");
            return;
        }

        List<Localidade> copiaLocalidades = new ArrayList<>(localidades);
        List<Localidade> angolanasTemp = new ArrayList<>();

        for (Localidade localidade : copiaLocalidades) {
            if (localidade.getLocalidadePai() != null &&
                "Angola".equalsIgnoreCase(localidade.getLocalidadePai().getNome())) {

                angolanasTemp.add(localidade);
                adicionarFilhosSemModificarOriginal(localidade, angolanasTemp);
            }
        }

        localidadesAngolanas.clear();
        localidadesAngolanas.addAll(angolanasTemp);
        
        logger.info("Localidades angolanas criadas: {}", localidadesAngolanas.size());
    }

    private void adicionarFilhosSemModificarOriginal(Localidade localidade, List<Localidade> listaTemp) {
        Queue<Localidade> fila = new LinkedList<>();
        fila.add(localidade);

        while (!fila.isEmpty()) {
            Localidade atual = fila.poll();
            List<Localidade> filhos = findAllFilhos(atual.getPkLocalidade());

            for (Localidade filho : filhos) {
                if (!listaTemp.contains(filho)) {
                    listaTemp.add(filho);
                    fila.add(filho);
                }
            }
        }
    }

    public Localidade escolherAleatoriamenteLocalidadeAngolana() {
        if (localidadesAngolanas == null || localidadesAngolanas.isEmpty()) {
            logger.warn("Nenhuma localidade angolana disponível");
            return null;
        }
        Random random = new Random();
        int size = localidadesAngolanas.size();
        int posicao = random.nextInt(size);
        return localidadesAngolanas.get(posicao);
    }

    public List<Localidade> findAllFilhos(int pkLocalidadePai) {
        List<Localidade> filhos = new ArrayList<>();
        if (localidades == null) {
            return filhos;
        }
        
        for (Localidade l : localidades) {
            if (l.getLocalidadePai() != null &&
                l.getLocalidadePai().getPkLocalidade() == pkLocalidadePai) {
                filhos.add(l);
            }
        }
        
        if (!filhos.isEmpty()) {
            Collections.sort(filhos, comparadorLocalidades);
        }
        
        return colocarLocalidadesIndefinidasNoFim(filhos);
    }

    public List<Localidade> colocarLocalidadesIndefinidasNoFim(List<Localidade> lista) {
        if (lista == null || lista.isEmpty()) return lista;

        List<Localidade> indefinidas = new ArrayList<>();
        List<Localidade> result = new ArrayList<>();

        for (Localidade loc : lista) {
            if (INDEFINIDO.equalsIgnoreCase(loc.getNome()) || INDEFINIDA.equalsIgnoreCase(loc.getNome())) {
                indefinidas.add(loc);
            } else {
                result.add(loc);
            }
        }

        if (!indefinidas.isEmpty()) {
            indefinidas.sort(comparadorLocalidadesIndefinidas);
            result.addAll(indefinidas);
        }

        return result;
    }

    public List<Localidade> listarTodas() {
        if (localidades == null || localidades.isEmpty()) {
            localidades = localidadeRepository.findAll();
            initLocalidadesCache();
        }
        return localidades;
    }

    public Optional<Localidade> buscarPorId(Integer id) {
        if (localidadesByPkLocalidadeCache != null && localidadesByPkLocalidadeCache.containsKey(id)) {
            return Optional.of(localidadesByPkLocalidadeCache.get(id));
        }
        return localidadeRepository.findById(id);
    }
    
    @Transactional
    public Localidade salvar(Localidade localidade) {
        Localidade locSalva = localidadeRepository.save(localidade);
        if (localidades != null) localidades.add(locSalva);
        if (localidadesByPkLocalidadeCache != null) localidadesByPkLocalidadeCache.put(locSalva.getPkLocalidade(), locSalva);
        return locSalva;
    }

    public void eliminar(Integer id) {
        localidadeRepository.deleteById(id);
        if (localidadesByPkLocalidadeCache != null) localidadesByPkLocalidadeCache.remove(id);
        if (localidades != null) localidades.removeIf(l -> l.getPkLocalidade() == id);
    }

    public List<Localidade> listarPorLocalidadePai(Integer idPai) {
        return findAllFilhos(idPai);
    }

    /** Províncias = filhas diretas de Angola. Formato { id, nome }. */
    public List<Map<String, Object>> listarProvinciasDto() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        Optional<Localidade> angola = listarTodas().stream()
                .filter(l -> "Angola".equalsIgnoreCase(l.getNome()) && l.getTipo() == TipoLocalidade.PAIS)
                .findFirst();
        if (angola.isEmpty()) {
            return resultado;
        }
        for (Localidade provincia : findAllFilhos(angola.get().getPkLocalidade())) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", provincia.getPkLocalidade());
            item.put("nome", provincia.getNome());
            resultado.add(item);
        }
        return resultado;
    }

    /** Municípios com provinciaId. Formato { id, nome, provinciaId }. */
    public List<Map<String, Object>> listarMunicipiosDto() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Localidade loc : listarTodas()) {
            if (loc.getTipo() == TipoLocalidade.MUNICIPIO) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", loc.getPkLocalidade());
                item.put("nome", loc.getNome());
                if (loc.getLocalidadePai() != null) {
                    item.put("provinciaId", loc.getLocalidadePai().getPkLocalidade());
                }
                resultado.add(item);
            }
        }
        return resultado;
    }

    /** Bairros/ruas com municipioId. Formato { id, nome, municipioId }. */
    public List<Map<String, Object>> listarBairrosDto() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Localidade loc : listarTodas()) {
            if (loc.getTipo() == TipoLocalidade.BAIRRO || loc.getTipo() == TipoLocalidade.RUA) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", loc.getPkLocalidade());
                item.put("nome", loc.getNome());
                if (loc.getLocalidadePai() != null) {
                    item.put("municipioId", loc.getLocalidadePai().getPkLocalidade());
                }
                resultado.add(item);
            }
        }
        return resultado;
    }
    
    // Métodos adicionais para suportar o DTO
    public Localidade findOrCreateMunicipio(String nomeMunicipio) {
        // Primeiro, buscar Angola
        Localidade angola = localidadeRepository.findByNome("Angola")
            .orElseGet(() -> {
                Localidade novaAngola = new Localidade();
                novaAngola.setNome("Angola");
                novaAngola.setTipo(TipoLocalidade.PAIS);
                return localidadeRepository.save(novaAngola);
            });
        
        // Buscar município
        return localidadeRepository.findByNomeAndLocalidadePai(nomeMunicipio, angola)
            .orElseGet(() -> {
                Localidade municipio = new Localidade();
                municipio.setNome(nomeMunicipio);
                municipio.setTipo(TipoLocalidade.MUNICIPIO);
                municipio.setLocalidadePai(angola);
                return localidadeRepository.save(municipio);
            });
    }
    
    public Localidade findOrCreateBairro(String nomeBairro, String nomeRua, Localidade municipio) {
        // Buscar bairro
        return localidadeRepository.findByNomeAndLocalidadePai(nomeBairro, municipio)
            .orElseGet(() -> {
                Localidade bairro = new Localidade();
                bairro.setNome(nomeBairro);
                bairro.setNomeRua(nomeRua);
                bairro.setTipo(TipoLocalidade.BAIRRO);
                bairro.setLocalidadePai(municipio);
                return localidadeRepository.save(bairro);
            });
    }
    
    // Método para buscar município por nome
    public Optional<Localidade> findMunicipioByNome(String nomeMunicipio) {
        return localidadeRepository.findByNomeAndTipo(nomeMunicipio, TipoLocalidade.MUNICIPIO);
    }
    
    // Método para buscar bairro por nome e município
    public Optional<Localidade> findBairroByNomeAndMunicipio(String nomeBairro, String nomeMunicipio) {
        Optional<Localidade> municipio = findMunicipioByNome(nomeMunicipio);
        if (municipio.isPresent()) {
            return localidadeRepository.findByNomeAndLocalidadePai(nomeBairro, municipio.get());
        }
        return Optional.empty();
    }
}
