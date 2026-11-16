package excecoes;

/**
 * Lançada quando uma faixa não pode ser criada ou tocada:
 *  - título vazio;
 *  - arquivo que não existe (foi apagado ou movido);
 *  - arquivo que não é .mp3 ou está corrompido.
 *
 * Herda de PlayerException -> é um "tipo de" erro do player.
 */

public class FaixaInvalidaException extends PlayerException {

    //necessidade do
    private static final long serialVersionUID = 1L;

    public FaixaInvalidaException(String message) {
        super(message);
    }

    public FaixaInvalidaException(String message, Throwable cause) { super(message, cause); }
}
