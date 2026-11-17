import Excecoes.FaixaInvalidaException;
import Excecoes.PlayerException;
import Modelo.Biblioteca;
import Modelo.EpisodioPodcast;
import Modelo.Faixa;
import Modelo.Historico;
import Modelo.Musica;
import Modelo.Playlist;
import Modelo.RegistroHistorico;
import Modelo.Reprodutor;
import Modelo.RepositorioHistorico;
import Reproducao.ModoReproducao;
import Reproducao.OuvintePlayer;
import Reproducao.PlayerModos;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Como o programa exige que o arquivo .mp3 exista, aqui criamos arquivos .mp3
 * "de mentira" (vazios) numa pasta temporária. Assim roda em qualquer computador.
 * Quando tiver .mp3 de verdade, troque os caminhos em criarArquivoFalso(...).
 *
 * A seção de reprodução usa um Reprodutor "de mentira" (ReprodutorFalso), que não produz som
 * nem precisa do JavaFX: o Player só conhece a interface Reprodutor, então dá para trocar.
 */
public class Main {

    public static void main(String[] args) {
        try {
            Path pasta = Files.createTempDirectory("pjbl_teste");
            String mp3A = criarArquivoFalso(pasta, "musica1.mp3");
            String mp3B = criarArquivoFalso(pasta, "podcast1.mp3");

            // 1) Biblioteca e playlist
            Biblioteca biblioteca = new Biblioteca();
            Playlist favoritas = biblioteca.criarPlaylist("Favoritas");
            System.out.println("Playlist criada: " + favoritas.getNome());

            // 2) Faixas
            Musica musica = new Musica("Bohemian Rhapsody", mp3A, "Queen", "A Night at the Opera");
            EpisodioPodcast episodio = new EpisodioPodcast("Episodio de teste", mp3B, "Podcast Java", 7);

            System.out.println("Adicionou musica? " + favoritas.adicionarFaixa(musica));
            System.out.println("Adicionou episodio? " + favoritas.adicionarFaixa(episodio));
            System.out.println("Adicionou musica de novo? " + favoritas.adicionarFaixa(musica) + " (false = duplicada)");

            // 3) Listar a playlist
            System.out.println("\n--- " + favoritas.getNome() + " (" + favoritas.getTamanho() + " faixas) ---");
            for (Faixa f : favoritas.getFaixas()) {
                System.out.println(f.getTipo() + " | " + f.getTitulo() + " | " + f.getDescricao());
            }

            // 4) Posição salva do podcast
            episodio.registrarPosicao(125);
            System.out.println("\nDescricao do podcast depois de pausar: " + episodio.getDescricao());

            // 5) Histórico
            // O histórico da demonstração fica na pasta temporária, para não se misturar com o histórico real do app.
            RepositorioHistorico repositorio = new RepositorioHistorico(pasta.resolve("historico.txt").toString());
            Historico historico = repositorio.carregar();
            System.out.println("\nRegistros que já estavam salvos ao abrir: " + historico.getTotal());
            for (Faixa f : favoritas.getFaixas()) {
                repositorio.adicionar(historico.registrar(f));
            }
            System.out.println("--- Historico (" + historico.getTotal() + " registros, ultimos 10) ---");
            for (RegistroHistorico r : historico.getUltimas(10)) {
                System.out.println(r.getDataFormatada() + " | " + r.getTitulo());
            }

            // 6) Casos de erro: todos devem ser tratados, sem quebrar o programa
            System.out.println("\n--- Testes de erro ---");
            tentar("arquivo que nao existe", () -> new Musica("X", "nao_existe.mp3", "A", "B"));
            tentar("arquivo que nao e mp3", () -> new Musica("X", mp3A.replace(".mp3", ".txt"), "A", "B"));
            tentar("playlist com nome repetido", () -> biblioteca.criarPlaylist("Favoritas"));
            tentar("playlist com nome vazio", () -> biblioteca.criarPlaylist("  "));

            // 7) Reprodução: modos, próxima/anterior, fim de faixa, podcast, erros
            demonstrarReproducao(pasta);

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7) REPRODUÇÃO
    // =========================================================================

    private static void demonstrarReproducao(Path pasta) throws Exception {
        System.out.println("\n==================== REPRODUCAO ====================");

        // 7.1) Os modos sozinhos: só calculam "qual é a próxima posição".
        System.out.println("\n--- 7.1 Modos: proximoIndice(atual, tamanho) numa playlist de 3 faixas ---");
        for (ModoReproducao modo : ModoReproducao.values()) {
            StringBuilder linha = new StringBuilder(String.format("%-17s", modo.getNome()));
            for (int atual = 0; atual < 3; atual++) {
                linha.append(atual).append(" -> ").append(modo.proximoIndice(atual, 3)).append("   ");
            }
            System.out.println(linha + (modo == ModoReproducao.ALEATORIO ? "(sorteado, nunca igual ao atual)" : ""));
        }
        System.out.println("Playlist vazia (qualquer modo): " + ModoReproducao.SEQUENCIAL.proximoIndice(-1, 0) + " (-1 = parar)");

        // Preparação: uma playlist com 3 músicas e 1 podcast, e o player com um reprodutor falso.
        Biblioteca biblioteca = new Biblioteca();
        Playlist lista = biblioteca.criarPlaylist("Reproducao");
        Musica m1 = new Musica("Faixa 1", criarArquivoFalso(pasta, "rep1.mp3"), "Artista A", "Album X");
        Musica m2 = new Musica("Faixa 2", criarArquivoFalso(pasta, "rep2.mp3"), "Artista B", "Album Y");
        Musica m3 = new Musica("Faixa 3", criarArquivoFalso(pasta, "rep3.mp3"), "Artista C", "Album Z");
        EpisodioPodcast ep = new EpisodioPodcast("Episodio 1", criarArquivoFalso(pasta, "rep4.mp3"), "Podcast Java", 1);
        lista.adicionarFaixa(m1);
        lista.adicionarFaixa(m2);
        lista.adicionarFaixa(m3);
        lista.adicionarFaixa(ep);

        RepositorioHistorico repositorio = new RepositorioHistorico(pasta.resolve("historico_reproducao.txt").toString());
        Historico historico = repositorio.carregar();
        ReprodutorFalso reprodutor = new ReprodutorFalso();
        PlayerModos player = new PlayerModos(reprodutor, historico, repositorio);
        player.setOuvinte(new OuvinteConsole());   // imprime faixaIniciada / reproducaoParada / erroReproducao

        // 7.2) Sequencial: toca na ordem e para depois da última.
        System.out.println("\n--- 7.2 Modo SEQUENCIAL (a cada passo, a faixa acaba sozinha) ---");
        player.setModo(ModoReproducao.SEQUENCIAL);
        player.tocarPlaylist(lista);
        System.out.println("   agora: " + descrever(player));
        for (int passo = 0; passo < 10 && player.getFaixaAtual() != null; passo++) {
            System.out.println("   temProxima()? " + player.temProxima());
            reprodutor.terminarFaixa();
            System.out.println("   agora: " + descrever(player));
        }

        // 7.3) Repetir playlist: depois da última volta para a primeira.
        System.out.println("\n--- 7.3 Modo REPETIR_PLAYLIST (5 faixas acabando numa playlist de 4) ---");
        player.setModo(ModoReproducao.REPETIR_PLAYLIST);
        player.tocarPlaylist(lista);
        System.out.println("   agora: " + descrever(player));
        for (int passo = 0; passo < 5; passo++) {
            reprodutor.terminarFaixa();
            System.out.println("   agora: " + descrever(player));
        }
        System.out.println("   temProxima() na ultima? " + player.temProxima() + " (repetir sempre tem)");
        player.parar();

        // 7.4) Aleatório: sorteia, mas nunca repete a mesma faixa em seguida.
        System.out.println("\n--- 7.4 Modo ALEATORIO (10 faixas acabando) ---");
        player.setModo(ModoReproducao.ALEATORIO);
        player.tocarPlaylist(lista);
        List<Integer> sequencia = new ArrayList<>();
        sequencia.add(player.getIndiceAtual());
        boolean repetiuEmSeguida = false;
        for (int passo = 0; passo < 10; passo++) {
            int antes = player.getIndiceAtual();
            reprodutor.terminarFaixa();
            int depois = player.getIndiceAtual();
            sequencia.add(depois);
            if (antes == depois) {
                repetiuEmSeguida = true;
            }
        }
        System.out.println("   posicoes sorteadas: " + sequencia);
        System.out.println("   repetiu a mesma faixa em seguida? " + repetiuEmSeguida + " (esperado: false)");
        player.parar();

        // 7.5) Próxima e anterior (modo sequencial).
        System.out.println("\n--- 7.5 Proxima e anterior (SEQUENCIAL) ---");
        player.setModo(ModoReproducao.SEQUENCIAL);
        player.tocar(lista, m3);
        System.out.println("   tocando: " + descrever(player));
        reprodutor.avancar(1);
        player.anterior();
        System.out.println("   anterior com 1s tocado -> " + descrever(player));
        reprodutor.avancar(5);
        player.anterior();
        System.out.println("   anterior com 5s tocado -> " + descrever(player)
                + " | posicao: " + (int) player.getPosicao() + "s (volta ao inicio da mesma faixa)");
        player.tocar(lista, m1);
        player.anterior();
        System.out.println("   anterior na 1a faixa   -> " + descrever(player) + " (continua nela)");
        player.tocar(lista, ep);
        System.out.println("   temProxima() na ultima? " + player.temProxima());
        player.proxima();
        System.out.println("   proxima na ultima      -> " + descrever(player));

        // 7.6) Podcast: lembra onde parou; ao terminar, volta para o início.
        System.out.println("\n--- 7.6 Podcast retoma de onde parou ---");
        player.tocar(lista, ep);
        reprodutor.avancar(125);
        player.pausarOuRetomar();
        System.out.println("   depois de pausar aos 125s: " + ep.getDescricao());
        player.parar();
        player.tocar(lista, ep);
        System.out.println("   tocando de novo, comeca em: " + (int) player.getPosicao() + "s");
        reprodutor.terminarFaixa();
        System.out.println("   depois de terminar: " + ep.getDescricao() + " (posicao zerada)");

        // 7.7) Troca automática com arquivo apagado: avisa o erro e pula para a seguinte.
        System.out.println("\n--- 7.7 Faixa 2 apagada do disco: a troca automatica pula ela ---");
        player.setModo(ModoReproducao.SEQUENCIAL);
        player.tocar(lista, m1);
        new File(m2.getCaminhoArquivo()).delete();
        reprodutor.terminarFaixa();
        System.out.println("   agora: " + descrever(player));
        player.parar();

        // 7.8) Volume sempre entre 0 e 1.
        System.out.println("\n--- 7.8 Volume ---");
        player.setVolume(1.5);
        System.out.println("   setVolume(1.5)  -> " + player.getVolume());
        player.setVolume(-0.3);
        System.out.println("   setVolume(-0.3) -> " + player.getVolume());
        player.setVolume(0.8);
        System.out.println("   setVolume(0.8)  -> " + player.getVolume() + " (reprodutor recebeu " + reprodutor.getVolume() + ")");

        // 7.9) Erros de quem chama (tratados, sem quebrar o programa).
        System.out.println("\n--- 7.9 Testes de erro da reproducao ---");
        Musica fora = new Musica("Fora da playlist", criarArquivoFalso(pasta, "rep_fora.mp3"), "A", "B");
        Playlist vazia = biblioteca.criarPlaylist("Vazia");
        tentar("tocar playlist nula", () -> player.tocarPlaylist(null));
        tentar("tocar playlist vazia", () -> player.tocarPlaylist(vazia));
        tentar("tocar faixa nula", () -> player.tocar(lista, (Faixa) null));
        tentar("tocar faixa que nao esta na playlist", () -> player.tocar(lista, fora));
        tentar("tocar indice fora da playlist", () -> player.tocar(lista, 99));

        System.out.println("\nRegistros no historico da reproducao: " + historico.getTotal());
        System.out.println("--- Ultimos 3 ---");
        for (RegistroHistorico r : historico.getUltimas(3)) {
            System.out.println(r.getDataFormatada() + " | " + r.getTitulo());
        }
    }

    /** "Faixa 2 (2/4)" ou "nada tocando", para acompanhar o estado do player. */
    private static String descrever(PlayerModos player) {
        Faixa atual = player.getFaixaAtual();
        if (atual == null) {
            return "nada tocando";
        }
        return atual.getTitulo() + " (" + (player.getIndiceAtual() + 1) + "/" + player.getPlaylistAtual().getTamanho() + ")";
    }

    /** O que a tela faria com os eventos do player; aqui só imprime. */
    private static class OuvinteConsole implements OuvintePlayer {
        @Override
        public void faixaIniciada(Faixa faixa) {
            System.out.println("   [ouvinte] iniciou: " + faixa.getTitulo());
        }

        @Override
        public void reproducaoParada() {
            System.out.println("   [ouvinte] reproducao parada");
        }

        @Override
        public void erroReproducao(String mensagem) {
            System.out.println("   [ouvinte] ERRO: " + mensagem);
        }
    }

    /**
     * Reprodutor sem som: só guarda posição e volume. O tempo não passa sozinho; quem testa
     * chama avancar(...) para "tocar" alguns segundos e terminarFaixa() para a faixa acabar.
     */
    private static class ReprodutorFalso implements Reprodutor {
        private boolean carregado;
        private double posicao;
        private double volume = 0.8;
        private Runnable aoTerminar = () -> { };
        private Consumer<FaixaInvalidaException> aoFalhar = e -> { };

        @Override public void carregar(String caminho) { carregado = true; posicao = 0; }
        @Override public void tocar() { }
        @Override public void pausar() { }
        @Override public void parar() { carregado = false; posicao = 0; }
        @Override public void buscar(double segundos) { posicao = Math.max(0, segundos); }
        @Override public double getPosicao() { return posicao; }
        @Override public double getDuracao() { return carregado ? 180 : 0; }
        @Override public void setVolume(double volume) { this.volume = volume; }
        @Override public void setAoTerminar(Runnable acao) { this.aoTerminar = acao; }
        @Override public void setAoFalhar(Consumer<FaixaInvalidaException> acao) { this.aoFalhar = acao; }

        double getVolume() { return volume; }
        void avancar(double segundos) { posicao += segundos; }
        void terminarFaixa() { aoTerminar.run(); }
    }

    private static String criarArquivoFalso(Path pasta, String nome) throws Exception {
        File arquivo = pasta.resolve(nome).toFile();
        arquivo.createNewFile();
        arquivo.deleteOnExit();
        return arquivo.getAbsolutePath();
    }

    private interface Acao { void executar() throws PlayerException; }

    private static void tentar(String descricao, Acao acao) {
        try {
            acao.executar();
            System.out.println("[!] Nao deu erro em: " + descricao);
        } catch (PlayerException e) {
            System.out.println("[ok] " + descricao + " -> " + e.getMessage());
        }
    }
}