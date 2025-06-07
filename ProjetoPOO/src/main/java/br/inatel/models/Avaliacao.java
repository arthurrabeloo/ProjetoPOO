package br.inatel.models;

import java.util.Date;

public class Avaliacao {
    private Usuario usuario;
    private int nota;
    private String comentario;
    private Date data;

    public Avaliacao(Usuario usuario, int nota, String comentario) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota deve estar entre 1 e 5.");
        }
        this.usuario = usuario;
        this.nota = nota;
        this.comentario = comentario;
        this.data = new Date();
    }

    // Getters
    public Usuario getUsuario() { return usuario; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }
    public Date getData() { return data; }
}

