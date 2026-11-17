package ui;

import excecoes.PlayerException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import modelo.Player;

/**
 * O "click wheel": um anel com as teclas de parar, voltar, avançar e tocar/pausar,
 * e o botão central, que toca a faixa escolhida na lista. Só chama o Player; quem
 * mostra o resultado é a tela.
 */
public class RodaDeClique extends StackPane {

    private static final double RAIO_ANEL = 108;
    private static final double RAIO_CENTRO = 42;

    private final Player player;

    private final Button parar = tecla("btnParar", Icones.PARAR, "Parar", 62, 46);
    private final Button anterior = tecla("btnAnterior", Icones.ANTERIOR, "Faixa anterior", 46, 62);
    private final Button proxima = tecla("btnProxima", Icones.PROXIMA, "Próxima faixa", 46, 62);
    private final Button play = tecla("btnPlay", Icones.PLAY, "Tocar / pausar", 62, 46);
    private final Button centro = new Button();

    public RodaDeClique(Player player, Runnable tocarSelecionada) {
        this.player = player;

        getStyleClass().add("roda");
        setMaxSize(2 * RAIO_ANEL, 2 * RAIO_ANEL);
        setMinSize(2 * RAIO_ANEL, 2 * RAIO_ANEL);

        Circle anel = new Circle(RAIO_ANEL);
        anel.getStyleClass().add("roda-anel");

        centro.setId("btnCentro");
        centro.getStyleClass().add("roda-centro");
        centro.setMinSize(2 * RAIO_CENTRO, 2 * RAIO_CENTRO);
        centro.setMaxSize(2 * RAIO_CENTRO, 2 * RAIO_CENTRO);
        centro.setFocusTraversable(false);
        centro.setTooltip(new Tooltip("Tocar a faixa escolhida"));

        StackPane.setAlignment(parar, Pos.TOP_CENTER);
        StackPane.setMargin(parar, new Insets(10, 0, 0, 0));
        StackPane.setAlignment(play, Pos.BOTTOM_CENTER);
        StackPane.setMargin(play, new Insets(0, 0, 10, 0));
        StackPane.setAlignment(anterior, Pos.CENTER_LEFT);
        StackPane.setMargin(anterior, new Insets(0, 0, 0, 8));
        StackPane.setAlignment(proxima, Pos.CENTER_RIGHT);
        StackPane.setMargin(proxima, new Insets(0, 8, 0, 0));

        getChildren().addAll(anel, parar, play, anterior, proxima, centro);

        parar.setOnAction(e -> player.parar());
        anterior.setOnAction(e -> executar(player::anterior));
        proxima.setOnAction(e -> executar(player::proxima));
        play.setOnAction(e -> {
            if (player.getFaixaAtual() == null) {
                tocarSelecionada.run();
            } else {
                player.pausarOuRetomar();
            }
        });
        centro.setOnAction(e -> tocarSelecionada.run());

        atualizar();
    }

    /** Chamado pelo Player sempre que o estado muda: liga e desliga as teclas e troca o ícone de play. */
    public void atualizar() {
        boolean tem = player.getFaixaAtual() != null;
        parar.setDisable(!tem);
        anterior.setDisable(!player.temAnterior());
        proxima.setDisable(!player.temProxima());
        Icones.trocar((SVGPath) play.getGraphic(), player.isTocando() ? Icones.PAUSA : Icones.PLAY);
    }

    private void executar(AcaoDoPlayer acao) {
        try {
            acao.executar();
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
    }

    private interface AcaoDoPlayer {
        void executar() throws PlayerException;
    }

    private static Button tecla(String id, String desenho, String dica, double largura, double altura) {
        Button b = new Button();
        b.setId(id);
        b.getStyleClass().add("roda-tecla");
        b.setGraphic(Icones.icone(desenho, "icone-roda"));
        b.setMinSize(largura, altura);
        b.setPrefSize(largura, altura);
        b.setMaxSize(largura, altura);
        b.setFocusTraversable(false);
        b.setTooltip(new Tooltip(dica));
        return b;
    }
}
