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
- **Teste de Verificação M2 (Sistema de Voz SayIt + Vosk Russo):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\verify_m2.ps1"
  ```
- **Provisionamento de Reconhecimento de Voz:**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\setup_voice_ai.ps1"
  ```
- **Teste de Verificação M3 (Progressão e Fontes Cirílicas):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\verify_m3.ps1"
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

### L6. Quests do Heracles e Codificação UTF-8 de Caracteres Cirílicos
- **Armadilha:** O motor Heracles Odyssey Quests armazena missões em arquivos JSON avulsos. Se os arquivos forem salvos com codificações heterogêneas no Windows, o parser Gson do Minecraft falha silenciosamente ou corrompe caracteres cirílicos em caixas de diálogo.
- **Solução:** Gerar todos os arquivos JSON via `[System.IO.File]::WriteAllText($path, $json, (New-Object System.Text.UTF8Encoding($false)))` garantindo UTF-8 puro sem BOM, compatível com os mixins do Heracles e Resourceful Lib.

### L7. Detecção de Mojibake vs Palavras Acentuadas em Português
- **Armadilha:** Expressões regulares para detecção de mojibake que checam sequências de caracteres na faixa Latin-1 (`[\u00C0-\u00FF]{2,}`) geram falsos-positivos com palavras legítimas em português (ex: `redução`, `vocálica`, `pronúncia`), bloqueando a verificação automatizada.
- **Solução:** O padrão assertivo para captura de decodificação cruzada UTF-8/Windows-1252 deve verificar prefixos específicos de corrupção: `[ÃÐÑâ][\x80-\xFF]|\uFFFD`.

### L8. Mapeamento Biunívoco de Itens Pedagógicos em Dicionários
- **Armadilha:** Quando substantivos compartilham entidades (ex: `vaca` e `bife`, `porco` e `carne de porco`), o uso do mesmo identificador de item (`minecraft:porkchop`) em tabelas hash provoca sobrescrita de chaves e quebra a contagem estrita de traduções em `ru_ru.json`.
- **Solução:** Garantir identificadores exclusivos para cada item do currículo (ex: `minecraft:pig_spawn_egg` para a criatura 'Porco/Свинья' e `minecraft:porkchop` para o alimento 'Carne de Porco/Свинина').

### L9. Precedência de Operadores em Test-Path com Condicionais (-and) no PowerShell
- **Armadilha:** Em comandos condicionais como `if (Test-Path $a -and -not (Test-Path $b))`, o PowerShell interpreta `-and` como argumento posicional/parâmetro do cmdlet `Test-Path`, gerando `ParameterBindingException: Não é possível localizar um parâmetro que coincida com o nome 'and'`.
- **Solução:** Sempre encapsular invocações de cmdlets em parênteses antes de operadores lógicos: `if ((Test-Path $a) -and (-not (Test-Path $b)))`.

### L10. Divergência de Nomenclatura em Traduções Oficiais do ru_ru.json
- **Armadilha:** Em especificações informais da comunidade, itens como Pérola do Fim são descritos como "Жемчуг Края". Porém, no asset oficial do Minecraft 1.20.1 (`ru_ru.json`), o valor canônico da chave `item.minecraft.ender_pearl` é `Эндер-жемчуг`.
- **Solução:** Os testes de validação devem sempre extrair as strings oficiais diretamente de `assets/minecraft/lang/ru_ru.json` para assegurar fidelidade estrita à UI nativa do jogo.

