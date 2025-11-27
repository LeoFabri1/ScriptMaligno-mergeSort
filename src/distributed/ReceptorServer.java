package distributed;

import java.io.*;
import java.net.*;

/**
 * Programa R (Receptor) - Servidor que recebe pedidos de ordenação.
 * 
 * Este servidor fica aguardando conexões de clientes.
 * Quando recebe um Pedido, ordena o vetor e retorna uma Resposta.
 * 
 * Uso: java ReceptorServer [host] [porta]
 * Exemplo: java ReceptorServer 0.0.0.0 12345
 */
public class ReceptorServer {
    
    public static void main(String[] args) {
        // Ler parâmetros da linha de comando
        String host = "0.0.0.0";  // padrão: aceita conexões de qualquer IP
        int porta = 12345;        // padrão: porta 12345
        
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            porta = Integer.parseInt(args[1]);
        }
        
        try {
            // Criar o socket servidor
            ServerSocket servidor = new ServerSocket();
            servidor.bind(new InetSocketAddress(host, porta));
            
            Log.info("R", "Servidor R ouvindo em " + host + ":" + porta);
            Log.info("R", "Aguardando conexões de clientes...");
            
            // Loop infinito para aceitar múltiplas conexões
            while (true) {
                // Aceitar uma conexão (bloqueia até chegar um cliente)
                Socket conexao = servidor.accept();
                
                Log.info("R", "Conexão aceita de " + conexao.getRemoteSocketAddress());
                
                // Criar uma thread para atender este cliente
                // Assim podemos atender múltiplos clientes ao mesmo tempo
                Thread thread = new Thread(new Atendedor(conexao));
                thread.start();
            }
            
        } catch (IOException e) {
            Log.error("R", "Erro ao iniciar servidor", e);
        }
    }
    
    /**
     * Classe interna que atende um cliente específico.
     * Cada cliente tem sua própria thread.
     */
    private static class Atendedor implements Runnable {
        private Socket socket;
        
        public Atendedor(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                // Criar streams para enviar e receber objetos
                ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                
                // Loop para receber múltiplos pedidos do mesmo cliente
                while (true) {
                    // Ler objeto recebido
                    Object objeto = entrada.readObject();
                    
                    // Verificar o tipo do objeto
                    if (objeto instanceof Pedido) {
                        // É um pedido de ordenação
                        Pedido pedido = (Pedido) objeto;
                        
                        Log.info("R", "Pedido recebido de " + socket.getRemoteSocketAddress() + 
                                " — tamanho: " + pedido.getNumeros().length);
                        
                        // Ordenar o vetor
                        int[] vetorOrdenado = pedido.ordenar();
                        
                        // Criar resposta com o vetor ordenado
                        Resposta resposta = new Resposta(vetorOrdenado);
                        
                        // Enviar resposta de volta para o cliente
                        saida.writeObject(resposta);
                        saida.flush();  // garantir que foi enviado
                        
                        Log.info("R", "Resposta enviada para " + socket.getRemoteSocketAddress());
                        
                    } else if (objeto instanceof ComunicadoEncerramento) {
                        // Cliente quer encerrar a conexão
                        Log.warn("R", "Encerramento recebido de " + socket.getRemoteSocketAddress());
                        break;  // sair do loop
                        
                    } else {
                        // Objeto desconhecido
                        Log.warn("R", "Objeto desconhecido recebido: " + objeto.getClass().getSimpleName());
                    }
                }
                
                // Fechar streams
                entrada.close();
                saida.close();
                
            } catch (EOFException e) {
                // Cliente fechou a conexão normalmente
                Log.warn("R", "Cliente fechou a conexão: " + socket.getRemoteSocketAddress());
                
            } catch (IOException e) {
                Log.error("R", "Erro na conexão com cliente", e);
                
            } catch (ClassNotFoundException e) {
                Log.error("R", "Erro ao ler objeto", e);
                
            } finally {
                // Sempre fechar o socket, mesmo se der erro
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignorar erro ao fechar
                }
                Log.info("R", "Conexão encerrada: " + socket.getRemoteSocketAddress());
            }
        }
    }
}
