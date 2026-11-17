package modelo;

import excecoes.FaixaInvalidaException;

import java.io.File;
import java.io.Serializable;

public abstract class Faixa implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titulo;
    // "final": depois de criada, a faixa sempre aponta para o mesmo arquivo.
    private final String caminhoArquivo;

    public Faixa(String titulo, String caminhoArquivo) throws FaixaInvalidaException {
        setTitulo(titulo);
        validarCaminho(caminhoArquivo);
        this.caminhoArquivo = caminhoArquivo;
    }

    //Metodo para validar o arquivo de entrada, vendo se tem um arquivo em sí, se está em mp3 e se seu caminho está certo
    public static void validarCaminho(String caminho) throws FaixaInvalidaException {
        if (caminho == null || caminho.isBlank()) {
            throw new FaixaInvalidaException("Nenhum arquivo informado");
        }
        if (!caminho.toLowerCase().endsWith(".mp3")) {
            throw new FaixaInvalidaException("O arquivo precisa ser .mp3: " + caminho);
        }
        if (!new File(caminho).isFile()) {
            throw new FaixaInvalidaException("Arquivo não encontrado:  " + caminho);
        }
    }

    //get para mostrar o texto secundárioi da lista
    public abstract String getDescricao();

    //get para pegar o tipo(Musica ou Podcast), para ser mostrado na interdace.
    public abstract String getTipo();

    /**
     * De onde a reprodução deve começar (em segundos).
     * Padrão: do início. O podcast sobrescreve.
     */
    public double getPosicaoInicial(){ return 0; }

    /**
     * Chamado pelo Player quando a faixa é pausada/parada/trocada,
     * informando em que segundo ela estava.
     * Padrão: não faz nada (música não precisa de lembrar). O podcast sobrescreve.
     */
    public void registrarPosicao(double segundos) { }

    //Gets para pegar o título escolhido e o caminho do arquivo.
    public String getTitulo() { return titulo; }
    public String getCaminhoArquivo() { return caminhoArquivo; }

    //Setter para valídação do título da faixa.
    public void setTitulo(String titulo) throws FaixaInvalidaException {
        if (titulo == null || titulo.isBlank()) {   // isBlank também recusa "   "
            throw new FaixaInvalidaException("O título da faixa não pode estar vazio");
        }
        this.titulo = titulo.trim();
    }

    //Verificar ao tocar, se o arquivo está no disco.
    public boolean arquivoExiste() { return new File(caminhoArquivo).isFile(); }

    /**
     * Duas faixas são a mesma se apontam para o mesmo arquivo.
     * Sem isso, a Playlist (que usa contains) só reconheceria a MESMA instância,
     * e adicionar o mesmo .mp3 duas vezes pela interface criaria duplicatas.
     */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Faixa)) {
            return false;
        }
        return caminhoArquivo.equals(((Faixa) outro).caminhoArquivo);
    }

    @Override
    public int hashCode() {
        return caminhoArquivo.hashCode();
    }

    /**
     * A faixa em texto, para depurar e para o Main de console.
     * Repare: ele chama getTipo() e getDescricao(), que são ABSTRATOS.
     * Em tempo de execução o Java chama a versão da subclasse real
     * Além do polimorfismo de musica e podcast
     */
    @Override
    public String toString() {
        return "[" + getTipo() + "] " + titulo + "  —  " + getDescricao();
    }
}
