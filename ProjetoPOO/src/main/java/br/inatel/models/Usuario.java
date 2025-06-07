package br.inatel.models;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nome;
    private String email;
    private List<Avaliacao> avaliacoes;

    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
        this.avaliacoes = new ArrayList<>();
    }

    public void avaliar(Conteudo conteudo, int nota, String comentario) {
        Avaliacao avaliacao = new Avaliacao(this, nota, comentario);
        conteudo.adicionarAvaliacao(avaliacao);
        avaliacoes.add(avaliacao);
    }

    // Getters
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public List<Avaliacao> getAvaliacoes() { return avaliacoes; }
}

