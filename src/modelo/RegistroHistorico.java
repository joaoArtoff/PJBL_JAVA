package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Override
    public String toString() {
        return "[" + getDataFormatada() + "] " + tipo + " - " + titulo + "  (" + descricao + ")";
    }
}
