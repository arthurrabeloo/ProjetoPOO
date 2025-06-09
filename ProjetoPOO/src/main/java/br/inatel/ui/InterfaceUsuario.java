package br.inatel.ui;

import br.inatel.models.*;
import br.inatel.services.Gerenciador;
import br.inatel.exceptions.ConteudoNaoEncontradoException;
import br.inatel.exceptions.NotaInvalidaException;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class InterfaceUsuario {
    private Scanner scanner;
    private Gerenciador gerenciador;

    public InterfaceUsuario(Scanner scanner, Gerenciador gerenciador) {
        this.scanner = scanner;
        this.gerenciador = gerenciador;
    }

    public void iniciar() {
        while (true) {
            System.out.println("\n--- Menu Principal ---");
            System.out.println("1. Adicionar conteúdo");
            System.out.println("2. Listar conteúdo por gênero");
            System.out.println("3. Recomendação top 5");
            System.out.println("4. Avaliar conteúdo");
            System.out.println("5. Pesquisar conteúdo");
            System.out.println("6. Deletar conteúdo");
            System.out.println("7. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir a quebra de linha

            try {
                switch (opcao) {
                    case 1 -> adicionarConteudo();
                    case 2 -> listarPorGenero();
                    case 3 -> recomendarTop5();
                    case 4 -> avaliarConteudo();
                    case 5 -> pesquisarConteudo();
                    case 6 -> deletarConteudo();
                    case 7 -> {
                        System.out.println("Encerrando o programa...");
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) { /*tenta capturar qualquer exceção vinda do bloco do switch*/
                System.err.println("Erro: " + e.getMessage());
            }
        }
    }

    private void adicionarConteudo() {
        System.out.println("\n--- Adicionar Conteúdo ---");
        System.out.print("Título: ");
        String titulo = scanner.nextLine();

        //verificação se o conteudo ja existe, se existir imprime que ja esta add
        if (gerenciador.contemConteudo(titulo)) {
            System.out.println("O conteúdo com esse título já está adicionado.");
            return;
        }

        System.out.print("Gênero: ");
        String genero = scanner.nextLine();
        System.out.print("Ano de lançamento: ");
        int ano = scanner.nextInt();
        scanner.nextLine(); // Consumir quebra de linha
        System.out.print("Tipo (1 = Filme, 2 = Série, 3 = Livro): ");
        int tipo = scanner.nextInt();
        scanner.nextLine(); // Consumir quebra de linha

        //com base no conteudo, pede mais detalhes conforme o tipo
        Conteudo conteudo = switch (tipo) {
            case 1 -> {
                System.out.print("Diretor: ");
                String diretor = scanner.nextLine();
                System.out.print("Duração (minutos): ");
                int duracao = scanner.nextInt();
                yield new Filme(titulo, genero, ano, diretor, duracao); //yield new retorna um valor a variavel que esta esperando o estado do switch
            }
            case 2 -> {
                System.out.print("Número de temporadas: ");
                int temporadas = scanner.nextInt();
                System.out.print("Episódios totais: ");
                int episodios = scanner.nextInt();
                yield new Serie(titulo, genero, ano, temporadas, episodios);
            }
            case 3 -> {
                System.out.print("Autor: ");
                String autor = scanner.nextLine();
                System.out.print("Editora: ");
                String editora = scanner.nextLine();
                yield new Livro(titulo, genero, ano, autor, editora);
            }
            default -> throw new IllegalArgumentException("Tipo inválido.");
        };

        gerenciador.adicionarConteudo(conteudo);
        System.out.println("Conteúdo adicionado com sucesso!");
    }

    private void deletarConteudo() {
        System.out.print("\nInforme o título do conteúdo a ser deletado: ");
        String titulo = scanner.nextLine();

        try {
            boolean removido = gerenciador.removerConteudo(titulo);
            if (removido) {
                System.out.println("Conteúdo '" + titulo + "' deletado com sucesso.");
            } else {
                System.out.println("Erro: Conteúdo não encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao deletar o conteúdo: " + e.getMessage());
        }
    }


    private void listarPorGenero() {
        System.out.print("\nInforme o gênero: ");
        String genero = scanner.nextLine();

        List<Conteudo> recomendados = gerenciador.recomendarPorGenero(genero);
        if (recomendados.isEmpty()) {
            System.out.println("Nenhum conteúdo encontrado para o gênero informado.");
        } else {
            System.out.println("\n--- Conteúdo do gênero " + genero + " ---");
            for (Conteudo c : recomendados) {
                System.out.println(c.getTitulo() + " (" + String.format("%.2f", c.getNotaMedia()) + ")");
            }
        }
    }

    private void recomendarTop5() {
        List<Conteudo> top5 = gerenciador.recomendarTop(5);
        if (top5.isEmpty()) {
            System.out.println("Nenhum conteúdo disponível para recomendação.");
        } else {
            System.out.println("\n--- Top 5 Recomendações ---");
            for (Conteudo c : top5) {
                System.out.println(c.getTitulo() + " (" + String.format("%.2f", c.getNotaMedia()) + ")");
            }
        }
    }

    private void avaliarConteudo() {
        try {
            System.out.print("\nInforme o título do conteúdo: ");
            String titulo = scanner.nextLine();

            //pesquisando titulo
            Conteudo conteudo = gerenciador.getConteudos().stream()
                    .filter(c -> c.getTitulo().equalsIgnoreCase(titulo))
                    .findFirst()
                    .orElseThrow(() -> new ConteudoNaoEncontradoException("Conteúdo não encontrado."));

            System.out.print("Nome do usuário: ");
            String nome = scanner.nextLine();
            System.out.print("Email do usuário: ");
            String email = scanner.nextLine();
            Usuario usuario = new Usuario(nome, email); //novo objeto usuario com as informaçoes digitadas

            System.out.print("Nota (1 a 5): ");
            int nota = scanner.nextInt();
            if (nota < 1 || nota > 5) {
                throw new NotaInvalidaException("Nota deve estar entre 1 e 5.");
            }
            scanner.nextLine(); // Consumir quebra de linha após o número

            System.out.print("Comentário: ");
            String comentario = scanner.nextLine();

            // Adicionar avaliação
            usuario.avaliar(conteudo, nota, comentario); //passa o conteudo, a nota e o comentario
            System.out.println("Avaliação registrada com sucesso!");

            // Salvar alterações no arquivo
            gerenciador.salvarConteudosComoTexto("dados/conteudos.txt");
        }
        catch (ConteudoNaoEncontradoException e) {
            System.err.println(e.getMessage());
        } catch (NotaInvalidaException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao salvar os dados: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        }
    }


    private void pesquisarConteudo() {
        System.out.print("\nInforme o título ou parte do título do conteúdo: ");
        String titulo = scanner.nextLine();

        List<Conteudo> resultados = gerenciador.pesquisarPorTitulo(titulo);

        if (resultados.isEmpty()) {
            System.out.println("Nenhum conteúdo encontrado com o título fornecido.");
        } else {
            System.out.println("\n--- Resultados da Pesquisa ---");
            for (int i = 0; i < resultados.size(); i++) {
                Conteudo c = resultados.get(i);
                System.out.println((i + 1) + ". " + c.getTitulo() + " (" + c.getTipo() + ") - Nota Média: " + c.getNotaMedia());
            }

            System.out.print("\nEscolha o número de um conteúdo para ver mais detalhes (0 para voltar): ");
            int escolha = scanner.nextInt();
            scanner.nextLine(); // Consumir quebra de linha

            if (escolha > 0 && escolha <= resultados.size()) {
                Conteudo escolhido = resultados.get(escolha - 1);
                mostrarDetalhesConteudo(escolhido);
            } else {
                System.out.println("Voltando ao menu principal.");
            }
        }
    }

    private void mostrarDetalhesConteudo(Conteudo conteudo) {
        System.out.println("\n--- Detalhes de " + conteudo.getTitulo() + " ---");
        System.out.println("Tipo: " + conteudo.getTipo());
        System.out.println("Gênero: " + conteudo.getGenero());
        System.out.println("Ano de Lançamento: " + conteudo.getAnoLancamento());

        if (conteudo instanceof Filme filme) {
            System.out.println("Diretor: " + filme.getDiretor());
            System.out.println("Duração: " + filme.getDuracao() + " minutos");
        } else if (conteudo instanceof Serie serie) {
            System.out.println("Temporadas: " + serie.getTemporadas());
            System.out.println("Episódios: " + serie.getEpisodios());
        } else if (conteudo instanceof Livro livro) {
            System.out.println("Autor: " + livro.getAutor());
            System.out.println("Editora: " + livro.getEditora());
        }

        System.out.println("\n--- Ver avaliações ---");
        System.out.println("1. Avaliações");
        System.out.println("2. Voltar");
        System.out.print("Escolha uma opção: ");

        int escolha = scanner.nextInt();
        scanner.nextLine(); // Consumir quebra de linha

        if (escolha == 1) {
            List<Avaliacao> avaliacoes = conteudo.getAvaliacoes();
            if (avaliacoes.isEmpty()) {
                System.out.println("Esse conteúdo ainda não possui avaliações.");
            } else {
                System.out.println("\n--- Avaliações de " + conteudo.getTitulo() + " ---");
                for (Avaliacao avaliacao : avaliacoes) {
                    System.out.println("Usuário: " + avaliacao.getUsuario().getNome());
                    System.out.println("Nota: " + avaliacao.getNota());
                    System.out.println("Comentário: " + avaliacao.getComentario());
                    System.out.println("---------------------------");
                }
            }
        } else {
            System.out.println("Voltando à pesquisa...");
        }
    }

}