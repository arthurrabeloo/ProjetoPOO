package br.inatel.app;

import br.inatel.models.*;
import br.inatel.services.Recomendador;
import br.inatel.ui.InterfaceUsuario;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final String ARQUIVO_DADOS = "dados/recomendador.txt";

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

        // Exemplo inicial de conteúdos e avaliações (se o carregamento não trouxe nada)
        if (recomendador.getConteudos().isEmpty()) {
            inicializarDadosExemplo(recomendador);
        }

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

    private static void inicializarDadosExemplo(Recomendador recomendador) {
        Usuario usuario1 = new Usuario("João", "joao@email.com");
        Usuario usuario2 = new Usuario("Maria", "maria@email.com");

        Livro livro1 = new Livro("Dom Quixote", "Aventura", 1605, "Miguel de Cervantes", "Penguin Books");
        Filme filme1 = new Filme("Inception", "Ficção", 2010, "Christopher Nolan", 148);
        Serie serie1 = new Serie("Breaking Bad", "Drama", 2008, 5, 62);

        recomendador.adicionarConteudo(livro1);
        recomendador.adicionarConteudo(filme1);
        recomendador.adicionarConteudo(serie1);

        usuario1.avaliar(livro1, 5, "Obra-prima da literatura.");
        usuario1.avaliar(filme1, 4, "Muito interessante e complexo.");
        usuario2.avaliar(serie1, 5, "Melhor série que já vi!");
    }

    private static void salvarDados(Recomendador recomendador) throws IOException {
        // Monta uma lista de strings para salvar — formato simples: cada conteúdo em uma linha com campos separados por ";"
        List<String> linhas = recomendador.getConteudos().stream()
                .map(Main::conteudoParaLinha)
                .collect(Collectors.toList());

        Path path = Paths.get(ARQUIVO_DADOS);
        Files.createDirectories(path.getParent()); // cria pasta se não existir
        Files.write(path, linhas, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void carregarDados(Recomendador recomendador) throws IOException {
        Path path = Paths.get(ARQUIVO_DADOS);
        if (!Files.exists(path)) {
            throw new IOException("Arquivo de dados não encontrado");
        }

        List<String> linhas = Files.readAllLines(path);
        for (String linha : linhas) {
            Conteudo c = linhaParaConteudo(linha);
            if (c != null) {
                recomendador.adicionarConteudo(c);
            }
        }
    }

    // Converte um Conteúdo para uma linha String no arquivo
    private static String conteudoParaLinha(Conteudo c) {
        // Exemplo formato simples:
        // tipo;titulo;genero;ano;campoExtra1;campoExtra2
        // Vamos armazenar o tipo para reconstituir depois
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

    // Converte uma linha do arquivo para um Conteudo
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
}
