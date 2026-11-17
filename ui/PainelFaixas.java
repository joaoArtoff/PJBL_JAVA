package ui;

import Excecoes.PlayerException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import Modelo.Faixa;
import Modelo.Playlist;
import Reproducao.PlayerModos;

import java.util.Optional;

/**
 * Faixas da playlist selecionada, dentro da tela do aparelho. Duplo clique (ou Enter) toca.
 *
 * Cada linha mostra título, descrição e tipo chamando só os métodos de Faixa
 * (getTitulo, getDescricao, getTipo). A mesma célula serve para música e para podcast,
 * porque cada subclasse responde do seu jeito (polimorfismo).
 */
public class PainelFaixas extends VBox {

    private final PlayerModos player;
    private final ReadOnlyObjectProperty<Playlist> playlistSelecionada;

    private final ListView<Faixa> lista = new ListView<>();
    private final Label placeholder = new Label();

    public PainelFaixas(PlayerModos player, ReadOnlyObjectProperty<Playlist> playlistSelecionada) {
        this.player = player;
        this.playlistSelecionada = playlistSelecionada;

        getStyleClass().add("painel-lista");
        lista.setId("listaFaixas");
        lista.getStyleClass().add("lista-visor");
        lista.setCellFactory(l -> new CelulaFaixa());
        placeholder.getStyleClass().add("texto-vazio");
        placeholder.setTextAlignment(TextAlignment.CENTER);
        lista.setPlaceholder(placeholder);
        lista.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tocarSelecionada();
            }
        });
        VBox.setVgrow(lista, Priority.ALWAYS);
        getChildren().add(lista);

        playlistSelecionada.addListener((o, antes, agora) -> recarregar());
        recarregar();
    }

    /** Verdadeiro enquanto nenhuma playlist está escolhida (desliga o botão Adicionar). */
    public BooleanBinding semPlaylist() {
        return playlistSelecionada.isNull();
    }

    /** Verdadeiro enquanto nenhuma faixa está escolhida (desliga o botão Remover). */
    public BooleanBinding semSelecao() {
        return lista.getSelectionModel().selectedItemProperty().isNull();
    }

    /** Redesenha as linhas (vezes tocada, "Parou em", marca de "tocando") sem mexer na seleção. */
    public void atualizar() {
        lista.refresh();
    }

    /**
     * Usada pelo botão central quando nada está tocando: toca a faixa escolhida. Sem faixa escolhida,
     * quem decide a primeira é o modo de reprodução (a 1ª da lista, ou uma sorteada no aleatório).
     */
    public void tocarSelecionadaOuPrimeira() {
        Playlist playlist = playlistSelecionada.get();
        if (playlist == null) {
            Alertas.aviso("Escolha uma playlist", "Selecione uma playlist na tela para tocar.");
            return;
        }
        int indice = lista.getSelectionModel().getSelectedIndex();
        try {
            if (indice >= 0) {
                player.tocar(playlist, indice);
            } else {
                player.tocarPlaylist(playlist);
            }
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
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
            placeholder.setText("Escolha uma playlist\npara ver as faixas.");
            lista.getItems().clear();
            return;
        }
        placeholder.setText("Playlist vazia.\nUse Adicionar faixa.");
        lista.getItems().setAll(playlist.getFaixas());   // a Playlist devolve uma cópia
    }

    public void adicionar() {
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
            player.playlistMudou();
        } catch (PlayerException e) {
            Alertas.erro(e);
        }
    }

    public void remover() {
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
        player.playlistMudou();
    }

    /** Uma linha da lista: título, descrição, tipo e um ícone se é a faixa em reprodução. */
    private class CelulaFaixa extends ListCell<Faixa> {
        private final Label nome = new Label();
        private final Label descricao = new Label();
        private final Label tipo = new Label();
        private final SVGPath marca = Icones.icone(Icones.PLAY, "marcador-tocando");
        private final HBox caixa;

        CelulaFaixa() {
            nome.getStyleClass().add("item-titulo");
            descricao.getStyleClass().add("item-detalhe");
            tipo.getStyleClass().add("item-detalhe");
            nome.setMinWidth(0);
            descricao.setMinWidth(0);

            VBox textos = new VBox(1, nome, descricao);
            textos.setMinWidth(0);
            HBox.setHgrow(textos, Priority.ALWAYS);
            tipo.setMinWidth(Region.USE_PREF_SIZE);   // o título encolhe (com "…"), o tipo não
            caixa = new HBox(8, textos, tipo, Celulas.reservaMarcador(marca));
            caixa.setAlignment(Pos.CENTER_LEFT);
            Celulas.ajustarLargura(this, caixa);

            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !isEmpty()) {
                    tocarSelecionada();
                }
            });
        }

        @Override
        protected void updateItem(Faixa faixa, boolean vazia) {
            super.updateItem(faixa, vazia);
            if (vazia || faixa == null) {
                setGraphic(null);
                return;
            }
            nome.setText(faixa.getTitulo());
            descricao.setText(faixa.getDescricao());
            tipo.setText(faixa.getTipo());

            boolean tocandoAgora = player.getFaixaAtual() == faixa;
            marca.setVisible(tocandoAgora);
            Icones.trocar(marca, tocandoAgora && !player.isTocando() ? Icones.PAUSA : Icones.PLAY);
            setGraphic(caixa);
        }
    }
}
