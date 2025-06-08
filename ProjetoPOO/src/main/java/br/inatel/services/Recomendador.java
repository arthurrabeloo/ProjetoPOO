package br.inatel.services;

import br.inatel.models.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Recomendador {
    private List<Conteudo> conteudos;

    public Recomendador() {
        this.conteudos = new ArrayList<>();
    }

    public boolean contemConteudo(String titulo) {
        return conteudos.stream()
                .anyMatch(c -> c.getTitulo().equalsIgnoreCase(titulo));
    }

    public void adicionarConteudo(Conteudo conteudo) {
        conteudos.add(conteudo);
    }

    public List<Conteudo> recomendarPorGenero(String genero) {
        return conteudos.stream()
                .filter(c -> c.getGenero().equalsIgnoreCase(genero))
                .sorted(Comparator.comparingDouble(Conteudo::getNotaMedia).reversed())
                .collect(Collectors.toList());
    }

    public List<Conteudo> recomendarTop(int topN) {
        return conteudos.stream()
                .sorted(Comparator.comparingDouble(Conteudo::getNotaMedia).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    public List<Conteudo> getConteudos() {
        return conteudos;
    }

    public void salvarConteudosComoTexto(String caminho) throws IOException {
        List<String> linhas = new ArrayList<>();

        for (Conteudo conteudo : conteudos) {
            // Identificar o tipo de conteúdo
            String tipo = conteudo.getTipo();

            // Formatando a linha do conteúdo com base no tipo
            String linhaConteudo = switch (tipo) {
                case "Filme" -> String.format("Filme;%s;%s;%d;%s;%d",
                        conteudo.getTitulo(),
                        conteudo.getGenero(),
                        conteudo.getAnoLancamento(),
                        ((Filme) conteudo).getDiretor(),
                        ((Filme) conteudo).getDuracao());
                case "Série" -> String.format("Serie;%s;%s;%d;%d;%d",
                        conteudo.getTitulo(),
                        conteudo.getGenero(),
                        conteudo.getAnoLancamento(),
                        ((Serie) conteudo).getTemporadas(),
                        ((Serie) conteudo).getEpisodios());
                case "Livro" -> String.format("Livro;%s;%s;%d;%s;%s",
                        conteudo.getTitulo(),
                        conteudo.getGenero(),
                        conteudo.getAnoLancamento(),
                        ((Livro) conteudo).getAutor(),
                        ((Livro) conteudo).getEditora());
                default -> throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
            };
            linhas.add(linhaConteudo);

            // Adicionando as avaliações associadas ao conteúdo
            for (Avaliacao avaliacao : conteudo.getAvaliacoes()) {
                String linhaAvaliacao = String.format("Avaliacao;%s;%d;%s",
                        avaliacao.getUsuario().getEmail(),
                        avaliacao.getNota(),
                        avaliacao.getComentario());
                linhas.add(linhaAvaliacao);
            }
        }

        // Caminho do arquivo e gravação
        Path path = Paths.get(caminho);
        try {
            Files.createDirectories(path.getParent()); // Garante que o diretório existe
            Files.write(path, linhas); // Grava os dados no arquivo
            System.out.println("Dados salvos com sucesso em: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao salvar os dados: " + e.getMessage());
            throw e; // Lança novamente a exceção para que possa ser tratada em nível superior
        }
    }



    public void carregarConteudosDeTexto(String caminho) throws IOException {
        Path path = Paths.get(caminho);

        if (!Files.exists(path)) {
            throw new IOException("Arquivo não encontrado: " + caminho);
        }

        List<String> linhas = Files.readAllLines(path);
        conteudos.clear();

        Map<String, Conteudo> conteudoMap = new HashMap<>();

        for (String linha : linhas) {
            String[] partes = linha.split(";", -1); // Inclui campos vazios
            String tipo = partes[0];

            switch (tipo) {
                case "Filme" -> {
                    Filme filme = new Filme(
                            partes[1], // Título
                            partes[2], // Gênero
                            Integer.parseInt(partes[3]), // Ano
                            partes[4], // Diretor
                            Integer.parseInt(partes[5]) // Duração
                    );
                    conteudos.add(filme);
                    conteudoMap.put(filme.getTitulo(), filme);
                }
                case "Serie" -> {
                    Serie serie = new Serie(
                            partes[1], // Título
                            partes[2], // Gênero
                            Integer.parseInt(partes[3]), // Ano
                            Integer.parseInt(partes[4]), // Temporadas
                            Integer.parseInt(partes[5]) // Episódios
                    );
                    conteudos.add(serie);
                    conteudoMap.put(serie.getTitulo(), serie);
                }
                case "Livro" -> {
                    Livro livro = new Livro(
                            partes[1], // Título
                            partes[2], // Gênero
                            Integer.parseInt(partes[3]), // Ano
                            partes[4], // Autor
                            partes[5] // Editora
                    );
                    conteudos.add(livro);
                    conteudoMap.put(livro.getTitulo(), livro);
                }
                case "Avaliacao" -> {
                    String emailUsuario = partes[1];
                    int nota = Integer.parseInt(partes[2]);
                    String comentario = partes[3];
                    String tituloConteudo = partes[4];

                    Conteudo conteudo = conteudoMap.get(tituloConteudo);
                    if (conteudo != null) {
                        Usuario usuario = new Usuario("Desconhecido", emailUsuario);
                        Avaliacao avaliacao = new Avaliacao(usuario, nota, comentario);
                        conteudo.adicionarAvaliacao(avaliacao);
                    } else {
                        System.err.println("Conteúdo não encontrado para avaliação: " + tituloConteudo);
                    }
                }
                default -> throw new IllegalArgumentException("Tipo desconhecido no arquivo: " + tipo);
            }
        }
    }



    public List<Conteudo> pesquisarPorTitulo(String titulo) {
        return conteudos.stream()
                .filter(c -> c.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
    }



}
