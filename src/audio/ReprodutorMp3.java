package audio;

import excecoes.FaixaInvalidaException;
import javafx.animation.PauseTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import modelo.Reprodutor;

import java.io.File;
import java.util.function.Consumer;

/**
 * Reprodutor de verdade: toca MP3 usando o JavaFX Media.
 * Precisa ser usado na thread da interface (JavaFX Application Thread).
 *
 * O MediaPlayer só fica pronto de forma assíncrona, então guardamos o que o Player
 * pediu antes disso (buscar, tocar) e aplicamos quando o arquivo estiver pronto.
 */
public class ReprodutorMp3 implements Reprodutor {

    /**
     * Quanto esperar o arquivo ficar pronto antes de desistir. É uma escolha, não um valor medido:
     * um MP3 válido costuma carregar em menos de 1 segundo, mas um arquivo grande num disco externo
     * que ainda está acordando, ou num arquivo do iCloud ainda não baixado, pode demorar bem mais.
     * Passando disso, dizemos que o arquivo não parece um MP3 válido.
     */
    private static final double LIMITE_CARREGAMENTO_SEGUNDOS = 10;

    private MediaPlayer player;
    private boolean pronto;
    private boolean querTocar;
    private double posicaoPendente;
    private double volume = 0.8;
    private final PauseTransition limiteCarregamento = new PauseTransition(Duration.seconds(LIMITE_CARREGAMENTO_SEGUNDOS));

    private Runnable aoTerminar = () -> { };
    private Consumer<FaixaInvalidaException> aoFalhar = e -> { };

    @Override
    public void carregar(String caminho) throws FaixaInvalidaException {
        descartar();
        try {
            Media media = new Media(new File(caminho).toURI().toString());
            MediaPlayer novo = new MediaPlayer(media);
            novo.setVolume(volume);
            novo.setOnReady(() -> {
                if (player != novo) {
                    return;
                }
                limiteCarregamento.stop();
                pronto = true;
                if (posicaoPendente > 0) {
                    novo.seek(Duration.seconds(posicaoPendente));
                }
                if (querTocar) {
                    novo.play();
                }
            });
            novo.setOnEndOfMedia(() -> {
                if (player == novo) {
                    aoTerminar.run();
                }
            });
            novo.setOnError(() -> falhar(novo, caminho, novo.getError()));
            media.setOnError(() -> falhar(novo, caminho, media.getError()));
            // Num .mp3 vazio ou corrompido o JavaFX (no macOS) não avisa de erro nenhum:
            // o player fica para sempre "carregando". Por isso há um tempo limite.
            limiteCarregamento.setOnFinished(e -> {
                if (!pronto) {
                    falhar(novo, caminho, null);
                }
            });
            this.player = novo;
            limiteCarregamento.playFromStart();
        } catch (MediaException e) {
            throw new FaixaInvalidaException(mensagemFalha(caminho), e);
        }
    }

    /** Descarta a faixa e avisa o Player (só se ainda for a faixa atual, ignora avisos atrasados). */
    private void falhar(MediaPlayer origem, String caminho, Throwable causa) {
        if (player != origem) {
            return;
        }
        descartar();
        aoFalhar.accept(new FaixaInvalidaException(mensagemFalha(caminho), causa));
    }

    private static String mensagemFalha(String caminho) {
        return "Não foi possível tocar \"" + new File(caminho).getName()
                + "\": o arquivo não parece ser um MP3 válido.";
    }

    @Override
    public void tocar() {
        querTocar = true;
        if (player != null && pronto) {
            player.play();
        }
    }

    @Override
    public void pausar() {
        querTocar = false;
        if (player != null && pronto) {
            player.pause();
        }
    }

    @Override
    public void parar() {
        descartar();
    }

    @Override
    public void buscar(double segundos) {
        posicaoPendente = Math.max(0, segundos);
        if (player != null && pronto) {
            player.seek(Duration.seconds(posicaoPendente));
        }
    }

    @Override
    public double getPosicao() {
        if (player == null || !pronto) {
            return posicaoPendente;
        }
        return player.getCurrentTime().toSeconds();
    }

    @Override
    public double getDuracao() {
        if (player == null || !pronto) {
            return 0;
        }
        double d = player.getTotalDuration().toSeconds();
        return Double.isNaN(d) || Double.isInfinite(d) ? 0 : d;
    }

    @Override
    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        if (player != null) {
            player.setVolume(this.volume);
        }
    }

    @Override
    public void setAoTerminar(Runnable acao) {
        this.aoTerminar = acao == null ? () -> { } : acao;
    }

    @Override
    public void setAoFalhar(Consumer<FaixaInvalidaException> acao) {
        this.aoFalhar = acao == null ? e -> { } : acao;
    }

    /** Solta o arquivo atual e zera o estado para a próxima faixa. */
    private void descartar() {
        limiteCarregamento.stop();
        MediaPlayer antigo = player;
        player = null;
        pronto = false;
        querTocar = false;
        posicaoPendente = 0;
        if (antigo != null) {
            antigo.dispose();
        }
    }
}
