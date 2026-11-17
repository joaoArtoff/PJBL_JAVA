package ui;

import javafx.scene.Group;
import javafx.scene.shape.SVGPath;

/**
 * Ícones desenhados como formas vetoriais (sem imagens e sem glifos de fonte),
 * para ficarem nítidos em qualquer tamanho e iguais em qualquer computador.
 */
final class Icones {

    static final String PLAY = "M0,0 L12,7 L0,14 Z";
    static final String PAUSA = "M0,0 H4.2 V14 H0 Z M7.8,0 H12 V14 H7.8 Z";
    static final String PARAR = "M0,0 H12 V12 H0 Z";
    static final String ANTERIOR = "M0,0 H2.6 V14 H0 Z M2.6,7 L13,0.5 V13.5 Z";
    static final String PROXIMA = "M10.4,0 H13 V14 H10.4 Z M10.4,7 L0,0.5 V13.5 Z";
    private static final String ALTO_FALANTE = "M0,3.6 H3 L7,0 V12 L3,8.4 H0 Z";
    private static final String ONDA = "M9.4,3.2 Q12,6 9.4,8.8";
    private static final String ONDA_GRANDE = "M11.6,1.4 Q16,6 11.6,10.6";

    private Icones() { }

    static SVGPath icone(String desenho, String classeCss) {
        SVGPath s = new SVGPath();
        s.setContent(desenho);
        s.getStyleClass().add(classeCss);
        return s;
    }

    static void trocar(SVGPath icone, String desenho) {
        icone.setContent(desenho);
    }

    /** Alto-falante com uma onda (volume baixo). */
    static Group volumeBaixo() {
        return new Group(icone(ALTO_FALANTE, "icone-volume"), onda(ONDA));
    }

    /** Alto-falante com duas ondas (volume alto). */
    static Group volumeAlto() {
        return new Group(icone(ALTO_FALANTE, "icone-volume"), onda(ONDA), onda(ONDA_GRANDE));
    }

    private static SVGPath onda(String desenho) {
        SVGPath s = new SVGPath();
        s.setContent(desenho);
        s.getStyleClass().add("icone-onda");
        return s;
    }
}
