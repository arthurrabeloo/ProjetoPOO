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
        List<String> linhas = new ArrayList<>(); //cada string da lista representa uma linha a ser escrita

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
                        avaliacao.getUsuario(), //usuario tem nome, email e suas avaliacoes
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
        } catch (IOException e) {
            System.err.println("Erro ao salvar os dados: " + e.getMessage());
            throw e; // Lança novamente a exceção para que possa ser tratada em nível superior
        }
    }

    public boolean removerConteudo(String titulo) {
        return conteudos.removeIf(c -> c.getTitulo().equalsIgnoreCase(titulo));
    }

    public List<Conteudo> pesquisarPorTitulo(String titulo) {
        return conteudos.stream()
                .filter(c -> c.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
    }



}
