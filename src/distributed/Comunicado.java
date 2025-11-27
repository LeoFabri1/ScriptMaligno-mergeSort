package distributed;

import java.io.Serializable;

/**
 * Classe base para todas as mensagens trocadas entre cliente e servidor.
 * 
 * Todas as classes de mensagem (Pedido, Resposta, ComunicadoEncerramento)
 * devem estender esta classe.
 * 
 * Implementa Serializable para poder ser enviada pela rede.
 */
public class Comunicado implements Serializable {
    private static final long serialVersionUID = 1L;
    // Classe vazia, serve apenas como marcador comum
}
