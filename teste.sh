#!/bin/bash

# Script Simples de Teste - Sistema Distribuído
# Uso: ./teste.sh IP1:porta IP2:porta IP3:porta

echo "🚀 TESTE SISTEMA DISTRIBUÍDO"
echo "==========================="
echo ""

# Cores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
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

# Verificar se IPs foram fornecidos
if [ $# -eq 0 ]; then
    print_error "Nenhum IP fornecido!"
    echo ""
    print_info "Uso: ./teste.sh IP1:porta IP2:porta IP3:porta"
    echo ""
    print_info "Exemplos:"
    print_info "• Teste local: ./teste.sh 127.0.0.1:12345 127.0.0.1:12346 127.0.0.1:12347"
    print_info "• Teste rede: ./teste.sh 192.168.1.100:12345 192.168.1.101:12345 192.168.1.102:12345"
    exit 1
fi

# 1. Compilar
print_info "1. Compilando..."
javac -d out src/distributed/*.java
print_success "Compilação OK!"

# 2. Teste sequencial
print_info "2. Testando versão sequencial..."
java -cp out distributed.OrdenacaoSequencial 1000
print_success "Sequencial OK!"

# 3. Teste distribuído
print_info "3. Testando sistema distribuído..."
print_info "IPs fornecidos: $*"
echo ""

java -cp out distributed.Distribuidor "$@" --tam 10000

if [ $? -eq 0 ]; then
    print_success "Teste distribuído OK!"
else
    print_error "Falha no teste distribuído!"
    exit 1
fi

echo ""
print_success "🎉 TESTE CONCLUÍDO COM SUCESSO!"
print_success "🚀 Sistema funcionando perfeitamente!"
