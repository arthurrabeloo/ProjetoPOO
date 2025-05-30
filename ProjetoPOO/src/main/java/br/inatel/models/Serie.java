package br.inatel.models;

public class Serie extends Conteudo {
    private int temporadas;
    private int episodiosTotais;

    public Serie(String titulo, String genero, int anoLancamento, int temporadas, int episodiosTotais) {
        super(titulo, genero, anoLancamento);
        this.temporadas = temporadas;
        this.episodiosTotais = episodiosTotais;
    }

    @Override
    public String getTipo() {
        return "Série";
    }

    // Getters
    public int getTemporadas() { return temporadas; }
    public int getEpisodiosTotais() { return episodiosTotais; }
}

