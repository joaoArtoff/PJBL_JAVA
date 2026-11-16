package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Histórico de tudo que já foi ouvido, na memória.
 */
public class Historico {

    private ArrayList<RegistroHistorico> registros;

    public Historico() {
        this.registros = new ArrayList<>();
    }

    public Historico(List<RegistroHistorico> iniciais) {
        this.registros = new ArrayList<>(iniciais);
    }

    public RegistroHistorico registrar(Faixa faixa) {
        RegistroHistorico r = new RegistroHistorico(
                LocalDateTime.now(), faixa.getTipo(), faixa.getTitulo(), faixa.getDescricao());
        registros.add(r);
        return r;
    }

    /** As últimas reproduções da mais recente para a mais antiga. */
    public ArrayList<RegistroHistorico> getUltimas(int n) {
        ArrayList<RegistroHistorico> resultado = new ArrayList<>();
        for (int i = registros.size() - 1; i >= 0 && resultado.size() < n; i--) {
            resultado.add(registros.get(i));
        }
        return resultado;
    }

    public ArrayList<RegistroHistorico> getRegistros() {
        return new ArrayList<>(registros);
    }

    public int getTotal() {
        return registros.size();
    }

    public boolean isVazio() {
        return registros.isEmpty();
    }

    /** Quantas vezes uma faixa pelo título aparece no histórico. */
    public int contar(String titulo) {
        int vezes = 0;
        for (RegistroHistorico r : registros) {
            if (r.getTitulo().equalsIgnoreCase(titulo.trim())) {
                vezes++;
            }
        }
        return vezes;
    }

    public void limpar() {
        registros.clear();
    }
}
