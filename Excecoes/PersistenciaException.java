package Excecoes;

/**
 * Lançada quando não conseguimos SALVAR ou CARREGAR a biblioteca do disco.
 *
 * Ela "embrulha" as exceções técnicas do Java (IOException,
 * ClassNotFoundException). Assim, a interface gráfica não precisa conhecer
 * detalhes de arquivo: ela só sabe que "a persistência falhou".
 * Se um dia trocarmos o arquivo por um banco de dados, a interface continua
 * tratando a MESMA exceção.
 */
public class PersistenciaException extends PlayerException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String message, Throwable cause) { super(message, cause); }
}
