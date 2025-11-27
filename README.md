# Sistema Distribuído de Ordenação (Merge Sort)

## 📋 Visão Geral

Este projeto implementa um **sistema distribuído** para ordenação de vetores grandes usando **Merge Sort**, demonstrando conceitos de **programação paralela e distribuída** em Java. O sistema compara a performance entre processamento **sequencial** e **distribuído** para ordenar vetores de números inteiros.

## 🏗️ Arquitetura do Sistema

### Componentes Principais

#### 🖥️ **CLIENTE**
- **Distribuidor (D)** - Coordena todo o processamento distribuído

#### 🖥️ **SERVIDORES** 
- **ReceptorServer (R)** - Ordena partes do vetor em paralelo usando Merge Sort
- **Múltiplas instâncias** - Cada servidor ordena um bloco diferente

#### 📊 **REFERÊNCIA**
- **OrdenacaoSequencial** - Versão sequencial usando `Arrays.sort()` para comparação de performance

### Fluxo de Execução

```
CLIENTE (Distribuidor)          SERVIDORES (ReceptorServer)
     ↓                              ↓
1. Gera vetor grande           1. Ficam aguardando conexões
     ↓                              ↓
2. Divide vetor em blocos      2. Recebem pedidos de ordenação
     ↓                              ↓
3. Envia blocos para servidores 3. Ordenam em paralelo (Merge Sort)
     ↓                              ↓
4. Coleta partes ordenadas     4. Retornam vetores ordenados
     ↓                              ↓
5. Faz merge final             5. Aguardam próximos pedidos
     ↓
6. Compara com versão sequencial
```

## 📁 Estrutura do Código

### Classes de Comunicação (Compartilhadas)

#### `Comunicado.java`
```java
public class Comunicado implements Serializable {
    private static final long serialVersionUID = 1L;
}
```
- **Função**: Classe base para comunicação entre Cliente e Servidor
- **Características**: Implementa `Serializable` para transmissão via rede

#### `ComunicadoEncerramento.java`
```java
public class ComunicadoEncerramento extends Comunicado {
    private static final long serialVersionUID = 4L;
}
```
- **Função**: Sinaliza encerramento de conexões
- **Uso**: Enviado pelo **CLIENTE** para finalizar **SERVIDORES**

#### `Pedido.java`
```java
public class Pedido extends Comunicado {
    private final int[] numeros;
    
    public int[] ordenar() {
        // Merge Sort paralelo usando ExecutorService
        // Divide o vetor recursivamente e ordena em paralelo
    }
}
```
- **Função**: Contém o vetor a ser ordenado
- **Processamento**: Implementa Merge Sort paralelo usando `ExecutorService`
- **Algoritmo**: Divide e conquista com paralelização quando o tamanho é grande

#### `Resposta.java`
```java
public class Resposta extends Comunicado {
    private final int[] vetorOrdenado;
}
```
- **Função**: Retorna o vetor ordenado
- **Características**: Vetor completo ordenado pelo servidor

---

## 🖥️ **CÓDIGO DO CLIENTE**

### **Distribuidor.java** - Cliente Coordenador

#### **Responsabilidades do Cliente:**
- ✅ Gera vetor aleatório de tamanho configurável
- ✅ Conecta com múltiplos servidores simultaneamente
- ✅ Divide o vetor em blocos iguais para cada servidor
- ✅ Envia blocos para servidores em paralelo
- ✅ Coleta partes ordenadas de todos os servidores
- ✅ Faz merge final de todas as partes ordenadas
- ✅ Compara performance: distribuído vs sequencial
- ✅ Valida se o resultado está corretamente ordenado

#### **Características Técnicas:**
- **Threading**: Usa threads para comunicação paralela com servidores
- **Divisão Inteligente**: Calcula blocos de tamanho igual para cada servidor
- **Merge Recursivo**: Faz merge de múltiplos vetores ordenados recursivamente
- **Medição Precisa**: Cronometra tempo de processamento distribuído vs sequencial
- **Gerenciamento de Conexão**: Classe interna `Connection` para gerenciar sockets
- **Robustez**: Trata falhas de conexão e timeouts

#### **Exemplo de Uso:**
```bash
java -cp out distributed.Distribuidor 192.168.1.100:12345 192.168.1.101:12346 --tam 1000000
```

---

## 🖥️ **CÓDIGO DO SERVIDOR**

### **ReceptorServer.java** - Servidor de Processamento

#### **Responsabilidades do Servidor:**
- ✅ Fica aguardando conexões de clientes
- ✅ Aceita múltiplas conexões simultâneas
- ✅ Recebe pedidos de ordenação (`Pedido`)
- ✅ Processa ordenação em paralelo usando Merge Sort
- ✅ Retorna vetor ordenado via rede (`Resposta`)
- ✅ Gerencia encerramento de conexões

#### **Características Técnicas:**
- **Concorrência**: Uma thread por conexão (`Atendedor`)
- **Protocolo de Comunicação**: Processa `Pedido` e `ComunicadoEncerramento`
- **Processamento Paralelo**: Usa Merge Sort paralelo para otimizar ordenação
- **Robustez**: Trata exceções de rede e objetos desconhecidos
- **Logging Detalhado**: Registra todas as operações para debug

#### **Exemplo de Uso:**
```bash
java -cp out distributed.ReceptorServer 0.0.0.0 12345
```

---

## 📊 **REFERÊNCIA - OrdenacaoSequencial.java**

### **Função:**
- Implementa ordenação sequencial usando `Arrays.sort()`
- Serve como **baseline** para comparação de performance
- Valida se o resultado está ordenado

### **Características:**
- **Simplicidade**: Usa algoritmo otimizado do Java (Timsort)
- **Medição Precisa**: Cronometra tempo de execução em nanosegundos
- **Validação**: Verifica se o vetor está corretamente ordenado

---

## 🔄 **COMUNICAÇÃO CLIENTE ↔ SERVIDOR**

### **Fluxo de Dados:**
```
CLIENTE (Distribuidor)          SERVIDOR (ReceptorServer)
     ↓                              ↓
1. Gera vetor[1M]              1. Aguarda conexão
     ↓                              ↓
2. Divide em blocos            2. Aceita conexão
     ↓                              ↓
3. Envia Pedido(bloco)         3. Recebe Pedido
     ↓                              ↓
4. Aguarda Resposta            4. Processa p.ordenar()
     ↓                              ↓
5. Recebe Resposta(ordenado)  5. Envia Resposta(ordenado)
     ↓                              ↓
6. Faz merge de todas partes   6. Aguarda próximo pedido
     ↓                              ↓
7. Envia ComunicadoEncerramento 7. Encerra conexão
```

### **Protocolo de Comunicação:**
- **Pedido**: `{int[] numeros}` → Cliente para Servidor
- **Resposta**: `{int[] vetorOrdenado}` → Servidor para Cliente  
- **Encerramento**: `ComunicadoEncerramento` → Cliente para Servidor

### Utilitários

#### `Log.java` - Sistema de Logging

```java
public final class Log {
    public static void info(String tag, String msg);
    public static void warn(String tag, String msg);
    public static void error(String tag, String msg, Throwable t);
}
```

**Características:**
- **Thread-safe**: Métodos estáticos seguros para concorrência
- **Formatação**: Timestamp automático e tags identificadoras
- **Níveis**: Info, Warning e Error com stack traces

## 🚀 Como Executar

### 📦 1. Compilação

```bash
# Compilar classes compartilhadas
javac -d shared/out shared/src/distributed/*.java

# Compilar servidor
javac -cp shared/out -d servidor/out servidor/src/distributed/*.java

# Compilar cliente
javac -cp shared/out -d cliente/out cliente/src/distributed/*.java
```

**Ou use os scripts de compilação:**
```bash
# Compilar servidor
cd servidor && ./compilar.sh

# Compilar cliente
cd cliente && ./compilar.sh
```

---

### 🧪 2. Teste Sequencial (Referência)

```bash
java -cp cliente/out:shared/out distributed.OrdenacaoSequencial 10000
```

---

### 🖥️ 3. Teste em 3 Terminais na Mesma Máquina

Ideal para testar rapidamente na sua máquina local.

#### **TERMINAL 1 - Servidor 1 (Porta 12345)**

```bash
cd /Users/leofabri/IdeaProjects/ScriptMalignoServidor-Client

java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12345
```

**Você deve ver:**
```
[R] 2024-XX-XX XX:XX:XX — Servidor R ouvindo em 0.0.0.0:12345
```

**⚠️ Deixe este terminal aberto!**

---

#### **TERMINAL 2 - Servidor 2 (Porta 12346)**

```bash
cd /Users/leofabri/IdeaProjects/ScriptMalignoServidor-Client

java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12346
```

**Você deve ver:**
```
[R] 2024-XX-XX XX:XX:XX — Servidor R ouvindo em 0.0.0.0:12346
```

**⚠️ Deixe este terminal aberto!**

---

#### **TERMINAL 3 - Cliente (Executa o teste)**

```bash
cd /Users/leofabri/IdeaProjects/ScriptMalignoServidor-Client

java -cp cliente/out:shared/out distributed.Distribuidor \
  127.0.0.1:12345 127.0.0.1:12346 \
  --tam 100000
```

**O que acontece:**
1. ✅ Cliente gera vetor de 100.000 elementos
2. ✅ Conecta aos 2 servidores
3. ✅ Divide o vetor entre eles
4. ✅ Cada servidor ordena sua parte usando Merge Sort
5. ✅ Cliente faz merge das partes ordenadas
6. ✅ Mostra resultado, tempo e valida ordenação

**Resultado esperado:**
```
[D] 2024-XX-XX XX:XX:XX — Vetor gerado: 100000 elementos
[D] 2024-XX-XX XX:XX:XX — — Iniciando ordenação distribuída —
[D] 2024-XX-XX XX:XX:XX — Conectado a 127.0.0.1:12345
[D] 2024-XX-XX XX:XX:XX — Conectado a 127.0.0.1:12346
[D] 2024-XX-XX XX:XX:XX — Resposta de 127.0.0.1:12345: parte ordenada com 50000 elementos
[D] 2024-XX-XX XX:XX:XX — Resposta de 127.0.0.1:12346: parte ordenada com 50000 elementos
[D] 2024-XX-XX XX:XX:XX — Tempo distribuído (ordenação): XX.XX ms
[D] 2024-XX-XX XX:XX:XX — Tempo de merge: XX.XX ms
[D] 2024-XX-XX XX:XX:XX — Tempo total: XX.XX ms
[D] 2024-XX-XX XX:XX:XX — Vetor final ordenado com 100000 elementos
[D] 2024-XX-XX XX:XX:XX — Vetor está CORRETAMENTE ordenado
[D] 2024-XX-XX XX:XX:XX — Tempo sequencial (Arrays.sort): XX.XX ms
```

---

### 🌐 4. Teste em 3 Máquinas Diferentes

Ideal para demonstrar sistema distribuído real em rede.

#### **Pré-requisitos:**
- 3 máquinas na mesma rede
- Java instalado em todas
- Portas 12345, 12346 abertas no firewall (se necessário)
- Compartilhar pastas `shared/` e `servidor/` para as máquinas servidoras
- Compartilhar pastas `shared/` e `cliente/` para a máquina cliente

---

#### **MÁQUINA 1 (IP: 192.168.1.100) - Servidor 1**

**1. Copiar arquivos necessários:**
```bash
# Copiar para esta máquina:
# - shared/
# - servidor/
```

**2. Compilar:**
```bash
cd /caminho/do/projeto

# Compilar classes compartilhadas
javac -d shared/out shared/src/distributed/*.java

# Compilar servidor
javac -cp shared/out -d servidor/out servidor/src/distributed/*.java
```

**3. Executar servidor:**
```bash
java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12345
```

**Você deve ver:**
```
[R] 2024-XX-XX XX:XX:XX — Servidor R ouvindo em 0.0.0.0:12345
```

**⚠️ Deixe este terminal aberto!**

---

#### **MÁQUINA 2 (IP: 192.168.1.101) - Servidor 2**

**1. Copiar arquivos necessários:**
```bash
# Copiar para esta máquina:
# - shared/
# - servidor/
```

**2. Compilar:**
```bash
cd /caminho/do/projeto

# Compilar classes compartilhadas
javac -d shared/out shared/src/distributed/*.java

# Compilar servidor
javac -cp shared/out -d servidor/out servidor/src/distributed/*.java
```

**3. Executar servidor:**
```bash
java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12346
```

**Você deve ver:**
```
[R] 2024-XX-XX XX:XX:XX — Servidor R ouvindo em 0.0.0.0:12346
```

**⚠️ Deixe este terminal aberto!**

---

#### **MÁQUINA 3 (IP: 192.168.1.102) - Cliente**

**1. Copiar arquivos necessários:**
```bash
# Copiar para esta máquina:
# - shared/
# - cliente/
```

**2. Compilar:**
```bash
cd /caminho/do/projeto

# Compilar classes compartilhadas
javac -d shared/out shared/src/distributed/*.java

# Compilar cliente
javac -cp shared/out -d cliente/out cliente/src/distributed/*.java
```

**3. Executar cliente:**
```bash
java -cp cliente/out:shared/out distributed.Distribuidor \
  192.168.1.100:12345 192.168.1.101:12346 \
  --tam 1000000
```

**Parâmetros:**
- `192.168.1.100:12345` - IP e porta da Máquina 1 (Servidor 1)
- `192.168.1.101:12346` - IP e porta da Máquina 2 (Servidor 2)
- `--tam 1000000` - Tamanho do vetor (1 milhão de elementos)

**Resultado esperado:**
```
[D] 2024-XX-XX XX:XX:XX — Vetor gerado: 1000000 elementos
[D] 2024-XX-XX XX:XX:XX — — Iniciando ordenação distribuída —
[D] 2024-XX-XX XX:XX:XX — Conectado a 192.168.1.100:12345
[D] 2024-XX-XX XX:XX:XX — Conectado a 192.168.1.101:12346
[D] 2024-XX-XX XX:XX:XX — Resposta de 192.168.1.100:12345: parte ordenada com 500000 elementos
[D] 2024-XX-XX XX:XX:XX — Resposta de 192.168.1.101:12346: parte ordenada com 500000 elementos
[D] 2024-XX-XX XX:XX:XX — Tempo distribuído (ordenação): XX.XX ms
[D] 2024-XX-XX XX:XX:XX — Tempo de merge: XX.XX ms
[D] 2024-XX-XX XX:XX:XX — Tempo total: XX.XX ms
[D] 2024-XX-XX XX:XX:XX — Vetor final ordenado com 1000000 elementos
[D] 2024-XX-XX XX:XX:XX — Vetor está CORRETAMENTE ordenado
[D] 2024-XX-XX XX:XX:XX — Tempo sequencial (Arrays.sort): XX.XX ms
```

---

### 5. Teste Automatizado (Recomendado para desenvolvimento)

O script automatiza tudo: compilação, inicialização de servidores e testes.

```bash
chmod +x teste_distribuido.sh
./teste_distribuido.sh
```

**O que faz:**
- ✅ Compila todas as classes automaticamente
- ✅ Testa versão sequencial
- ✅ Inicia 3 servidores em background
- ✅ Testa com vetores pequenos, médios e grandes
- ✅ Limpa processos ao finalizar

---

### 🔍 6. Verificações de Sucesso

#### ✅ **Checklist - Teste Local (3 Terminais):**

- [ ] Servidores mostram "Servidor R ouvindo em..."
- [ ] Cliente conecta a todos os servidores
- [ ] Mensagem "Conectado a 127.0.0.1:XXXXX" aparece
- [ ] Servidores recebem pedidos ("Pedido recebido...")
- [ ] Cliente recebe respostas ("Resposta de...")
- [ ] Mensagem "Vetor está CORRETAMENTE ordenado"
- [ ] Tempos de execução são mostrados
- [ ] Conexões são encerradas corretamente

#### ✅ **Checklist - Teste Distribuído (3 Máquinas):**

- [ ] Servidores nas máquinas 1 e 2 estão rodando
- [ ] Cliente na máquina 3 conecta aos servidores remotos
- [ ] Não há erros de "Connection refused"
- [ ] Mensagem "Conectado a 192.168.1.XXX:XXXXX" aparece
- [ ] Processamento ocorre em paralelo nas máquinas
- [ ] Resultado final está ordenado corretamente
- [ ] Performance mostra ganho com distribuição

---

### ⚠️ Problemas Comuns e Soluções

#### **Erro: "Connection refused"**
- ✅ Verifique se os servidores estão rodando
- ✅ Verifique se as portas estão corretas
- ✅ Verifique firewall/antivírus (para teste em rede)
- ✅ Teste conectividade: `ping IP_DO_SERVIDOR`

#### **Erro: "ClassNotFoundException"**
- ✅ Verifique se compilou todas as classes
- ✅ Verifique o classpath (deve incluir `shared/out`)
- ✅ Recompile tudo: `javac -d shared/out shared/src/distributed/*.java`

#### **Vetor não está ordenado**
- ✅ Verifique os logs dos servidores
- ✅ Verifique se há erros durante o processamento
- ✅ Teste com vetor menor primeiro (`--tam 1000`)

#### **Porta já em uso**
- ✅ Use portas diferentes (12348, 12349, etc.)
- ✅ Ou encerre o processo que está usando a porta:
  ```bash
  # Linux/Mac
  lsof -ti:12345 | xargs kill -9
  
  # Windows
  netstat -ano | findstr :12345
  taskkill /PID <PID> /F
  ```

## 📊 Conceitos Demonstrados

### 1. **Programação Distribuída**
- Comunicação via sockets TCP
- Serialização de objetos Java
- Protocolo cliente-servidor

### 2. **Programação Paralela**
- `ExecutorService` para processamento paralelo
- Merge Sort paralelo recursivo
- Divisão de trabalho entre threads

### 3. **Algoritmos de Ordenação**
- Merge Sort (divide e conquista)
- Merge de múltiplos vetores ordenados
- Otimização com threshold de paralelização

### 4. **Concorrência**
- Múltiplas conexões simultâneas
- Sincronização de recursos compartilhados
- Gerenciamento de threads

### 5. **Otimização de Performance**
- Comparação sequencial vs distribuído
- Medição precisa de tempo (nanosegundos)
- Análise de escalabilidade

## 🔧 Parâmetros de Configuração

### Distribuidor
- `--tam N`: Tamanho do vetor (padrão: 10.000.000)
- `host:porta`: Endereços dos servidores

### ReceptorServer
- `host`: IP para bind (padrão: 0.0.0.0)
- `porta`: Porta de escuta (padrão: 12345)

## 📈 Análise de Performance

O sistema permite comparar:

1. **Tempo Sequencial**: Processamento em uma única thread com `Arrays.sort()`
2. **Tempo Distribuído**: Processamento dividido entre servidores
3. **Tempo de Merge**: Tempo gasto para fazer merge das partes ordenadas
4. **Speedup**: Ganho de performance com paralelização
5. **Eficiência**: Relação entre speedup e número de servidores

## 🎯 Objetivos de Aprendizado

1. **Compreender** arquiteturas cliente-servidor
2. **Implementar** comunicação distribuída em Java
3. **Aplicar** conceitos de programação paralela
4. **Implementar** algoritmos de ordenação distribuída
5. **Medir** e analisar performance de sistemas distribuídos
6. **Gerenciar** recursos de rede e concorrência

## 🛠️ Tecnologias Utilizadas

- **Java**: Linguagem de programação
- **Sockets TCP**: Comunicação de rede
- **Serialização**: Transmissão de objetos
- **ExecutorService**: Pool de threads
- **Merge Sort**: Algoritmo de ordenação

## 📝 Logs e Debugging

O sistema gera logs detalhados para cada componente:
- **Tag "D"**: Distribuidor
- **Tag "R"**: ReceptorServer  
- **Tag "SEQ"**: OrdenacaoSequencial

Exemplo de log:
```
[D] 2024-01-15 10:30:15 — Vetor gerado: 1000000 elementos
[R] 2024-01-15 10:30:16 — Pedido recebido de /127.0.0.1:54321 — tamanho: 500000
[D] 2024-01-15 10:30:17 — Tempo distribuído (ordenação): 45.67 ms
[D] 2024-01-15 10:30:17 — Tempo de merge: 12.34 ms
[D] 2024-01-15 10:30:17 — Tempo total: 58.01 ms
[D] 2024-01-15 10:30:17 — Vetor está CORRETAMENTE ordenado
```

Este sistema demonstra de forma prática os conceitos fundamentais de programação distribuída e paralela, sendo uma excelente base para entender como sistemas modernos processam grandes volumes de dados de forma eficiente.
