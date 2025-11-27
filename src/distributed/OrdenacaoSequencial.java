package distributed;

import java.util.Arrays;
import java.util.Random;

/**
 * Programa de referência para ordenação sequencial.
 * 
 * Este programa ordena um vetor usando Arrays.sort() (método do Java).
 * Serve para comparar o tempo de execução com a versão distribuída.
 * 
 * Uso: java OrdenacaoSequencial [TAMANHO]
 * Exemplo: java OrdenacaoSequencial 100000
 */
public class OrdenacaoSequencial {
    
    public static void main(String[] args) {
        // Tamanho padrão do vetor
        int tamanho = 10000000;  // 10 milhões
        
        // Ler tamanho dos argumentos (se fornecido)
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                tamanho = Integer.parseInt(args[i]);
            }
        }
        
        Log.info("SEQ", "Gerando vetor com " + tamanho + " elementos...");
        
        // Gerar vetor aleatório
        Random random = new Random();
        int[] vetor = new int[tamanho];
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = random.nextInt(201) - 100;  // números de -100 a 100
        }
        
        Log.info("SEQ", "Vetor gerado");
        
        // Medir tempo de ordenação
        medirOrdenacao(vetor);
    }
    
    /**
     * Mede o tempo de ordenação usando Arrays.sort().
     */
    private static void medirOrdenacao(int[] vetor) {
        // Criar cópia do vetor (para não modificar o original)
        int[] copia = new int[vetor.length];
        for (int i = 0; i < vetor.length; i++) {
            copia[i] = vetor[i];
        }
        
        // Marcar início do tempo
        long inicio = System.nanoTime();
        
        // Ordenar usando método do Java
        Arrays.sort(copia);
        
        // Marcar fim do tempo
        long fim = System.nanoTime();
        
        // Calcular tempo em milissegundos
        double tempoMs = (fim - inicio) / 1_000_000.0;
        
        // Verificar se está ordenado
        boolean estaOrdenado = true;
        for (int i = 1; i < copia.length; i++) {
            if (copia[i] < copia[i - 1]) {
                estaOrdenado = false;
                break;
            }
        }
        
        // Mostrar resultado
        Log.info("SEQ", "Tempo de ordenação: " + String.format("%.2f", tempoMs) + " ms");
        if (estaOrdenado) {
            Log.info("SEQ", "Vetor está ordenado corretamente");
        } else {
            Log.info("SEQ", "ERRO: Vetor não está ordenado!");
        }
    }
}
