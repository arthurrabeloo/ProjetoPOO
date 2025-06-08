package br.inatel.app;

import br.inatel.models.*;
import br.inatel.services.Recomendador;
import br.inatel.ui.InterfaceUsuario;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

    private static final String ARQUIVO_DADOS = "dados/conteudos.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Recomendador recomendador = new Recomendador();

        // Carregar dados salvos (se existir)
        try {
            carregarDados(recomendador);
            System.out.println("Dados carregados com sucesso!");
        } catch (IOException e) {
            System.out.println("Nenhum dado salvo encontrado, iniciando com dados vazios.");
        }

        // Inicializar UI
        InterfaceUsuario ui = new InterfaceUsuario(scanner, recomendador);

        // Inicia interação com o usuário
        ui.iniciar();

        // Salvar dados ao final da execução
        try {
            salvarDados(recomendador);
            System.out.println("Dados salvos com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }

        scanner.close();
    }

    private static void salvarDados(Recomendador recomendador) throws IOException {
        List<String> linhas = recomendador.getConteudos().stream()
                .flatMap(conteudo -> {
                    // Linha do conteúdo
                    String linhaConteudo = conteudoParaLinha(conteudo);
                    // Linhas das avaliações associadas
                    List<String> linhasAvaliacoes = conteudo.getAvaliacoes().stream()
                            .map(avaliacao -> avaliacaoParaLinha(avaliacao, conteudo.getTitulo()))
                            .toList();
                    // Combinar as linhas
                    return Stream.concat(
                            List.of(linhaConteudo).stream(),
                            linhasAvaliacoes.stream()
                    );

                })
                .toList();

        Path path = Paths.get(ARQUIVO_DADOS);
        Files.write(path, linhas, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void carregarDados(Recomendador recomendador) throws IOException {
        Path path = Paths.get(ARQUIVO_DADOS);
        if (!Files.exists(path)) {
            throw new IOException("Arquivo de dados não encontrado");
        }

        List<String> linhas = Files.readAllLines(path);
        Conteudo conteudoAtual = null;
        for (String linha : linhas) {
            if (linha.startsWith("Avaliacao;")) {
                if (conteudoAtual != null) {
                    Avaliacao avaliacao = linhaParaAvaliacao(linha);
                    conteudoAtual.adicionarAvaliacao(avaliacao);
                }
            } else {
                conteudoAtual = linhaParaConteudo(linha);
                if (conteudoAtual != null) {
                    recomendador.adicionarConteudo(conteudoAtual);
                }
            }
        }
    }

    private static String conteudoParaLinha(Conteudo c) {
        if (c instanceof Filme f) {
            return String.format("Filme;%s;%s;%d;%s;%d",
                    f.getTitulo(),
                    f.getGenero(),
                    f.getAnoLancamento(),
                    f.getDiretor(),
                    f.getDuracao());
        } else if (c instanceof Serie s) {
            return String.format("Serie;%s;%s;%d;%d;%d",
                    s.getTitulo(),
                    s.getGenero(),
                    s.getAnoLancamento(),
                    s.getTemporadas(),
                    s.getEpisodios());
        } else if (c instanceof Livro l) {
            return String.format("Livro;%s;%s;%d;%s;%s",
                    l.getTitulo(),
                    l.getGenero(),
                    l.getAnoLancamento(),
                    l.getAutor(),
                    l.getEditora());
        }
        return "";
    }

    private static Conteudo linhaParaConteudo(String linha) {
        String[] partes = linha.split(";");
        if (partes.length < 6) return null;

        String tipo = partes[0];
        String titulo = partes[1];
        String genero = partes[2];
        int ano = Integer.parseInt(partes[3]);

        try {
            switch (tipo) {
                case "Filme" -> {
                    String diretor = partes[4];
                    int duracao = Integer.parseInt(partes[5]);
                    return new Filme(titulo, genero, ano, diretor, duracao);
                }
                case "Serie" -> {
                    int temporadas = Integer.parseInt(partes[4]);
                    int episodios = Integer.parseInt(partes[5]);
                    return new Serie(titulo, genero, ano, temporadas, episodios);
                }
                case "Livro" -> {
                    String autor = partes[4];
                    String editora = partes[5];
                    return new Livro(titulo, genero, ano, autor, editora);
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar linha: " + linha);
            return null;
        }
    }

    private static String avaliacaoParaLinha(Avaliacao avaliacao, String tituloConteudo) {
        return String.format("Avaliacao;%s;%d;%s;%s",
                tituloConteudo,
                avaliacao.getNota(),
                avaliacao.getUsuario().getEmail(),
                avaliacao.getComentario());
    }

    private static Avaliacao linhaParaAvaliacao(String linha) {
        String[] partes = linha.split(";");
        if (partes.length != 5) {
            throw new IllegalArgumentException("Formato de linha de avaliação inválido: " + linha);
        }

        String nomeUsuario = partes[1];
        String emailUsuario = partes[3];
        int nota = Integer.parseInt(partes[2]);
        String comentario = partes[4];
        Usuario usuario = new Usuario(nomeUsuario, emailUsuario);
        return new Avaliacao(usuario, nota, comentario);
    }

}
