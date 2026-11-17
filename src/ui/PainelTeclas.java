package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Quatro teclinhas do corpo do aparelho para organizar a biblioteca: criar e remover
 * playlist, adicionar e remover faixa. Cada uma só chama o painel que cuida do assunto.
 */
public class PainelTeclas extends GridPane {

    public PainelTeclas(PainelPlaylists playlists, PainelFaixas faixas) {
        setHgap(8);
        setVgap(8);
        setAlignment(Pos.CENTER);

        Button novaPlaylist = tecla("btnNovaPlaylist", "Nova playlist");
        novaPlaylist.setOnAction(e -> playlists.criar());

        Button removerPlaylist = tecla("btnRemoverPlaylist", "Remover playlist");
        removerPlaylist.setOnAction(e -> playlists.remover());
        removerPlaylist.disableProperty().bind(playlists.semSelecao());

        Button adicionarFaixa = tecla("btnAdicionarFaixa", "Adicionar faixa");
        adicionarFaixa.setOnAction(e -> faixas.adicionar());
        adicionarFaixa.disableProperty().bind(faixas.semPlaylist());

        Button removerFaixa = tecla("btnRemoverFaixa", "Remover faixa");
        removerFaixa.setOnAction(e -> faixas.remover());
        removerFaixa.disableProperty().bind(faixas.semSelecao());

        add(novaPlaylist, 0, 0);
        add(removerPlaylist, 1, 0);
        add(adicionarFaixa, 0, 1);
        add(removerFaixa, 1, 1);

        for (int i = 0; i < 2; i++) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setPercentWidth(50);
            coluna.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(coluna);
        }
    }

    private static Button tecla(String id, String texto) {
        Button b = new Button(texto);
        b.setId(id);
        b.getStyleClass().add("tecla-prata");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFocusTraversable(false);
        return b;
    }
}
