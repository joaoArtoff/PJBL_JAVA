import excecoes.PlayerException;
import modelo.Biblioteca;
import modelo.Faixa;
import modelo.ImportadorDePasta;
import modelo.Playlist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Escolher uma pasta cria uma playlist com o nome da pasta e um .mp3 dela em cada faixa. */
class ImportadorDePastaTest {

    @TempDir
    Path raiz;

    private Path pasta(String nome) throws IOException {
        return Files.createDirectory(raiz.resolve(nome));
    }

    private void arquivo(Path pasta, String nome) throws IOException {
        Files.createFile(pasta.resolve(nome));
    }

    @Test
    void criaPlaylistComONomeDaPastaEUmaFaixaPorMp3() throws Exception {
        Path rock = pasta("Rock Anos 80");
        arquivo(rock, "Africa.mp3");
        arquivo(rock, "Beat It.mp3");
        Biblioteca biblioteca = new Biblioteca();

        Playlist playlist = new ImportadorDePasta().importar(biblioteca, rock.toFile());

        assertEquals("Rock Anos 80", playlist.getNome());
        assertEquals(2, playlist.getTamanho());
        assertEquals(playlist, biblioteca.buscarPorNome("Rock Anos 80"));
    }

    @Test
    void usaONomeDoArquivoSemExtensaoComoTituloEANomeDaPastaComoAlbum() throws Exception {
        Path rock = pasta("Rock");
        arquivo(rock, "Africa.mp3");

        Faixa faixa = new ImportadorDePasta().importar(new Biblioteca(), rock.toFile()).getFaixa(0);

        assertEquals("Africa", faixa.getTitulo());
        assertEquals("Artista desconhecido - Rock", faixa.getDescricao());
        assertEquals(rock.resolve("Africa.mp3").toString(), faixa.getCaminhoArquivo());
    }

    @Test
    void ordenaPorNomeSemDiferenciarMaiusculas() throws Exception {
        Path pasta = pasta("Mistura");
        arquivo(pasta, "b.mp3");
        arquivo(pasta, "C.mp3");
        arquivo(pasta, "A.mp3");

        List<Faixa> faixas = new ImportadorDePasta().importar(new Biblioteca(), pasta.toFile()).getFaixas();

        assertEquals(List.of("A", "b", "C"), faixas.stream().map(Faixa::getTitulo).toList());
    }

    @Test
    void ignoraOutrosFormatosSubpastasEArquivosOcultos() throws Exception {
        Path pasta = pasta("Misto");
        arquivo(pasta, "musica.mp3");
        arquivo(pasta, "CAPA.MP3");
        arquivo(pasta, "capa.jpg");
        arquivo(pasta, "letra.txt");
        arquivo(pasta, "._musica.mp3");   // resto que o macOS deixa em pen drives
        Path sub = Files.createDirectory(pasta.resolve("outra"));
        arquivo(sub, "dentro.mp3");

        Playlist playlist = new ImportadorDePasta().importar(new Biblioteca(), pasta.toFile());

        assertEquals(List.of("CAPA", "musica"), playlist.getFaixas().stream().map(Faixa::getTitulo).toList());
    }

    @Test
    void pastaSemMp3NaoCriaPlaylist() throws Exception {
        Path vazia = pasta("Vazia");
        arquivo(vazia, "foto.jpg");
        Biblioteca biblioteca = new Biblioteca();

        assertThrows(PlayerException.class, () -> new ImportadorDePasta().importar(biblioteca, vazia.toFile()));

        assertTrue(biblioteca.isVazia());
    }

    @Test
    void nomeRepetidoRecusaEMantemAPlaylistQueJaExistia() throws Exception {
        Path rock = pasta("Rock");
        arquivo(rock, "Africa.mp3");
        Biblioteca biblioteca = new Biblioteca();
        biblioteca.criarPlaylist("rock");

        assertThrows(PlayerException.class, () -> new ImportadorDePasta().importar(biblioteca, rock.toFile()));

        assertEquals(1, biblioteca.getPlaylists().size());
        assertTrue(biblioteca.buscarPorNome("Rock").isVazia(), "a playlist antiga não pode ganhar faixas");
    }

    @Test
    void caminhoQueNaoEUmaPastaDaErro() throws Exception {
        Path pasta = pasta("Musicas");
        arquivo(pasta, "a.mp3");
        File arquivoSolto = pasta.resolve("a.mp3").toFile();

        assertThrows(PlayerException.class, () -> new ImportadorDePasta().importar(new Biblioteca(), arquivoSolto));
        assertThrows(PlayerException.class, () -> new ImportadorDePasta().importar(new Biblioteca(), null));
    }
}
