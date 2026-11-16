import excecoes.PlayerException;
import modelo.Biblioteca;
import modelo.EpisodioPodcast;
import modelo.Faixa;
import modelo.Historico;
import modelo.Musica;
import modelo.Playlist;
import modelo.RegistroHistorico;
import modelo.RepositorioHistorico;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Como o programa exige que o arquivo .mp3 exista, aqui criamos arquivos .mp3
 * "de mentira" (vazios) numa pasta temporária. Assim roda em qualquer computador.
 * Quando tiver .mp3 de verdade, troque os caminhos em criarArquivoFalso(...).
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
            RepositorioHistorico repositorio = new RepositorioHistorico("dados/historico.txt");
            Historico historico = repositorio.carregar();
            System.out.println("\nRegistros que já estavam salvos ao abrir: " + historico.getTotal());
            for (Faixa f : favoritas.getFaixas()) {
                f.registrarReproducao();
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

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
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
