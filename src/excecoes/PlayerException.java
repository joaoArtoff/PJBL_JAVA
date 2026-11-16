package excecoes;

/** Por ser Exception, e não RuntimeException, é uma checked exception: o compilador obriga quem chama a tratar (try-catch) ou repassar (throws).
 * Usamos para situações que vão acontecer no uso real (MP3 apagado, playlist vazia).
 * Nunca deixar o programa "quebrar" na cara do usuário.  */
public class PlayerException extends Exception {

    // Número de versão exigido porque Exception implementa Serializable.
    private static final long serialVersionUID = 1L;

    //Cria uma exceção com uma mensagem, que repassa para o Excepetion guardar
    public PlayerException(String message) { super(message); }

    /** Cria a exceção com mensagem + a CAUSA original.
     * Ex.: uma IOException aconteceu e nós a "embrulhamos" numa PersistenciaException.
     * Guardar a causa ajuda a depurar (stack trace).
    */
    public PlayerException(String message, Throwable cause) { super(message, cause); }
}
