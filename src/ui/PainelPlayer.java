package ui;

import excecoes.PlayerException;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import modelo.Faixa;
import modelo.Player;

/**
 * O "deck" do aparelho: disco que gira, visor LCD, barra de progresso, volume e as teclas.
 * Não toca nada por conta própria: cada tecla chama o Player e o visor só reflete o estado dele.
 * O Player avisa quando algo muda (atualizar), e um relógio de 250 ms move o tempo e a barra.
 */
public class PainelPlayer extends HBox {

    private static final double RAIO_DISCO = 78;

    private final Player player;
    private final Runnable tocarSelecionada;

    private final Group disco = criarDisco();
    private final RotateTransition giro = new RotateTransition(Duration.seconds(3.2), disco);

    private final Label lcdEstado = new Label();
    private final Label lcdFaixa = new Label();
    private final Label lcdTitulo = new Label();
    private final Label lcdDescricao = new Label();
    private final Label lcdTempo = new Label();

    private final Slider progresso = new Slider(0, 1, 0);
    private final Region trilhoFundo = new Region();
    private final Region trilhoAceso = new Region();
    private final StackPane barraProgresso = new StackPane(trilhoFundo, trilhoAceso, progresso);
    private final Slider volume = new Slider(0, 1, 0.8);
    private boolean arrastando;

    private final Button botaoAnterior = botaoTecla("btnAnterior", "M0,0 H3 V14 H0 Z M3,7 L13,0 V14 Z", "Faixa anterior");
    private final Button botaoPlay = botaoTecla("btnPlay", ICONE_PLAY, "Tocar / pausar");
    private final Button botaoParar = botaoTecla("btnParar", "M0,0 H13 V13 H0 Z", "Parar");
    private final Button botaoProxima = botaoTecla("btnProxima", "M10,0 H13 V14 H10 Z M10,7 L0,0 V14 Z", "Próxima faixa");

    private static final String ICONE_PLAY = "M0,0 L13,7 L0,14 Z";
    private static final String ICONE_PAUSA = "M0,0 H4.5 V14 H0 Z M8.5,0 H13 V14 H8.5 Z";

    private final Timeline relogio = new Timeline(new KeyFrame(Duration.millis(250), e -> tique()));

    public PainelPlayer(Player player, Runnable tocarSelecionada) {
        this.player = player;
        this.tocarSelecionada = tocarSelecionada;

        getStyleClass().add("deck");
        setSpacing(26);
        setPadding(new Insets(18, 22, 18, 22));
        setAlignment(Pos.CENTER_LEFT);

        giro.setByAngle(360);
        giro.setCycleCount(Animation.INDEFINITE);
        giro.setInterpolator(Interpolator.LINEAR);

        StackPane baia = new StackPane(disco);
        baia.getStyleClass().add("baia-disco");
        baia.setMinSize(2 * RAIO_DISCO + 28, 2 * RAIO_DISCO + 28);
        baia.setMaxSize(2 * RAIO_DISCO + 28, 2 * RAIO_DISCO + 28);

        VBox controles = montarControles();
        HBox.setHgrow(controles, Priority.ALWAYS);
        getChildren().addAll(baia, controles);

        relogio.setCycleCount(Animation.INDEFINITE);
        relogio.play();
        atualizar();
    }

    /** Visor LCD, barra de progresso e a fileira de teclas com o volume. */
    private VBox montarControles() {
        lcdEstado.getStyleClass().add("lcd-estado");
        lcdFaixa.getStyleClass().add("lcd-faixa");
        lcdTitulo.getStyleClass().add("lcd-titulo");
        lcdDescricao.getStyleClass().add("lcd-descricao");
        lcdTempo.getStyleClass().add("lcd-tempo");
        lcdTitulo.setMaxWidth(Double.MAX_VALUE);
        lcdDescricao.setMaxWidth(Double.MAX_VALUE);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox linhaTopo = new HBox(10, lcdEstado, lcdFaixa, espaco, lcdTempo);
        linhaTopo.setAlignment(Pos.BASELINE_LEFT);

        VBox visor = new VBox(4, linhaTopo, lcdTitulo, lcdDescricao);
        visor.getStyleClass().add("lcd");
        visor.setPadding(new Insets(10, 14, 10, 14));

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

        volume.setId("barraVolume");
        volume.getStyleClass().add("barra-volume");
        volume.setFocusTraversable(false);
        volume.setPrefWidth(110);
        volume.valueProperty().addListener((o, antes, agora) -> player.setVolume(agora.doubleValue()));
        Label rotuloVolume = new Label("VOL");
        rotuloVolume.getStyleClass().add("rotulo-gravado");
        rotuloVolume.setMinWidth(Region.USE_PREF_SIZE);

        botaoAnterior.setOnAction(e -> executar(player::anterior));
        botaoProxima.setOnAction(e -> executar(player::proxima));
        botaoParar.setOnAction(e -> player.parar());
        botaoPlay.setOnAction(e -> {
            if (player.getFaixaAtual() == null) {
                tocarSelecionada.run();
            } else {
                player.pausarOuRetomar();
            }
        });

        Region folga = new Region();
        HBox.setHgrow(folga, Priority.ALWAYS);
        HBox teclas = new HBox(10, botaoAnterior, botaoPlay, botaoParar, botaoProxima, folga, rotuloVolume, volume);
        teclas.setAlignment(Pos.CENTER_LEFT);

        trilhoFundo.getStyleClass().add("trilho-fundo");
        trilhoAceso.getStyleClass().add("trilho-aceso");
        StackPane.setAlignment(trilhoAceso, Pos.CENTER_LEFT);
        trilhoFundo.setMaxHeight(8);
        trilhoAceso.setMaxHeight(8);
        barraProgresso.widthProperty().addListener((o, antes, agora) -> pintarTrilho());

        VBox caixa = new VBox(14, visor, barraProgresso, teclas);
        caixa.setAlignment(Pos.CENTER_LEFT);
        return caixa;
    }

    /** Chamado pelo Player sempre que o estado muda (tocou, pausou, parou, trocou de faixa). */
    public void atualizar() {
        Faixa faixa = player.getFaixaAtual();
        boolean tem = faixa != null;
        boolean tocando = player.isTocando();

        lcdEstado.setText(!tem ? "STOP" : (tocando ? "PLAY" : "PAUSA"));
        if (tem) {
            lcdFaixa.setText(String.format("FAIXA %02d/%02d", player.getIndiceAtual() + 1,
                    player.getPlaylistAtual().getTamanho()));
            lcdTitulo.setText(faixa.getTitulo());
            lcdDescricao.setText(faixa.getDescricao());
        } else {
            lcdFaixa.setText("");
            lcdTitulo.setText("Nenhuma faixa");
            lcdDescricao.setText("Escolha uma faixa e aperte play");
            lcdTempo.setText("00:00 / 00:00");
            progresso.setValue(0);
            progresso.setMax(1);
        }

        progresso.setDisable(!tem);
        botaoParar.setDisable(!tem);
        botaoAnterior.setDisable(!player.temAnterior());
        botaoProxima.setDisable(!player.temProxima());
        trocarIcone(botaoPlay, tocando ? ICONE_PAUSA : ICONE_PLAY);

        if (tocando) {
            giro.play();
        } else if (tem) {
            giro.pause();
        } else {
            giro.stop();
            disco.setRotate(0);
        }
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
        lcdTempo.setText(formatar(arrastando ? progresso.getValue() : posicao) + " / " + formatar(duracao));
    }

    /**
     * Acende em âmbar a parte já tocada do trilho. O Slider do JavaFX não tem preenchimento
     * próprio, então a barra é um StackPane: trilho escuro, trilho âmbar (largura proporcional)
     * e o Slider transparente por cima, que só cuida do clique e do arrasto.
     */
    private void pintarTrilho() {
        double max = progresso.getMax();
        double razao = max <= 0 ? 0 : Math.min(1, Math.max(0, progresso.getValue() / max));
        double largura = barraProgresso.getWidth();
        double polegar = 12;   // largura do botãozinho do slider (definida no CSS)
        double aceso = largura <= 0 ? 0 : (razao == 0 ? 0 : polegar / 2 + razao * (largura - polegar));
        trilhoAceso.setPrefWidth(aceso);
        trilhoAceso.setMaxWidth(aceso);
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

    private static String formatar(double segundos) {
        int total = (int) Math.max(0, segundos);
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private static Button botaoTecla(String id, String desenho, String dica) {
        Button b = new Button();
        b.setId(id);
        b.getStyleClass().add("botao-tecla");
        b.setGraphic(icone(desenho));
        b.setFocusTraversable(false);
        b.setTooltip(new javafx.scene.control.Tooltip(dica));
        return b;
    }

    private static SVGPath icone(String desenho) {
        SVGPath s = new SVGPath();
        s.setContent(desenho);
        s.getStyleClass().add("icone-tecla");
        return s;
    }

    private static void trocarIcone(Button b, String desenho) {
        ((SVGPath) b.getGraphic()).setContent(desenho);
    }

    /**
     * Desenha o CD em código (sem imagem): bandeja escura, disco prateado com reflexos,
     * sulcos, aro interno e furo central. Gira inteiro quando está tocando.
     */
    private static Group criarDisco() {
        Circle base = new Circle(RAIO_DISCO);
        base.setFill(new RadialGradient(0, 0, 0, 0, RAIO_DISCO, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#f2eee4")),
                new Stop(0.35, Color.web("#cfc9ba")),
                new Stop(0.75, Color.web("#a9a395")),
                new Stop(1.0, Color.web("#8d877a"))));
        base.setStroke(Color.web("#5f5a50"));
        base.setStrokeWidth(1.5);

        Circle sulco1 = anel(RAIO_DISCO * 0.82);
        Circle sulco2 = anel(RAIO_DISCO * 0.64);
        Circle sulco3 = anel(RAIO_DISCO * 0.48);

        // Reflexos opostos: são eles que fazem a rotação ser visível.
        Arc reflexo1 = reflexo(35);
        Arc reflexo2 = reflexo(215);

        Circle aro = new Circle(RAIO_DISCO * 0.30, Color.web("#d9d2bf"));
        aro.setStroke(Color.web("#6d675b"));
        Circle miolo = new Circle(RAIO_DISCO * 0.20, Color.web("#3a2618"));
        Circle furo = new Circle(RAIO_DISCO * 0.085, Color.web("#1c120b"));

        return new Group(base, sulco1, sulco2, sulco3, reflexo1, reflexo2, aro, miolo, furo);
    }

    private static Circle anel(double raio) {
        Circle c = new Circle(raio);
        c.setFill(Color.TRANSPARENT);
        c.setStroke(Color.web("#000000", 0.10));
        c.setStrokeWidth(1);
        return c;
    }

    private static Arc reflexo(double anguloInicial) {
        Arc a = new Arc(0, 0, RAIO_DISCO - 3, RAIO_DISCO - 3, anguloInicial, 38);
        a.setType(ArcType.ROUND);
        a.setFill(new RadialGradient(0, 0, 0, 0, RAIO_DISCO, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff", 0.0)),
                new Stop(0.55, Color.web("#ffffff", 0.55)),
                new Stop(1.0, Color.web("#ffffff", 0.05))));
        a.setStroke(null);
        return a;
    }
}
