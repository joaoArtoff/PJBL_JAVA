package ui;

import audio.ReprodutorMp3;
import excecoes.PersistenciaException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Biblioteca;
import modelo.Historico;
import modelo.Player;
import modelo.RepositorioBiblioteca;
import modelo.RepositorioHistorico;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Janela principal. Aqui só se montam as peças e se ligam umas às outras:
 *
 *   Biblioteca ── Playlist ── Faixa      (o modelo, sem nada de interface)
 *   Player ── Reprodutor                 (quem toca; o Player só conhece a interface)
 *
 * O aparelho tem três regiões: a tampa do CD com o volume (PainelDisco), a tela LCD
 * (Visor, que reúne PainelPlaylists, PainelFaixas e VisorAgora) e o click wheel com as
 * teclas de organização (RodaDeClique e PainelTeclas).
 *
 * Para rodar: ./mvnw javafx:run
 */
public class AppPlayer extends Application {

    /** Onde o programa guarda o que precisa lembrar entre uma execução e outra (a pasta fica fora do git). */
    private static final String ARQUIVO_HISTORICO = "dados/historico.txt";
    private static final String ARQUIVO_BIBLIOTECA = "dados/biblioteca.dat";

    private Player player;
    private Biblioteca biblioteca;
    private boolean salvarAoFechar = true;

    @Override
    public void start(Stage palco) {
        List<PersistenciaException> falhasAoAbrir = new ArrayList<>();

        try {
            biblioteca = carregarBiblioteca();
        } catch (PersistenciaException e) {
            // Não salva ao fechar: senão a biblioteca vazia sobrescreveria o arquivo que não conseguimos ler.
            biblioteca = new Biblioteca();
            salvarAoFechar = false;
            falhasAoAbrir.add(e);
        }

        RepositorioHistorico repositorio = new RepositorioHistorico(ARQUIVO_HISTORICO);
        Historico historico;
        try {
            historico = repositorio.carregar();
        } catch (PersistenciaException e) {
            historico = new Historico();
            falhasAoAbrir.add(e);
        }

        player = new Player(new ReprodutorMp3(), historico, repositorio);

        PainelPlaylists playlists = new PainelPlaylists(biblioteca, player);
        PainelFaixas faixas = new PainelFaixas(player, playlists.selecionadaProperty());
        VisorAgora agora = new VisorAgora(player);
        Visor visor = new Visor(player, playlists, faixas, agora);
        PainelDisco disco = new PainelDisco(player);
        RodaDeClique roda = new RodaDeClique(player, faixas::tocarSelecionadaOuPrimeira);
        PainelTeclas teclas = new PainelTeclas(playlists, faixas);

        // O Player avisa a tela sempre que algo muda; a tela nunca adivinha o estado sozinha.
        player.setAoMudar(() -> {
            visor.atualizar();
            agora.atualizar();
            disco.atualizar();
            roda.atualizar();
            faixas.atualizar();
            playlists.atualizar();
        });
        player.setAoErro(Alertas::erro);

        VBox colunaDireita = new VBox(22, roda, teclas);
        colunaDireita.setAlignment(Pos.CENTER);
        colunaDireita.setPrefWidth(240);
        colunaDireita.setMinWidth(240);
        disco.setPrefWidth(240);
        disco.setMinWidth(240);

        BorderPane aparelho = new BorderPane();
        aparelho.getStyleClass().add("aparelho");
        aparelho.setTop(cabecalhoGravado());
        aparelho.setLeft(disco);
        aparelho.setCenter(visor);
        aparelho.setRight(colunaDireita);
        BorderPane.setMargin(disco, new Insets(0, 20, 0, 0));
        BorderPane.setMargin(colunaDireita, new Insets(0, 0, 0, 20));
        BorderPane.setMargin(visor, new Insets(0));

        StackPane mesa = new StackPane(aparelho);
        mesa.getStyleClass().add("mesa");
        StackPane.setMargin(aparelho, new Insets(22));

        Scene cena = new Scene(mesa, 1120, 700);
        URL css = getClass().getResource("/estilo.css");
        if (css != null) {
            cena.getStylesheets().add(css.toExternalForm());
        }

        palco.setTitle("PJBL Player");
        palco.setMinWidth(1040);
        palco.setMinHeight(660);
        palco.setScene(cena);
        palco.show();

        if (!falhasAoAbrir.isEmpty()) {
            Platform.runLater(() -> falhasAoAbrir.forEach(Alertas::erro));
        }
    }

    /** Linha gravada no topo do corpo do aparelho (nome e selo impressos). */
    private static HBox cabecalhoGravado() {
        Label marca = new Label("PJBL");
        marca.getStyleClass().add("gravado-marca");
        Label modelo = new Label("DISC PLAYER");
        modelo.getStyleClass().add("gravado-modelo");
        Label selo = new Label("MP3 DIGITAL AUDIO");
        selo.getStyleClass().add("selo-impresso");
        Region folga = new Region();
        HBox.setHgrow(folga, Priority.ALWAYS);

        HBox linha = new HBox(10, marca, modelo, folga, selo);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(0, 4, 14, 4));
        return linha;
    }

    @Override
    public void stop() {
        if (player != null) {
            player.parar();   // guarda onde o podcast parou
        }
        if (salvarAoFechar) {
            try {
                salvarBiblioteca(biblioteca);
            } catch (PersistenciaException e) {
                System.err.println(e.getMessage());   // a janela já fechou, não há onde mostrar um alerta
            }
        }
    }

    /** Lê as playlists gravadas na última vez. Na primeira execução (sem arquivo), a biblioteca começa vazia. */
    protected Biblioteca carregarBiblioteca() throws PersistenciaException {
        return new RepositorioBiblioteca(ARQUIVO_BIBLIOTECA).carregar();
    }

    /** Grava as playlists ao fechar a janela. */
    protected void salvarBiblioteca(Biblioteca biblioteca) throws PersistenciaException {
        new RepositorioBiblioteca(ARQUIVO_BIBLIOTECA).salvar(biblioteca);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
