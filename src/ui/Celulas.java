package ui;

import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** O que as células das duas listas (playlists e faixas) têm em comum. */
final class Celulas {

    /** Soma dos paddings laterais da célula, definidos em `.lista-visor .list-cell` no CSS. */
    private static final double PADDING_LATERAL = 24;

    private Celulas() { }

    /** Espaço fixo para o ícone de "tocando", reservado mesmo escondido, para o texto não pular. */
    static StackPane reservaMarcador(SVGPath marca) {
        StackPane reserva = new StackPane(marca);
        reserva.setMinWidth(14);
        reserva.setPrefWidth(14);
        return reserva;
    }

    /**
     * Faz a linha ocupar só a largura da célula. Sem isso ela tem a largura que "gostaria" de ter
     * e é cortada pela célula (o tipo da faixa sumia), em vez de encolher o título com "…".
     */
    static void ajustarLargura(ListCell<?> celula, Region linha) {
        linha.prefWidthProperty().bind(celula.widthProperty().subtract(PADDING_LATERAL));
        linha.maxWidthProperty().bind(celula.widthProperty().subtract(PADDING_LATERAL));
    }
}
