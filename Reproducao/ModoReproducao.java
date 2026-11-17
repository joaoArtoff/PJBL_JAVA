package Reproducao;

import java.util.Random;

/**
 * Modos de reprodução que definem a ordem que as faixas são tocadas
 * Cada modo não guarda dados próprios.
 */
public enum ModoReproducao {

    /** Toca na ordem e para após a última faixa */
    SEQUENCIAL("Sequencial"),

    /** Toca na ordem e depois da última faixa reinicia voltando para a primeira faixa */
    REPETIR_PLAYLIST("Repetir playlist"),

    /** Sorteia a próxima faixa e evita repetir se houver mais de uma */
    ALEATORIO("Aleatório");

    private static final Random SORTEIO = new Random();

    private final String nome;

    ModoReproducao(String nome) {
        this.nome = nome;
    }

    /**
     * @param indiceAtual posição da faixa que está tocando (-1 se nenhuma)
     * @param tamanho     quantidade de faixas na playlist
     * @return posição da próxima faixa, ou -1 para parar a reprodução
     */
    public int proximoIndice(int indiceAtual, int tamanho) {
        if (tamanho <= 0) {
            return -1;
        }
        return switch (this) {
            case SEQUENCIAL -> {
                int proximo = indiceAtual + 1;
                yield (proximo < tamanho) ? proximo : -1;
            }
            case REPETIR_PLAYLIST -> (indiceAtual + 1) % tamanho;
            case ALEATORIO -> {
                if (tamanho == 1) {
                    yield 0;
                }
                int sorteado;
                do {
                    sorteado = SORTEIO.nextInt(tamanho);
                } while (sorteado == indiceAtual);
                yield sorteado;
            }
        };
    }

    /** Altera o modo de reprodução à cada clique na interface */
    public ModoReproducao proximo() {
        ModoReproducao[] todos = values();
        return todos[(ordinal() + 1) % todos.length];
    }

    /** Nome mostrado no botão de modo da interface */
    public String getNome() { return nome; }

    @Override
    public String toString() { return nome; }
}
