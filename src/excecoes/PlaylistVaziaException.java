package excecoes;

/** Acontece quando o usuário tenta rodar uma playlist vazia.
 * Em vez de dar erro de índice (IndexOutOfBoundsException),
 * É avisado do que aconteceu.
 */
public class PlaylistVaziaException extends PlayerException {
    private static final long serialVersionUID = 1L;

    public PlaylistVaziaException(String nomePlaylist) {
        // Montamos a mensagem aqui dentro: quem lança só informa o nome.
        super("A Playlist " + nomePlaylist + "está vazia. Adicione músicar primeiro.");
    }
}
