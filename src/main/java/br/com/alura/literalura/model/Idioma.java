package br.com.alura.literalura.model;

public enum Idioma {
    ES("es", "Espanhol"),
    EN("en", "Inglês"),
    FR("fr", "Francês"),
    PT("pt", "Português");

    private String sigla;
    private String nomePortugues;

    Idioma(String sigla, String nomePortugues){
        this.sigla = sigla;
        this.nomePortugues = nomePortugues;
    }

    public static Idioma fromString(String text) {
        for (Idioma idioma : Idioma.values()) {
            if (idioma.sigla.equalsIgnoreCase(text)) {
                return idioma;
            }
        }
        throw new IllegalArgumentException("Nenhum idioma encontrado para a string fornecida: " + text);
    }

    public String getNomePortugues(){
        return nomePortugues;
    }
}
