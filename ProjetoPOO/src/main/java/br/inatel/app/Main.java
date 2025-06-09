package br.inatel.app;

import br.inatel.services.Gerenciador;
import br.inatel.ui.InterfaceUsuario;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final String ARQUIVO_DADOS = "dados/conteudos.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Gerenciador gerenciador = new Gerenciador();

        // Carregar dados salvos (se existir)
        try {
            gerenciador.carregarConteudosDeTexto(ARQUIVO_DADOS);
            System.out.println("Dados carregados com sucesso!");
        } catch (IOException e) {
            System.out.println("Nenhum dado salvo encontrado, iniciando com dados vazios.");
        }

        // Inicializar UI
        InterfaceUsuario ui = new InterfaceUsuario(scanner, gerenciador);

        // Inicia interação com o usuário
        ui.iniciar();

        // Salvar dados ao final da execução
        try {
            gerenciador.salvarConteudosComoTexto(ARQUIVO_DADOS);
            System.out.println("Dados salvos com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }

        scanner.close();
    }

}