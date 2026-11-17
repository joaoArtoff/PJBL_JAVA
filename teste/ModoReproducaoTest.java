import org.junit.jupiter.api.Test;
import reproducao.ModoReproducao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModoReproducaoTest {

    @Test
    void sequencialAvancaEPareDepoisDaUltima() {
        assertEquals(0, ModoReproducao.SEQUENCIAL.proximoIndice(-1, 3));
        assertEquals(2, ModoReproducao.SEQUENCIAL.proximoIndice(1, 3));
        assertEquals(-1, ModoReproducao.SEQUENCIAL.proximoIndice(2, 3));
    }

    @Test
    void repetirPlaylistVoltaParaAPrimeira() {
        assertEquals(0, ModoReproducao.REPETIR_PLAYLIST.proximoIndice(2, 3));
        assertEquals(1, ModoReproducao.REPETIR_PLAYLIST.proximoIndice(0, 3));
    }

    @Test
    void aleatorioNuncaRepeteAFaixaAtualComVariasFaixas() {
        for (int i = 0; i < 200; i++) {
            int proximo = ModoReproducao.ALEATORIO.proximoIndice(1, 4);
            assertNotEquals(1, proximo);
            assertTrue(proximo >= 0 && proximo < 4);
        }
    }

    @Test
    void aleatorioComUmaFaixaTocaEla() {
        assertEquals(0, ModoReproducao.ALEATORIO.proximoIndice(0, 1));
    }

    @Test
    void playlistVaziaParaEmQualquerModo() {
        for (ModoReproducao modo : ModoReproducao.values()) {
            assertEquals(-1, modo.proximoIndice(-1, 0));
        }
    }

    @Test
    void proximoModoDaAVoltaNaOrdemDoEnum() {
        assertEquals(ModoReproducao.REPETIR_PLAYLIST, ModoReproducao.SEQUENCIAL.proximo());
        assertEquals(ModoReproducao.ALEATORIO, ModoReproducao.REPETIR_PLAYLIST.proximo());
        assertEquals(ModoReproducao.SEQUENCIAL, ModoReproducao.ALEATORIO.proximo());
    }
}
