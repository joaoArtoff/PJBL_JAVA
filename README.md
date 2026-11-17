# PJBL Player

Player de músicas e podcasts em Java, com a aparência de um aparelho de CD/MP3 dos anos 2000.
Projeto final de Programação Orientada a Objetos (PJBL).

## O problema

Quem guarda músicas e episódios de podcast em arquivos `.mp3` precisa de um lugar para organizá-los em
playlists, tocá-los e, no caso do podcast, **continuar de onde parou**. O programa resolve isso:
as playlists e o histórico de reproduções são lembrados entre uma execução e outra.

## Funcionalidades

- Criar e remover **playlists** (sem nome vazio e sem nomes repetidos).
- Criar uma **playlist a partir de uma pasta** (**Playlist de uma pasta…**): o nome da playlist é o nome da pasta e
  cada `.mp3` dela vira uma música, em ordem alfabética (subpastas não entram).
- Adicionar **músicas** (título, artista, álbum) e **episódios de podcast** (título, programa, número do episódio)
  escolhendo o arquivo `.mp3`; remover faixas. O mesmo arquivo não entra duas vezes na mesma playlist.
- **Tocar de verdade**: play/pausa, parar, faixa anterior e próxima, barra de progresso (clique ou arraste) e volume.
  Ao terminar uma faixa, a próxima começa sozinha.
- O **podcast lembra onde parou**: ao pausar, trocar de faixa ou fechar, a posição fica guardada e ele recomeça dali.
- **Histórico** de tudo que foi tocado, gravado em arquivo de texto.
- **Playlists salvas** ao fechar a janela e carregadas ao abrir.
- Erros tratados com mensagem na tela, sem fechar o programa (arquivo apagado, arquivo que não é MP3, playlist vazia,
  nome repetido, falha ao ler ou gravar dados).

## Como rodar

**Requisitos:** JDK 17 ou mais novo (`java -version` mostra qual você tem) e internet na primeira execução.
Não precisa instalar o Maven: o projeto traz o Maven Wrapper, que baixa o Maven e o JavaFX sozinho.

macOS e Linux:

```bash
./mvnw javafx:run
```

Windows:

```bat
mvnw.cmd javafx:run
```

Para ver o programa funcionando, crie uma playlist (**Nova playlist**), adicione alguns `.mp3` (**Adicionar faixa**)
e aperte o botão central do *click wheel*.

**Testes:**

```bash
./mvnw test
```

**Demonstração do modelo no console** (sem interface; toca arquivos de mentira e mostra os erros tratados):

```bash
./mvnw -q compile
java -cp target/classes Main
```

### Onde ficam os dados

O programa cria a pasta `dados/` no diretório de onde foi executado (já está no `.gitignore`):

- `dados/biblioteca.dat`: playlists e faixas, gravadas ao fechar a janela.
- `dados/historico.txt`: uma linha por reprodução.

As faixas guardam o caminho completo do `.mp3`. Se o arquivo mudar de lugar ou o programa for aberto em
outro computador, a faixa continua na playlist e o erro "Arquivo não encontrado" aparece só ao tocar.

## Integrantes

- Lucas Dal Pra Brascher
- Lucas Lorenzo Kosake de Mesquita Fialho
- Guilherme Schuchardt
- Joao Vitor Rodrigues Artoff

## Estrutura

```
src/
├── excecoes/   exceções do player (raiz: PlayerException)
├── modelo/     domínio e regras: Faixa, Musica, EpisodioPodcast, Playlist, Biblioteca, Historico,
│               RegistroHistorico, Player, Reprodutor, ImportadorDePasta, RepositorioHistorico,
│               RepositorioBiblioteca
├── audio/      ReprodutorMp3: toca MP3 com o JavaFX Media
├── ui/         interface JavaFX (AppPlayer e os painéis da tela)
└── Main.java   demonstração do modelo no console
recursos/       estilo.css (aparência da interface)
teste/          testes JUnit do modelo
```

## Decisões de modelagem

**Classes e objetos.** Cada coisa do problema é uma classe com uma responsabilidade: `Faixa` (um arquivo de áudio),
`Playlist` (uma lista de faixas), `Biblioteca` (todas as playlists), `Historico` (o que já foi tocado) e `Player`
(quem coordena tudo na hora de tocar). A interface não tem regra de negócio: ela chama o modelo e mostra o resultado.

**Encapsulamento.** Os atributos são privados e só mudam por métodos que validam: o título de uma faixa não pode ser
vazio, o nome de uma playlist também não, o número do episódio é no mínimo 1 e o caminho do arquivo é `final`.
`Playlist.getFaixas()` e `Biblioteca.getPlaylists()` devolvem uma **cópia** da lista, então quem chama não consegue
alterar o estado interno por fora.

**Coleções.** `ArrayList<Faixa>` na playlist, `ArrayList<Playlist>` na biblioteca e `ArrayList<RegistroHistorico>`
no histórico. A regra "sem faixa repetida" usa `contains`, que depende do `equals` de `Faixa` (duas faixas são a mesma
se apontam para o mesmo arquivo).

**Associação.** `Biblioteca` contém `Playlist`, que contém `Faixa`. A biblioteca é quem cria as playlists (elas só
existem dentro dela), enquanto as faixas são criadas fora e apenas adicionadas. O `Player` colabora com
`Playlist`, `Reprodutor` e `Historico`.

**Herança.** `Musica` e `EpisodioPodcast` são especializações de `Faixa`: herdam título, arquivo e validação, e
acrescentam o que é só delas (artista e álbum; programa, número do episódio e posição salva). As exceções também
formam uma hierarquia, com `PlayerException` na raiz.

**Polimorfismo.** O `Player` e a tela tratam qualquer `Faixa` do mesmo jeito, sem `if` para o tipo: `getDescricao()`,
`getTipo()`, `getPosicaoInicial()` e `registrarPosicao()` respondem de forma diferente em cada subclasse (a música
começa sempre do zero e ignora a posição; o podcast guarda e devolve onde parou). A mesma linha da lista mostra música
e podcast.

**Abstração e contratos.**
- `Faixa` é **abstrata**, com `getDescricao()` e `getTipo()` abstratos, porque não existe uma "faixa genérica":
  toda faixa real é uma música ou um episódio.
- `Reprodutor` é uma **interface**: o contrato de "algo que sabe tocar um arquivo". O `Player` só conhece esse
  contrato, e quem toca de verdade é `ReprodutorMp3` (JavaFX Media). Por isso as regras do `Player` (qual faixa toca,
  onde o podcast parou, o que fazer no fim da playlist e nos erros) são testadas em `teste/PlayerTest.java` com um
  reprodutor falso, sem precisar de som nem de janela.

## Recursos extras

- **Tratamento de exceções.** `PlayerException` (verificada) e suas filhas `FaixaInvalidaException`,
  `PlaylistInvalidaException`, `PlaylistVaziaException` e `PersistenciaException`. As mensagens são pensadas para o
  usuário, e a interface mostra todas no mesmo lugar (`ui/Alertas`). Falhas do áudio do JavaFX, que chegam depois e
  sem aviso (como um `.mp3` vazio), também viram `FaixaInvalidaException`.
- **Interface gráfica.** JavaFX, com a tela de um aparelho de CD/MP3 dos anos 2000: tampa do CD com o disco girando,
  tela LCD com as listas e *click wheel*. Os desenhos são feitos em código e em CSS, sem imagens.
- **Persistência de objetos.** A `Biblioteca` inteira é gravada por serialização (`RepositorioBiblioteca`) e o
  histórico em texto (`RepositorioHistorico`).
