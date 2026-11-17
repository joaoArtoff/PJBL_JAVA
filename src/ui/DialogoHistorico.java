package ui;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import modelo.Historico;
import modelo.RegistroHistorico;

/**
 * Janelinha só para ler: mostra as últimas reproduções, da mais recente para a mais antiga.
 * Não altera nada; os dados vêm do Historico que o Player já mantém.
 */
public class DialogoHistorico {

    private static final int QUANTAS = 50;

    public static void mostrar(Historico historico) {
        ListView<RegistroHistorico> lista = new ListView<>();
        lista.getStyleClass().add("lista-visor");
        lista.setId("listaHistorico");
        lista.setPrefSize(460, 340);
        lista.setCellFactory(l -> new CelulaRegistro());
        lista.getItems().setAll(historico.getUltimas(QUANTAS));
        Label vazio = new Label("Nada foi tocado ainda.");
        vazio.getStyleClass().add("texto-vazio");
        lista.setPlaceholder(vazio);

        ButtonType fechar = new ButtonType("Fechar", ButtonData.OK_DONE);
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Histórico");
        dialogo.setHeaderText("Histórico (" + historico.getTotal() + " reproduções, últimas " + QUANTAS + ")");
        dialogo.getDialogPane().setContent(lista);
        dialogo.getDialogPane().getButtonTypes().setAll(fechar);
        Alertas.estilizar(dialogo);
        dialogo.showAndWait();
    }

    /** Uma linha: título em cima, tipo, descrição e data embaixo. */
    private static class CelulaRegistro extends ListCell<RegistroHistorico> {
        @Override
        protected void updateItem(RegistroHistorico r, boolean vazia) {
            super.updateItem(r, vazia);
            if (vazia || r == null) {
                setGraphic(null);
                return;
            }
            Label titulo = new Label(r.getTitulo());
            titulo.getStyleClass().add("item-titulo");
            Label detalhe = new Label(r.getTipo() + " · " + r.getDescricao() + " · " + r.getDataFormatada());
            detalhe.getStyleClass().add("item-detalhe");
            setGraphic(new VBox(2, titulo, detalhe));
        }
    }
}
