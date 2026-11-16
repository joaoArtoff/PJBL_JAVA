package Modelo;

import Excecoes.FaixaInvalidaException;
import Excecoes.PlaylistInvalidaException;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private ArrayList<Faixa>  faixas;

    //Usa o setNome para verificar o nome, para assim progredir no código.
    public Playlist(String nome)  throws PlaylistInvalidaException {
        setNome(nome);
        faixas = new ArrayList<>();
    }

    /**O parâmetro é do tipo Faixa (pai): aceita Música ou EpisodioPodcast.
     * Retorna true se adicionou ou false se a faixa já estava na playlist.
     */
    public boolean adicionarFaixa(Faixa faixa) throws PlaylistInvalidaException {
        if (faixa == null) {
            throw new PlaylistInvalidaException("Falha ao adicionar faixa.");
        }
        if (faixas.contains(faixa)) {
            //regra de negócio: sem duplicatas
            return false;
        }
        return faixas.add(faixa);
    }

    //Remove a faixa
    public boolean removerFaixa(Faixa faixa) { return faixas.remove(faixa); }

    //Devolve a faixa da posição informada, começando em 0.
    public Faixa getFaixa(int indice) { return faixas.get(indice); }

    //Posição da faixa na lista
    public int indexOf(Faixa faixa) { return faixas.indexOf(faixa); }

    public int getTamanho() {
        return faixas.size();
    }

    public boolean isVazia() {
        return faixas.isEmpty();
    }

    // CÓPIA da lista — protege a lista interna.
    public ArrayList<Faixa> getFaixas() {
        return new ArrayList<>(faixas);
    }

    public String getNome() {
        return nome;
    }

    //Set o nome caso ele estiver nas condições necessárias, verificando se ele está vazio.
    public void setNome(String nome) throws PlaylistInvalidaException {
        if( nome == null || nome.isBlank() ) {
            throw new PlaylistInvalidaException("O nome da Playlist não pode estar vazio");
        }
        this.nome = nome.trim();
    }

    //Mostrado na lista de playlists da interface.
    @Override
    public String toString() {
        return nome + " (" + faixas.size() + ")";
    }
}
