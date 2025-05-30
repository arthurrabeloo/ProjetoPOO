package br.inatel.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Conteudo implements Serializable {
    protected String titulo;
    protected String genero;
    protected int anoLancamento;
    protected List<Avaliacao> avaliacoes;

    public Conteudo(String titulo, String genero, int anoLancamento) {
        this.titulo = titulo;
        this.genero = genero;
        this.anoLancamento = anoLancamento;
        this.avaliacoes = new ArrayList<>();
    }

    public void adicionarAvaliacao(Avaliacao avaliacao) {
        avaliacoes.add(avaliacao);
    }

    public double getNotaMedia() {
        if (avaliacoes.isEmpty()) return 0.0;
        return avaliacoes.stream().mapToDouble(Avaliacao::getNota).average().orElse(0.0);
    }

    public abstract String getTipo();

    // Getters
    public String getTitulo() { return titulo; }
    public String getGenero() { return genero; }
    public int getAnoLancamento() { return anoLancamento; }
    public List<Avaliacao> getAvaliacoes() { return avaliacoes; }
}

