package reproducao;

import modelo.Faixa;

/**
 * É um complemento ao setAoMudar/setAoErro do Player: em vez de "algo mudou, olhe o estado"
 * recebe os eventos separados, como "começou", "parou" ou "erro"
 */
public interface OuvintePlayer {

    /** Uma faixa inciou, por clique do usuário ou automáticamente */
    void faixaIniciada(Faixa faixa);

    /** Parou a reprodução */
    void reproducaoParada();

    /** Erro que aconteceu quando nõa se esperava um throw (troca automática de faixa) */
    void erroReproducao(String mensagem);
}
