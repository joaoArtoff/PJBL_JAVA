package ui;

import excecoes.PlayerException;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.net.URL;
import java.util.Optional;

/**
 * Janelinhas de aviso, erro, confirmação e pergunta, todas com o tema do app e em português.
 * É o único lugar que transforma uma exceção em algo que o usuário lê.
 */
public final class Alertas {

    private Alertas() { }

    /** Mostra qualquer erro. PlayerException já traz uma mensagem pensada para o usuário. */
    public static void erro(Throwable e) {
        String mensagem = (e instanceof PlayerException)
                ? e.getMessage()
                : "Algo inesperado aconteceu: " + e.getMessage();
        Alert alerta = new Alert(Alert.AlertType.NONE, mensagem, botao("Entendi", ButtonData.OK_DONE));
        alerta.setTitle("Atenção");
        alerta.setHeaderText("Não foi possível continuar");
        estilizar(alerta);
        alerta.getDialogPane().getStyleClass().add("dialogo-erro");
        alerta.showAndWait();
    }

    public static void aviso(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.NONE, mensagem, botao("Ok", ButtonData.OK_DONE));
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        estilizar(alerta);
        alerta.showAndWait();
    }

    public static boolean confirmar(String titulo, String mensagem) {
        ButtonType sim = botao("Sim", ButtonData.YES);
        ButtonType nao = botao("Não", ButtonData.NO);
        Alert alerta = new Alert(Alert.AlertType.NONE, mensagem, sim, nao);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        estilizar(alerta);
        return alerta.showAndWait().filter(b -> b == sim).isPresent();
    }

    /** Pergunta um texto curto (ex.: nome de uma playlist). Vazio se o usuário cancelar. */
    public static Optional<String> pedirTexto(String titulo, String rotulo, String botaoOk) {
        ButtonType ok = botao(botaoOk, ButtonData.OK_DONE);
        ButtonType cancelar = botao("Cancelar", ButtonData.CANCEL_CLOSE);

        TextField campo = new TextField();
        VBox conteudo = new VBox(8, new Label(rotulo), campo);
        conteudo.setPadding(new Insets(16, 20, 8, 20));
        conteudo.setMinWidth(340);

        Dialog<String> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(titulo);
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().setAll(ok, cancelar);
        dialogo.setResultConverter(b -> b == ok ? campo.getText() : null);
        estilizar(dialogo);
        dialogo.setOnShown(e -> campo.requestFocus());
        return dialogo.showAndWait();
    }

    /** Aplica o tema e liga a janelinha à janela que está em uso. */
    static void estilizar(Dialog<?> dialogo) {
        URL css = Alertas.class.getResource("/estilo.css");
        if (css != null) {
            dialogo.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        dialogo.getDialogPane().getStyleClass().add("dialogo");
        Window dona = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        if (dona != null) {
            dialogo.initOwner(dona);
        }
    }

    private static ButtonType botao(String texto, ButtonData tipo) {
        return new ButtonType(texto, tipo);
    }
}
