package Modelo;

import Excecoes.PersistenciaException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Histórico de tudo que já foi ouvido, gravado em um arquivo .txt (uma linha por reprodução).
 *
 * É uma classe NOVA e independente: não altera Faixa, Playlist nem Biblioteca.
 * Ela apenas LÊ uma Faixa (getTipo, getTitulo, getDescricao) para registrar o que foi tocado.
 * (Associação por uso: Historico recebe uma Faixa no método registrar.)
 *
 * Falhas de arquivo viram PersistenciaException, a mesma exceção que já existe no projeto,
 * então a interface gráfica pode tratá-la do mesmo jeito que trata o salvar/carregar da Biblioteca.
 */
public class Historico {

    private ArrayList<RegistroHistorico> registros;
    private String arquivo;

    /** Já carrega o que estava salvo de execuções anteriores. */
    public Historico(String arquivo) throws PersistenciaException {
        this.arquivo = arquivo;
        this.registros = new ArrayList<>();
        carregar();
    }

    /** Chame quando uma faixa COMEÇAR a tocar. Guarda na lista e grava no arquivo. */
    public void registrar(Faixa faixa) throws PersistenciaException {
        RegistroHistorico r = new RegistroHistorico(
                LocalDateTime.now(), faixa.getTipo(), faixa.getTitulo(), faixa.getDescricao());
        registros.add(r);
        gravar(r);
    }

    /** As últimas n reproduções, da mais recente para a mais antiga. */
    public ArrayList<RegistroHistorico> getUltimas(int n) {
        ArrayList<RegistroHistorico> resultado = new ArrayList<>();
        for (int i = registros.size() - 1; i >= 0 && resultado.size() < n; i--) {
            resultado.add(registros.get(i));
        }
        return resultado;
    }

    // Cópia da lista, pelo mesmo motivo explicado na Playlist (encapsulamento).
    public ArrayList<RegistroHistorico> getRegistros() {
        return new ArrayList<>(registros);
    }

    public int getTotal() {
        return registros.size();
    }

    public boolean isVazio() {
        return registros.isEmpty();
    }

    /** Quantas vezes uma faixa (pelo título) aparece no histórico. */
    public int contar(String titulo) {
        int vezes = 0;
        for (RegistroHistorico r : registros) {
            if (r.getTitulo().equalsIgnoreCase(titulo.trim())) {
                vezes++;
            }
        }
        return vezes;
    }

    /** Apaga o histórico da memória e do arquivo. */
    public void limpar() throws PersistenciaException {
        registros.clear();
        // abrir o arquivo SEM append zera o conteúdo
        try (FileWriter escritor = new FileWriter(arquivo, StandardCharsets.UTF_8)) {
            escritor.write("");
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível limpar o histórico em " + arquivo, e);
        }
    }

    // =========================================================================
    // ARQUIVO .txt
    // =========================================================================

    // Acrescenta UMA linha no final do arquivo (append = true: não apaga o que já existe).
    private void gravar(RegistroHistorico registro) throws PersistenciaException {
        try {
            File pasta = new File(arquivo).getParentFile();
            if (pasta != null) {
                pasta.mkdirs();     // cria a pasta se ainda não existir
            }
            try (FileWriter escritor = new FileWriter(arquivo, StandardCharsets.UTF_8, true)) {
                escritor.write(registro.toLinha() + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar o histórico em " + arquivo, e);
        }
    }

    // Lê o arquivo linha por linha e recria os registros.
    private void carregar() throws PersistenciaException {
        File f = new File(arquivo);
        if (!f.exists()) {
            return;                
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                RegistroHistorico r = RegistroHistorico.fromLinha(linha);
                if (r != null) {   
                    registros.add(r);
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o histórico de " + arquivo, e);
        }
    }
}
