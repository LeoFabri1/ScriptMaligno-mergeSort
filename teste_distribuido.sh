#!/bin/bash

# Script de Teste - Sistema Distribuído Separado
# Testa cliente e servidor em máquinas diferentes

echo "🧪 TESTE - SISTEMA DISTRIBUÍDO SEPARADO"
echo "======================================="
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

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
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
print_info "1. Compilando projeto completo..."

print_info "   Compilando classes compartilhadas..."
if ! javac -d shared/out shared/src/distributed/*.java; then
    print_error "Falha na compilação das classes compartilhadas!"
    exit 1
fi

print_info "   Compilando cliente..."
if ! javac -cp shared/out -d cliente/out cliente/src/distributed/*.java; then
    print_error "Falha na compilação do cliente!"
    exit 1
fi

print_info "   Compilando servidor..."
if ! javac -cp shared/out -d servidor/out servidor/src/distributed/*.java; then
    print_error "Falha na compilação do servidor!"
    exit 1
fi

print_success "Compilação completa!"

# 2. Teste sequencial
print_info "2. Testando versão sequencial..."
if ! java -cp cliente/out:shared/out distributed.OrdenacaoSequencial 1000; then
    print_error "Falha na versão sequencial!"
    exit 1
fi
print_success "Sequencial OK!"

# 3. Iniciar servidores
print_info "3. Iniciando servidores..."

# Servidor 1 (porta 12345)
java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12345 > servidor1.log 2>&1 &
PID1=$!
echo "   Servidor 1 iniciado (PID: $PID1) - 0.0.0.0:12345"

# Servidor 2 (porta 12346)
java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12346 > servidor2.log 2>&1 &
PID2=$!
echo "   Servidor 2 iniciado (PID: $PID2) - 0.0.0.0:12346"

# Servidor 3 (porta 12347)
java -cp servidor/out:shared/out distributed.ReceptorServer 0.0.0.0 12347 > servidor3.log 2>&1 &
PID3=$!
echo "   Servidor 3 iniciado (PID: $PID3) - 0.0.0.0:12347"

print_success "3 servidores iniciados!"
print_info "Aguardando servidores ficarem prontos..."
sleep 3

# 4. Teste distribuído
print_info "4. Testando sistema distribuído..."
echo "   Conectando aos servidores..."
echo ""

# Teste com vetor pequeno
print_info "Teste 1: Vetor pequeno (10K elementos)"
java -cp cliente/out:shared/out distributed.Distribuidor 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347 --tam 10000

if [ $? -eq 0 ]; then
    print_success "Teste 1 OK!"
else
    print_error "Falha no teste 1!"
    exit 1
fi

echo ""

# Teste com vetor médio
print_info "Teste 2: Vetor médio (100K elementos)"
java -cp cliente/out:shared/out distributed.Distribuidor 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347 --tam 100000

if [ $? -eq 0 ]; then
    print_success "Teste 2 OK!"
else
    print_error "Falha no teste 2!"
    exit 1
fi

echo ""

# Teste com vetor grande
print_info "Teste 3: Vetor grande (1M elementos)"
java -cp cliente/out:shared/out distributed.Distribuidor 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347 --tam 1000000

if [ $? -eq 0 ]; then
    print_success "Teste 3 OK!"
else
    print_error "Falha no teste 3!"
    exit 1
fi

echo ""
print_success "🎉 TESTE CONCLUÍDO COM SUCESSO!"
print_success "🚀 Sistema distribuído funcionando perfeitamente!"
print_info "📝 Testou cliente e servidores separados"
echo ""
print_info "📋 LOGS GERADOS:"
print_info "   • servidor1.log - Log do servidor 1 (0.0.0.0:12345)"
print_info "   • servidor2.log - Log do servidor 2 (0.0.0.0:12346)"
print_info "   • servidor3.log - Log do servidor 3 (0.0.0.0:12347)"
echo ""
print_info "💡 Para executar em máquinas diferentes:"
print_info "   Máquina 1: java -cp servidor/out distributed.ReceptorServer 0.0.0.0 12345"
print_info "   Máquina 2: java -cp servidor/out distributed.ReceptorServer 0.0.0.0 12346"
print_info "   Máquina 3: java -cp cliente/out distributed.Distribuidor IP1:12345 IP2:12346 --tam 1000000"
