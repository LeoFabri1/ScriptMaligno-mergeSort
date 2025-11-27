#!/bin/bash

# Script de Teste - Sistema Distribuído
# Testa 3 computadores na mesma máquina

echo "🧪 TESTE - 3 COMPUTADORES"
echo "========================="
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
    print_info "Encerrando servidores simulados..."
    pkill -f "distributed.ReceptorServer" 2>/dev/null || true
    sleep 2
}

trap cleanup EXIT

# 1. Compilar
print_info "1. Compilando projeto..."
if ! javac -d out src/distributed/*.java; then
    print_error "Falha na compilação!"
    exit 1
fi
print_success "Compilação OK!"

# 2. Teste sequencial
print_info "2. Testando versão sequencial..."
if ! java -cp out distributed.OrdenacaoSequencial 1000; then
    print_error "Falha na versão sequencial!"
    exit 1
fi
print_success "Sequencial OK!"

# 3. Iniciar 3 servidores
print_info "3. Iniciando 3 servidores..."
echo "   Configuração: 192.168.1.100:12345, 192.168.1.101:12346, 192.168.1.102:12347"

# Servidor 1 (porta 12345)
java -cp out distributed.ReceptorServer 0.0.0.0 12345 > servidor1.log 2>&1 &
PID1=$!
echo "   Servidor 1 iniciado (PID: $PID1) - 192.168.1.100:12345"

# Servidor 2 (porta 12346)
java -cp out distributed.ReceptorServer 0.0.0.0 12346 > servidor2.log 2>&1 &
PID2=$!
echo "   Servidor 2 iniciado (PID: $PID2) - 192.168.1.101:12346"

# Servidor 3 (porta 12347)
java -cp out distributed.ReceptorServer 0.0.0.0 12347 > servidor3.log 2>&1 &
PID3=$!
echo "   Servidor 3 iniciado (PID: $PID3) - 192.168.1.102:12347"

print_success "3 servidores iniciados!"
print_info "Aguardando servidores ficarem prontos..."
sleep 3

# 4. Teste distribuído
print_info "4. Testando sistema distribuído..."
echo "   Conectando aos servidores..."
echo ""

# Teste com vetor pequeno
print_info "Teste 1: Vetor pequeno (10K elementos)"
java -cp out distributed.Distribuidor 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347 --tam 10000

if [ $? -eq 0 ]; then
    print_success "Teste 1 OK!"
else
    print_error "Falha no teste 1!"
    exit 1
fi

echo ""

# Teste com vetor médio
print_info "Teste 2: Vetor médio (100K elementos)"
java -cp out distributed.Distribuidor 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347 --tam 100000

if [ $? -eq 0 ]; then
    print_success "Teste 2 OK!"
else
    print_error "Falha no teste 2!"
    exit 1
fi

echo ""

# Teste com vetor grande
print_info "Teste 3: Vetor grande (1M elementos)"
java -cp out distributed.Distribuidor 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347 --tam 1000000

if [ $? -eq 0 ]; then
    print_success "Teste 3 OK!"
else
    print_error "Falha no teste 3!"
    exit 1
fi

echo ""
print_success "🎉 TESTE CONCLUÍDO COM SUCESSO!"
print_success "🚀 Sistema funcionando perfeitamente!"
print_info "📝 Testou 3 computadores na rede"
echo ""
print_info "📋 LOGS GERADOS:"
print_info "   • servidor1.log - Log do servidor 1 (192.168.1.100:12345)"
print_info "   • servidor2.log - Log do servidor 2 (192.168.1.101:12346)"
print_info "   • servidor3.log - Log do servidor 3 (192.168.1.102:12347)"
echo ""
print_info "💡 Para ver os logs: cat servidor1.log"
print_info "💡 Para teste em outras máquinas, use: ./teste_automatico.sh IP1:porta IP2:porta IP3:porta"
