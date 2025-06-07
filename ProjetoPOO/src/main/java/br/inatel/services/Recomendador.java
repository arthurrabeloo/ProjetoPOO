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
            String tipo = conteudo.getTipo();
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

            for (Avaliacao avaliacao : conteudo.getAvaliacoes()) {
                String linhaAvaliacao = String.format("Avaliacao;%s;%d;%s",
                        avaliacao.getUsuario().getEmail(),
                        avaliacao.getNota(),
                        avaliacao.getComentario());
                linhas.add(linhaAvaliacao);
            }
        }

        Path path = Paths.get(caminho);
        Files.createDirectories(path.getParent());
        Files.write(path, linhas);
    }


    public void carregarConteudosDeTexto(String caminho) throws IOException {
        Path path = Paths.get(caminho);

        if (!Files.exists(path)) {
            throw new IOException("Arquivo não encontrado: " + caminho);
        }

        List<String> linhas = Files.readAllLines(path);
        conteudos.clear();

        Conteudo conteudoAtual = null;
        for (String linha : linhas) {
            String[] partes = linha.split(";");
            String tipo = partes[0];

            switch (tipo) {
                case "Filme" -> {
                    conteudoAtual = new Filme(partes[1], partes[2], Integer.parseInt(partes[3]), partes[4], Integer.parseInt(partes[5]));
                    conteudos.add(conteudoAtual);
                }
                case "Serie" -> {
                    conteudoAtual = new Serie(partes[1], partes[2], Integer.parseInt(partes[3]), Integer.parseInt(partes[4]), Integer.parseInt(partes[5]));
                    conteudos.add(conteudoAtual);
                }
                case "Livro" -> {
                    conteudoAtual = new Livro(partes[1], partes[2], Integer.parseInt(partes[3]), partes[4], partes[5]);
                    conteudos.add(conteudoAtual);
                }
                case "Avaliacao" -> {
                    if (conteudoAtual != null) {
                        Usuario usuario = new Usuario("Desconhecido", partes[1]);
                        Avaliacao avaliacao = new Avaliacao(usuario, Integer.parseInt(partes[2]), partes[3]);
                        conteudoAtual.adicionarAvaliacao(avaliacao);
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
