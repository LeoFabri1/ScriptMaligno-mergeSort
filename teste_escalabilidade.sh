#!/bin/bash

# Script para testar escalabilidade com diferentes números de servidores
# Uso: ./teste_escalabilidade.sh [NUM_SERVIDORES] [TAMANHO_VETOR]

NUM_SERVIDORES=${1:-3}
TAMANHO=${2:-100000}

echo "🧪 TESTE DE ESCALABILIDADE"
echo "=========================="
echo "Número de servidores: $NUM_SERVIDORES"
echo "Tamanho do vetor: $TAMANHO"
echo ""

# Cores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Função para limpeza
cleanup() {
    echo ""
    print_info "Encerrando servidores..."
    pkill -f "distributed.ReceptorServer" 2>/dev/null || true
    sleep 2
}

trap cleanup EXIT

# 1. Compilar tudo
print_info "1. Compilando projeto..."
if ! javac -d shared/out shared/src/distributed/*.java; then
    print_error "Falha na compilação das classes compartilhadas!"
    exit 1
fi

if ! javac -cp shared/out -d cliente/out cliente/src/distributed/*.java; then
    print_error "Falha na compilação do cliente!"
    exit 1
fi

if ! javac -cp shared/out -d servidor/out servidor/src/distributed/*.java; then
    print_error "Falha na compilação do servidor!"
    exit 1
fi

print_success "Compilação completa!"

# 2. Iniciar servidores
print_info "2. Iniciando $NUM_SERVIDORES servidores..."

SERVIDORES=()
for i in $(seq 1 $NUM_SERVIDORES); do
    PORTA=$((12344 + i))
    java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 $PORTA > servidor$i.log 2>&1 &
    PID=$!
    SERVIDORES+=("127.0.0.1:$PORTA")
    echo "   Servidor $i iniciado (PID: $PID) - 0.0.0.0:$PORTA"
done

print_success "$NUM_SERVIDORES servidores iniciados!"
print_info "Aguardando servidores ficarem prontos..."
sleep 3

# 3. Teste sequencial
print_info "3. Testando versão sequencial..."
java -cp cliente/out:shared/out distributed.OrdenacaoSequencial $TAMANHO > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_success "Sequencial OK!"
else
    print_error "Falha na versão sequencial!"
    exit 1
fi

# 4. Teste distribuído
print_info "4. Testando sistema distribuído com $NUM_SERVIDORES servidores..."
echo "   Conectando aos servidores: ${SERVIDORES[*]}"
echo ""

# Construir comando do cliente
COMANDO="java -cp cliente/out:shared/out distributed.Distribuidor"
for servidor in "${SERVIDORES[@]}"; do
    COMANDO="$COMANDO $servidor"
done
COMANDO="$COMANDO --tam $TAMANHO"

echo "Executando: $COMANDO"
echo ""

# Executar teste
eval $COMANDO

if [ $? -eq 0 ]; then
    print_success "🎉 TESTE CONCLUÍDO COM SUCESSO!"
    print_success "🚀 Sistema funcionando com $NUM_SERVIDORES servidores!"
    print_info "📊 Vetor de $TAMANHO elementos processado em paralelo"
else
    print_error "Falha no teste distribuído!"
    exit 1
fi

echo ""
print_info "📋 LOGS GERADOS:"
for i in $(seq 1 $NUM_SERVIDORES); do
    print_info "   • servidor$i.log - Log do servidor $i"
done

echo ""
print_info "💡 Para testar com mais servidores:"
print_info "   ./teste_escalabilidade.sh 5 1000000    # 5 servidores, 1M elementos"
print_info "   ./teste_escalabilidade.sh 10 5000000   # 10 servidores, 5M elementos"
