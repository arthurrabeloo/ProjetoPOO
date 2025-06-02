package br.inatel.app;

import br.inatel.models.*;
import br.inatel.services.Recomendador;
import br.inatel.ui.InterfaceUsuario;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Recomendador recomendador = new Recomendador();
        InterfaceUsuario ui = new InterfaceUsuario(scanner, recomendador);

        // Exemplos de inicialização de dados
        Usuario usuario1 = new Usuario("João", "joao@email.com");
        Usuario usuario2 = new Usuario("Maria", "maria@email.com");

        Livro livro1 = new Livro("Dom Quixote", "Aventura", 1605, "Miguel de Cervantes", "Penguin Books");
        Filme filme1 = new Filme("Inception", "Ficção", 2010, "Christopher Nolan", 148);
        Serie serie1 = new Serie("Breaking Bad", "Drama", 2008, 5, 62);

        recomendador.adicionarConteudo(livro1);
        recomendador.adicionarConteudo(filme1);
        recomendador.adicionarConteudo(serie1);

        usuario1.avaliar(livro1, 5, "Obra-prima da literatura.");
        usuario1.avaliar(filme1, 4, "Muito interessante e complexo.");
        usuario2.avaliar(serie1, 5, "Melhor série que já vi!");

        // Interação com o usuário
        ui.iniciar();

        // Salvando os dados
        try {
            recomendador.salvarEmArquivo("dados/recomendador.dat");
            System.out.println("Dados salvos com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }

        scanner.close();
        }
}