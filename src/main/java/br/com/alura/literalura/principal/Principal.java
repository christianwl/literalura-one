package br.com.alura.literalura.principal;

import br.com.alura.literalura.model.*;
import br.com.alura.literalura.repository.AutorRepository;
import br.com.alura.literalura.repository.LivroRepository;
import br.com.alura.literalura.service.ConsumoApi;
import br.com.alura.literalura.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {
    private final String ENDERECO = "https://gutendex.com/books/?search=";
    private ConverteDados conversor = new ConverteDados();
    private ConsumoApi consumo = new ConsumoApi();
    private Scanner scanner = new Scanner(System.in);

    private List<Livro> livros = new ArrayList<>();
    private List<Autor> autores = new ArrayList<>();

    @Autowired
    private LivroRepository livroRepositorio;

    @Autowired
    private AutorRepository autorRepositorio;

    public void exibeMenu() {
        String textoMenu = """
                \n------------------
                Escolha o número de sua opção:
                1- Buscas livro pelo Título
                2- Listar livros registrados
                3- Listar autores registrados
                4- Listar autores vivos em um determinado ano
                5- Listar livros em um determinado idioma
                
                0- Sair
                """;

        var opcao = -1;
        do {
            System.out.println(textoMenu);
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    buscarLivroPorTitulo();
                    break;
                case 2:
                    listarLivros();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    listarAutoresVivosPorAno();
                    break;
                case 5:
                    buscarLivrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente...");
            }
        } while (opcao != 0);
    }

    private DadosConteudo getDadosConteudo(String nomeLivro) {
        String nomeLivroCodificado = URLEncoder.encode(nomeLivro, StandardCharsets.UTF_8);

        var json = this.consumo.obterDados(ENDERECO + nomeLivroCodificado);
        var dados = conversor.obterDados(json, DadosConteudo.class);

        return dados;
    }

    private void buscarLivroPorTitulo() {
        System.out.println("Insira o nome do livro que você deseja procurar");
        String nomeLivro = scanner.nextLine();

        DadosConteudo dadosBuscado = getDadosConteudo(nomeLivro);

        if(!dadosBuscado.livros().isEmpty()){
            DadosLivro dadosLivro = dadosBuscado.livros().getFirst();
            Livro livro = new Livro(dadosLivro);

            List<Autor> autoresLivro = dadosLivro.autores().stream()
                    .map(a -> autorRepositorio.findByNome(a.nome())
                            .orElseGet(() -> autorRepositorio.save(new Autor(a))))
                    .collect(Collectors.toList());

            livro.setAutores(autoresLivro);
            salvarLivroNoBanco(livro);

            System.out.println(livro);
        } else {
            System.out.println("Nenhum livro encontrado com o nome: " + nomeLivro);
        }

    }

    private void salvarLivroNoBanco(Livro livro) {
        livroRepositorio.save(livro);
    }

    private void listarLivros() {
        this.livros = livroRepositorio.findAll();
        exibirLista(livros);
    }

    private void listarAutores() {
        this.autores = autorRepositorio.findAll();
        exibirLista(autores);
    }

    private void listarAutoresVivosPorAno() {
        System.out.println("Insira o ano que deseja pesquisar");
        var ano = scanner.nextInt();
        this.autores = autorRepositorio.buscarAutoresVivoPorAno(ano);
        exibirLista(autores);
        if(autores.isEmpty()) System.out.println("Nenhum autor encontrado para essa busca");
    }

    private void buscarLivrosPorIdioma() {
        var textoIdiomas = "Insira o idioma para realizar a busca:\n";
        for(Idioma idioma : Idioma.values()){
            textoIdiomas += idioma.name().toLowerCase() + "- " + idioma.getNomePortugues() + '\n';
        }
        System.out.println(textoIdiomas);

        var idioma = scanner.nextLine();
        try {
            String idiomaEscolhido = Idioma.fromString(idioma).name();
            livros = livroRepositorio.buscaLivrosPorIdioma("pt");

            exibirLista(livros);
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }

    private <T> void exibirLista(List<T> lista){
        lista.forEach(System.out::println);
    }
}
