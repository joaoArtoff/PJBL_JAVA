package modelo;

import excecoes.PlayerException;
import excecoes.PlaylistInvalidaException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Transforma uma pasta do computador em uma playlist: o nome da playlist é o nome da pasta e
 * cada .mp3 dela vira uma Musica, em ordem alfabética.
 *
 * O arquivo não traz artista nem álbum (o programa não lê as etiquetas do MP3), então o título é
 * o nome do arquivo, o álbum é o nome da pasta e o artista fica "desconhecido". Só os arquivos
 * da própria pasta entram; subpastas e arquivos ocultos são ignorados.
 */
public class ImportadorDePasta {

    /**
     * Cria a playlist na biblioteca e devolve ela. Se algo der errado (pasta inválida, sem .mp3
     * ou nome já usado), nada é criado.
     */
    public Playlist importar(Biblioteca biblioteca, File pasta) throws PlayerException {
        if (pasta == null || !pasta.isDirectory()) {
            throw new PlaylistInvalidaException("A pasta escolhida não existe ou não pode ser lida.");
        }
        List<Faixa> faixas = lerFaixas(pasta);
        if (faixas.isEmpty()) {
            throw new PlaylistInvalidaException("Não há arquivos .mp3 na pasta \"" + pasta.getName() + "\".");
        }

        // As faixas ficam prontas antes de a playlist existir: se uma falhar, a biblioteca não muda.
        Playlist playlist = biblioteca.criarPlaylist(pasta.getName());
        for (Faixa faixa : faixas) {
            playlist.adicionarFaixa(faixa);
        }
        return playlist;
    }

    private List<Faixa> lerFaixas(File pasta) throws PlayerException {
        File[] arquivos = pasta.listFiles(ImportadorDePasta::ehMp3Visivel);
        if (arquivos == null) {
            throw new PlaylistInvalidaException("Não foi possível ler a pasta \"" + pasta.getName() + "\".");
        }
        Arrays.sort(arquivos, Comparator.comparing(f -> f.getName().toLowerCase()));

        List<Faixa> faixas = new ArrayList<>();
        for (File arquivo : arquivos) {
            faixas.add(new Musica(semExtensao(arquivo.getName()), arquivo.getAbsolutePath(), null, pasta.getName()));
        }
        return faixas;
    }

    private static boolean ehMp3Visivel(File arquivo) {
        String nome = arquivo.getName();
        return arquivo.isFile() && !nome.startsWith(".") && nome.toLowerCase().endsWith(".mp3");
    }

    private static String semExtensao(String nome) {
        return nome.substring(0, nome.length() - ".mp3".length());
    }
}
