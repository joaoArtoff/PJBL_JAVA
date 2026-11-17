package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import Modelo.Player;
import Modelo.Playlist;

/**
 * A tela LCD do aparelho. Em cima, uma barra de título (estado do player, nome da playlist
 * e posição da faixa); no meio, as playlists e as faixas lado a lado; embaixo, o que está tocando.
 * Por cima de tudo há só um reflexo de vidro, que não recebe cliques.
 */
public class Visor extends StackPane {

    private final Player player;
    private final Label titulo = new Label("Playlists");
    private final Label posicao = new Label();
    private final StackPane slotEstado = new StackPane();
    private final SVGPath iconeEstado = Icones.icone(Icones.PARAR, "icone-estado");

    public Visor(Player player, PainelPlaylists playlists, PainelFaixas faixas, VisorAgora agora) {
        this.player = player;

        titulo.getStyleClass().add("visor-titulo");
        posicao.getStyleClass().add("visor-posicao");
        slotEstado.getChildren().add(iconeEstado);
        slotEstado.setMinWidth(40);
        slotEstado.setAlignment(Pos.CENTER_LEFT);
        posicao.setMinWidth(40);
        posicao.setAlignment(Pos.CENTER_RIGHT);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);
        HBox.setHgrow(titulo, Priority.ALWAYS);

        HBox cabecalho = new HBox(slotEstado, titulo, posicao);
        cabecalho.getStyleClass().add("visor-cabecalho");
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(0, 12, 0, 12));

        Region divisoria = new Region();
        divisoria.getStyleClass().add("visor-divisoria");
        divisoria.setMinWidth(1);
        divisoria.setPrefWidth(1);
        divisoria.setMaxWidth(1);

        playlists.setPrefWidth(136);
        playlists.setMinWidth(110);
        playlists.setMaxWidth(190);
        HBox.setHgrow(faixas, Priority.ALWAYS);
        HBox corpo = new HBox(playlists, divisoria, faixas);
        VBox.setVgrow(corpo, Priority.ALWAYS);

        VBox tela = new VBox(cabecalho, corpo, agora);
        tela.getStyleClass().add("visor-tela");

        Region vidro = new Region();
        vidro.getStyleClass().add("visor-vidro");
        vidro.setMouseTransparent(true);

        getChildren().addAll(tela, vidro);
        getStyleClass().add("visor");

        playlists.selecionadaProperty().addListener((o, antes, agoraSel) -> atualizarTitulo(agoraSel));
        atualizarTitulo(playlists.selecionadaProperty().get());
        atualizar();
    }

    private void atualizarTitulo(Playlist selecionada) {
        titulo.setText(selecionada == null ? "Playlists" : selecionada.getNome());
    }

    /** Chamado pelo Player sempre que o estado muda. */
    public void atualizar() {
        boolean tem = player.getFaixaAtual() != null;
        if (!tem) {
            Icones.trocar(iconeEstado, Icones.PARAR);
            posicao.setText("");
        } else {
            Icones.trocar(iconeEstado, player.isTocando() ? Icones.PLAY : Icones.PAUSA);
            posicao.setText((player.getIndiceAtual() + 1) + "/" + player.getPlaylistAtual().getTamanho());
        }
    }
}
