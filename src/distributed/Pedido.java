package distributed;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Classe que representa um pedido de ordenação.
 * Contém um vetor de números inteiros que precisa ser ordenado.
 * Pode ordenar o vetor usando Merge Sort.
 */
public class Pedido extends Comunicado implements Serializable {
    private static final long serialVersionUID = 2L;
    
    // O vetor que precisa ser ordenado
    private final int[] numeros;
    
    /**
     * Construtor que recebe o vetor a ser ordenado.
     * @param numeros o vetor de números inteiros
     */
    public Pedido(int[] numeros) {
        this.numeros = numeros;
    }
    
    /**
     * Retorna o vetor de números.
     * @return o vetor
     */
    public int[] getNumeros() {
        return numeros;
    }
    
    /**
     * Ordena o vetor usando Merge Sort.
     * Cria uma cópia do vetor original para não modificar o original.
     * @return o vetor ordenado
     */
    public int[] ordenar() {
        // Se o vetor está vazio, retorna vetor vazio
        if (numeros == null || numeros.length == 0) {
            return new int[0];
        }
        
        // Criar uma cópia para não modificar o original
        int[] copia = Arrays.copyOf(numeros, numeros.length);
        
        // Chamar o merge sort recursivo
        mergeSort(copia, 0, copia.length - 1);
        
        return copia;
    }
    
    /**
     * Merge Sort recursivo.
     * Divide o vetor ao meio, ordena cada metade e depois faz o merge.
     * @param vetor o vetor a ser ordenado
     * @param inicio índice inicial
     * @param fim índice final
     */
    private void mergeSort(int[] vetor, int inicio, int fim) {
        // Caso base: se inicio >= fim, não há nada para ordenar
        if (inicio >= fim) {
            return;
        }
        
        // Calcular o meio do vetor
        int meio = (inicio + fim) / 2;
        
        // Ordenar a metade esquerda
        mergeSort(vetor, inicio, meio);
        
        // Ordenar a metade direita
        mergeSort(vetor, meio + 1, fim);
        
        // Fazer o merge das duas metades ordenadas
        merge(vetor, inicio, meio, fim);
    }
    
    /**
     * Faz o merge (intercalação) de duas partes ordenadas do vetor.
     * A parte esquerda vai de 'inicio' até 'meio'.
     * A parte direita vai de 'meio+1' até 'fim'.
     * @param vetor o vetor completo
     * @param inicio início da parte esquerda
     * @param meio fim da parte esquerda (e início da direita - 1)
     * @param fim fim da parte direita
     */
    private void merge(int[] vetor, int inicio, int meio, int fim) {
        // Criar arrays temporários para as duas partes
        int tamanhoEsquerda = meio - inicio + 1;
        int tamanhoDireita = fim - meio;
        
        int[] esquerda = new int[tamanhoEsquerda];
        int[] direita = new int[tamanhoDireita];
        
        // Copiar os elementos para os arrays temporários
        for (int i = 0; i < tamanhoEsquerda; i++) {
            esquerda[i] = vetor[inicio + i];
        }
        for (int j = 0; j < tamanhoDireita; j++) {
            direita[j] = vetor[meio + 1 + j];
        }
        
        // Fazer o merge: comparar elementos e colocar no lugar certo
        int i = 0;  // índice para array esquerda
        int j = 0;  // índice para array direita
        int k = inicio;  // índice para o vetor original
        
        // Comparar elementos dos dois arrays e colocar o menor no vetor
        while (i < tamanhoEsquerda && j < tamanhoDireita) {
            if (esquerda[i] <= direita[j]) {
                vetor[k] = esquerda[i];
                i++;
            } else {
                vetor[k] = direita[j];
                j++;
            }
            k++;
        }
        
        // Copiar elementos restantes da esquerda (se houver)
        while (i < tamanhoEsquerda) {
            vetor[k] = esquerda[i];
            i++;
            k++;
        }
        
        // Copiar elementos restantes da direita (se houver)
        while (j < tamanhoDireita) {
            vetor[k] = direita[j];
            j++;
            k++;
        }
    }
}
