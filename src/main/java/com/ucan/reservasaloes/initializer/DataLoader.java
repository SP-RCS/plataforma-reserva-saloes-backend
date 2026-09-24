package com.ucan.reservasaloes.initializer;

import com.ucan.reservasaloes.entities.*;
import com.ucan.reservasaloes.enumerable.TipoContaEnum;
import com.ucan.reservasaloes.enumerable.TipoLocalidade;
import com.ucan.reservasaloes.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Optional;

@Component
public class DataLoader implements CommandLineRunner {

    private final PessoaRepository pessoaRepository;
    private final FuncionalidadeRepository funcionalidadeRepository;
    private final FuncionalidadePerfilRepository funcionalidadePerfilRepository;
    private final PerfilRepository perfilRepository;
    private final ContaRepository contaRepository;
    private final ContaPerfilRepository contaPerfilRepository;
    private final TelefoneRepository telefoneRepository;
    private final GeneroRepository generoRepository;
    private final EstadoCivilRepository estadoCivilRepository;
    private final VersaoRepository versaoRepository;
    private final LocalidadeRepository localidadeRepository;

    public DataLoader(PessoaRepository pessoaRepository,
                      FuncionalidadeRepository funcionalidadeRepository,
                      FuncionalidadePerfilRepository funcionalidadePerfilRepository,
                      PerfilRepository perfilRepository,
                      ContaRepository contaRepository,
                      ContaPerfilRepository contaPerfilRepository,
                      TelefoneRepository telefoneRepository,
                      GeneroRepository generoRepository,
                      EstadoCivilRepository estadoCivilRepository,
                      VersaoRepository versaoRepository,
                      LocalidadeRepository localidadeRepository) {
        this.pessoaRepository = pessoaRepository;
        this.funcionalidadeRepository = funcionalidadeRepository;
        this.funcionalidadePerfilRepository = funcionalidadePerfilRepository;
        this.perfilRepository = perfilRepository;
        this.contaRepository = contaRepository;
        this.contaPerfilRepository = contaPerfilRepository;
        this.telefoneRepository = telefoneRepository;
        this.generoRepository = generoRepository;
        this.estadoCivilRepository = estadoCivilRepository;
        this.versaoRepository = versaoRepository;
        this.localidadeRepository = localidadeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (generoRepository.findById(1).isEmpty()) {
            generoRepository.save(new Genero(1, "Masculino"));
            generoRepository.save(new Genero(2, "Feminino"));
        }

        if (estadoCivilRepository.findById(1).isEmpty()) {
            estadoCivilRepository.save(new EstadoCivil(1, "SOLTEIRO"));
            estadoCivilRepository.save(new EstadoCivil(2, "CASADO"));
            estadoCivilRepository.save(new EstadoCivil(3, "DIVORCIADO"));
            estadoCivilRepository.save(new EstadoCivil(4, "VIUVO"));
            estadoCivilRepository.save(new EstadoCivil(5, "UNIAO_DE_FACTO"));
        }

        Optional<Conta> contaExistente = contaRepository.findByEmail("root@root.com");
        if (contaExistente.isPresent()) {
            Conta conta = contaExistente.get();
            conta.setTipoConta(TipoContaEnum.ROOT);
            conta.setPasswordHash("root");
            contaRepository.save(conta);
            ensureVersoes();
            return;
        }

        // Reutilizar hierarquia real (não criar outra "Luanda" tipo RUA)
        Localidade localidade = localidadeRepository
                .findByNomeAndTipo("Maculusso", TipoLocalidade.BAIRRO)
                .or(() -> localidadeRepository.findByTipo(TipoLocalidade.BAIRRO).stream().findFirst())
                .or(() -> localidadeRepository.findByNomeAndTipo("Luanda", TipoLocalidade.PROVINCIA))
                .orElseGet(() -> {
                    Localidade fallback = new Localidade();
                    fallback.setNome("Sede Administrativa");
                    fallback.setTipo(TipoLocalidade.BAIRRO);
                    fallback.setNomeRua("Porto Santo");
                    return localidadeRepository.save(fallback);
                });
        if (localidade.getNomeRua() == null || localidade.getNomeRua().isBlank()) {
            localidade.setNomeRua("Porto Santo");
            localidade = localidadeRepository.save(localidade);
        }

        Pessoa pessoa = new Pessoa();
        pessoa.setIdentificacao("AAABBBCCC");
        pessoa.setNome("Root");
        pessoa.setDataNascimento(LocalDate.now());
        pessoa.setFkGenero(new Genero(1));
        pessoa.setFkEstadoCivil(new EstadoCivil(1));
        pessoa.setLocalidade(localidade);

        Optional<Pessoa> pessoaExistente =
                pessoaRepository.findByIdentificacao(pessoa.getIdentificacao());

        if (pessoaExistente.isEmpty()) {
            pessoa = pessoaRepository.save(pessoa);
        } else {
            pessoa = pessoaExistente.orElseThrow();
        }

        Conta conta = new Conta();
        conta.setTipoConta(TipoContaEnum.ROOT);
        conta.setFkPessoa(pessoa);
        conta.setEmail("root@root.com");
        conta.setPasswordHash("root");
        contaRepository.save(conta);

        Perfil perfil = new Perfil();
        perfil.setDescricao("ROOT");
        perfil.setDesignacao("ROOT");
        perfil.setEstado(1);
        perfil.setCreatedAt(LocalDateTime.now());
        perfil.setUpdatedAt(LocalDateTime.now());
        perfilRepository.save(perfil);

        ContaPerfil contaPerfil = new ContaPerfil();
        contaPerfil.setFkConta(conta);
        contaPerfil.setFkPerfil(perfil);
        contaPerfilRepository.save(contaPerfil);

        ensureVersoes();
    }

    private void ensureVersoes() {
        if (versaoRepository.findByNomeTabela("tipo_funcionalidade").isEmpty()) {
            Versao versao = new Versao();
            versao.setData(Calendar.getInstance().getTime());
            versao.setNomeTabela("tipo_funcionalidade");
            versao.setDescricao("versao 1");
            versaoRepository.save(versao);
        }

        if (versaoRepository.findByNomeTabela("funcionalidade").isEmpty()) {
            Versao versao2 = new Versao();
            versao2.setData(Calendar.getInstance().getTime());
            versao2.setNomeTabela("funcionalidade");
            versao2.setDescricao("versão 1");
            versaoRepository.save(versao2);
        }
    }
}
