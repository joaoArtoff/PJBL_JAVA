import excecoes.PersistenciaException;
import modelo.Biblioteca;
import modelo.EpisodioPodcast;
import modelo.Faixa;
import modelo.Musica;
import modelo.Playlist;
import modelo.RepositorioBiblioteca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A biblioteca gravada e lida de volta tem que ser a mesma: playlists, faixas e onde o podcast parou. */
class RepositorioBibliotecaTest {

    @TempDir
    Path pasta;

    private String mp3(String nome) throws IOException {
        File f = pasta.resolve(nome).toFile();
        f.createNewFile();
        return f.getAbsolutePath();
    }

    @Test
    void arquivoQueAindaNaoExisteDevolveBibliotecaVazia() throws Exception {
        Biblioteca lida = new RepositorioBiblioteca(pasta.resolve("nao_existe.dat").toString()).carregar();

        assertTrue(lida.isVazia());
    }

    @Test
    void gravarELerDevolveAsMesmasPlaylistsEFaixas() throws Exception {
        Biblioteca original = new Biblioteca();
        Playlist favoritas = original.criarPlaylist("Favoritas");
        favoritas.adicionarFaixa(new Musica("Space Oddity", mp3("a.mp3"), "David Bowie", "Space Oddity"));
        EpisodioPodcast episodio = new EpisodioPodcast("Coleções", mp3("b.mp3"), "Podcast Java", 3);
        episodio.registrarPosicao(125);
        favoritas.adicionarFaixa(episodio);
        original.criarPlaylist("Vazia");

        RepositorioBiblioteca repositorio = new RepositorioBiblioteca(pasta.resolve("dados/biblioteca.dat").toString());
        repositorio.salvar(original);
        Biblioteca lida = repositorio.carregar();

        assertEquals(2, lida.getPlaylists().size());
        Playlist favoritasLida = lida.buscarPorNome("favoritas");
        List<Faixa> faixas = favoritasLida.getFaixas();
        assertEquals(2, faixas.size());
        assertEquals("Space Oddity", faixas.get(0).getTitulo());
        assertEquals("David Bowie - Space Oddity", faixas.get(0).getDescricao());
        assertEquals("Coleções", faixas.get(1).getTitulo());
        assertEquals(125, faixas.get(1).getPosicaoInicial(), "o podcast lembra onde parou");
        assertTrue(lida.buscarPorNome("Vazia").isVazia());
    }

    @Test
    void salvarCriaAPastaQueAindaNaoExiste() throws Exception {
        Path destino = pasta.resolve("nova/pasta/biblioteca.dat");

        new RepositorioBiblioteca(destino.toString()).salvar(new Biblioteca());

        assertTrue(Files.exists(destino));
    }

    @Test
    void salvarNaoDeixaArquivoTemporarioParaTras() throws Exception {
        Path destino = pasta.resolve("biblioteca.dat");

        new RepositorioBiblioteca(destino.toString()).salvar(new Biblioteca());

        assertFalse(Files.exists(pasta.resolve("biblioteca.dat.tmp")));
    }

    @Test
    void salvarDeNovoSubstituiOConteudoAnterior() throws Exception {
        RepositorioBiblioteca repositorio = new RepositorioBiblioteca(pasta.resolve("biblioteca.dat").toString());
        Biblioteca primeira = new Biblioteca();
        primeira.criarPlaylist("Antiga");
        repositorio.salvar(primeira);

        Biblioteca segunda = new Biblioteca();
        segunda.criarPlaylist("Nova");
        repositorio.salvar(segunda);

        Biblioteca lida = repositorio.carregar();
        assertEquals(1, lida.getPlaylists().size());
        assertEquals("Nova", lida.getPlaylists().get(0).getNome());
    }

    @Test
    void arquivoCorrompidoDaPersistenciaException() throws Exception {
        Path lixo = pasta.resolve("biblioteca.dat");
        Files.writeString(lixo, "isto não é uma biblioteca serializada");

        assertThrows(PersistenciaException.class, () -> new RepositorioBiblioteca(lixo.toString()).carregar());
    }
}
