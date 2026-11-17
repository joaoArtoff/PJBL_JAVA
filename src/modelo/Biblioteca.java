package modelo;

import excecoes.PlaylistInvalidaException;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Biblioteca = o conjunto de TODAS as playlists do usuário.
 *
 * Ela é o "objeto raiz" do sistema: para salvar tudo em arquivo, basta
 * salvar a Biblioteca, porque ela contém as Playlists, que contêm as Faixas.
 * Biblioteca CRIA as playlists dentro dela (criarPlaylist). Elas só
 * existem dentro da biblioteca — diferente da relação Playlist -> Faixa, em que as faixas são criadas fora.
 */
public class Biblioteca implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Playlist> playlists;

    public Biblioteca() {
        this.playlists = new ArrayList<>();
    }

    /**
     * Cria uma nova playlist, garantindo que não haja duas com o mesmo nome.
     * Quem cria a Playlist é a Biblioteca (ela controla suas partes).
     */
    public Playlist criarPlaylist(String nome) throws PlaylistInvalidaException {
        if (buscarPorNome(nome) != null ){
            throw new PlaylistInvalidaException("Já existe uma playlist chamada \"" + nome.trim() + "\".");
        }
        Playlist nova = new Playlist(nome);
        playlists.add(nova);
        return nova;
    }

    public boolean removerPlaylist(Playlist playlist) { return playlists.remove(playlist); }

    // Busca ignorando maiúsculas/minúsculas. Retorna null se não achar.
    public Playlist buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        // percorrendo a coleção com for
        for (Playlist p : playlists) {
            if(p.getNome().equalsIgnoreCase(nome.trim())) {
                return p;
            }
        }
        return null;
    }

    //Cópia da lista, pelo mesmo motivo explicado na Playlist.
    public ArrayList<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    public boolean isVazia() {
        return playlists.isEmpty();
    }
}
