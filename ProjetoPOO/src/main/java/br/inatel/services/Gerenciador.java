package br.inatel.services;

import br.inatel.models.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Gerenciador {
    private List<Conteudo> conteudos;

    public Gerenciador() {
        this.conteudos = new ArrayList<>();
    }

    public boolean contemConteudo(String titulo) {
        return conteudos.stream()
                .anyMatch(c -> c.getTitulo().equalsIgnoreCase(titulo));
    }

    public List<Conteudo> recomendarPorGenero(String genero) {
        return conteudos.stream()
                .filter(c -> c.getGenero().equalsIgnoreCase(genero))
                .sorted(Comparator.comparingDouble(Conteudo::getNotaMedia).reversed())
                .collect(Collectors.toList());
    } //stream, filtra por gênero e usa o case INsensitive, ordena os dados de forma decrescente e transforma o resultado em lista

    public List<Conteudo> recomendarTop(int topN) {
        return conteudos.stream()
                .sorted(Comparator.comparingDouble(Conteudo::getNotaMedia).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    public void adicionarConteudo(Conteudo conteudo) {
        conteudos.add(conteudo);
    }

    public void salvarConteudosComoTexto(String caminho) throws IOException {
        List<String> linhas = new ArrayList<>(); //Cada string da lista representará uma linha a ser escrita no arquivo

        for (Conteudo conteudo : conteudos) {
            // Identificar o tipo de conteúdo
            String tipo = conteudo.getTipo();

            // Formatando a linha do conteúdo com base no tipo
            String linhaConteudo = switch (tipo) {
                //string format formata as strings conforme o modelo especificado, placeholders: %d para int, %s para string
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
            linhas.add(linhaConteudo); //adiciona linhas formatadas na lista

            //Adicionando as avaliações associadas ao conteúdo
            for (Avaliacao avaliacao : conteudo.getAvaliacoes()) {
                String linhaAvaliacao = String.format("Avaliacao;%s;%d;%s;%s",
                        avaliacao.getUsuario().getNome(),
                        avaliacao.getNota(),
                        avaliacao.getUsuario().getEmail(),
                        avaliacao.getComentario());
                linhas.add(linhaAvaliacao);
            }
        }

        //Caminho do arquivo e gravação
        Path path = Paths.get(caminho);
        try {
            Files.createDirectories(path.getParent()); // Garante que o diretório existe
            Files.write(path, linhas); //Grava os dados no arquivo
        } catch (IOException e) {
            System.err.println("Erro ao salvar os dados: " + e.getMessage());
        }
    }

    public void carregarConteudosDeTexto(String caminhoArquivo) throws IOException {
        Path path = Paths.get(caminhoArquivo); //recebendo caminho do arquivo
        if (!Files.exists(path)) {
            throw new IOException("Arquivo de dados não encontrado.");
        }

        List<String> linhas = Files.readAllLines(path); //Faz a leitura para a lista completa
        Conteudo conteudoAtual = null;
        for (String linha : linhas) {
            if (linha.startsWith("Avaliacao;")) {
                if (conteudoAtual != null) {
                    Avaliacao avaliacao = linhaParaAvaliacao(linha); //Cria uma avaliação
                    conteudoAtual.adicionarAvaliacao(avaliacao); //Adiciona avaliação na lista
                }
            } else {
                conteudoAtual = linhaParaConteudo(linha); //Cria um conteúdo
                if (conteudoAtual != null) {
                    adicionarConteudo(conteudoAtual); //Adiciona na lista de conteudo
                }
            }
        }
    }

    private Avaliacao linhaParaAvaliacao(String linha) {
        String[] partes = linha.split(";"); //Divide a linha em partes ao encontrar um ";"
        if (partes.length != 5) {
            throw new IllegalArgumentException("Formato de linha de avaliação inválido: " + linha);
        }

        String nomeUsuario = partes[1];
        String emailUsuario = partes[3];
        int nota = Integer.parseInt(partes[2]); //Cast de string para int
        String comentario = partes[4];
        Usuario usuario = new Usuario(nomeUsuario, emailUsuario);
        return new Avaliacao(usuario, nota, comentario);
    }

    private Conteudo linhaParaConteudo(String linha) {
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

    public boolean removerConteudo(String titulo) {
        return conteudos.removeIf(c -> c.getTitulo().equalsIgnoreCase(titulo));
    } //usa a API collections do java para tentar remover, retorna true se conseguir

    public List<Conteudo> pesquisarPorTitulo(String titulo) {
        return conteudos.stream()
                .filter(c -> c.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
    } //Faz a pesquisa de um conteúdo completo ou parcial, e devolve o resultado como uma lista


    public List<Conteudo> getConteudos() {
        return conteudos;
    }

}