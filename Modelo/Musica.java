package Modelo;

import Excecoes.FaixaInvalidaException;

/**
 * Música: uma Faixa que tem artista e álbum.
 * Ela herda título, caminho do arquivo, validação, toString() e só acrescenta o que é específico dela.
 * NÃO SOBRESCREVE getPosicaoIncial(), começa do zero.
 */
public class Musica extends Faixa{

    private static final  long serialVersionUID = 1L;

    private String artista;
    private String album;

    public Musica(String titulo, String caminhoArquivo, String artista, String album) throws FaixaInvalidaException {
        super(titulo, caminhoArquivo);
        // CORRIGIDO: usar os setters, senão o valor padrão "Artista desconhecido" nunca era aplicado
        setArtista(artista);
        setAlbum(album);
    }

    //Getters para reescrita da descrição e o tipo de mídia.
    @Override
    public String getDescricao() { return  artista + " - " + album; }

    @Override
    public String getTipo() { return "Musica"; }

    public String getArtista() { return artista; }
    public String getAlbum() { return album; }


    //Se artista ou album estiverem vazios, retorna valor padrão ao invez de null
    public void setArtista(String artista) {
        this.artista =  (artista == null || artista.isBlank()) ? "Artista desconhecido" : artista.trim();
    }
    public void setAlbum(String album) {
        this.album = (album == null || album.isBlank()) ? "Álbum desconhecido" : album.trim();
    }
}
