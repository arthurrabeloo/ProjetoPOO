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
            String linha = switch (tipo) {
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
            linhas.add(linha);
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

        for (String linha : linhas) {
            String[] partes = linha.split(";");
            String tipo = partes[0];
            Conteudo conteudo = switch (tipo) {
                case "Filme" -> new Filme(partes[1], partes[2], Integer.parseInt(partes[3]), partes[4], Integer.parseInt(partes[5]));
                case "Serie" -> new Serie(partes[1], partes[2], Integer.parseInt(partes[3]), Integer.parseInt(partes[4]), Integer.parseInt(partes[5]));
                case "Livro" -> new Livro(partes[1], partes[2], Integer.parseInt(partes[3]), partes[4], partes[5]);
                default -> throw new IllegalArgumentException("Tipo desconhecido no arquivo: " + tipo);
            };
            conteudos.add(conteudo);
        }
    }
}
