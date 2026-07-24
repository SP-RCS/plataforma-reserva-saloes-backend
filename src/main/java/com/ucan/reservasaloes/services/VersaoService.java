package com.ucan.reservasaloes.services;

import com.ucan.reservasaloes.config.DataUtils;
import com.ucan.reservasaloes.entities.Versao;
import com.ucan.reservasaloes.repositories.VersaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 *
 * @author Sebastiao
 */

@Service
public class VersaoService {

    @Autowired
    private VersaoRepository versaoRepository;

    public int comparaDataVersao(String tabelaNome, Date dataArquivo) {
        Optional<Versao> versaoOptional = versaoRepository.findByNomeTabela(tabelaNome);
        
        if (versaoOptional.isEmpty()) {
            return 1; // Não existe versão anterior, arquivo é considerado novo
        } else {
            Versao versao = versaoOptional.get();
            Date dataBanco = versao.getData();
            return DataUtils.compare(dataArquivo, dataBanco);
        }
    }

    public Versao atualizarDataVersao(String tabelaNome, Date dataArquivo, String descricao) {
        Optional<Versao> versaoOptional = versaoRepository.findByNomeTabela(tabelaNome);
        Versao versao;
        
        if (versaoOptional.isEmpty()) {
            versao = new Versao(tabelaNome, dataArquivo, descricao);
        } else {
            versao = versaoOptional.get();
            versao.setData(dataArquivo);
            if (descricao != null) {
                versao.setDescricao(descricao);
            }
        }
        
        return versaoRepository.save(versao);
    }

    /**
     *
     * @param tabelaNome
     * @return
     */
    public Versao obterVersao(String tabelaNome) {
        return versaoRepository.findByNomeTabela(tabelaNome).orElse(null);
    }

    /**
     *
     * @return
     */
    public boolean isEmpty()
    {
        return versaoRepository.count() == 0;
    }
}
