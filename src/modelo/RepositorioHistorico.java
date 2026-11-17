package modelo;

import excecoes.PersistenciaException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Lê e grava o Historico em um arquivo .txt 
 */
public class RepositorioHistorico {

    private final String arquivo;

    public RepositorioHistorico(String arquivo) {
        this.arquivo = arquivo;
    }

    /** Lê o arquivo e devolve o Historico */
    public Historico carregar() throws PersistenciaException {
        File f = new File(arquivo);
        if (!f.exists()) {
            return new Historico();
        }
        ArrayList<RegistroHistorico> registros = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                RegistroHistorico r = deLinha(linha);
                if (r != null) {   
                    registros.add(r);
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o histórico de " + arquivo, e);
        }
        return new Historico(registros);
    }

    /** Acrescenta o registro no fim do arquivo (cria o arquivo e a pasta na primeira vez). */
    public void adicionar(RegistroHistorico registro) throws PersistenciaException {
        try {
            File pasta = new File(arquivo).getParentFile();
            if (pasta != null) {
                pasta.mkdirs();     // cria a pasta se ainda não existir
            }
            try (FileWriter escritor = new FileWriter(arquivo, StandardCharsets.UTF_8, true)) {
                escritor.write(paraLinha(registro) + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar o histórico em " + arquivo, e);
        }
    }

    // FORMATO DA LINH
    private static String paraLinha(RegistroHistorico r) {
        return r.getDataHora() + ";" + limpar(r.getTipo()) + ";" + limpar(r.getTitulo()) + ";" + limpar(r.getDescricao());
    }

    private static RegistroHistorico deLinha(String linha) {
        String[] partes = linha.split(";", -1);
        if (partes.length != 4) {
            return null;
        }
        try {
            return new RegistroHistorico(LocalDateTime.parse(partes[0]), partes[1], partes[2], partes[3]);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String limpar(String texto) {
        return texto.replace(";", ",").replace("\r", " ").replace("\n", " ");
    }
}
