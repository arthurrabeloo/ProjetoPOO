package br.inatel.models;

public class Filme extends Conteudo {
    private String diretor;
    private int duracao;

    public Filme(String titulo, String genero, int anoLancamento, String diretor, int duracaoMinutos) {
        super(titulo, genero, anoLancamento);
        this.diretor = diretor;
        this.duracao = duracaoMinutos;
    }

    @Override
    public String getTipo() {
        return "Filme";
    }

    // Getters
    public String getDiretor() { return diretor; }
    public int getDuracao() { return duracao; }
}

