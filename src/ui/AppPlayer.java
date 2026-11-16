package ui;

import audio.ReprodutorMp3;
import excecoes.PersistenciaException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Biblioteca;
import modelo.Historico;
import modelo.Player;
import modelo.RepositorioHistorico;

import java.net.URL;

/**
 * Janela principal. Aqui só se montam as peças e se ligam umas às outras:
 *
 *   Biblioteca ── Playlist ── Faixa      (o modelo, sem nada de interface)
 *   Player ── Reprodutor                 (quem toca; o Player só conhece a interface)
 *   PainelPlaylists, PainelFaixas, PainelPlayer   (as três partes da tela)
 *
 * Para rodar: ./mvnw javafx:run
 */
public class AppPlayer extends Application {

    private Player player;
    private Biblioteca biblioteca;

    @Override
    public void start(Stage palco) {
        biblioteca = carregarBiblioteca();

        RepositorioHistorico repositorio = new RepositorioHistorico("dados/historico.txt");
        Historico historico;
        PersistenciaException falhaAoAbrir = null;
        try {
            historico = repositorio.carregar();
        } catch (PersistenciaException e) {
            historico = new Historico();
            falhaAoAbrir = e;
        }

        player = new Player(new ReprodutorMp3(), historico, repositorio);

        PainelPlaylists painelPlaylists = new PainelPlaylists(biblioteca, player);
        PainelFaixas painelFaixas = new PainelFaixas(player, painelPlaylists.selecionadaProperty(), painelPlaylists::atualizar);
        PainelPlayer painelPlayer = new PainelPlayer(player, painelFaixas::tocarSelecionadaOuPrimeira);

        // O Player avisa a tela sempre que algo muda; a tela nunca adivinha o estado sozinha.
        player.setAoMudar(() -> {
            painelPlayer.atualizar();
            painelFaixas.atualizar();
            painelPlaylists.atualizar();
        });
        player.setAoErro(Alertas::erro);

        VBox.setVgrow(painelFaixas, Priority.ALWAYS);
        VBox centro = new VBox(16, painelPlayer, painelFaixas);

        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("raiz");
        raiz.setLeft(painelPlaylists);
        raiz.setCenter(centro);
        BorderPane.setMargin(painelPlaylists, new Insets(0, 16, 0, 0));
        raiz.setPadding(new Insets(18));

        Scene cena = new Scene(raiz, 980, 660);
        URL css = getClass().getResource("/estilo.css");
        if (css != null) {
            cena.getStylesheets().add(css.toExternalForm());
        }

        palco.setTitle("PJBL Player");
        palco.setMinWidth(860);
        palco.setMinHeight(600);
        palco.setScene(cena);
        palco.show();

        if (falhaAoAbrir != null) {
            PersistenciaException falha = falhaAoAbrir;
            Platform.runLater(() -> Alertas.erro(falha));
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.parar();   // guarda onde o podcast parou
        }
        salvarBiblioteca(biblioteca);
    }

    /**
     * Ponto de ligação com a persistência das playlists (feita à parte, por serialização).
     * Enquanto ela não estiver ligada, o app começa com a biblioteca vazia.
     */
    protected Biblioteca carregarBiblioteca() {
        return new Biblioteca();
    }

    /** Par do método acima: chamado ao fechar a janela. */
    protected void salvarBiblioteca(Biblioteca biblioteca) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
