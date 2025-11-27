package distributed;

import java.io.Serializable;

/**
 * Mensagem especial que indica que não haverá mais comunicação.
 * 
 * Quando o cliente envia esta mensagem, o servidor sabe que deve
 * encerrar a conexão e parar de esperar por mais pedidos.
 */
public class ComunicadoEncerramento extends Comunicado implements Serializable {
    private static final long serialVersionUID = 4L;
    // Classe vazia, serve apenas para sinalizar encerramento
}
