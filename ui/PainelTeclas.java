package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import Reproducao.PlayerModos;

/**
 * Teclinhas do corpo do aparelho para organizar a biblioteca: criar, remover e importar de uma
 * pasta uma playlist; adicionar e remover faixa. Cada uma só chama o painel que cuida do assunto.
 * A última tecla muda o modo de reprodução (sequencial, repetir playlist, aleatório) no player.
 */
public class PainelTeclas extends GridPane {

    public PainelTeclas(PainelPlaylists playlists, PainelFaixas faixas, PlayerModos player) {
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

        Button importarPasta = tecla("btnImportarPasta", "Playlist de uma pasta…");
        importarPasta.setOnAction(e -> playlists.importarPasta());

        // Cada clique passa para o modo seguinte; o player avisa a tela (setModo -> playlistMudou).
        Button modo = tecla("btnModo", textoModo(player));
        modo.setTooltip(new Tooltip("Muda a ordem de reprodução: sequencial, repetir playlist ou aleatório"));
        modo.setOnAction(e -> {
            player.setModo(player.getModo().proximo());
            modo.setText(textoModo(player));
        });

        add(novaPlaylist, 0, 0);
        add(removerPlaylist, 1, 0);
        add(importarPasta, 0, 1, 2, 1);
        add(adicionarFaixa, 0, 2);
        add(removerFaixa, 1, 2);
        add(modo, 0, 3, 2, 1);

        for (int i = 0; i < 2; i++) {
            ColumnConstraints coluna = new ColumnConstraints();
            coluna.setPercentWidth(50);
            coluna.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(coluna);
        }
    }

    private static String textoModo(PlayerModos player) {
        return "Modo: " + player.getModo().getNome();
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
