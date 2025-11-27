package distributed;

import java.io.Serializable;

/**
 * Classe que representa a resposta do servidor.
 * Contém o vetor já ordenado que será enviado de volta para o cliente.
 */
public class Resposta extends Comunicado implements Serializable {
    private static final long serialVersionUID = 3L;
    
    // O vetor já ordenado
    private final int[] vetorOrdenado;
    
    /**
     * Construtor que recebe o vetor ordenado.
     * @param vetorOrdenado o vetor já ordenado
     */
    public Resposta(int[] vetorOrdenado) {
        this.vetorOrdenado = vetorOrdenado;
    }
    
    /**
     * Retorna o vetor ordenado.
     * @return o vetor ordenado
     */
    public int[] getVetorOrdenado() {
        return vetorOrdenado;
    }
}
