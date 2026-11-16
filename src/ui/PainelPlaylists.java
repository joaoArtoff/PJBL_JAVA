package ui;

import excecoes.PlayerException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Biblioteca;
import modelo.Player;
import modelo.Playlist;

import java.util.Optional;

/**
 * Coluna da esquerda: as playlists da Biblioteca. Criar e remover passam sempre pela
 * Biblioteca (que garante nome válido e sem repetição); este painel só mostra o resultado.
 */
public class PainelPlaylists extends VBox {

    private final Biblioteca biblioteca;
    private final Player player;
    private final ListView<Playlist> lista = new ListView<>();
    private final Button botaoRemover = new Button("Remover");

    public PainelPlaylists(Biblioteca biblioteca, Player player) {
        this.biblioteca = biblioteca;
        this.player = player;

        getStyleClass().addAll("painel", "painel-papel");
        setSpacing(10);
        setPadding(new Insets(14));
        setPrefWidth(250);
        setMinWidth(220);

        Label titulo = new Label("PLAYLISTS");
        titulo.getStyleClass().add("titulo-painel");

        lista.setId("listaPlaylists");
        lista.setCellFactory(l -> new CelulaPlaylist());
        Label vazio = new Label("Nenhuma playlist ainda.\nClique em + Nova para criar a primeira.");
        vazio.getStyleClass().add("texto-vazio");
        lista.setPlaceholder(vazio);
        VBox.setVgrow(lista, Priority.ALWAYS);

        Button botaoNova = new Button("+ Nova");
        botaoNova.setId("btnNovaPlaylist");
        botaoNova.getStyleClass().add("botao-papel");
        botaoNova.setOnAction(e -> criar());

        botaoRemover.setId("btnRemoverPlaylist");
        botaoRemover.getStyleClass().addAll("botao-papel", "botao-perigo");
        botaoRemover.setOnAction(e -> remover());
        botaoRemover.disableProperty().bind(lista.getSelectionModel().selectedItemProperty().isNull());

        HBox botoes = new HBox(8, botaoNova, botaoRemover);
        botoes.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(titulo, lista, botoes);
        recarregar();
    }

    public ReadOnlyObjectProperty<Playlist> selecionadaProperty() {
        return lista.getSelectionModel().selectedItemProperty();
    }

    /** Redesenha as linhas (contagem de faixas, marca de "tocando") sem mexer na seleção. */
    public void atualizar() {
        lista.refresh();
    }

    private void recarregar() {
        lista.getItems().setAll(biblioteca.getPlaylists());   // a Biblioteca devolve uma cópia
    }

    private void criar() {
        Optional<String> nome = Alertas.pedirTexto("Nova playlist", "Nome da playlist", "Criar");
        if (nome.isEmpty()) {
            return;
        }
        try {
            Playlist nova = biblioteca.criarPlaylist(nome.get());
            recarregar();
            lista.getSelectionModel().select(nova);
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
    }

    private void remover() {
        Playlist escolhida = lista.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            return;
        }
        if (!Alertas.confirmar("Remover playlist",
                "Remover a playlist \"" + escolhida.getNome() + "\"? As faixas dela saem da lista, mas os arquivos continuam no disco.")) {
            return;
        }
        if (player.getPlaylistAtual() == escolhida) {
            player.parar();
        }
        biblioteca.removerPlaylist(escolhida);
        recarregar();
    }

    /** Uma linha: nome da playlist, quantidade de faixas e uma marca se é a que está tocando. */
    private class CelulaPlaylist extends ListCell<Playlist> {
        private final Label nome = new Label();
        private final Label contagem = new Label();
        private final Label marca = new Label("●");
        private final HBox caixa = new HBox(8, marca, nome, new Region(), contagem);

        CelulaPlaylist() {
            nome.getStyleClass().add("playlist-nome");
            contagem.getStyleClass().add("playlist-contagem");
            marca.getStyleClass().add("marca-tocando");
            marca.setMinWidth(12);
            HBox.setHgrow(caixa.getChildren().get(2), Priority.ALWAYS);
            caixa.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Playlist playlist, boolean vazia) {
            super.updateItem(playlist, vazia);
            if (vazia || playlist == null) {
                setGraphic(null);
                return;
            }
            nome.setText(playlist.getNome());
            int n = playlist.getTamanho();
            contagem.setText(n + (n == 1 ? " faixa" : " faixas"));
            boolean tocandoAqui = player.getPlaylistAtual() == playlist && player.getFaixaAtual() != null;
            marca.setVisible(tocandoAqui);
            setGraphic(caixa);
        }
    }
}
