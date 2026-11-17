package modelo;

import excecoes.PersistenciaException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Lê e grava a Biblioteca inteira (playlists e faixas) em um arquivo, por serialização.
 * A Biblioteca é a raiz do modelo: ao gravá-la, tudo que ela contém é gravado junto.
 *
 * Guarda os caminhos dos .mp3 como estavam. Se o arquivo de áudio sumir ou o programa for
 * aberto em outro computador, a faixa continua na playlist e o erro aparece só ao tocar.
 */
public class RepositorioBiblioteca {

    private final String arquivo;

    public RepositorioBiblioteca(String arquivo) {
        this.arquivo = arquivo;
    }

    /** Lê o arquivo. Se ele ainda não existe (primeira vez), devolve uma Biblioteca vazia. */
    public Biblioteca carregar() throws PersistenciaException {
        File f = new File(arquivo);
        if (!f.exists()) {
            return new Biblioteca();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))) {
            return (Biblioteca) entrada.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new PersistenciaException("Não foi possível ler as playlists de " + arquivo
                    + ". O programa abre sem elas e não altera esse arquivo; para começar do zero, apague-o.", e);
        }
    }

    /**
     * Grava num arquivo temporário e só depois troca pelo definitivo. Assim, se o programa for
     * fechado no meio da gravação, o arquivo anterior continua inteiro em vez de ficar pela metade.
     */
    public void salvar(Biblioteca biblioteca) throws PersistenciaException {
        File destino = new File(arquivo);
        File temporario = new File(arquivo + ".tmp");
        try {
            File pasta = destino.getParentFile();
            if (pasta != null) {
                pasta.mkdirs();
            }
            try (ObjectOutputStream saida = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(temporario)))) {
                saida.writeObject(biblioteca);
            }
            Files.move(temporario.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar as playlists em " + arquivo, e);
        }
    }
}
