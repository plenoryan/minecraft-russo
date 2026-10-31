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
- **Teste de Verificação M4 (Multiplayer, Skins Offline e Empacotamento):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\verify_m4.ps1"
  ```
- **Provisionamento Multiplayer e Servidor Dedicado (M4):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\setup_multiplayer.ps1"
  ```
- **Bateria Canônica de Testes E2E (Modo Estrito):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
  ```
- **Execução do Cliente:**
  - Clique duplo em `F:\Jogos\Mine\Jogar.bat`
- **Execução do Servidor Dedicado:**
  - Clique duplo em `F:\Jogos\Mine\Iniciar_Servidor.bat`

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

### L11. NTFS Junctions e Conflito de Cópia Circular (IOException)
- **Armadilha:** A pasta `Launcher\instances\Minecraft_Russo\.minecraft\mods` e `config` são junctions NTFS que apontam diretamente para `Instancia_Cliente\mods` e `config`. Ao tentar copiar arquivos entre cliente e lançador em scripts de setup, o PowerShell tenta sobrescrever o arquivo sobre si mesmo gerando `IOException: O processo não pode acessar o arquivo porque ele está sendo usado por outro processo`.
- **Solução:** Detectar previamente se o diretório de destino é uma Junction (`(Get-Item $dir).LinkType -eq 'Junction'`) e ignorar a cópia redundante.

### L12. Dependência de Versão do Fabric em Forks do e4mc (e4all)
- **Armadilha:** Builds mais recentes (2.2.0/2.3.0) do mod `e4all` introduziram dependências de runtime do Minecraft 1.21+ (`>=26.1-`), gerando falhas de resolução de mixins e bloqueio de boot em instâncias 1.20.1 Fabric.
- **Solução:** Utilizar a versão estável e canônica `e4all-fabric-1.6.4.jar`, que declara compatibilidade nativa com Minecraft `>=1.18` e Fabric Loader `>=0.14.0`, mantendo o switch de `Online Mode: [OFF]` e o proxying UDP de voz para Simple Voice Chat.

### L13. Segregação Estrita de Mods Client-Only em Servidores Dedicados Fabric
- **Armadilha:** Mods de otimização de renderização gráfica e aceleração de fontes (como Sodium, Modern UI, ImmediatelyFast e CustomSkinLoader) contêm mixins exclusivos do ambiente cliente (`environment: client`). Incluí-los na pasta `mods` do servidor dedicado causa `ClassNotFoundException` e encerramento abrupto na inicialização.
- **Solução:** O script de provisionamento (`setup_multiplayer.ps1`) filtra estritamente a pasta `Servidor_Dedicado\mods\`, mantendo apenas mods universais (`fabric-api`, `sayit`, `lithium`, `ferritecore`, `fabrictailor`).

### L14. Compatibilidade Estrita entre Fabric Loader e Fabric API no Servidor Dedicado
- **Armadilha:** `fabric-api-0.92.12+1.20.1` exige expressamente `fabricloader >= 0.16.10`. Inicializar o servidor dedicado com o JAR padrão bundling Fabric Loader `0.15.11` dispara `net.fabricmc.loader.impl.FormattedException: Some of your mods are incompatible with the game` e aborta o servidor com código de saída 1.
- **Solução:** Atualizar o launcher de servidor para `0.16.14` via API Meta da Fabric (`https://meta.fabricmc.net/v2/versions/loader/1.20.1/0.16.14/1.0.1/server/jar`) e sincronizar todos os manifestos `mmc-pack.json` para `"version": "0.16.14"`.

### L15. Sintaxe de Regex em Java vs .NET (\p{IsBasicLatin} vs [a-zA-Z])
- **Armadilha:** A propriedade de bloco Unicode `\p{IsBasicLatin}` é aceita pelo motor de regex do .NET/C#, mas inexiste na JVM (Java `PatternSyntaxException: Unknown character property name {IsBasicLatin}`). Quando invocada em `VoskEngine.normalizeCyrillic`, quebrava o processamento de áudio do jogo no primeiro caractere processado.
- **Solução:** Utilizar a classe posix/ascii padrão `[a-zA-Z]` em substituições cirílicas: `replaceAll("[^\\p{IsCyrillic}a-zA-Z\\s]", "")`.

### L16. Rejeição de Facadas em Testes Automatizados (Zero-Tautology Verification)
- **Armadilha:** Testes que validam variáveis locais declaradas no próprio script de teste (como `$serverAction = "DROP_ITEM_ENTITY"` ou `$eulaState = "eula=true"`) criam fachadas autovalidantes que mascaram falhas graves de arquivos de configuração em disco e quebras de subprocessos.
- **Solução:** Todos os testes E2E devem inspecionar arquivos reais em disco (`sayit.json`, `server.properties`, `eula.txt`, `ru_ru.json`) e invocar dry-runs reais dos executáveis do jogo (`java.exe -jar fabric-server-launch.jar --help`).

### L17. Discriminação Fonética de Palavras Curtas (3 Letras) e Eliminação de Exploit de Grunhidos
- **Armadilha:** Palavras cirílicas de 3 letras com 1 substituição (como "лук" vs "люк") atingem score Levenshtein $1.0 - (1/3) = 0.6667$, ultrapassando o limiar padrão de 0.65 e aceitando falsos-amigos fonêmicos. Além disso, verificações ingênuas de `contains` permitiam emitir uma única letra (ex: "а" ou "к") para ganhar o desafio de "алмаз" ou "яблоко".
- **Solução:** Para palavras com comprimento $\le 3$, exigir distância Levenshtein estritamente 0 ($dist == 0$). Para correspondência de substrings, restringir a tokens inteiros de no mínimo 4 caracteres.

### L18. Injeção de BOM no Pipeline do PowerShell 5.1 para Processos CLI Nativos
- **Armadilha:** No Windows PowerShell 5.1, o encadeamento via pipe (`$script | & jshell.exe`) insere bytes de preâmbulo/BOM no fluxo de entrada padrão (STDIN), fazendo com que utilitários nativos como o JShell falhem com `illegal character: '\ufeff'` ou `illegal character: '\u00bb'`.
- **Solução:** Escrever o script em arquivo temporário sem BOM (`[System.Text.UTF8Encoding]::new($false)`) e passá-lo como argumento direto (`jshell.exe -s "$tempFile"`).

### L19. Parâmetros de Memória JVM e Resolução de Java no MultiMC/UltimMC
- **Armadilha:** O MultiMC/UltimMC possui campos dedicados (`MinMemAlloc` e `MaxMemAlloc`) para gerenciar `-Xms` e `-Xmx`. Se esses argumentos forem inseridos manualmente dentro da string `JvmArgs`, o inicializador bloqueia a inicialização exibindo o erro: `You tried to manually set a JVM memory option (using "-XX:PermSize", "-XX-MaxHeapSize", "-XX:InitialHeapSize", "-Xmx" or "-Xms")...`. Além disso, caminhos relativos em `JavaPath` que resolvem para caminhos inexistentes disparam o assistente inicial (wizard de seleção de idioma e Java).
- **Solução:** Remover `-Xms` e `-Xmx` da chave `JvmArgs` em todos os `instance.cfg`, utilizando exclusivamente `MinMemAlloc=2048` e `MaxMemAlloc=4096`. No script `Jogar.bat`, sincronizar dinamicamente o `JavaPath` absoluto em `ultimmc.cfg` e `instance.cfg`, e criar junções NTFS (`Launcher\Java17` e `Launcher\data\Java17`) para garantir portabilidade em qualquer diretório ou máquina sem nunca exibir o assistente.


