import excecoes.FaixaInvalidaException;
import excecoes.PlaylistInvalidaException;
import excecoes.PlaylistVaziaException;
import modelo.EpisodioPodcast;
import modelo.Faixa;
import modelo.Historico;
import modelo.Musica;
import modelo.Player;
import modelo.Playlist;
import modelo.Reprodutor;
import modelo.RepositorioHistorico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa as regras do Player (qual faixa toca, onde o podcast parou, o que acontece no fim da
 * playlist e nos erros) sem tocar som: o Reprodutor é trocado por um falso que só anota as chamadas.
 * É exatamente para isso que o Player conversa com a interface Reprodutor e não com o JavaFX.
 */
class PlayerTest {

    /** Reprodutor de mentira: guarda o que o Player pediu e deixa o teste escolher a posição. */
    static class ReprodutorFalso implements Reprodutor {
        final List<String> chamadas = new ArrayList<>();
        double posicao = 0;
        Runnable aoTerminar = () -> { };
        Consumer<FaixaInvalidaException> aoFalhar = e -> { };

        public void carregar(String caminho) { chamadas.add("carregar:" + new File(caminho).getName()); }
        public void tocar() { chamadas.add("tocar"); }
        public void pausar() { chamadas.add("pausar"); }
        public void parar() { chamadas.add("parar"); }
        public void buscar(double segundos) { chamadas.add("buscar:" + segundos); }
        public double getPosicao() { return posicao; }
        public double getDuracao() { return 100; }
        public void setVolume(double volume) { chamadas.add("volume:" + volume); }
        public void setAoTerminar(Runnable acao) { aoTerminar = acao; }
        public void setAoFalhar(Consumer<FaixaInvalidaException> acao) { aoFalhar = acao; }
    }

    @TempDir
    Path pasta;

    ReprodutorFalso falso;
    Player player;
    Musica musica1;
    EpisodioPodcast podcast;
    Musica musica3;
    Playlist lista;
    List<String> erros;
    int mudancas;

    @BeforeEach
    void preparar() throws Exception {
        falso = new ReprodutorFalso();
        player = new Player(falso, new Historico(), new RepositorioHistorico(pasta.resolve("historico.txt").toString()));
        erros = new ArrayList<>();
        mudancas = 0;
        player.setAoMudar(() -> mudancas++);
        player.setAoErro(e -> erros.add(e.getMessage()));

        musica1 = new Musica("M1", arquivo("a.mp3"), "x", "y");
        podcast = new EpisodioPodcast("EP", arquivo("b.mp3"), "programa", 1);
        musica3 = new Musica("M3", arquivo("c.mp3"), "x", "y");
        lista = new Playlist("Lista");
        lista.adicionarFaixa(musica1);
        lista.adicionarFaixa(podcast);
        lista.adicionarFaixa(musica3);
    }

    private String arquivo(String nome) throws IOException {
        File f = pasta.resolve(nome).toFile();
        f.createNewFile();
        return f.getAbsolutePath();
    }

    // ---------- entrada inválida ----------

    @Test
    void playlistNulaDaErro() {
        assertThrows(PlaylistInvalidaException.class, () -> player.tocar(null, 0));
    }

    @Test
    void playlistVaziaDaErroComONomeDela() throws Exception {
        PlaylistVaziaException e = assertThrows(PlaylistVaziaException.class, () -> player.tocar(new Playlist("Vazia"), 0));
        assertTrue(e.getMessage().contains("Vazia"));
    }

    @Test
    void indiceForaDaPlaylistDaErro() {
        assertThrows(PlaylistInvalidaException.class, () -> player.tocar(lista, -1));
        assertThrows(PlaylistInvalidaException.class, () -> player.tocar(lista, 3));
    }

    @Test
    void semPlaylistNaoTemProximaNemAnterior() {
        assertFalse(player.temProxima());
        assertFalse(player.temAnterior());
    }

    // ---------- tocar, pausar, retomar ----------

    @Test
    void tocarAPrimeiraChamaOReprodutorNaOrdemCerta() throws Exception {
        player.tocar(lista, 0);

        assertTrue(player.isTocando());
        assertSame(musica1, player.getFaixaAtual());
        assertEquals(List.of("carregar:a.mp3", "buscar:0.0", "tocar"), falso.chamadas);
        assertTrue(player.temProxima());
        assertFalse(player.temAnterior());
    }

    @Test
    void tocarGravaNoHistoricoEmArquivo() throws Exception {
        player.tocar(lista, 0);
        player.tocar(lista, 1);

        int total = new RepositorioHistorico(pasta.resolve("historico.txt").toString()).carregar().getTotal();
        assertEquals(2, total);
    }

    @Test
    void anteriorNaPrimeiraNaoFazNada() throws Exception {
        player.tocar(lista, 0);
        falso.chamadas.clear();

        player.anterior();

        assertEquals(0, player.getIndiceAtual());
        assertTrue(falso.chamadas.isEmpty());
    }

    @Test
    void pausarERetomar() throws Exception {
        player.tocar(lista, 0);

        player.pausarOuRetomar();
        assertFalse(player.isTocando());
        assertTrue(falso.chamadas.contains("pausar"));

        player.pausarOuRetomar();
        assertTrue(player.isTocando());
        assertEquals("tocar", falso.chamadas.get(falso.chamadas.size() - 1));
    }

    // ---------- podcast lembra onde parou ----------

    @Test
    void pausarGuardaAPosicaoDoPodcastEOTocarDeNovoRetomaDela() throws Exception {
        player.tocar(lista, 1);
        falso.posicao = 42.5;
        player.pausarOuRetomar();
        assertEquals(42.5, podcast.getPosicaoInicial());

        falso.chamadas.clear();
        player.tocar(lista, 1);

        assertTrue(falso.chamadas.contains("buscar:42.5"), falso.chamadas.toString());
    }

    @Test
    void trocarDeFaixaGuardaAPosicaoDoPodcastQueEstavaTocando() throws Exception {
        player.tocar(lista, 1);
        falso.posicao = 60;

        player.anterior();

        assertEquals(60, podcast.getPosicaoInicial());
        assertSame(musica1, player.getFaixaAtual());
    }

    @Test
    void pararLimpaAFaixaEGuardaAPosicao() throws Exception {
        player.tocar(lista, 1);
        falso.posicao = 9;

        player.parar();

        assertNull(player.getFaixaAtual());
        assertFalse(player.isTocando());
        assertEquals(9, podcast.getPosicaoInicial());
    }

    @Test
    void buscarRepassaAoReprodutor() throws Exception {
        player.tocar(lista, 0);
        player.buscar(12);
        assertTrue(falso.chamadas.contains("buscar:12.0"));
    }

    // ---------- fim da faixa ----------

    @Test
    void noFimDaFaixaAvancaEPodcastTerminadoVoltaAoInicio() throws Exception {
        player.tocar(lista, 1);
        falso.posicao = 100;

        falso.aoTerminar.run();

        assertSame(musica3, player.getFaixaAtual());
        assertEquals(0, podcast.getPosicaoInicial());
    }

    @Test
    void noFimDaUltimaFaixaOPlayerPara() throws Exception {
        player.tocar(lista, 2);

        falso.aoTerminar.run();

        assertNull(player.getFaixaAtual());
        assertFalse(player.isTocando());
        assertFalse(player.temProxima());
    }

    // ---------- erros ----------

    @Test
    void arquivoApagadoDaErroENaoFicaTocando() throws Exception {
        assertTrue(new File(musica3.getCaminhoArquivo()).delete());

        assertThrows(FaixaInvalidaException.class, () -> player.tocar(lista, 2));

        assertFalse(player.isTocando());
        assertNull(player.getFaixaAtual());
    }

    @Test
    void falhaAssincronaDoReprodutorParaOPlayerEChegaAoAoErro() throws Exception {
        player.tocar(lista, 0);

        falso.aoFalhar.accept(new FaixaInvalidaException("quebrou"));

        assertFalse(player.isTocando());
        assertNull(player.getFaixaAtual());
        assertEquals(List.of("quebrou"), erros);
    }

    @Test
    void volumeEAvisosDeMudancaChegam() throws Exception {
        player.setVolume(0.3);
        player.tocar(lista, 0);

        assertTrue(falso.chamadas.contains("volume:0.3"));
        assertTrue(mudancas > 0);
    }

    // ---------- bug da auditoria: remover faixa enquanto outra toca ----------

    @Test
    void removerFaixaAntesDaQueToca_anteriorVaiParaAFaixaCerta() throws Exception {
        player.tocar(lista, 2);          // M3 (a última) está tocando
        lista.removerFaixa(musica1);     // sai M1; sobram [EP, M3]

        assertEquals(1, player.getIndiceAtual(), "M3 agora é a segunda faixa (índice 1)");
        assertTrue(player.temAnterior());

        player.anterior();

        assertSame(podcast, player.getFaixaAtual(), "'anterior' deveria tocar o podcast, não repetir M3");
    }

    @Test
    void removerFaixaAntesDaQueToca_proximaContinuaDisponivelENoFimTocaAProxima() throws Exception {
        player.tocar(lista, 1);          // EP (do meio) está tocando
        lista.removerFaixa(musica1);     // sai M1; sobram [EP, M3]

        assertTrue(player.temProxima(), "ainda existe M3 depois do EP");

        falso.aoTerminar.run();

        assertSame(musica3, player.getFaixaAtual(), "no fim do EP, M3 deveria tocar");
    }

    @Test
    void avisarQueAPlaylistMudouFazATelaSeAtualizar() throws Exception {
        player.tocar(lista, 0);
        int antes = mudancas;

        player.playlistMudou();

        assertEquals(antes + 1, mudancas);
    }

    @Test
    void removerFaixaDepoisDaQueToca_naoMudaNada() throws Exception {
        player.tocar(lista, 0);
        lista.removerFaixa(musica3);

        assertEquals(0, player.getIndiceAtual());
        assertTrue(player.temProxima());
        assertFalse(player.temAnterior());
        Faixa atual = player.getFaixaAtual();
        assertSame(musica1, atual);
    }
}
