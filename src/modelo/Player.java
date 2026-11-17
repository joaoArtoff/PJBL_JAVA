package modelo;

import excecoes.FaixaInvalidaException;
import excecoes.PlayerException;
import excecoes.PlaylistInvalidaException;
import excecoes.PlaylistVaziaException;

import java.util.function.Consumer;

/**
 * O Player faz os objetos do sistema trabalharem juntos: escolhe a faixa de uma Playlist,
 * manda o Reprodutor tocar, devolve ao podcast onde ele parou, conta a reprodução na Faixa
 * e registra no Historico.
 *
 * Ele só conhece a interface Reprodutor, nunca a biblioteca de áudio.
 */
public class Player {

    private final Reprodutor reprodutor;
    private final Historico historico;
    private final RepositorioHistorico repositorio;

    private Playlist playlist;
    private int indice = -1;
    private Faixa faixaAtual;
    private boolean tocando;

    private Runnable aoMudar = () -> { };
    private Consumer<PlayerException> aoErro = e -> { };

    public Player(Reprodutor reprodutor, Historico historico, RepositorioHistorico repositorio) {
        this.reprodutor = reprodutor;
        this.historico = historico;
        this.repositorio = repositorio;
        reprodutor.setAoTerminar(this::faixaTerminou);
        reprodutor.setAoFalhar(this::reproducaoFalhou);
    }

    /** Toca a faixa da posição informada (a partir de 0) na playlist. */
    public void tocar(Playlist playlist, int indice) throws PlayerException {
        if (playlist == null) {
            throw new PlaylistInvalidaException("Nenhuma playlist selecionada.");
        }
        if (playlist.isVazia()) {
            throw new PlaylistVaziaException(playlist.getNome());
        }
        if (indice < 0 || indice >= playlist.getTamanho()) {
            throw new PlaylistInvalidaException("Essa faixa não existe na playlist.");
        }
        iniciar(playlist, indice);
    }

    private void iniciar(Playlist novaPlaylist, int novoIndice) throws PlayerException {
        encerrarFaixaAtual();
        this.playlist = novaPlaylist;
        this.indice = novoIndice;

        Faixa faixa = novaPlaylist.getFaixa(novoIndice);
        try {
            if (!faixa.arquivoExiste()) {
                throw new FaixaInvalidaException("Arquivo não encontrado: " + faixa.getCaminhoArquivo());
            }
            reprodutor.carregar(faixa.getCaminhoArquivo());
        } catch (FaixaInvalidaException e) {
            aoMudar.run();
            throw e;
        }
        // O podcast devolve a posição em que parou; a música devolve 0 (polimorfismo de Faixa).
        reprodutor.buscar(faixa.getPosicaoInicial());
        reprodutor.tocar();

        this.faixaAtual = faixa;
        this.tocando = true;
        aoMudar.run();
        // Por último: se o disco falhar, a faixa já está tocando e o erro de gravação vai para a tela.
        repositorio.adicionar(historico.registrar(faixa));
    }

    /** Pausa se estiver tocando, ou retoma se estiver pausado. */
    public void pausarOuRetomar() {
        if (faixaAtual == null) {
            return;
        }
        if (tocando) {
            faixaAtual.registrarPosicao(reprodutor.getPosicao());
            reprodutor.pausar();
            tocando = false;
        } else {
            reprodutor.tocar();
            tocando = true;
        }
        aoMudar.run();
    }

    public void proxima() throws PlayerException {
        if (temProxima()) {
            iniciar(playlist, getIndiceAtual() + 1);
        }
    }

    public void anterior() throws PlayerException {
        if (temAnterior()) {
            iniciar(playlist, getIndiceAtual() - 1);
        }
    }

    public void parar() {
        encerrarFaixaAtual();
        aoMudar.run();
    }

    public void buscar(double segundos) {
        if (faixaAtual != null) {
            reprodutor.buscar(segundos);
        }
    }

    public void setVolume(double volume) {
        reprodutor.setVolume(volume);
    }

    /** Guarda a posição na faixa (só o podcast usa) e para o áudio. */
    private void encerrarFaixaAtual() {
        if (faixaAtual != null) {
            faixaAtual.registrarPosicao(reprodutor.getPosicao());
            reprodutor.parar();
        }
        soltarFaixaAtual();
    }

    private void faixaTerminou() {
        Faixa terminada = faixaAtual;
        soltarFaixaAtual();
        if (terminada != null) {
            reprodutor.parar();
            terminada.registrarPosicao(0);   // terminou: da próxima vez começa do início
        }
        try {
            if (temProxima()) {
                iniciar(playlist, getIndiceAtual() + 1);
            } else {
                aoMudar.run();
            }
        } catch (PlayerException e) {
            aoErro.accept(e);
        }
    }

    private void reproducaoFalhou(FaixaInvalidaException erro) {
        soltarFaixaAtual();
        aoMudar.run();
        aoErro.accept(erro);
    }

    /** Esquece a faixa atual, mas guarda em que lugar da playlist ela estava (para anterior/próxima). */
    private void soltarFaixaAtual() {
        indice = getIndiceAtual();
        faixaAtual = null;
        tocando = false;
    }

    public double getPosicao() { return reprodutor.getPosicao(); }
    public double getDuracao() { return reprodutor.getDuracao(); }
    public Faixa getFaixaAtual() { return faixaAtual; }
    public Playlist getPlaylistAtual() { return playlist; }
    public boolean isTocando() { return tocando; }
    public boolean temProxima() { return playlist != null && getIndiceAtual() + 1 < playlist.getTamanho(); }
    public boolean temAnterior() { return playlist != null && getIndiceAtual() > 0; }

    /**
     * Posição (a partir de 0) da faixa atual na playlist. É procurada a cada chamada, e não só
     * guardada, porque a Playlist pode mudar enquanto a faixa toca (por exemplo, o usuário remove
     * uma faixa que vinha antes) e um número guardado passaria a apontar para a faixa errada.
     * Sem faixa tocando (parado ou depois de um erro), vale o último lugar em que o player esteve.
     */
    public int getIndiceAtual() {
        if (faixaAtual != null) {
            int posicao = playlist.indexOf(faixaAtual);
            if (posicao >= 0) {
                return posicao;
            }
        }
        return indice;
    }

    /** Quem mexeu na playlist por fora (adicionou ou removeu faixa) avisa aqui, para a tela se atualizar. */
    public void playlistMudou() {
        aoMudar.run();
    }

    /** A interface se registra aqui para ser avisada de qualquer mudança de estado. */
    public void setAoMudar(Runnable acao) {
        this.aoMudar = acao == null ? () -> { } : acao;
    }

    /** Erros que acontecem sem o usuário ter clicado em nada (faixa acabou e a próxima falhou). */
    public void setAoErro(Consumer<PlayerException> acao) {
        this.aoErro = acao == null ? e -> { } : acao;
    }
}
