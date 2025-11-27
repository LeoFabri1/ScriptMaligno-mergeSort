@echo off
REM Script de Teste Automático - Sistema Distribuído de Contagem
REM Executa todos os testes do sistema automaticamente

echo 🚀 INICIANDO TESTES AUTOMATICOS DO SISTEMA DISTRIBUIDO
echo ==================================================
echo.

REM 1. TESTE DE COMPILAÇÃO
echo 📦 TESTE 1: COMPILAÇÃO
echo ----------------------
echo ℹ️  Compilando projeto...

javac -d out src/distributed/*.java
if %errorlevel% neq 0 (
    echo ❌ Falha na compilação!
    exit /b 1
)
echo ✅ Compilação bem-sucedida!
echo.

REM 2. TESTE DA VERSÃO SEQUENCIAL
echo 🔄 TESTE 2: VERSAO SEQUENCIAL
echo -----------------------------
echo ℹ️  Testando versão sequencial com vetor pequeno...

java -cp out distributed.OrdenacaoSequencial 1000
if %errorlevel% neq 0 (
    echo ❌ Falha na versão sequencial!
    exit /b 1
)
echo ✅ Versão sequencial funcionando!
echo.

REM 3. TESTE COM VETOR GRANDE (SEQUENCIAL)
echo 📊 TESTE 3: VERSAO SEQUENCIAL - VETOR GRANDE
echo --------------------------------------------
echo ℹ️  Testando versão sequencial com vetor grande (1M elementos)...

java -cp out distributed.OrdenacaoSequencial 1000000
if %errorlevel% neq 0 (
    echo ❌ Falha na versão sequencial com vetor grande!
    exit /b 1
)
echo ✅ Versão sequencial com vetor grande funcionando!
echo.

REM 4. TESTE DO SISTEMA DISTRIBUÍDO
echo 🌐 TESTE 4: SISTEMA DISTRIBUIDO
echo -------------------------------
echo ℹ️  Iniciando servidores R em background...

REM Iniciar servidores R em background
start /B java -cp out distributed.ReceptorServer 0.0.0.0 12345 > nul 2>&1
timeout /t 1 /nobreak > nul

start /B java -cp out distributed.ReceptorServer 0.0.0.0 12346 > nul 2>&1
timeout /t 1 /nobreak > nul

start /B java -cp out distributed.ReceptorServer 0.0.0.0 12347 > nul 2>&1
timeout /t 2 /nobreak > nul

echo ℹ️  Servidores R iniciados
echo ℹ️  Testando sistema distribuído com vetor pequeno...

REM Teste com vetor pequeno
java -cp out distributed.Distribuidor localhost:12345 localhost:12346 localhost:12347 --tam 1000
if %errorlevel% neq 0 (
    echo ❌ Falha no sistema distribuído com vetor pequeno!
    goto cleanup
)
echo ✅ Sistema distribuído com vetor pequeno funcionando!
echo.

REM 5. TESTE COM VETOR MÉDIO
echo 📈 TESTE 5: SISTEMA DISTRIBUIDO - VETOR MEDIO
echo ---------------------------------------------
echo ℹ️  Testando sistema distribuído com vetor médio (100K elementos)...

java -cp out distributed.Distribuidor localhost:12345 localhost:12346 localhost:12347 --tam 100000
if %errorlevel% neq 0 (
    echo ❌ Falha no sistema distribuído com vetor médio!
    goto cleanup
)
echo ✅ Sistema distribuído com vetor médio funcionando!
echo.

REM 6. TESTE COM VETOR GRANDE
echo 🚀 TESTE 6: SISTEMA DISTRIBUIDO - VETOR GRANDE
echo ----------------------------------------------
echo ℹ️  Testando sistema distribuído com vetor grande (1M elementos)...

java -cp out distributed.Distribuidor localhost:12345 localhost:12346 localhost:12347 --tam 1000000 --missing
if %errorlevel% neq 0 (
    echo ❌ Falha no sistema distribuído com vetor grande!
    goto cleanup
)
echo ✅ Sistema distribuído com vetor grande funcionando!
echo.

REM 7. TESTE COM APENAS 2 SERVIDORES
echo 🔗 TESTE 7: SISTEMA DISTRIBUIDO - 2 SERVIDORES
echo -----------------------------------------------
echo ℹ️  Testando sistema distribuído com apenas 2 servidores...

java -cp out distributed.Distribuidor localhost:12345 localhost:12346 --tam 50000
if %errorlevel% neq 0 (
    echo ❌ Falha no sistema distribuído com 2 servidores!
    goto cleanup
)
echo ✅ Sistema distribuído com 2 servidores funcionando!
echo.

REM 8. TESTE DE ESTRESSE
echo 💪 TESTE 8: TESTE DE ESTRESSE
echo -----------------------------
echo ℹ️  Testando sistema com múltiplas execuções...

for /L %%i in (1,1,3) do (
    echo ℹ️  Execução %%i/3...
    java -cp out distributed.Distribuidor localhost:12345 localhost:12346 localhost:12347 --tam 10000 > nul 2>&1
    if %errorlevel% neq 0 (
        echo ❌ Falha na execução %%i!
        goto cleanup
    )
    echo ✅ Execução %%i bem-sucedida!
)
echo.

REM RESUMO FINAL
echo 📋 RESUMO DOS TESTES
echo ====================
echo ✅ Compilação: OK
echo ✅ Versão sequencial: OK
echo ✅ Sistema distribuído: OK
echo ✅ Vetores pequenos: OK
echo ✅ Vetores médios: OK
echo ✅ Vetores grandes: OK
echo ✅ 2 servidores: OK
echo ✅ Estresse: OK
echo.

echo 🎉 TODOS OS TESTES FORAM EXECUTADOS COM SUCESSO!
echo 🚀 O SISTEMA ESTA FUNCIONANDO PERFEITAMENTE!
echo.
echo 📝 Para executar manualmente:
echo    1. java -cp out distributed.ReceptorServer 0.0.0.0 12345
echo    2. java -cp out distributed.ReceptorServer 0.0.0.0 12346
echo    3. java -cp out distributed.ReceptorServer 0.0.0.0 12347
echo    4. java -cp out distributed.Distribuidor localhost:12345 localhost:12346 localhost:12347 --tam 1000000
echo.
echo ✨ Sistema pronto para demonstração ao professor!
goto end

:cleanup
echo.
echo 🧹 LIMPEZA FINAL
echo ----------------
echo ℹ️  Limpando processos em background...
taskkill /F /IM java.exe > nul 2>&1
timeout /t 2 /nobreak > nul

:end
pause
