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

### L20. Provedor de Contas Offline no UltimMC ("dummy" vs "Offline") e Janela de Console
- **Armadilha:** No UltimMC, o identificador interno do provedor de contas offline/locais é `dummy` (referente ao `DummyAuthProvider`). Declarar `"type": "Offline"` em `accounts.json` causa falha de resolução (`LOOKUP AUTH_PROVIDER "Offline"` -> `Lookup failed`), deixando o lançador sem nenhuma conta ativa carregada. Em combinação com `ShowConsole=false` no `instance.cfg`, a flag `--launch` abortava silenciosamente sem exibir nenhuma janela na tela para o usuário.
- **Solução:** Utilizar expressamente `"type": "dummy"` em `accounts.json`, configurar `ShowConsole=true` para exibir a janela de progresso/console imediatamente ao iniciar o jogo e fornecer o script `Abrir_Launcher.bat` para acesso visual à interface do gerenciador de instâncias.

### L21. LinkageError por Empacotamento de Classes de Framework (net.fabricmc.api.*) em JARs de Mods
- **Armadilha:** Durante a compilação avulsa de mods (como `sayit`), se stubs de interfaces do Fabric (`net.fabricmc.api.EnvType`, `ModInitializer`, `ClientModInitializer`, etc.) forem empacotados dentro do JAR final, a JVM carrega primeiro essa classe através do classloader do sistema (`app`). Quando o motor do Fabric Loader tenta definir sua própria versão de `net.fabricmc.api.EnvType` dentro do classloader isolado `knot`, a JVM dispara `java.lang.LinkageError: loader constraint violation: loader 'knot' wants to load class net.fabricmc.api.EnvType. A different class with the same name was previously loaded by 'app'`. Isso quebrava mods que checam o ambiente no estágio `preLaunch` (como `modernfix` e `ModernUI`).
- **Solução:** Nunca incluir pacotes do framework `net/fabricmc/*` dentro do JAR do mod. O mod JAR deve conter unicamente seu próprio pacote (`net/spicesfire/sayit/*`) e o manifesto `fabric.mod.json`, delegando todas as classes da API do Fabric para serem providas exclusivamente pelo Fabric Loader em tempo de execução.

### L22. Binário Autêntico do SayIt, Vosk Offline e Mecânica do Item Sumir (Melty)
- **Armadilha:** Stubs locais de simulação não possuem os event handlers do Minecraft para capturar aquisição de novos itens e aplicar penalidades autoritativas de exclusão. Isso fazia com que os itens ficassem no inventário indefinidamente sem acionar o HUD nem serem deletados.
- **Solução:** Integrar o binário autêntico `sayitenglish-fabric-1.20.1-1.1.0.jar` (por SpicesFire) tanto no cliente quanto no servidor. Configurar `penalty: "delete"`, `engine: "vosk"`, `mode: "read"` em `config/sayitenglish.json` e pré-instalar o modelo acústico offline em `config/sayitenglish/models/vosk-model-small-ru-0.22`. Quando o jogador pega um item novo pela primeira vez, o HUD exibe a palavra em cirílico com transcrição fonética e o temporizador de 12 segundos; se a palavra não for dita no microfone a tempo, o item é automaticamente excluído do inventário.

### L23. Poluição das Chaves de Tradução em Resource Packs e Conflito com SayIt
- **Armadilha:** Ao tentar injetar fonética e tradução em português (`Madeira / Árvore [RU: Дéрево [DYE-rye-va]]`) diretamente nas chaves `item.*` e `block.*` do arquivo `lang/ru_ru.json` e `lang/pt_br.json` através de um resource pack, o mod SayIt lê essa string inteira poluída como o nome alvo a ser pronunciado. Como resultado, o HUD pedia para pronunciar `Derevo Dye-re-va pt-br...` e o modelo acústico Vosk nunca reconhecia as palavras estrangeiras, forçando a perda inevitável do item com baixa correspondência (12%).
- **Solução:** Nunca poluir as strings de nome de item nos arquivos `lang/`. O SayIt deve ler o nome limpo padrão em cirílico (`Дубовое бревно`, `Камень`, etc.), pois ele já possui um transliterador interno nativo (`Romanizer.class`) que gera automaticamente a leitura latina abaixo do cirílico e envia o vocábulo puro para o Vosk. Detalhes pedagógicos (fonética, gênero gramatical e regras) devem ser providos exclusivamente nos tooltips via script KubeJS (`ItemEvents.tooltip`), mantendo os nomes dos itens 100% canônicos.

### L24. Separação Estrita de Idiomas para Nomes Bilíngues com Reconhecimento de Voz Limpo
- **Armadilha:** Usuários desejam visualizar ambos os nomes lado a lado ao selecionar o item na hotbar ou inventário (`Madeira [RU: Дерево]`), mas se a mesma string composta for colocada em `ru_ru.json`, o SayIt extrai a string composta como alvo de fala do microfone.
- **Solução:** Aplicar a separação assimétrica de traduções:
  1. Em `assets/minecraft/lang/pt_br.json` (idioma do cliente do jogador), definir o nome bilíngue: `"block.minecraft.oak_log": "Madeira [RU: Дерево]"`. Isso garante que na hotbar, no inventário e nas dicas o jogador sempre veja ambos os nomes lado a lado.
  2. Em `assets/minecraft/lang/ru_ru.json` (idioma alvo avaliado pelo `inTarget` do SayIt), definir estritamente a palavra russa pura em cirílico limpo: `"block.minecraft.oak_log": "Дерево"`.
  Dessa forma, o SayIt extrai unicamente a palavra russa, gera a fonética latina `[derevo]` e o Vosk pontua 100% ao ouvir a palavra russa falada, enquanto a interface do jogo mantém a tradução bilíngue lado a lado.

### L25. Sistema de Fixação de 3 Níveis, Repetição Espaçada e Perda Cirúrgica de 1 Item
- **Armadilha:** Por padrão, o mod SayIt marca o item como dominado permanentemente logo após a primeira tentativa bem-sucedida (`pr.words().put(id, score)`), sem qualquer curva de retenção nem repetição espaçada. Além disso, se o jogador errar uma única pronúncia, a função padrão `takeAll()` varre todo o inventário e remove pilhas inteiras (64 itens) de uma só vez, gerando frustração extrema.
- **Solução:**
  1. **Níveis de Fixação (1 a 3):** Reutilizar o mapa `pr.words()` (que armazena `Map<String, Integer>`) para guardar o nível de fixação do vocábulo (`1`, `2` ou `3`). O `scanInventory` só ignora permanentemente itens cujo nível for $\ge 3$.
  2. **Repetição Espaçada:** Ao acertar o nível 1, aplicar um cooldown de 3 minutos (3600 ticks) antes de reexigir o vocábulo. Ao acertar o nível 2, aplicar um cooldown de 5 minutos (6000 ticks) para consolidar a fixação final no nível 3.
  3. **Penalidade Cirúrgica e Regressão:** Em caso de erro, regredir o nível de fixação em 1 (ex: de 2/3 para 1/3; de 1/3 para 0/3) e remover estritamente **1 único item** (`takeOne`/`dropOne` decrementando 1 item do cursor ou inventário com `method_7933(1)`), com um cooldown de carência de 30 segundos para evitar loops imediatos de falha.
  4. **Feedback Visual:** Exibir o progresso tanto nas mensagens de chat (`§e★ [Fixação X/3]`) quanto na interface do Dicionário (`★★★ [3/3]`) e nos tooltips KubeJS (`§a[Fixação] ★★★ (3/3 - Fixado!)`).



