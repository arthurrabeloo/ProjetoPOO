package br.inatel.models;

public class Livro extends Conteudo {
    private String autor;
    private String editora;

    public Livro(String titulo, String genero, int anoLancamento, String autor, String editora) {
        super(titulo, genero, anoLancamento);
        this.autor = autor;
        this.editora = editora;
    }

    @Override
    public String getTipo() {
        return "Livro";
    }

    // Getters
    public String getAutor() { return autor; }
    public String getEditora() { return editora; }
}

