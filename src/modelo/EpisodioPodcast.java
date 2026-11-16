package modelo;

import excecoes.FaixaInvalidaException;

public class EpisodioPodcast extends Faixa {
    private static final long serialVersionUID = 1L;

    private String programa;
    private int numeroEpisodio;
    //Último segundo ouvido. É salvo em arquivo junto com o resto
    private double posicaoSalva;

    //Construtor já confere se o programa escolhido é válido e se tem pelo menos um episódio, para assim continuar
    public EpisodioPodcast(String titulo, String caminhoArquivo, String programa, int numeroEpisodio) throws FaixaInvalidaException {
        super(titulo, caminhoArquivo);
        this.programa = (programa == null || programa.isBlank()) ? "Programa desconhecido" : programa.trim();
        if  (numeroEpisodio < 1) {
            throw new FaixaInvalidaException("Precisa ter pelo menos um episódio");
        }
        this.numeroEpisodio = numeroEpisodio;
        this.posicaoSalva = 0;
    }


    //Pegar a descrição do Episódio de podcats, válidando se a posição salva é maior que zero.
    @Override
    public String getDescricao() {
        String texto = programa + " - " + numeroEpisodio;
        if (posicaoSalva > 0) {
            texto += " - Parou em " + formatarTempo(posicaoSalva);
        }
        return texto;
    }

    //gets do para pegar o tipo de mídia e a posição ínicial.
    @Override
    public String getTipo() { return "Episodio Podcast"; }
    @Override
    public double getPosicaoInicial() { return posicaoSalva; }   // CORRIGIDO: devolvia 0 (o podcast nunca retomava)

    @Override
    public void registrarPosicao(double segundos) {
        //Para não aceitar valor negativo
        this.posicaoSalva = Math.max(0, segundos);   // CORRIGIDO: era += (somava as posições em vez de guardar a última)
    }

    //Transforma 125.0 segundos em "2:05".
    public static String formatarTempo(double segundos) {
        int total = (int) segundos;
        return String.format("%d:%02d", total/60, total % 60);
    }

    public String getPrograma() { return programa; }
    public int getNumeroEpisodio() { return numeroEpisodio; }
}
