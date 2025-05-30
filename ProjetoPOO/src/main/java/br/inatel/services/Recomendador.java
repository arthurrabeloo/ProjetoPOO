package br.inatel.services;

import br.inatel.models.*;
import java.io.*;
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

    public void salvarEmArquivo(String caminho) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(conteudos);
        }
    }

    @SuppressWarnings("unchecked")
    public void carregarDeArquivo(String caminho) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            conteudos = (List<Conteudo>) ois.readObject();
        }
    }

    public List<Conteudo> getConteudos() {
        return conteudos;
    }
}

