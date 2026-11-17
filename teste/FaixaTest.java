import excecoes.FaixaInvalidaException;
import modelo.EpisodioPodcast;
import modelo.Musica;
import modelo.Playlist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regras da Faixa: validação do arquivo e "duas faixas são a mesma se apontam para o mesmo arquivo". */
class FaixaTest {

    @TempDir
    Path pasta;

    private String criar(String nome) throws IOException {
        File f = pasta.resolve(nome).toFile();
        f.createNewFile();
        return f.getAbsolutePath();
    }

    @Test
    void mesmoArquivoComTitulosDiferentesSaoIguais() throws Exception {
        String caminho = criar("a.mp3");

        Musica um = new Musica("Um", caminho, "x", "y");
        Musica outro = new Musica("Outro título", caminho, "z", "w");

        assertEquals(um, outro);
        assertEquals(um.hashCode(), outro.hashCode());
    }

    @Test
    void arquivosDiferentesNaoSaoIguais() throws Exception {
        assertNotEquals(new Musica("A", criar("a.mp3"), "x", "y"), new Musica("A", criar("b.mp3"), "x", "y"));
    }

    @Test
    void musicaEPodcastDoMesmoArquivoSaoIguais() throws Exception {
        String caminho = criar("a.mp3");
        assertEquals(new Musica("A", caminho, "x", "y"), new EpisodioPodcast("A", caminho, "p", 1));
    }

    @Test
    void playlistNaoAceitaOMesmoArquivoDuasVezes() throws Exception {
        String caminho = criar("a.mp3");
        Playlist lista = new Playlist("Lista");

        assertTrue(lista.adicionarFaixa(new Musica("A", caminho, "x", "y")));
        assertFalse(lista.adicionarFaixa(new Musica("A de novo", caminho, "x", "y")));
        assertEquals(1, lista.getTamanho());
    }

    @Test
    void caminhoVazioNaoExisteOuNaoEMp3SaoRecusados() throws Exception {
        assertThrows(FaixaInvalidaException.class, () -> Musica.validarCaminho("   "));
        assertThrows(FaixaInvalidaException.class, () -> Musica.validarCaminho(null));
        assertThrows(FaixaInvalidaException.class, () -> Musica.validarCaminho(pasta.resolve("nao_existe.mp3").toString()));

        File texto = pasta.resolve("nota.txt").toFile();
        texto.createNewFile();
        assertThrows(FaixaInvalidaException.class, () -> Musica.validarCaminho(texto.getAbsolutePath()));
    }

    @Test
    void arquivoMp3QueExisteEAceitoMesmoComExtensaoEmMaiuscula() throws Exception {
        assertDoesNotThrow(() -> Musica.validarCaminho(criar("a.mp3")));
        assertDoesNotThrow(() -> Musica.validarCaminho(criar("B.MP3")));
    }

    @Test
    void tituloVazioEPodcastSemEpisodioSaoRecusados() throws Exception {
        String caminho = criar("a.mp3");
        assertThrows(FaixaInvalidaException.class, () -> new Musica("  ", caminho, "x", "y"));
        assertThrows(FaixaInvalidaException.class, () -> new EpisodioPodcast("EP", caminho, "p", 0));
    }
}
