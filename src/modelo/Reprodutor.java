package modelo;

import excecoes.FaixaInvalidaException;

import java.util.function.Consumer;

/**
 * Contrato de "algo que sabe tocar um arquivo de áudio".
 *
 * O Player (que conhece playlists, índice, histórico e posição do podcast) só fala
 * com esta interface e não sabe COMO o som é produzido. Quem toca de verdade é uma
 * implementação (ReprodutorMp3, que usa o JavaFX Media). Assim a regra do player
 * fica separada da biblioteca de áudio e dá para trocar uma pela outra sem mexer no Player.
 */
public interface Reprodutor {

    /** Prepara o arquivo para tocar. Não começa a tocar sozinho. */
    void carregar(String caminho) throws FaixaInvalidaException;

    void tocar();

    void pausar();

    /** Para e libera o arquivo carregado. */
    void parar();

    /** Vai para o segundo informado (pode ser chamado antes de tocar). */
    void buscar(double segundos);

    /** Segundo atual da reprodução. */
    double getPosicao();

    /** Duração total em segundos, ou 0 enquanto ainda não for conhecida. */
    double getDuracao();

    /** Volume de 0.0 (mudo) a 1.0 (máximo). */
    void setVolume(double volume);

    /** Ação chamada quando a faixa chega ao fim sozinha. */
    void setAoTerminar(Runnable acao);

    /** Ação chamada quando o áudio falha de forma assíncrona (ex.: arquivo .mp3 corrompido). */
    void setAoFalhar(Consumer<FaixaInvalidaException> acao);
}
