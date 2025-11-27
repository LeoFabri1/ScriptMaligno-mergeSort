package distributed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe utilitária para fazer logs (registros) das operações.
 * 
 * Usa System.out.println para imprimir mensagens com timestamp.
 * Facilita o debug e acompanhamento do que está acontecendo.
 */
public final class Log {
    
    // Formato da data/hora: ano-mês-dia hora:minuto:segundo
    private static final DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Construtor privado para não permitir criar instâncias
    private Log() {
    }
    
    /**
     * Imprime uma mensagem informativa.
     * @param tag identificador de quem está logando (ex: "D" para Distribuidor, "R" para Receptor)
     * @param msg a mensagem a ser impressa
     */
    public static void info(String tag, String msg) {
        String dataHora = LocalDateTime.now().format(formato);
        System.out.println("[" + tag + "] " + dataHora + " — " + msg);
    }
    
    /**
     * Imprime uma mensagem de aviso (warning).
     * Usado para situações que podem ser problemas mas não impedem a execução.
     * @param tag identificador
     * @param msg a mensagem
     */
    public static void warn(String tag, String msg) {
        String dataHora = LocalDateTime.now().format(formato);
        System.out.println("[" + tag + "][WARN] " + dataHora + " — " + msg);
    }
    
    /**
     * Imprime uma mensagem de erro.
     * @param tag identificador
     * @param msg a mensagem
     * @param erro a exceção que ocorreu (pode ser null)
     */
    public static void error(String tag, String msg, Throwable erro) {
        String dataHora = LocalDateTime.now().format(formato);
        System.out.println("[" + tag + "][ERRO] " + dataHora + " — " + msg);
        
        // Se tiver uma exceção, imprimir o stack trace
        if (erro != null) {
            erro.printStackTrace(System.out);
        }
    }
}
