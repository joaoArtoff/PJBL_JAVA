package excecoes;

/** Lançada quando o nome de uma playlist é vazio ou já existe. */
public class PlaylistInvalidaException extends PlayerException {
    public PlaylistInvalidaException(String mensagem) {
        super(mensagem);
    }
}