package ui;

import excecoes.PlayerException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelo.Faixa;
import modelo.Player;
import modelo.Playlist;

import java.util.Optional;

/**
 * "Encarte": as faixas da playlist selecionada. Duplo clique (ou Enter) toca a faixa.
 *
 * Cada linha mostra tipo, título e descrição chamando só os métodos de Faixa
 * (getTipo, getDescricao, getTitulo). A mesma célula serve para música e para podcast,
 * porque cada subclasse responde do seu jeito (polimorfismo).
 */
public class PainelFaixas extends VBox {

    private final Player player;
    private final ReadOnlyObjectProperty<Playlist> playlistSelecionada;
    private final Runnable aoAlterarPlaylist;

    private final Label titulo = new Label();
    private final ListView<Faixa> lista = new ListView<>();
    private final Label placeholder = new Label();
    private final Button botaoAdicionar = new Button("+ Adicionar faixa");
    private final Button botaoRemover = new Button("Remover faixa");

    public PainelFaixas(Player player, ReadOnlyObjectProperty<Playlist> playlistSelecionada, Runnable aoAlterarPlaylist) {
        this.player = player;
        this.playlistSelecionada = playlistSelecionada;
        this.aoAlterarPlaylist = aoAlterarPlaylist;

        getStyleClass().addAll("painel", "painel-papel");
        setSpacing(10);
        setPadding(new Insets(14));

        titulo.getStyleClass().add("titulo-painel");

        lista.setId("listaFaixas");
        lista.setCellFactory(l -> new CelulaFaixa());
        placeholder.getStyleClass().add("texto-vazio");
        placeholder.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lista.setPlaceholder(placeholder);
        lista.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tocarSelecionada();
            }
        });
        VBox.setVgrow(lista, Priority.ALWAYS);

        botaoAdicionar.setId("btnAdicionarFaixa");
        botaoAdicionar.getStyleClass().add("botao-papel");
        botaoAdicionar.setOnAction(e -> adicionar());
        botaoAdicionar.disableProperty().bind(playlistSelecionada.isNull());

        botaoRemover.setId("btnRemoverFaixa");
        botaoRemover.getStyleClass().addAll("botao-papel", "botao-perigo");
        botaoRemover.setOnAction(e -> remover());
        botaoRemover.disableProperty().bind(lista.getSelectionModel().selectedItemProperty().isNull());

        HBox botoes = new HBox(8, botaoAdicionar, botaoRemover);
        botoes.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(titulo, lista, botoes);

        playlistSelecionada.addListener((o, antes, agora) -> recarregar());
        recarregar();
    }

    /** Redesenha as linhas (vezes tocada, "Parou em", marca de "tocando") sem mexer na seleção. */
    public void atualizar() {
        lista.refresh();
    }

    /** Usada pelo botão play quando nada está tocando: toca a faixa escolhida, ou a primeira. */
    public void tocarSelecionadaOuPrimeira() {
        Playlist playlist = playlistSelecionada.get();
        if (playlist == null) {
            Alertas.aviso("Escolha uma playlist", "Selecione uma playlist na coluna da esquerda para tocar.");
            return;
        }
        int indice = lista.getSelectionModel().getSelectedIndex();
        tocar(playlist, Math.max(indice, 0));
    }

    private void tocarSelecionada() {
        Playlist playlist = playlistSelecionada.get();
        int indice = lista.getSelectionModel().getSelectedIndex();
        if (playlist != null && indice >= 0) {
            tocar(playlist, indice);
        }
    }

    private void tocar(Playlist playlist, int indice) {
        try {
            player.tocar(playlist, indice);
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
    }

    private void recarregar() {
        Playlist playlist = playlistSelecionada.get();
        if (playlist == null) {
            titulo.setText("ENCARTE");
            placeholder.setText("Crie ou escolha uma playlist\npara ver as faixas dela aqui.");
            lista.getItems().clear();
            return;
        }
        titulo.setText("ENCARTE  ·  " + playlist.getNome().toUpperCase());
        placeholder.setText("Esta playlist está vazia.\nClique em + Adicionar faixa.");
        lista.getItems().setAll(playlist.getFaixas());   // a Playlist devolve uma cópia
    }

    private void adicionar() {
        Playlist playlist = playlistSelecionada.get();
        if (playlist == null) {
            return;
        }
        Optional<Faixa> nova = DialogoFaixa.mostrar();
        if (nova.isEmpty()) {
            return;
        }
        try {
            if (!playlist.adicionarFaixa(nova.get())) {
                Alertas.aviso("Faixa repetida", "Esse arquivo já está na playlist \"" + playlist.getNome() + "\".");
                return;
            }
            recarregar();
            lista.getSelectionModel().selectLast();
            lista.scrollTo(lista.getItems().size() - 1);
            aoAlterarPlaylist.run();
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
    }

    private void remover() {
        Playlist playlist = playlistSelecionada.get();
        Faixa faixa = lista.getSelectionModel().getSelectedItem();
        if (playlist == null || faixa == null) {
            return;
        }
        if (!Alertas.confirmar("Remover faixa",
                "Remover \"" + faixa.getTitulo() + "\" da playlist? O arquivo continua no disco.")) {
            return;
        }
        if (player.getFaixaAtual() == faixa) {
            player.parar();
        }
        playlist.removerFaixa(faixa);
        recarregar();
        aoAlterarPlaylist.run();
    }

    /** Uma linha do encarte: número, selo do tipo, título, descrição e quantas vezes já tocou. */
    private class CelulaFaixa extends ListCell<Faixa> {
        private final Label numero = new Label();
        private final Label selo = new Label();
        private final Label nome = new Label();
        private final Label descricao = new Label();
        private final Label vezes = new Label();
        private final Label marca = new Label("▶");
        private final HBox caixa;

        CelulaFaixa() {
            numero.getStyleClass().add("faixa-numero");
            selo.getStyleClass().add("faixa-selo");
            nome.getStyleClass().add("faixa-titulo");
            descricao.getStyleClass().add("faixa-descricao");
            vezes.getStyleClass().add("faixa-vezes");
            marca.getStyleClass().add("marca-tocando");

            // Colunas fixas (marca, número, selo e contagem) para as linhas ficarem alinhadas.
            marca.setMinWidth(12);
            VBox textos = new VBox(2, nome, descricao);
            HBox.setHgrow(textos, Priority.ALWAYS);
            caixa = new HBox(10, marca, numero, textos, selo, vezes);
            caixa.setAlignment(Pos.CENTER_LEFT);

            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !isEmpty()) {
                    tocarSelecionada();
                }
            });
        }

        @Override
        protected void updateItem(Faixa faixa, boolean vazia) {
            super.updateItem(faixa, vazia);
            getStyleClass().remove("tocando");
            if (vazia || faixa == null) {
                setGraphic(null);
                return;
            }
            numero.setText(String.format("%02d", getIndex() + 1));
            selo.setText(faixa.getTipo().toUpperCase());
            nome.setText(faixa.getTitulo());
            descricao.setText(faixa.getDescricao());
            int n = faixa.getVezesTocadas();
            vezes.setText(n == 0 ? "" : n + "x");

            boolean tocandoAgora = player.getFaixaAtual() == faixa;
            marca.setVisible(tocandoAgora);
            if (tocandoAgora) {
                getStyleClass().add("tocando");
            }
            setGraphic(caixa);
        }
    }
}
