package reproducao;

import excecoes.FaixaInvalidaException;
import excecoes.PlayerException;
import modelo.Faixa;
import modelo.Historico;
import modelo.Player;
import modelo.Playlist;
import modelo.Reprodutor;
import modelo.RepositorioHistorico;

import java.util.function.Consumer;

/**
 * PlayerModos segue o modo de reprodução (sequencial, repetir ou aleatório)
 * Extende o Player, e troca somente o que depende do modo de reprodução,
 * como ordem das faixas, quando a faixa acaba, e o que o anterior faz.
 * A interface gráfica usa o PlayerModos sem saber a diferença do Player
 */
public class PlayerModos extends Player {

    // Se já tocou mais que isso, "anterior" volta ao início da mesma faixa.
    private static final double LIMITE_ANTERIOR_SEGUNDOS = 3.0;

    private final Reprodutor audio;

    private ModoReproducao modo = ModoReproducao.SEQUENCIAL;
    private double volume = 0.8;

    // O que a tela registrou; o Player guarda um "embrulho" que repassa para estes.
    private Runnable aoMudarTela = () -> { };
    private Consumer<PlayerException> aoErroTela = e -> { };

    public PlayerModos(Reprodutor reprodutor, Historico historico, RepositorioHistorico repositorio) {
        super(reprodutor, historico, repositorio);
        this.audio = reprodutor;
        // Substitui o aviso de "faixa acabou" do Player pelo que obedece ao modo.
        reprodutor.setAoTerminar(this::aoTerminarFaixa);
        super.setAoMudar(this::notificarMudanca);
        super.setAoErro(this::notificarErro);
    }

    // =========================================================================
    // INICIAR
    // =========================================================================

    /** Toca a playlist pela primeira faixa que o modo escolhe */
    public void tocarPlaylist(Playlist playlist) throws PlayerException {
        int primeiro = (playlist == null) ? -1 : modo.proximoIndice(-1, playlist.getTamanho());
        tocar(playlist, primeiro);   // playlist nula ou vazia: o Player lança a exceção certa
    }

    /** Toca uma faixa específica da playlist */
    public void tocar(Playlist playlist, Faixa faixa) throws PlayerException {
        if (playlist == null || faixa == null) {
            throw new FaixaInvalidaException("Nenhuma faixa selecionada para tocar.");
        }
        int indice = playlist.indexOf(faixa);
        if (indice < 0) {
            throw new FaixaInvalidaException("Essa faixa não está na playlist.");
        }
        tocar(playlist, indice);
    }

    // Proxima / Anterior

    @Override
    public void proxima() throws PlayerException {
        Playlist lista = getPlaylistAtual();
        if (lista == null || lista.isVazia()) {
            return;
        }
        int proximo = modo.proximoIndice(getIndiceAtual(), lista.getTamanho());
        if (proximo == -1) {
            parar();
        } else {
            tocar(lista, proximo);
        }
    }

    /** Passou de 3 s (ou é a primeira faixa): volta ao início dela, se não volta pra anterior */
    @Override

    public void anterior() throws PlayerException {
        Playlist lista = getPlaylistAtual();
        if (lista == null) {
            return;
        }
        int atual = getIndiceAtual();
        if (getFaixaAtual() != null && (getPosicao() > LIMITE_ANTERIOR_SEGUNDOS || atual <= 0)) {
            buscar(0);
        } else if (atual > 0) {
            tocar(lista, atual - 1);
        }
    }
    /** Informa à tela se o botão "próxima" deve ficar habilitado */
    @Override
    public boolean temProxima() {
        Playlist lista = getPlaylistAtual();
        if (lista == null || lista.isVazia()) {
            return false;
        }
        // Sequencial acaba na última faixa; repetir e aleatório sempre têm uma próxima.
        return modo == ModoReproducao.SEQUENCIAL ? super.temProxima() : true;
    }
    /** Informa à tela se o botão "anterior" deve ficar habilitado */
    @Override
    public boolean temAnterior() {
        return getFaixaAtual() != null || super.temAnterior();
    }

    // Terminou a faixa: zera a posição, salva e passa pra a próxima que o modo escolher.
    // Se a próxima não puder tocar (caso o arquivo seja inválido), notifica e tenta a seguinte,
    // e dá no máximo uma volta completa na playlist.
    private void aoTerminarFaixa() {
        Faixa terminada = getFaixaAtual();
        Playlist lista = getPlaylistAtual();
        if (terminada == null || lista == null) {
            return;
        }
        int indice = getIndiceAtual();
        terminada.registrarPosicao(0);   // terminou: começa do ínicio da próxima vez
        audio.parar();                   // zera a posição do áudio, pro Player não gravar o fim por cima do 0

        for (int tentativa = 0; tentativa < lista.getTamanho(); tentativa++) {
            int proximo = modo.proximoIndice(indice, lista.getTamanho());
            if (proximo == -1) {
                break;
            }
            try {
                tocar(lista, proximo);
                return;
            } catch (FaixaInvalidaException e) {
                indice = proximo;
                notificarErro(e);
            } catch (PlayerException e) {
                notificarErro(e);   // ex.: falha ao gravar o histórico (a faixa já está tocando)
                return;
            }
        }
        parar();
    }


    // Aviso à tela e ao usuário

    private void notificarMudanca() {
        aoMudarTela.run();
    }

    private void notificarErro(PlayerException erro) {
        aoErroTela.accept(erro);
    }

    /** A tela registra aqui o que fazer quando algo muda */
    @Override
    public void setAoMudar(Runnable acao) {
        this.aoMudarTela = acao == null ? () -> { } : acao;
    }

    /** Erros sem clique do usuario caso a troca automática falhe. */
    @Override
    public void setAoErro(Consumer<PlayerException> acao) {
        this.aoErroTela = acao == null ? e -> { } : acao;
    }

    //Configuração

    public ModoReproducao getModo() { return modo; }

    public void setModo(ModoReproducao modo) {
        if (modo != null) {
            this.modo = modo;
            playlistMudou();   // a tela reavalia os botões (ex.: "próxima" no modo sequencial)
        }
    }

    public double getVolume() { return volume; }

    @Override
    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        super.setVolume(this.volume);
    }
}
