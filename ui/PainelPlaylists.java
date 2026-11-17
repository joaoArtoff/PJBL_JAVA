package ui;

import Excecoes.PlayerException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import Modelo.Biblioteca;
import Modelo.ImportadorDePasta;
import Modelo.Player;
import Modelo.Playlist;

import java.io.File;
import java.util.Optional;

/**
 * Lista de playlists dentro da tela do aparelho. Criar e remover passam sempre pela
 * Biblioteca (que garante nome válido e sem repetição); este painel só mostra o resultado.
 * Os botões que chamam criar() e remover() ficam no corpo do aparelho (PainelTeclas).
 */
public class PainelPlaylists extends VBox {

    private static File ultimaPasta;

    private final Biblioteca biblioteca;
    private final Player player;
    private final ListView<Playlist> lista = new ListView<>();

    public PainelPlaylists(Biblioteca biblioteca, Player player) {
        this.biblioteca = biblioteca;
        this.player = player;

        getStyleClass().add("painel-lista");
        lista.setId("listaPlaylists");
        lista.getStyleClass().add("lista-visor");
        lista.setCellFactory(l -> new CelulaPlaylist());
        Label vazio = new Label("Nenhuma playlist.\nUse Nova playlist.");
        vazio.getStyleClass().add("texto-vazio");
        lista.setPlaceholder(vazio);
        VBox.setVgrow(lista, Priority.ALWAYS);

        getChildren().add(lista);
        recarregar();
    }

    public ReadOnlyObjectProperty<Playlist> selecionadaProperty() {
        return lista.getSelectionModel().selectedItemProperty();
    }

    /** Verdadeiro enquanto nenhuma playlist está selecionada (usado para desligar o botão Remover). */
    public BooleanBinding semSelecao() {
        return lista.getSelectionModel().selectedItemProperty().isNull();
    }

    /** Redesenha as linhas (contagem de faixas, marca de "tocando") sem mexer na seleção. */
    public void atualizar() {
        lista.refresh();
    }

    private void recarregar() {
        lista.getItems().setAll(biblioteca.getPlaylists());   // a Biblioteca devolve uma cópia
    }

    public void criar() {
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

    /** Escolhe uma pasta do computador e cria uma playlist com os .mp3 dela. */
    public void importarPasta() {
        DirectoryChooser seletor = new DirectoryChooser();
        seletor.setTitle("Escolher pasta de músicas");
        File inicial = ultimaPasta != null ? ultimaPasta : new File(System.getProperty("user.home"), "Music");
        if (!inicial.isDirectory()) {
            inicial = new File(System.getProperty("user.home"));
        }
        seletor.setInitialDirectory(inicial);

        File pasta = seletor.showDialog(getScene() == null ? null : getScene().getWindow());
        if (pasta == null) {
            return;
        }
        ultimaPasta = pasta.getParentFile();
        try {
            Playlist nova = new ImportadorDePasta().importar(biblioteca, pasta);
            recarregar();
            lista.getSelectionModel().select(nova);
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
    }

    public void remover() {
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

    /** Uma linha: nome da playlist, quantidade de faixas e um ícone se é a que está tocando. */
    private class CelulaPlaylist extends ListCell<Playlist> {
        private final Label nome = new Label();
        private final Label contagem = new Label();
        private final SVGPath marca = Icones.icone(Icones.PLAY, "marcador-tocando");
        private final HBox caixa;

        CelulaPlaylist() {
            nome.getStyleClass().add("item-titulo");
            contagem.getStyleClass().add("item-detalhe");
            nome.setMinWidth(0);
            Region folga = new Region();
            HBox.setHgrow(folga, Priority.ALWAYS);
            caixa = new HBox(6, nome, folga, contagem, Celulas.reservaMarcador(marca));
            caixa.setAlignment(Pos.CENTER_LEFT);
            Celulas.ajustarLargura(this, caixa);
        }

        @Override
        protected void updateItem(Playlist playlist, boolean vazia) {
            super.updateItem(playlist, vazia);
            if (vazia || playlist == null) {
                setGraphic(null);
                return;
            }
            nome.setText(playlist.getNome());
            contagem.setText(String.valueOf(playlist.getTamanho()));
            marca.setVisible(player.getPlaylistAtual() == playlist && player.getFaixaAtual() != null);
            setGraphic(caixa);
        }
    }
}
