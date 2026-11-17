package ui;

import Excecoes.FaixaInvalidaException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import Modelo.EpisodioPodcast;
import Modelo.Faixa;
import Modelo.Musica;

import java.io.File;
import java.util.Optional;

/**
 * Janelinha para cadastrar uma faixa: escolhe o tipo (música ou podcast), o arquivo .mp3
 * e preenche os dados. Os campos mudam conforme o tipo.
 *
 * Ela não valida nada sozinha: monta a Musica ou o EpisodioPodcast e deixa o modelo recusar
 * dados inválidos (FaixaInvalidaException). A mensagem aparece dentro da própria janelinha,
 * que continua aberta para o usuário corrigir.
 */
public class DialogoFaixa {

    private static File ultimaPasta;

    private final Dialog<Faixa> dialogo = new Dialog<>();
    private final RadioButton rbMusica = new RadioButton("Música");
    private final RadioButton rbPodcast = new RadioButton("Podcast");
    private final TextField campoArquivo = new TextField();
    private final TextField campoTitulo = new TextField();
    private final TextField campoArtista = new TextField();
    private final TextField campoAlbum = new TextField();
    private final TextField campoPrograma = new TextField();
    private final TextField campoEpisodio = new TextField("1");
    private final Label mensagemErro = new Label();

    private Faixa resultado;

    public static Optional<Faixa> mostrar() {
        return new DialogoFaixa().dialogo.showAndWait();
    }

    private DialogoFaixa() {
        ToggleGroup tipo = new ToggleGroup();
        rbMusica.setToggleGroup(tipo);
        rbPodcast.setToggleGroup(tipo);
        rbMusica.setSelected(true);

        campoArquivo.setEditable(false);
        campoArquivo.setPromptText("Nenhum arquivo escolhido");
        Button escolher = new Button("Escolher .mp3…");
        escolher.getStyleClass().add("tecla-prata");
        escolher.setOnAction(e -> escolherArquivo());
        HBox linhaArquivo = new HBox(8, campoArquivo, escolher);
        HBox.setHgrow(campoArquivo, Priority.ALWAYS);

        campoTitulo.setPromptText("Como a faixa vai aparecer na lista");
        campoArtista.setPromptText("Opcional");
        campoAlbum.setPromptText("Opcional");
        campoPrograma.setPromptText("Opcional");

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(16, 20, 8, 20));
        grade.setMinWidth(460);
        int linha = 0;
        grade.add(rotulo("Tipo"), 0, linha);
        grade.add(new HBox(16, rbMusica, rbPodcast), 1, linha++);
        grade.add(rotulo("Arquivo"), 0, linha);
        grade.add(linhaArquivo, 1, linha++);
        grade.add(rotulo("Título"), 0, linha);
        grade.add(campoTitulo, 1, linha++);

        // Música e podcast usam as mesmas duas linhas; só os campos do tipo marcado aparecem.
        int primeiraLinha = linha++;
        int segundaLinha = linha++;
        Label rotArtista = rotulo("Artista");
        Label rotAlbum = rotulo("Álbum");
        grade.add(rotArtista, 0, primeiraLinha);
        grade.add(campoArtista, 1, primeiraLinha);
        grade.add(rotAlbum, 0, segundaLinha);
        grade.add(campoAlbum, 1, segundaLinha);

        Label rotPrograma = rotulo("Programa");
        Label rotEpisodio = rotulo("Nº do episódio");
        grade.add(rotPrograma, 0, primeiraLinha);
        grade.add(campoPrograma, 1, primeiraLinha);
        grade.add(rotEpisodio, 0, segundaLinha);
        grade.add(campoEpisodio, 1, segundaLinha);

        for (Node n : new Node[]{rotArtista, campoArtista, rotAlbum, campoAlbum}) {
            n.visibleProperty().bind(rbMusica.selectedProperty());
            n.managedProperty().bind(rbMusica.selectedProperty());
        }
        for (Node n : new Node[]{rotPrograma, campoPrograma, rotEpisodio, campoEpisodio}) {
            n.visibleProperty().bind(rbPodcast.selectedProperty());
            n.managedProperty().bind(rbPodcast.selectedProperty());
        }

        mensagemErro.getStyleClass().add("mensagem-erro");
        mensagemErro.setWrapText(true);
        mensagemErro.setMaxWidth(460);
        mensagemErro.managedProperty().bind(mensagemErro.textProperty().isNotEmpty());
        mensagemErro.visibleProperty().bind(mensagemErro.textProperty().isNotEmpty());
        grade.add(mensagemErro, 0, linha, 2, 1);
        for (Node n : new Node[]{campoArquivo, campoTitulo, campoArtista, campoAlbum, campoPrograma, campoEpisodio}) {
            GridPane.setHgrow(n, Priority.ALWAYS);
        }

        ButtonType adicionar = new ButtonType("Adicionar", ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
        dialogo.setTitle("Nova faixa");
        dialogo.setHeaderText("Adicionar faixa à playlist");
        dialogo.getDialogPane().setContent(grade);
        dialogo.getDialogPane().getButtonTypes().setAll(adicionar, cancelar);
        Alertas.estilizar(dialogo);

        // Se o modelo recusar os dados, a janelinha não fecha: mostra o motivo e deixa corrigir.
        Button botaoOk = (Button) dialogo.getDialogPane().lookupButton(adicionar);
        botaoOk.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                resultado = montarFaixa();
                mensagemErro.setText("");
            } catch (FaixaInvalidaException e) {
                mensagemErro.setText(e.getMessage());
                evento.consume();
            }
        });
        dialogo.setResultConverter(b -> b == adicionar ? resultado : null);
    }

    private Faixa montarFaixa() throws FaixaInvalidaException {
        String caminho = campoArquivo.getText();
        String titulo = campoTitulo.getText();
        if (rbMusica.isSelected()) {
            return new Musica(titulo, caminho, campoArtista.getText(), campoAlbum.getText());
        }
        int numero;
        try {
            numero = Integer.parseInt(campoEpisodio.getText().trim());
        } catch (NumberFormatException e) {
            throw new FaixaInvalidaException("O número do episódio precisa ser um número inteiro.");
        }
        return new EpisodioPodcast(titulo, caminho, campoPrograma.getText(), numero);
    }

    private void escolherArquivo() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Escolher arquivo de áudio");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos MP3", "*.mp3", "*.MP3"));
        File inicial = ultimaPasta != null ? ultimaPasta : new File(System.getProperty("user.home"), "Music");
        if (!inicial.isDirectory()) {
            inicial = new File(System.getProperty("user.home"));
        }
        seletor.setInitialDirectory(inicial);

        Window dona = dialogo.getDialogPane().getScene() == null ? null : dialogo.getDialogPane().getScene().getWindow();
        File arquivo = seletor.showOpenDialog(dona);
        if (arquivo == null) {
            return;
        }
        ultimaPasta = arquivo.getParentFile();
        campoArquivo.setText(arquivo.getAbsolutePath());
        if (campoTitulo.getText().isBlank()) {
            String nome = arquivo.getName();
            int ponto = nome.lastIndexOf('.');
            campoTitulo.setText(ponto > 0 ? nome.substring(0, ponto) : nome);
        }
    }

    private static Label rotulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo-campo");
        return l;
    }
}
