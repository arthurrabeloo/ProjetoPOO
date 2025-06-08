package br.inatel.ui;

import br.inatel.models.*;
import br.inatel.services.Recomendador;
import br.inatel.exceptions.ConteudoNaoEncontradoException;
import br.inatel.exceptions.NotaInvalidaException;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class InterfaceUsuario {
    private Scanner scanner;
    private Recomendador recomendador;

    public InterfaceUsuario(Scanner scanner, Recomendador recomendador) {
        this.scanner = scanner;
        this.recomendador = recomendador;
    }

    public void iniciar() {
        while (true) {
            System.out.println("\n--- Menu Principal ---");
            System.out.println("1. Adicionar conteúdo");
            System.out.println("2. Listar conteúdo por gênero");
            System.out.println("3. Recomendação top 5");
            System.out.println("4. Avaliar conteúdo");
            System.out.println("5. Pesquisar conteúdo");
            System.out.println("6. Ver avaliações");
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
                    case 6 -> verAvaliacoes();
                    case 7 -> {
                        System.out.println("Encerrando o programa...");
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
            }
        }
    }

    private void adicionarConteudo() {
        System.out.println("\n--- Adicionar Conteúdo ---");
        System.out.print("Título: ");
        String titulo = scanner.nextLine();

        if (recomendador.contemConteudo(titulo)) {
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

        Conteudo conteudo = switch (tipo) {
            case 1 -> {
                System.out.print("Diretor: ");
                String diretor = scanner.nextLine();
                System.out.print("Duração (minutos): ");
                int duracao = scanner.nextInt();
                yield new Filme(titulo, genero, ano, diretor, duracao);
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

        recomendador.adicionarConteudo(conteudo);
        System.out.println("Conteúdo adicionado com sucesso!");
    }


    private void listarPorGenero() {
        System.out.print("\nInforme o gênero: ");
        String genero = scanner.nextLine();

        List<Conteudo> recomendados = recomendador.recomendarPorGenero(genero);
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
        List<Conteudo> top5 = recomendador.recomendarTop(5);
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

            Conteudo conteudo = recomendador.getConteudos().stream()
                    .filter(c -> c.getTitulo().equalsIgnoreCase(titulo))
                    .findFirst()
                    .orElseThrow(() -> new ConteudoNaoEncontradoException("Conteúdo não encontrado."));

            System.out.print("Nome do usuário: ");
            String nome = scanner.nextLine();
            System.out.print("Email do usuário: ");
            String email = scanner.nextLine();
            Usuario usuario = new Usuario(nome, email);

            System.out.print("Nota (1 a 5): ");
            int nota = scanner.nextInt();
            if (nota < 1 || nota > 5) {
                throw new NotaInvalidaException("Nota deve estar entre 1 e 5.");
            }
            scanner.nextLine(); // Consumir quebra de linha após o número

            System.out.print("Comentário: ");
            String comentario = scanner.nextLine();

            // Adicionar avaliação
            usuario.avaliar(conteudo, nota, comentario);
            System.out.println("Avaliação registrada com sucesso!");

            // Salvar alterações no arquivo
            recomendador.salvarConteudosComoTexto("dados/conteudos.txt");
        } catch (ConteudoNaoEncontradoException e) {
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

        List<Conteudo> resultados = recomendador.pesquisarPorTitulo(titulo);

        if (resultados.isEmpty()) {
            System.out.println("Nenhum conteúdo encontrado com o título fornecido.");
        } else {
            System.out.println("\n--- Resultados da Pesquisa ---");
            for (Conteudo c : resultados) {
                System.out.println(c.getTitulo() + " (" + c.getTipo() + ") - Nota Média: " + c.getNotaMedia());
            }
        }
    }

    private void verAvaliacoes() {
        System.out.print("\nInforme o título do conteúdo: ");
        String titulo = scanner.nextLine();

        Conteudo conteudo = recomendador.getConteudos().stream()
                .filter(c -> c.getTitulo().equalsIgnoreCase(titulo))
                .findFirst()
                .orElse(null);

        if (conteudo == null) {
            System.out.println("Erro: Conteúdo não encontrado.");
            return;
        }

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
    }

}
