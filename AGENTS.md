# AGENTS.md - Diretrizes Operacionais & Governança do Projeto Minecraft Russo

## 1. Perfil do Ambiente & Hardware
- **Sistema Operacional:** Windows 11 Host.
- **Shell Padrão:** PowerShell (nunca usar sintaxe bash; utilizar `chcp 65001` para suporte a UTF-8/cirílico).
- **GPU:** AMD Radeon RX 6750 XT (Arquitetura RDNA 2).
  - *Regra Estrita de Gráficos e Mods:* Não existem Tensor Cores Nvidia. Shaders e mods de otimização devem ser 100% compatíveis com AMD Vulkan/OpenGL via Sodium, Lithium e ImmediatelyFast.
- **Rede Local:** Suporte a túneis reversos P2P (`e4all`) e VPNs locais (Radmin / Playit.gg) para contornar limitações de CGNAT de operadoras residenciais sem exigência de abertura de portas de roteador.
- **Diretório Raiz do Projeto:** `F:\Jogos\Mine\`
- **Java Runtime:** OpenJDK 17 portátil em `F:\Jogos\Mine\Java17\bin\javaw.exe` (isolamento total sem poluição do host).

---

## 2. Regras de Compatibilidade & Execução
- **Contas Offline (Cracked):** O modpack é 100% compatível com contas offline/não originais.
  - O cliente inicia via lançador portátil UltimMC configurado com contas offline.
  - O servidor e sessões LAN devem sempre manter `online-mode=false` e `enforce-secure-profile=false` em `server.properties` para prevenir desconexões por falta de assinatura criptográfica da Mojang.
- **Reconhecimento de Voz Offline (Melty Mechanic):** O mod SayIt utiliza a biblioteca local Vosk C (`vosk-model-small-ru-0.22`), capturando microfone localmente em 16kHz mono. O áudio do microfone nunca é transmitido pela rede, evitando saturação de banda e descompasso de TPS.
- **Renderização Cirílica:** Textos em russo utilizam fontes TrueType e renderização acelerada por Modern UI e ImmediatelyFast, garantindo legibilidade nítida.

---

## 3. Comandos de Verificação & Smoke Test
- **Teste de Verificação M1:**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\verify_m1.ps1"
  ```
- **Execução do Cliente:**
  - Clique duplo em `F:\Jogos\Mine\Jogar.bat`

---

## 4. Lições Aprendidas & Armadilhas Superadas

### L1. Ausência de Java no Host Windows
- **Armadilha:** Em instalações limpas do Windows 11, o comando `java` global não existe no PATH (`CommandNotFoundException`).
- **Solução:** O projeto adota runtime OpenJDK 17 portátil embutido em `F:\Jogos\Mine\Java17\`. O lançador `Jogar.bat` e os scripts do servidor detectam primeiramente `%BASE_DIR%Java17\bin\javaw.exe`.

### L2. Assinatura de Mensagens de Chat no Minecraft 1.19+ (enforce-secure-profile)
- **Armadilha:** Contas offline não possuem certificados da Mojang. Se `enforce-secure-profile` estiver `true`, jogadores offline são desconectados imediatamente com `"Missing or invalid chat signature"`.
- **Solução:** `enforce-secure-profile=false` é compulsório em qualquer configuração de servidor dedicado e mundo LAN.

### L3. Encoding UTF-8 no Shell PowerShell / Batch
- **Armadilha:** Nomes de itens e caracteres cirílicos em russo (`ru_ru`) sofrem corrupção (mojibake) se executados sob a página de código padrão do Windows (CP1252 / CP850).
- **Solução:** Todos os scripts `.bat` iniciam com `chcp 65001 >nul` e os scripts PowerShell utilizam codificação UTF-8 explícita.

### L4. Saída do `java -version` para STDERR no PowerShell
- **Armadilha:** O executável `java.exe -version` emite a versão na saída padrão de erros (STDERR). Quando `$ErrorActionPreference = "Stop"` está ativo no PowerShell, o redirecionamento `2>&1` é interpretado como `NativeCommandError` e interrompe a execução com erro fatal falso-positivo.
- **Solução:** Capturar a saída via wrapper de processo ou `cmd.exe /c "`"$javaExe`" -version 2>&1"`, evitando interceptação indevida de erro pelo PowerShell.

### L5. Parsing de Caracteres Acentuados no PowerShell 5.1 (ANSI vs UTF-8)
- **Armadilha:** Scripts `.ps1` salvos em UTF-8 sem BOM são lidos pelo interpretador padrão do PowerShell 5.1 usando a página de código local (ANSI / Windows-1252), corrompendo literais como `"Lições"` para `"LiÃ§Ãµes"`.
- **Solução:** Em regexes e asserções de validação, utilizar padrões tolerantes (ex: `Li.*es Aprendidas`) ou salvar arquivos com BOM quando a execução estrita de literais for necessária.
