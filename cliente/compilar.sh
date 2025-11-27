#!/bin/bash

echo "🔨 Compilando CLIENTE..."
echo "========================="

# Criar diretório de saída
mkdir -p out

# Compilar classes compartilhadas primeiro
echo "📦 Compilando classes compartilhadas..."
javac -d out ../shared/src/distributed/*.java

# Compilar cliente
echo "🖥️  Compilando cliente..."
javac -cp out -d out src/distributed/*.java

echo "✅ Cliente compilado com sucesso!"
echo "📁 Classes em: cliente/out/distributed/"
echo ""
echo "🚀 Para executar:"
echo "   java -cp out:../shared/out distributed.Distribuidor IP1:porta1 IP2:porta2 --tam 1000000"
echo "   java -cp out:../shared/out distributed.OrdenacaoSequencial 1000000"
