package distributed;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Programa D (Distribuidor) - Cliente que coordena a ordenação distribuída.
 * 
 * Este programa:
 * 1. Gera um vetor aleatório
 * 2. Divide o vetor em partes
 * 3. Envia cada parte para um servidor diferente
 * 4. Recebe as partes ordenadas
 * 5. Faz o merge de todas as partes
 * 6. Verifica se está ordenado
 * 
 * Uso: java Distribuidor servidor1:porta1 servidor2:porta2 ... --tam TAMANHO
 * Exemplo: java Distribuidor 127.0.0.1:12345 127.0.0.1:12346 --tam 100000
 */
public class Distribuidor {
    
    public static void main(String[] args) throws Exception {
        // Verificar se tem argumentos
        if (args.length == 0) {
            System.out.println("Uso: java Distribuidor servidor1:porta1 servidor2:porta2 ... --tam TAMANHO");
            System.out.println("Exemplo: java Distribuidor 127.0.0.1:12345 127.0.0.1:12346 --tam 100000");
            return;
        }
        
        // Variáveis para armazenar os argumentos
        List<String> servidores = new ArrayList<String>();
        int tamanhoVetor = 1000000;  // tamanho padrão: 1 milhão
        
        // Ler argumentos da linha de comando
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equals("--tam")) {
                // Próximo argumento é o tamanho
                i++;
                tamanhoVetor = Integer.parseInt(args[i]);
            } else if (arg.contains(":")) {
                // É um servidor no formato host:porta
                servidores.add(arg);
            }
        }
        
        // Verificar se tem pelo menos um servidor
        if (servidores.isEmpty()) {
            System.out.println("Erro: nenhum servidor informado!");
            return;
        }
        
        Log.info("D", "Iniciando distribuidor...");
        Log.info("D", "Servidores: " + servidores.size());
        Log.info("D", "Tamanho do vetor: " + tamanhoVetor);
        
        // Gerar vetor aleatório
        Random random = new Random();
        int[] vetor = new int[tamanhoVetor];
        for (int i = 0; i < tamanhoVetor; i++) {
            vetor[i] = random.nextInt(201) - 100;  // números de -100 a 100
        }
        
        Log.info("D", "Vetor gerado com " + tamanhoVetor + " elementos");
        
        // Conectar com todos os servidores
        List<Conexao> conexoes = new ArrayList<Conexao>();
        for (String servidor : servidores) {
            String[] partes = servidor.split(":");
            String host = partes[0];
            int porta = Integer.parseInt(partes[1]);
            
            Conexao conexao = new Conexao(host, porta);
            conexao.conectar();
            conexoes.add(conexao);
        }
        
        Log.info("D", "Conectado a " + conexoes.size() + " servidor(es)");
        
        // Executar a ordenação distribuída
        executarOrdenacao(conexoes, vetor);
        
        // Encerrar conexões
        Log.info("D", "Encerrando conexões...");
        for (Conexao conexao : conexoes) {
            try {
                conexao.enviarEncerramento();
            } catch (Exception e) {
                Log.error("D", "Erro ao encerrar conexão", e);
            }
            conexao.fechar();
        }
        
        Log.info("D", "Fim do programa");
    }
    
    /**
     * Executa a ordenação distribuída.
     * Divide o vetor, envia para servidores, recebe resultados e faz merge.
     */
    private static void executarOrdenacao(List<Conexao> conexoes, int[] vetor) throws Exception {
        Log.info("D", "Iniciando ordenação distribuída...");
        
        int numServidores = conexoes.size();
        int tamanhoParte = vetor.length / numServidores;  // tamanho de cada parte
        
        // Array para guardar as partes ordenadas que vêm dos servidores
        int[][] partesOrdenadas = new int[numServidores][];
        
        // Array de threads para enviar pedidos em paralelo
        Thread[] threads = new Thread[numServidores];
        
        // Marcar início do tempo
        long tempoInicio = System.nanoTime();
        
        // Para cada servidor, criar uma thread que envia uma parte do vetor
        for (int i = 0; i < numServidores; i++) {
            final int indiceServidor = i;
            
            // Calcular qual parte do vetor este servidor vai ordenar
            int inicio = i * tamanhoParte;
            int fim;
            if (i == numServidores - 1) {
                // Último servidor pega o resto (caso a divisão não seja exata)
                fim = vetor.length;
            } else {
                fim = inicio + tamanhoParte;
            }
            
            // Copiar a parte do vetor para um novo array
            int[] parte = new int[fim - inicio];
            for (int j = 0; j < parte.length; j++) {
                parte[j] = vetor[inicio + j];
            }
            
            final int[] parteFinal = parte;
            final Conexao conexao = conexoes.get(i);
            
            // Criar thread para enviar pedido e receber resposta
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Criar pedido com a parte do vetor
                        Pedido pedido = new Pedido(parteFinal);
                        
                        // Enviar pedido e receber resposta
                        Resposta resposta = conexao.enviarPedido(pedido);
                        
                        if (resposta != null && resposta.getVetorOrdenado() != null) {
                            partesOrdenadas[indiceServidor] = resposta.getVetorOrdenado();
                            Log.info("D", "Recebida parte ordenada do servidor " + indiceServidor + 
                                    " com " + partesOrdenadas[indiceServidor].length + " elementos");
                        } else {
                            Log.warn("D", "Resposta inválida do servidor " + indiceServidor);
                            partesOrdenadas[indiceServidor] = new int[0];
                        }
                    } catch (Exception e) {
                        Log.error("D", "Erro ao comunicar com servidor " + indiceServidor, e);
                        partesOrdenadas[indiceServidor] = new int[0];
                    }
                }
            });
            
            threads[i].start();
        }
        
        // Aguardar todas as threads terminarem
        for (Thread thread : threads) {
            thread.join();
        }
        
        long tempoFim = System.nanoTime();
        double tempoDistribuido = (tempoFim - tempoInicio) / 1_000_000.0;  // converter para milissegundos
        
        Log.info("D", "Todas as partes foram ordenadas pelos servidores");
        Log.info("D", "Tempo de ordenação distribuída: " + String.format("%.2f", tempoDistribuido) + " ms");
        
        // Fazer merge de todas as partes ordenadas
        Log.info("D", "Fazendo merge das partes ordenadas...");
        long tempoMergeInicio = System.nanoTime();
        
        int[] resultadoFinal = fazerMergeMultiplos(partesOrdenadas);
        
        long tempoMergeFim = System.nanoTime();
        double tempoMerge = (tempoMergeFim - tempoMergeInicio) / 1_000_000.0;
        
        Log.info("D", "Merge concluído");
        Log.info("D", "Tempo de merge: " + String.format("%.2f", tempoMerge) + " ms");
        Log.info("D", "Tempo total: " + String.format("%.2f", tempoDistribuido + tempoMerge) + " ms");
        Log.info("D", "Vetor final tem " + resultadoFinal.length + " elementos");
        
        // Verificar se o vetor está ordenado
        boolean estaOrdenado = true;
        for (int i = 1; i < resultadoFinal.length; i++) {
            if (resultadoFinal[i] < resultadoFinal[i - 1]) {
                estaOrdenado = false;
                break;
            }
        }
        
        if (estaOrdenado) {
            Log.info("D", "✓ Vetor está CORRETAMENTE ordenado!");
        } else {
            Log.info("D", "✗ ERRO: Vetor NÃO está ordenado!");
        }
        
        // Comparar com ordenação sequencial
        Log.info("D", "Comparando com ordenação sequencial...");
        long tempoSeqInicio = System.nanoTime();
        
        int[] copia = new int[vetor.length];
        for (int i = 0; i < vetor.length; i++) {
            copia[i] = vetor[i];
        }
        Arrays.sort(copia);
        
        long tempoSeqFim = System.nanoTime();
        double tempoSequencial = (tempoSeqFim - tempoSeqInicio) / 1_000_000.0;
        
        Log.info("D", "Tempo sequencial (Arrays.sort): " + String.format("%.2f", tempoSequencial) + " ms");
    }
    
    /**
     * Faz merge de múltiplos vetores ordenados em um único vetor ordenado.
     * Usa abordagem recursiva: divide os arrays ao meio, faz merge de cada metade, depois merge final.
     */
    private static int[] fazerMergeMultiplos(int[][] arrays) {
        // Casos base
        if (arrays == null || arrays.length == 0) {
            return new int[0];
        }
        if (arrays.length == 1) {
            return arrays[0];
        }
        
        // Dividir ao meio
        int meio = arrays.length / 2;
        
        // Criar arrays para metade esquerda e direita
        int[][] esquerda = new int[meio][];
        int[][] direita = new int[arrays.length - meio][];
        
        for (int i = 0; i < meio; i++) {
            esquerda[i] = arrays[i];
        }
        for (int i = meio; i < arrays.length; i++) {
            direita[i - meio] = arrays[i];
        }
        
        // Fazer merge recursivo de cada metade
        int[] resultadoEsquerda = fazerMergeMultiplos(esquerda);
        int[] resultadoDireita = fazerMergeMultiplos(direita);
        
        // Fazer merge final das duas metades
        return fazerMergeDois(resultadoEsquerda, resultadoDireita);
    }
    
    /**
     * Faz merge de dois vetores ordenados em um único vetor ordenado.
     * Este é o algoritmo básico de merge/intercalação.
     */
    private static int[] fazerMergeDois(int[] vetor1, int[] vetor2) {
        // Criar array resultado com tamanho total
        int[] resultado = new int[vetor1.length + vetor2.length];
        
        int i = 0;  // índice para vetor1
        int j = 0;  // índice para vetor2
        int k = 0;  // índice para resultado
        
        // Comparar elementos e colocar o menor no resultado
        while (i < vetor1.length && j < vetor2.length) {
            if (vetor1[i] <= vetor2[j]) {
                resultado[k] = vetor1[i];
                i++;
            } else {
                resultado[k] = vetor2[j];
                j++;
            }
            k++;
        }
        
        // Copiar elementos restantes do vetor1 (se houver)
        while (i < vetor1.length) {
            resultado[k] = vetor1[i];
            i++;
            k++;
        }
        
        // Copiar elementos restantes do vetor2 (se houver)
        while (j < vetor2.length) {
            resultado[k] = vetor2[j];
            j++;
            k++;
        }
        
        return resultado;
    }
    
    /**
     * Classe interna para gerenciar conexão com um servidor.
     */
    private static class Conexao {
        private String host;
        private int porta;
        private Socket socket;
        private ObjectOutputStream saida;
        private ObjectInputStream entrada;
        
        public Conexao(String host, int porta) {
            this.host = host;
            this.porta = porta;
        }
        
        /**
         * Conecta com o servidor.
         */
        public void conectar() throws IOException {
            socket = new Socket(host, porta);
            saida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            Log.info("D", "Conectado a " + host + ":" + porta);
        }
        
        /**
         * Envia um pedido e recebe a resposta.
         */
        public Resposta enviarPedido(Pedido pedido) throws IOException, ClassNotFoundException {
            synchronized (this) {
                // Enviar pedido
                saida.writeObject(pedido);
                saida.flush();
                
                // Receber resposta
                Object resposta = entrada.readObject();
                
                if (resposta instanceof Resposta) {
                    return (Resposta) resposta;
                } else {
                    Log.warn("D", "Resposta inesperada de " + host + ":" + porta);
                    return null;
                }
            }
        }
        
        /**
         * Envia sinal de encerramento.
         */
        public void enviarEncerramento() throws IOException {
            synchronized (this) {
                saida.writeObject(new ComunicadoEncerramento());
                saida.flush();
            }
        }
        
        /**
         * Fecha a conexão.
         */
        public void fechar() {
            try {
                if (entrada != null) entrada.close();
                if (saida != null) saida.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Ignorar erros ao fechar
            }
            Log.info("D", "Conexão fechada: " + host + ":" + porta);
        }
        
        @Override
        public String toString() {
            return host + ":" + porta;
        }
    }
}
