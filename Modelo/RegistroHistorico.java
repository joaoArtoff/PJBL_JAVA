package Modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Uma linha do histórico: QUANDO uma faixa começou a tocar.
 *
 * Guarda só TEXTO (tipo, título, descrição), e não o objeto Faixa. Motivo: o histórico
 * é um registro do PASSADO. Se o .mp3 for apagado ou movido, recriar a Faixa falharia
 * (o construtor valida o arquivo), mas o registro de que ela foi ouvida continua válido.
 */
public class RegistroHistorico {

    private final LocalDateTime dataHora;
    private final String tipo;        
    private final String titulo;
    private final String descricao;   

    public RegistroHistorico(LocalDateTime dataHora, String tipo, String titulo, String descricao) {
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }

    public String getDataFormatada() {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /** Formato da linha no .txt:  dataHora;tipo;titulo;descricao */
    public String toLinha() {
        return dataHora + ";" + limpar(tipo) + ";" + limpar(titulo) + ";" + limpar(descricao);
    }

    public static RegistroHistorico fromLinha(String linha) {
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

    @Override
    public String toString() {
        return "[" + getDataFormatada() + "] " + tipo + " - " + titulo + "  (" + descricao + ")";
    }
}
