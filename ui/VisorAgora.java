package ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import Modelo.Faixa;
import Modelo.Player;

/**
 * Faixa de baixo da tela: o que está tocando, o tempo decorrido e restante e a barra de progresso.
 * Só lê o Player (nunca toca nada por conta própria), exceto ao soltar a barra, quando pede para ir
 * àquele segundo. O Player avisa quando algo muda (atualizar) e um relógio de 250 ms move o tempo.
 */
public class VisorAgora extends VBox {

    private final Player player;

    private final Label titulo = new Label();
    private final Label descricao = new Label();
    private final Label decorrido = new Label();
    private final Label restante = new Label();

    private final Slider progresso = new Slider(0, 1, 0);
    private final Region trilhoFundo = new Region();
    private final Region trilhoAceso = new Region();
    private final StackPane barra = new StackPane(trilhoFundo, trilhoAceso, progresso);
    private boolean arrastando;

    private final Timeline relogio = new Timeline(new KeyFrame(Duration.millis(250), e -> tique()));

    public VisorAgora(Player player) {
        this.player = player;

        getStyleClass().add("visor-agora");
        setSpacing(2);
        setPadding(new Insets(9, 14, 10, 14));

        titulo.getStyleClass().add("agora-titulo");
        descricao.getStyleClass().add("agora-descricao");
        titulo.setMaxWidth(Double.MAX_VALUE);
        descricao.setMaxWidth(Double.MAX_VALUE);
        decorrido.getStyleClass().add("agora-tempo");
        restante.getStyleClass().add("agora-tempo");
        decorrido.setMinWidth(Region.USE_PREF_SIZE);
        restante.setMinWidth(Region.USE_PREF_SIZE);

        progresso.setId("barraProgresso");
        progresso.getStyleClass().add("barra-progresso");
        progresso.setFocusTraversable(false);
        progresso.setOnMousePressed(e -> arrastando = true);
        progresso.setOnMouseReleased(e -> {
            arrastando = false;
            player.buscar(progresso.getValue());
        });
        progresso.valueProperty().addListener((o, antes, agora) -> pintarTrilho());
        progresso.maxProperty().addListener((o, antes, agora) -> pintarTrilho());

        trilhoFundo.getStyleClass().add("trilho-fundo");
        trilhoAceso.getStyleClass().add("trilho-aceso");
        StackPane.setAlignment(trilhoAceso, Pos.CENTER_LEFT);
        trilhoFundo.setMaxHeight(7);
        trilhoAceso.setMaxHeight(7);
        barra.widthProperty().addListener((o, antes, agora) -> pintarTrilho());
        HBox.setHgrow(barra, Priority.ALWAYS);

        HBox linhaTempo = new HBox(8, decorrido, barra, restante);
        linhaTempo.setAlignment(Pos.CENTER);
        VBox.setMargin(linhaTempo, new Insets(7, 0, 0, 0));

        getChildren().addAll(titulo, descricao, linhaTempo);

        relogio.setCycleCount(Animation.INDEFINITE);
        relogio.play();
        atualizar();
    }

    /** Chamado pelo Player sempre que o estado muda (tocou, pausou, parou, trocou de faixa). */
    public void atualizar() {
        Faixa faixa = player.getFaixaAtual();
        if (faixa != null) {
            titulo.setText(faixa.getTitulo());
            descricao.setText(faixa.getDescricao());
        } else {
            titulo.setText("Nenhuma faixa");
            descricao.setText("Escolha uma faixa e aperte o botão central");
            decorrido.setText("0:00");
            restante.setText("-0:00");
            progresso.setValue(0);
            progresso.setMax(1);
        }
        progresso.setDisable(faixa == null);
        tique();
    }

    private void tique() {
        if (player.getFaixaAtual() == null) {
            return;
        }
        double posicao = player.getPosicao();
        double duracao = player.getDuracao();
        if (duracao > 0 && progresso.getMax() != duracao) {
            progresso.setMax(duracao);
        }
        if (!arrastando) {
            progresso.setValue(Math.min(posicao, progresso.getMax()));
        }
        double atual = arrastando ? progresso.getValue() : posicao;
        decorrido.setText(formatar(atual));
        restante.setText("-" + formatar(Math.max(0, duracao - atual)));
    }

    /**
     * Acende a parte já tocada do trilho. O Slider do JavaFX não tem preenchimento próprio,
     * então a barra é um StackPane: trilho de fundo, trilho aceso (largura proporcional)
     * e o Slider transparente por cima, que só cuida do clique e do arrasto.
     */
    private void pintarTrilho() {
        double max = progresso.getMax();
        double razao = max <= 0 ? 0 : Math.min(1, Math.max(0, progresso.getValue() / max));
        double largura = barra.getWidth();
        double polegar = 14;   // largura do botãozinho do slider (definida no CSS)
        double aceso = largura <= 0 || razao == 0 ? 0 : polegar / 2 + razao * (largura - polegar);
        trilhoAceso.setPrefWidth(aceso);
        trilhoAceso.setMaxWidth(aceso);
    }

    private static String formatar(double segundos) {
        int total = (int) Math.max(0, segundos);
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
