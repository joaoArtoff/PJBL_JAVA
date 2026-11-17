package ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import modelo.Player;

/**
 * Tampa do CD: uma janela de plástico fumê com o disco girando por trás (gira quando está
 * tocando, para quando pausa) e, embaixo, o controle de volume.
 * O disco é desenhado em código (sem imagem) e o volume vai direto para o Player.
 */
public class PainelDisco extends VBox {

    private static final double RAIO_TAMPA = 112;
    private static final double RAIO_JANELA = 101;
    private static final double RAIO_DISCO = 92;

    private final Player player;
    private final Group disco = criarDisco();
    private final RotateTransition giro = new RotateTransition(Duration.seconds(3.2), disco);

    public PainelDisco(Player player) {
        this.player = player;
        setAlignment(Pos.CENTER);
        setSpacing(26);

        giro.setByAngle(360);
        giro.setCycleCount(Animation.INDEFINITE);
        giro.setInterpolator(Interpolator.LINEAR);

        Circle aro = new Circle(RAIO_TAMPA);
        aro.getStyleClass().add("tampa-aro");
        Circle janela = new Circle(RAIO_JANELA);
        janela.getStyleClass().add("tampa-janela");
        Circle reflexo = new Circle(RAIO_JANELA);
        reflexo.getStyleClass().add("tampa-reflexo");
        reflexo.setMouseTransparent(true);
        StackPane tampa = new StackPane(aro, janela, disco, reflexo);
        tampa.getStyleClass().add("tampa");
        tampa.setMaxSize(2 * RAIO_TAMPA, 2 * RAIO_TAMPA);

        Slider volume = new Slider(0, 1, 0.8);
        volume.setId("barraVolume");
        volume.getStyleClass().add("barra-volume");
        volume.setFocusTraversable(false);
        volume.valueProperty().addListener((o, antes, agora) -> player.setVolume(agora.doubleValue()));
        HBox.setHgrow(volume, Priority.ALWAYS);

        HBox linhaVolume = new HBox(10, Icones.volumeBaixo(), volume, Icones.volumeAlto());
        linhaVolume.setAlignment(Pos.CENTER);
        linhaVolume.setMaxWidth(2 * RAIO_TAMPA - 20);

        getChildren().addAll(tampa, linhaVolume);
        atualizar();
    }

    /** Chamado pelo Player sempre que o estado muda: o disco gira só enquanto toca. */
    public void atualizar() {
        if (player.isTocando()) {
            giro.play();
        } else if (player.getFaixaAtual() != null) {
            giro.pause();
        } else {
            giro.stop();
            disco.setRotate(0);
        }
    }

    /** Desenha o CD: prata com sulcos, dois reflexos opostos (que tornam a rotação visível) e o furo central. */
    private static Group criarDisco() {
        Circle base = new Circle(RAIO_DISCO);
        base.setFill(new RadialGradient(0, 0, 0, 0, RAIO_DISCO, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#f4f6f7")),
                new Stop(0.35, Color.web("#d3d8dc")),
                new Stop(0.78, Color.web("#aab2b9")),
                new Stop(1.0, Color.web("#8b939a"))));
        base.setStroke(Color.web("#5d656c"));
        base.setStrokeWidth(1.2);

        Circle sulco1 = anel(RAIO_DISCO * 0.84);
        Circle sulco2 = anel(RAIO_DISCO * 0.66);
        Circle sulco3 = anel(RAIO_DISCO * 0.50);

        Arc reflexo1 = reflexo(38);
        Arc reflexo2 = reflexo(218);

        Circle aro = new Circle(RAIO_DISCO * 0.30, Color.web("#e6eaec"));
        aro.setStroke(Color.web("#7a8288"));
        Circle miolo = new Circle(RAIO_DISCO * 0.20, Color.web("#2c3238"));
        Circle furo = new Circle(RAIO_DISCO * 0.085, Color.web("#0d1114"));

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
        Arc a = new Arc(0, 0, RAIO_DISCO - 3, RAIO_DISCO - 3, anguloInicial, 36);
        a.setType(ArcType.ROUND);
        a.setFill(new RadialGradient(0, 0, 0, 0, RAIO_DISCO, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff", 0.0)),
                new Stop(0.55, Color.web("#ffffff", 0.6)),
                new Stop(1.0, Color.web("#ffffff", 0.05))));
        a.setStroke(null);
        return a;
    }
}
