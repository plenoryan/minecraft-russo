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

### L25. Sistema de Fixação em 5 Níveis, Repetição Espaçada, Curva do Esquecimento (Decay) e Perda Cirúrgica
- **Armadilha:** Por padrão, o mod SayIt marca o item como dominado permanentemente logo após a primeira tentativa bem-sucedida (`pr.words().put(id, score)`), sem qualquer curva de retenção nem repetição espaçada. Além disso, se o jogador errar uma única pronúncia, a função padrão `takeAll()` varre todo o inventário e remove pilhas inteiras (64 itens) de uma só vez. Por outro lado, imunidade eterna com poucos níveis não reflete o aprendizado real de uma língua, permitindo que vocábulos sejam esquecidos após dias sem uso.
- **Solução:**
  1. **Níveis de Fixação (1 a 5):** Reutilizar o mapa `pr.words()` para representar a escala pedagógica: 1/5 (Iniciante), 2/5 (Básico), 3/5 (Intermediário), 4/5 (Avançado) e 5/5 (Mestre).
  2. **Repetição Espaçada em Jogo Ativo:** Intervalos progressivos entre desafios de um mesmo item: 2 min (L1), 5 min (L2), 12 min (L3) e 25 min (L4). Ao atingir o nível 5 (Mestre), o item nunca é silenciado para sempre: ele entra em modo de revisão periódica a cada 60 minutos de jogo ativo.
  3. **Curva do Esquecimento (Decay Temporal Real):** Campo persistente `timestamps` gravado no JSON do jogador (`saves/<Mundo>/sayitenglish/<UUID>.json`). Se o jogador passar dias sem jogar ou sem praticar o item (48h para L5, 24h para L4, 16h para L3, 8h para L2, 4h para L1), a fixação regride gradualmente e o jogo notifica o jogador no chat (`§6⚠ [Curva do Esquecimento]`) para falar novamente e recuperar a maestria.
  4. **Penalidade Cirúrgica e Carência de Inventário:** Em caso de erro, regredir 1 nível e remover estritamente **1 único item** (`takeOne`/`dropOne` decrementando o stack com `method_7933(1)`). Manter 30 segundos de carência no inventário para que itens restantes na mesma pilha não sejam consumidos em loop contínuo enquanto o jogador respira. Se o jogador não possuir mais o item, nenhuma remoção ocorre e o jogo aguarda uma nova coleta.
  5. **Feedback Visual Completo:** Exibição consistente em 5 estrelas no Chat, nos Tooltips KubeJS (`§a[Fixação] ★★★★★ (5/5 - Mestre!)`) e no menu visual do Dicionário (`★★★★★ [5/5 - Mestre]`).

### L26. Requisitos de Versão do Fabric Loader em Mods Recentes (Farmer's Delight 2.1.1 vs 2.5.x e Naturalist 4.0.3 vs 2.0.9)
- **Armadilha:** Builds lançadas recentemente para Minecraft 1.20.1 (ex: Farmer's Delight 2.5.7 e Naturalist 2.0.9) impõem dependências rígidas de `fabricloader >= 0.18.2` ou `>= 0.19.5` no `fabric.mod.json`. Como o modpack utiliza Fabric Loader 0.16.14 (padrão estável amplamente compatível com Sodium 0.5.13 e Lithium 0.11.4), a inicialização do jogo ou servidor dedicado falha imediatamente com `net.fabricmc.loader.impl.FormattedException: Incompatible mods found!`.
- **Solução:** Em modpacks consolidados 1.20.1 com loader 0.16.x, utilizar as versões canônicas e comprovadas dos mods: `FarmersDelight-1.20.1-2.1.1+refabricated.jar` (compatível com `fabric: *`) e `naturalist-fabric-4.0.3-1.20.1.jar` (compatível com `geckolib >= 4.0.0` e `fabric: *`). Sempre validar as dependências declaradas no manifesto antes da implantação.

### L27. Expansão de Vocabulário Prático, Vida Selvagem e Imersão Doméstica
- **Armadilha:** Mods de conteúdo adicional introduzem centenas de blocos, animais e itens que não possuem traduções pedagógicas nos arquivos de idioma, resultando em visualizações genéricas em inglês ou quebras na extração de fala do SayIt. Além disso, mods de renderização/som colocados no servidor dedicado causam poluição e potenciais falhas de inicialização headless.
- **Solução:**
  1. **Separação Cliente vs Servidor:** Mods de conteúdo com blocos/entidades (`FarmersDelight`, `naturalist`, `geckolib`, `handcrafted`, `resourcefullib`) são instalados no cliente e no servidor. Mods estritamente sonoros/visuais de imersão (`PresenceFootsteps`, `AmbientSounds`, `CreativeCore`) são restritos às instâncias do cliente.
  2. **Bilingual Resource Pack Namespaces:** Registrar os namespaces específicos (`assets/farmersdelight/lang/`, `assets/naturalist/lang/`, `assets/handcrafted/lang/`) com separação de nomes limpos em `ru_ru.json` e bilíngues em `pt_br.json`.
  3. **KubeJS Pedagogical Tooltips:** Mapear os itens de maior frequência cotidiana (facas, frigideiras, panelas, cebola, tomate, repolho, arroz, torta de maçã, carnes de caça, peles, cascos de tartaruga) com notas fonéticas, gênero gramatical e contexto cultural eslavo.

### L28. Exclusão Cirúrgica de Itens sem Duplicação, Novas Interações Criativas e Shaders Imersivos Iris
- **Armadilha:** Na implementação anterior da penalidade por erro no SayIt, a função `dropOne` instanciava uma `ItemEntity` no chão aos pés do jogador (`new class_1542(...)`) com 40 ticks de atraso para coleta. Como os contêineres de inventário do cliente (`playerScreenHandler`) não recebiam atualização imediata (`method_7623()`), o cliente mantinha a exibição do item na hotbar enquanto o jogador, ao se mover, pegava automaticamente a entidade dropada do chão após 2 segundos, resultando na criação de +1 item (duplicação) ao invés da exclusão.
- **Solução:**
  1. **Exclusão Atômica e Imutável (`deleteOne`):** Substituir toda lógica de drop por `deleteOne(player, item)`. O item é decrementado diretamente do cursor, do slot ativo da hotbar ou do inventário geral sem NUNCA criar uma entidade no mundo (`ItemEntity`). Chamar explicitamente `sendContentUpdates()` (`method_7623()`) tanto em `currentScreenHandler` (`field_7512`) quanto em `playerScreenHandler` (`field_7498`), forçando sincronização instantânea do pacote de slot para a UI do cliente.
  2. **Interações Criativas Situacionais (Russo & Português):** O motor de observação (`watch`) agora detecta 6 novos estados comportamentais dinâmicos do jogador em tempo real:
     - Natação/Mergulho (`swim`): `player.method_5799()` ("Я плыву в воде." / "Estou nadando na água.")
     - Em Chamas (`fire`): `player.method_20802() > 0` ("Я горю, мне нужна вода!" / "Estou pegando fogo, preciso de água!")
     - Montaria/Veículo (`ride`): `player.method_5854() != null` ("Я еду верхом." / "Estou cavalgando.")
     - Furtividade/Agachamento (`sneak`): `player.method_5715()` ("Я крадусь бесшумно." / "Estou andando agachado em silêncio.")
     - Escalada (`climb`): `player.method_6101()` ("Я лезу наверх." / "Estou subindo.")
     - Voo com Élitros (`fly`): `player.method_6128()` ("Я лечу как птица." / "Estou voando como um pássaro.")
     Todas as frases possuem frases correspondentes em cirílico (`ru_ru.json`) e português (`pt_br.json`) sincronizadas no JAR e no Resource Pack.
  3. **Shaders Leves e Pacote HD de Texturas:**
     - `iris-1.7.6+mc1.20.1.jar` integrado exclusivamente no cliente com aceleração Sodium, compatível com a GPU AMD Radeon RX 6750 XT.
     - Shaderpack `ComplementaryReimagined_r5.9.3.zip` ativado por padrão em `iris.properties` (`enableShaders=true`).
     - Pacote de texturas `Faithful 32x - 1.20.1.zip` posicionado abaixo do `Russian_Bilingual_Educational_Pack` no `options.txt`, mantendo a prioridade pedagógica bilíngue sobre os visuais em alta resolução.

### L29. Validação Estrita de Frases Longas, Patch de Uniforms 1.20.1 Iris e Pacotes de Imersão Visual
- **Armadilha:**
  1. **Truncamento de Frases no STT:** Em desafios de sentenças longas (`sentence`), o mod SayIt calculava a pontuação média proporcional acumulada e definia `passed = true` assim que o score parcial atingia o limiar, antes de o jogador pronunciar todas as palavras da frase.
  2. **Loop de Recarregamento e Congelamento dos Shaders:** O shaderpack `ComplementaryReimagined_r5.9.3` referenciava variáveis de biomas de versões futuras (Minecraft 1.21.4 `BIOME_PALE_GARDEN`, `BIOME_SULFUR_CAVES`) e `endFlashIntensity`. No Iris 1.7.6 em 1.20.1, a detecção de variável inexistente disparava `java.lang.RuntimeException: Unknown variable: BIOME_PALE_GARDEN` a cada mudança de câmera/bioma, destruindo e recriando o pipeline gráfico e travando o jogo.
  3. **Inconsistência de Tradução e Tooltips:** Dicionários de tooltip em KubeJS continham traduções genéricas (ex: `'minecraft:wooden_pickaxe': { pt: 'Picareta' }`) que divergiam do nome completo do item em português ("Picareta de Madeira"). Além disso, resets no `options.txt` desativavam pacotes de textura externos.
- **Solução:**
  1. **Critério Rígido de Conclusão de Frase:** Em `ChallengeSession.java`, a aprovação de desafios do tipo `sentence` agora exige estritamente que a contagem de palavras pronunciadas seja igual ou superior ao tamanho da frase alvo (`heardWords.size() >= targetWords.size()`) juntamente com score `>= Math.max(current.passScore(), 85)`.
  2. **Patch Cirúrgico de Uniforms no Shaderpack:** No arquivo interno `shaders/shaders.properties` de `ComplementaryReimagined_r5.9.3.zip`, as variáveis incompatíveis foram neutralizadas para `=0.0` (`inPaleGarden=0.0`, `inSulfurCaves=0.0`, `endFlashFactor0=0.0`, `endFlashFactor1=0.0`, `endFlashIntensityM=0.0`), eliminando 100% dos loops de recarregamento e crashes.
  3. **Texturas Imersivas Leves (1.20.1):** Instalados `Dramatic Skys.zip` (céus HD realistas, estrelas e iluminação atmosférica), `Better-Leaves-9.6.zip` (folhagem 3D ultra-otimizada Motschen) e `Fast Better Grass.zip` (blocos de grama contínuos sem perda de FPS).
  4. **Ativos Permanentes via KubeJS:** Espelhado todo o diretório de idiomas multilíngues em `kubejs/assets/`, garantindo que os nomes bilíngues sejam carregados de forma mandatória e incondicional no frame 1 mesmo em caso de regeneração do `options.txt`.

### L30. Prevenção de Byte Order Mark (BOM) em .properties/.txt e Imunidade Unicode (\uXXXX) em Java
- **Armadilha:**
  1. Ao salvar arquivos de texto no PowerShell via `[System.Text.Encoding]::UTF8`, o .NET insere um preâmbulo UTF-8 Byte Order Mark (`0xEF 0xBB 0xBF`) no byte 0.
  2. Em `options.txt`, a primeira linha `\uFEFFversion:3465` não é reconhecida como `version`, fazendo o Minecraft assumir versão 0 e rodar o DataFixer legacy `OptionsKeyLwjgl3Fix`, que quebra com `NumberFormatException: For input string: "key.mouse.right"` e reseta todo o `options.txt` para os padrões vanilla (desativando todos os resource packs).
  3. No shaderpack Iris, o pré-processador C (`org.anarres.cpp`) trava em `Bad token [»@1,1]:"»"`, rejeitando `shaders.properties`, impedindo o registro de `voxel_sampler` e desligando os shaders completamente no mundo.
  4. Strings literais compiladas pelo `javac` sem flag de encoding no Windows herdam Cp1252, gerando caracteres corrompidos (`Â§eâ˜…`, `FixaÃ§Ã£o`).
- **Solução:**
  1. **Zero BOM Obrigatório:** Salvar arquivos de propriedades e `options.txt` exclusivamente com `[System.Text.Encoding]::ASCII` ou `New-Object System.Text.UTF8Encoding($false)`.
  2. **Escapes Unicode Nativos (`\uXXXX`):** Converter todas as strings das classes Java (`ChallengeManager`, `DictionaryScreen`) para escapes hexadecimais puros (`\u00a7`, `\u2605`, `\u2714`, `\u00e7`, `\u00e3`), tornando os binários 100% imunes a variações de charset da JVM host.
  3. **Junções NTFS Globais no Launcher:** `resourcepacks`, `shaderpacks` e `kubejs` foram convertidos em junções automáticas vinculadas à `Instancia_Cliente`, garantindo sincronismo atômico entre cliente e inicializador.

### L31. Progressão de Fixação por Estrelas (Sub-Repetições, Delays Expandidos e Proteção de Itens Raros)
- **Armadilha:** No modelo anterior, cada teste aumentava 1 nível linear imediato até o nível 5 em poucos segundos. Não havia diferenciação entre "descobrir a palavra" e "fixação profunda", sobrecarregando o jogador.
- **Solução:**
  1. **Arquitetura de 5 Estrelas:** Estrela 1 (5 testes rápidos com intervalos de 1s a 5s -> descanso de 2.5 min). Estrelas 2, 3, 4 e 5 possuem testes espaçados com pausas progressivas de 5, 10, 20 e 35 minutos.
  2. **Escaneamento Contínuo de Posse:** `scanInventory` inspeciona os 41 slots (incluindo mão secundária e armaduras equipadas).

### L32. Desafio Decisivo de Itens Raros: 7 Tentativas e Eliminação por Maioria (4 Erros)
- **Armadilha:** Proteger itens raros com imunidade total contra perda reduzia a tensão pedagógica e o senso de conquista ao descobrir itens lendários (como ovos de tartaruga, diamantes ou élitros). Ao mesmo tempo, penalidades imediatas no primeiro erro eram desproporcionais para vocabulários complexos.
- **Solução:**
  1. **Bateria de 7 Tentativas na Descoberta (Estrela 1):** Para o primeiro estágio de qualquer item raro (`isRare(id) && currentStars == 0`), o jogador passa por uma bateria de até 7 tentativas.
  2. **Regra de Eliminação por Maioria Simples:**
     - Se o jogador acumular **4 erros** (a maioria de 7), o desafio é encerrado imediatamente e **1 item raro é excluído do inventário** (`deleteOne(player, item)`), com reset de progresso e mensagem de alerta.
     - Se o jogador concluir as 7 tentativas com **menos de 4 erros** (portanto, alcançando 4 ou mais acertos, a maioria absoluta), a **Estrela 1 (★☆☆☆☆)** é conquistada com sucesso, liberando um intervalo de descanso de ~3 minutos.
  3. **Persistência de Erros e Acertos:** Implementado mapa dedicado `errors` em `Progress.java` (`errors()`) espelhado em JSON, mantendo a contagem de acertos e falhas mesmo se o jogador deslogar ou fechar o jogo durante a bateria.
  4. **Proteção Pós-Descoberta:** Uma vez que o item raro alcança a Estrela 1, as etapas subsequentes (Estrelas 2 a 5) reativam o escudo de proteção (`Item Raro Protegido`), evitando a perda acidental do item conquistado durante as revisões de longo prazo.

### L33. Vazamento de Escopo em Closures no Rhino (KubeJS) & Equivalência Semântica Árvore/Madeira
- **Armadilha:**
  1. **Vazamento de Variável de Closure no Rhino:** Laços do tipo `for (const [id, data] of Object.entries(dict))` no motor Rhino (utilizado pelo KubeJS) não criam ativação léxica isolada por iteração. Como consequência, todos os callbacks `event.addAdvanced` capturavam a referência mutável de `data`, fazendo com que TODOS os 25 itens do currículo exibissem os dados do último item registrado (`naturalist:antler` -> `Chifre de Veado`).
  2. **Incongruência Semântica (Дерево / Madeira vs Árvore):** Em russo, `Дерево` significa primariamente `Árvore` (planta) e secundariamente `Madeira` vegetal. A tradução isolada como "Madeira" fazia o SayIt exigir "Derevo" na fala enquanto a UI exibia apenas "Madeira", gerando dissonância pedagógica perceptível pelo usuário.
- **Solução:**
  1. **Isolamento Funcional via `forEach`:** No `russian_tooltips.js`, a iteração foi refatorada para `Object.keys(ruCurriculum).forEach(function(itemId) { var entry = ruCurriculum[itemId]; ... })`. O `forEach` garante um novo quadro de pilha (activation record) na JVM/Rhino para cada chave, isolando permanentemente os dados de cada item. Validado diretamente com a engine Rhino embutida.
  2. **Unificação Bilíngue "Árvore / Madeira":** Em todos os arquivos de linguagem (`kubejs/assets/minecraft/lang/pt_br.json`, `resourcepacks/.../pt_br.json`, `russian_tooltips.js` e na missão `01_derevo.json`), a nomenclatura de `oak_log` e variantes de madeira foi consolidada como `"Árvore / Madeira [RU: Дереvo]"`. Agora, tanto a pronúncia falada (`Дерево`) quanto a legenda em português transmitem equivalência semântica exata.

### L34. Regra da Maioria Contínua para Itens Raros (Sem Escudo de Proteção)
- **Armadilha:**
  1. Conceder imunidade ou "escudo de proteção permanente" a itens raros nas estrelas avançadas (2 a 5) remove o risco real e a tensão pedagógica de pronúncia do jogo.
- **Solução:**
  1. **Bateria de 7 Tentativas Unificada:** Em todas as etapas de fixação (Estrelas 0 a 5), itens raros operam com uma bateria contínua de 7 tentativas por nível.
  2. **Perda por Maioria de Erros (4/7):** Se o jogador cometer 4 erros em qualquer nível de estrela, 1 item raro é imediatamente deletado do inventário (`deleteOne`) e o nível de estrelas regride (`currentStars - 1`). Não existe escudo protetor que impeça a perda.
  3. **Avanço por Maioria de Acertos:** Ao concluir as 7 tentativas com menos de 4 erros (4 ou mais acertos), a próxima estrela é conquistada e o temporizador de descanso longo do nível é acionado.

### L35. Armadilha de Mapeamento Intermediary: `method_7933` (Increment) vs `method_7934` (Decrement)
- **Armadilha:**
  1. Na classe `net.minecraft.class_1799` (ItemStack) do Fabric Intermediary 1.20.1, `method_7933(int)` realiza `iadd` (`this.count += amount`), ou seja, **INCREMENTA** o item. O método correspondente a decremento é `method_7934(int)` (`ineg + method_7933`), ou diretamente `method_7939(count - 1)` (`setCount`).
  2. O código legado de `deleteOne` invocava `st.method_7933(1)`, duplicando e somando +1 item ao inventário toda vez que o jogador errava uma pronúncia, ao invés de excluí-lo.
- **Solução:**
  1. **Redução Atômica e Esvaziamento Explicito:** Em `deleteOne`, se `count <= 1`, o slot é esvaziado explicitamente com `class_1799.field_8037` (`ItemStack.EMPTY`). Se `count > 1`, o método `method_7939(count - 1)` reduz matematicamente a contagem em 1.
  2. **Tratamento de Penalidade `applyPenaltyOne`:** Suporta tanto exclusão pura (`"delete"`) quanto descarte no chão (`"drop"` via `dropOne`) de acordo com a configuração de `Config.get().penalty`.

### L36. Cobertura Universal de Tooltips Bilíngues com SayIt e Romanizer (KubeJS)
- **Armadilha:** O script `russian_tooltips.js` registrava apenas 37 itens no `ruCurriculum`. Qualquer outro item do jogo (ex: `minecraft:feather`, `minecraft:beef`, `minecraft:iron_sword`, itens de mods) que acionasse um desafio de voz do SayIt não possuía nenhum tooltip em russo no inventário, exibindo apenas o nome padrão em português.
- **Solução:**
  1. **Fallback Universal via `addAdvancedToAll`:** Implementado listener global de tooltips no KubeJS (`event.addAdvancedToAll`). Itens já registrados no `ruCurriculum` são ignorados no loop global para manter suas notas fonéticas e gramaticais artesanais intactas.
  2. **Interrogação Dinâmica via `Words.word` e `Romanizer.of`:** Para qualquer outro item, o script consulta dinamicamente `com.spicesfire.sayitenglish.client.Words.word(itemId)` para extrair a tradução russa oficial e `Romanizer.of(ru, "ru")` para obter a transcrição fonética latina automática.
  3. **Validação Cirílica e Cache em Memória:** Validação estrita por regex `[\u0400-\u04FF]` para assegurar que apenas itens com traduções russas genuínas recebam a tarja `★ Russo: <Palavra> [<Pronúncia>]`, com cache in-memory (`dynamicWordCache`) para garantir 60+ FPS sem overhead no inventário.
  4. **Exibição Dinâmica de Estrelas de Maestria:** Tanto itens do currículo quanto itens descobertos dinamicamente consultam `ChallengeSession.words` e `scores`, exibindo a progressão real de estrelas do jogador (`★☆☆☆☆` a `★★★★★`) diretamente no tooltip do item.

### L37. Pedagogia do Alfabeto Cirílico: Mnemônicas, Cartilha Heracles e Decomposição Luso-Cirílica
- **Armadilha:** Exigir a leitura de palavras inteiras em cirílico sem um método mnemônico estruturado gera sobrecarga cognitiva, especialmente devido aos falsos amigos (В=V, Н=N, Р=R, С=S, У=U, Х=Kh) que induzem pronúncias errôneas no microfone e perda de itens.
- **Solução:**
  1. **Trilha Heracles `tier0_alfabeto` (Cartilha Cirílica Cardinal):** 5 missões lineares estruturadas por frequência e relevância cardinal (Gêmeas -> Falsos Amigos -> Construtores -> Chiantes -> Iodizadas/Sinais Ь/Ъ), concedendo XP e o Dicionário de Bolso logo na primeira lição.
  2. **Decomposição "Soletrando" nos Tooltips (KubeJS):** Ao segurar SHIFT sobre qualquer item, o script decompõe a palavra russa letra por letra com seus fonemas individuais (`Д [d] • е [ye] • р [r] • е [ye] • в [v] • о [o]`).
  3. **Alertas Mnemônicos Dinâmicos:** Se a palavra contiver letras de alto risco (falsos amigos), o tooltip emite alerta destacado (`В=V (vitória) | Р=R (roda)`).
  4. **Ponte de Transição Luso-Cirílica (Substituição Suave):** Projeta a palavra em português com as letras cirílicas equivalentes inseridas gradualmente (`Áрворе / Мадеira`), ativando a associação visual direta entre os alfabetos.

### L38. Motor Híbrido Progressivo de 32 Estágios e Desacoplamento de Missões do Heracles
- **Armadilha:**
  1. **Bypass Visual pela Linha Fonética:** No HUD original do SayIt (ChallengeHud.java), a renderização simultânea da palavra em cirílico (Дерево) e da transcrição fonética latina logo abaixo ([DYE-rye-va]) criava um atalho cognitivo involuntário: sob pressão de tempo (5s a 18s), o cérebro ignorava 100% dos caracteres cirílicos e lia apenas a fonética latina, anulando o aprendizado do alfabeto.
  2. **Conclusão Trivial por Coleta de Itens:** Nas missões da trilha tier0_alfabeto do Heracles, as tarefas utilizavam heracles:item associadas a itens genéricos (minecraft:stick, minecraft:coal, minecraft:oak_log). Como resultado, qualquer ação básica de sobrevivência (como quebrar folhas e pegar um graveto) completava instantaneamente a missão do alfabeto, declarando "18% do alfabeto dominado" sem que o jogador tivesse aprendido nenhuma letra.
- **Solução:**
  1. **Motor de Substituição Letra por Letra (Alphabet.java):** Criada a classe Alphabet implementando uma sequência canônica de 32 níveis pedagógicos dividida em 5 fases (Gêmeas Transparentes -> Falsos Amigos Cardinais -> Construtores/Gregas -> Chiantes/Sons Eslavos -> Iodizadas e Modificadores Ь/Ъ). Cada nível é desbloqueado a cada 2 estrelas de domínio acumuladas nos desafios (totalStars / 2), exigindo dezenas de itens para atingir a fluência completa.
  2. **Renderização de Palavra Única no HUD (ChallengeHud.java):** A linha de trapaça fonética latina separada foi eliminada do HUD. A palavra alvo agora inicia 100% em letras latinas (Derevo) e se converte cirilicamente caractere por caractere conforme o nível de alfabeto do jogador (Derevо -> Dеrево -> Дерево). A linha inferior passa a exibir exclusivamente a mnemônica e instrução da letra ativa no nível atual (Nível 7/32 • Letra Ativa: В, в [V] (Parece B, mas tem som de V!)).
  3. **Desacoplamento e Automação de Missões Heracles:** Substituídas as tarefas de itens das 5 missões de tier0_alfabeto por tarefas do tipo heracles:check. No servidor (ChallengeManager.java), quando o jogador atinge os marcos de nível (6, 12, 19, 26 e 32), o servidor executa atomicamente o comando heracles complete <quest_id> <player> além de emitir anúncio de celebração no chat, garantindo que as missões só progridam mediante avanço fonético autêntico no microfone.
  4. **Sincronia com Tooltips KubeJS:** No russian_tooltips.js, itens inspecionam dinamicamente Alphabet.calculateLevel e Alphabet.toHybrid, exibindo a projeção do texto híbrido e a letra ativa no inventário em tempo real.

### L39. Desbloqueio Inteligente Contextual, Mapa de Calor e Conhecimento Compartilhado
- **Armadilha:**
  1. No desbloqueio global estático (L38), ao praticar uma palavra (ex: `Дерево`), o aumento linear do nível global desbloqueava a letra `А` (nível 1). Porém, `Дерево` não contém a letra `А`! Se o jogador em seguida encontrasse uma palavra com `А`, esta aparecia em cirílico de repente sem nunca ter sido praticada, provocando sobrecarga cognitiva e sensação de perda de controle.
  2. Palavras novas com muitas letras raras podiam aparecer com caracteres cirílicos desconhecidos de surpresa, sem que o jogador tivesse contato prévio com elas.
- **Solução:**
  1. **Desbloqueio Contextual por Palavra (`computeUnlockedLetters` em `Alphabet.java`):** Praticar um item desbloqueia estritamente as letras presentes naquele item, ordenadas da mais fácil para a mais difícil (`getLettersOrderedByDifficulty`). Em `Дерево`, o progresso libera estritamente `О -> Е -> В -> Р -> Д`. Letras não contidas no item (como `А`) NUNCA são liberadas por ele.
  2. **Mapa de Calor de Exposição (`heatMap` / `CLIENT_HEAT`):** Letras ouvidas e vistas com frequência acumulam "calor" de exposição. Ao atingir o limiar pedagógico da categoria (Gêmeas: 3x, Falsos Amigos: 5x, Construtores: 7x, Chiantes: 9x, Iodizadas: 12x), a letra é assimilada e desbloqueada de forma orgânica.
  3. **Conhecimento Compartilhado:** Letras assimiladas em uma palavra são imediatamente reconhecidas em todas as outras palavras do jogo (ex: dominar `В`, `О` e `Д` em `Дерево` e assimilar `А` via mapa de calor faz com que `Вода` apareça instantaneamente 100% legível em cirílico).
  4. **Foco Pedagógico e Tooltips Vivos:** O HUD foca na próxima letra a ser dominada (`Em Foco: <Letra> • Calor: X/Yx`), e os tooltips do KubeJS (`russian_tooltips.js`) exibem a contagem exata de domínio (`X/Y letras`) e a análise de calor ao segurar SHIFT (`о: ✔ | е: 2/3x | в: 1/5x`).

### L40. Prevenção de Poluição Linguística Global, Reset de Heatmap no Cliente e Isolamento Pedagógico Gradual
- **Armadilha:**
  1. Ao injetar tags `[RU: <palavra>]` nos arquivos `pt_br.json` (via resource packs e KubeJS), os nomes nativos de todos os itens e blocos do jogo eram permanentemente alterados em português. Como consequência, o Jade e a interface exibiam cirílico antes mesmo de o jogador iniciar o aprendizado ("tudo já está 100% russo").
  2. Em `ChallengeHud.render`, a chamada `recordExposure(p.target(), 1)` executava a cada quadro gráfico (60 a 144 FPS). Em 0,2s, as letras do desafio atingiam o teto de calor, convertendo a palavra inteira em cirílico instantaneamente.
  3. `Alphabet.CLIENT_HEAT` persistia na memória estática da JVM entre mundos, fazendo novos mundos herdarem letras já desbloqueadas em sessões anteriores.
  4. Tooltips exibiam na primeira linha a palavra completa em cirílico (`★ Russo: Дéрево [DYE-rye-va]`), anulando o mistério e a transição gradual.
- **Solução:**
  1. **Purificação do Português:** Removidas todas as tags `[RU: ...]` de todos os arquivos `pt_br.json` em `kubejs/assets/` e no resource pack. Blocos e itens mantêm seus nomes limpos em português.
  2. **Eliminação do Bug de 60 FPS:** Removido qualquer registro de calor de métodos de renderização de interface (`ChallengeHud.render`).
  3. **Reset Atômico de Heatmap no Cliente:** `ChallengeSession.sync` invoca `Alphabet.CLIENT_HEAT.clear()` ao ingressar em qualquer mundo, garantindo estado zero em novos mundos.
  4. **Rebalanceamento de Limiares:** Limiares elevados para acúmulo real e orgânico (Gêmeas: 15, Falsos Amigos: 25, Construtores: 40, Chiantes: 60, Iodizadas: 80), com adição de calor desduplicada por palavra.
  5. **Tooltips Graduais:** Para palavras não dominadas (< 5 estrelas), a linha principal exibe `★ Treino Russo: <híbrido>` (ex: `Derevo (0/5 letras)`). A forma 100% russa e fonética completa só é revelada ao segurar SHIFT.

### L41. Integração com Jade Overlay, Resolução de Ambiguidade de Falsos Amigos (Cores e Alertas) e Painel Persistente de Fala ("O Que Eu Disse")
- **Armadilha:**
  1. Ao remover as tags `[RU: ...]` de `pt_br.json` (L40), a mira do Jade voltou a exibir apenas o nome em português para blocos naturais no mundo (água, flores, grama, árvores), perdendo a oportunidade de aprendizado visual imersivo com pronúncia.
  2. Na substituição híbrida gradual de letras, homóglifos e falsos amigos geram confusão visual intransponível: a letra `P` pode ser tanto o `P` latino (/p/) quanto o `Р` cirílico (/r/). Sem distinção visual, o jogador não sabe qual som pronunciar.
  3. Ao emitir uma frase inteira no microfone em desafios do tipo `sentence`, o reconhecimento concluía e o HUD sumia instantaneamente no mesmo tick, impedindo o jogador de conferir o que o motor de voz ouviu e qual era a tradução da frase dita.
- **Solução:**
  1. **Plugin Nativo de Jade (`SayItJadePlugin.java` via `@WailaPlugin`):** Registrado entrypoint `jade` em `fabric.mod.json`. O Jade agora inspeciona blocos, fluidos, plantas e entidades na mira e adiciona dinamicamente a linha educacional `★ Russo: <Palavra> [<Pronúncia>]` (ex: `Água` -> `★ Russo: Вода [Voda]`), mantendo o nome principal 100% limpo em português.
  2. **Diferenciação Cromática & Alerta de Falsos Amigos (`toSmartHybrid` com `formatCodes=true`):** Todas as letras cirílicas assimiladas são formatadas em **Dourado (`§6`)**, enquanto as letras latinas provisórias permanecem em **Branco (`§f`)**. No HUD, alertas contextuais ativos informam os falsos amigos (`§6Р§7=som de §eR§7 (Roda)`). A ambiguidade é 100% eliminada: `P` branco é /p/, `Р` dourado é /r/!
  3. **Quadro Lateral Persistente "O Que Eu Disse" (`SpeechFeedback`):** Ao concluir qualquer desafio (em especial sentenças), um painel dedicado é fixado no canto superior direito por **6 segundos**, exibindo a transcrição reconhecida em russo, a pontuação de precisão e a tradução correspondente em português.

### L42. Aplicação de ASD-STE100 em Mensagens e Despoluição Visual via Action Bar
- **Armadilha:**
  1. O chat do jogo ficava sobrecarregado com mensagens a cada repetição fonética individual (`[Descoberta 2/5] Correto!`, `[Tentativa 3/7] Correto!`), poluindo a janela de log com rolagem constante e distraindo o jogador.
  2. Notificações de letras cirílicas desbloqueadas geravam blocos de texto de 2 a 3 linhas por caractere (`[DESBLOQUEIO INTELIGENTE] Nova Letra Cirílica Assimilada... \n Mnemônica... \n (Agora ativa em todas as palavras!)`), repletos de elogios redundantes e poluição visual.
- **Solução:**
  1. **Separação Arquitetural de Feedback (Action Bar vs Chat):**
     - Feedback efêmero de repetições intermediárias de treino (`[Teste 2/5] Correto (85%) • Próximo em 3s` ou `[Item Raro 3/7] Erro (40%) • Falhas: 1/4 • Próximo em 5s`) foi redirecionado para a **Action Bar** via `player.method_7353(component, true)`. O texto é exibido diretamente acima da barra de vida/itens e desaparece suavemente após 2 segundos, gerando **zero acúmulo de mensagens no chat**.
     - Eventos canônicos de estado (conquista de estrelas, desbloqueio de letras, perdas de itens e curva de esquecimento) permanecem no Chat (`player.method_43496`), garantindo registro duradouro de conquistas essenciais.
  2. **Padronização Estrita ASD-STE100 (Simplified Technical English):**
     - Todas as mensagens foram condensadas em uma única linha factual, sem quebras de linha (`\n`), sem elogios vazios ou excesso de exclamações.
     - Formato padrão estruturado: `[CATEGORIA] Objeto: Status (Métricas)` (ex: `§6[Alfabeto] Letra ativa: §eО, о [O] §7• Idêntica ao português`, `§a[Progresso] Estrela 1/5 concluída. Próximo teste em 2 min.`, `§c[Falha] Item Raro: 4 erros atingidos (45%). 1 item dropado. Progresso zerado.`).

### L43. Mecânicas Cooperativas Multiplayer, Minidesafios Dinâmicos, Compactação do HUD e Progressão de Alfabeto em Dois Eixos
- **Armadilha:**
  1. Em sessões multiplayer, os jogadores agiam de forma isolada, focados apenas nas próprias tarefas e itens.
  2. A caixa de fala ("o que eu disse") aparecia para qualquer palavra solta, ocupando espaço desnecessário e poluindo o HUD com textos longos.
  3. Coletar apenas um bloco de madeira no início do jogo desbloqueava letras cirílicas não-idênticas precocemente, substituindo caracteres antes do tempo.
  4. Falta de dinamismo no gameplay vanilla sem sobrecarregar a memória com mods pesados de conteúdo.
- **Solução:**
  1. **Mecânicas Cooperativas (`CoopManager` & `Packets`):**
     - Invocação de frases práticas entre jogadores (`CoopInvoke`): pedidos de socorro, madeira, comida, seguir e apontar alvos, com envio de pistas compreensíveis para o parceiro (`CoopCue`).
     - Minidesafios dinâmicos/kamikaze (ex: "O chão é lava"): qualquer jogador pode acionar livremente o evento, exigindo sobrevivência coordenada.
     - Sistema de ranking de cooperação em 5 patamares pedagógicos (`Новичок`, `Товарищ`, `Следопыт`, `Мастер Команды`, `Легенда`), persistido em JSON no servidor.
  2. **Compactação Estrita do HUD de Fala:**
     - Caixa de feedback suprimida completamente para itens de palavra única (`speechFeedback = null`).
     - Ativação exclusiva para sentenças/frases completas, com dimensões compactadas (150px) e cabeçalho `"disse: "`.
  3. **Progressão Cirílica em Dois Eixos (`Alphabet.java`):**
     - Letras transparentes/idênticas (`А, О, Е, Т, К, М`) continuam com substituição fluida.
     - Letras não-idênticas ao alfabeto latino agora exigem maturidade de estágio global e domínio da palavra (mínimo de estrelas e repetições) para evitar substituição precoce.
  4. **Stack de Mods QoL e Desempenho:**
     - Integrados mods Fabric 1.20.1 leves: `Omnimovement` (movimentação ágil/sprint), `Inventory Sorting` (organização de baús e inventário com um clique), `Stack to Nearby Chests` (depósito rápido em contêineres próximos), `Entity Culling` e `AppleSkin`. Segregação rígida cliente/servidor mantida (zero mods de interface no servidor dedicado).

### L44. Incompatibilidade Fatal entre Krypton e e4all no Pipeline de Criptografia do Login
- **Armadilha:**
  - O mod `krypton` tenta aplicar um `@Redirect` em `onKey$initializeVelocityCipher` dentro de `ServerLoginNetworkHandlerMixin`. No entanto, o mod `e4all` (essencial para túnel P2P LAN sem portas abertas) já aplica um `@Redirect` com a mesma prioridade (1000) no mesmo método (`getCipher`).
  - O SpongePowered Mixin descarta o redirecionamento conflitante do Krypton, fazendo com que a checagem pós-injeção do Krypton falhe com `Critical injection failure: (0/1) succeeded`, derrubando o cliente Minecraft antes mesmo da tela de título.
- **Solução:**
  - `krypton` é estritamente **proibido** e foi removido de todas as instâncias (cliente e servidor).
  - A otimização de rede pura do Krypton é dispensável, enquanto a capacidade do `e4all` de fornecer túnel multiplayer P2P sem portas é fundamental para o projeto. Modpack estabilizado com os 5 mods QoL restantes.

### L45. Reconhecimento de Itens Compostos em Russo (Adjetivo + Substantivo), Relaxamento Fonético de Sinal Brando (ь) e Subtokens no Vosk
- **Armadilha:**
  - No Minecraft em russo, dezenas de itens essenciais são locuções nominais de 2 ou 3 palavras (ex: `item.minecraft.raw_copper` = `"Рудная медь"`, `copper_ore` = `"Медная руда"`, `raw_iron` = `"Необработанное железо"`).
  - O motor original de pontuação (`Scorer.java`) comparava a fala ouvida apenas contra a concatenação total da frase alvo. Se o jogador dizia apenas o substantivo principal (`"медь"`), a similaridade Levenshtein ficava limitada a 4/10 = 40%, falhando a meta (70-80%) e dropando o item como penalidade.
  - Além disso, a gramática restrita do Vosk continha apenas a frase inteira (`"рудная медь"`), e `ChallengeSession.offer` descartava qualquer palpite com `if (s < 95) return;`.
  - Falantes nativos de português brasileiro frequentemente omitem a palatalização estrita do sinal brando russo (`ь` em `медь` -> `мед`), sofrendo rejeição acústica no modelo Vosk.
- **Solução Implementada:**
  1. **Scorer com Correspondência de Subtokens e Substantivo Principal (`Scorer.java`):**
     - Alvos com $\ge 2$ palavras agora avaliam similaridade por palavras constituintes. Se o jogador pronuncia o substantivo principal com acurácia $\ge 75\%$, a pontuação recebe patamar de aprovação garantida (85% a 95%).
  2. **Relaxamento de Sinal Brando (`ь`/`ъ`):**
     - Em avaliações cirílicas, variantes fonéticas com ou sem sinal brando (`мед` vs `медь`) são tratadas com equivalência fonética, evitando bloqueios acústicos para não-nativos.
  3. **Injeção de Subtokens na Gramática Restrita do Vosk (`ChallengeSession.java`):**
     - Em `ChallengeSession.start`, subtokens (`медь`, `рудная`) e formas fonéticas (`мед`) são injetados automaticamente em `alternatives`, permitindo que o Vosk capture palavras isoladas com alta confiança.
  4. **Filtro de Confiança e Conclusão Imediata:**
     - Descarte restrito apenas a alternativas não relacionadas (`s < 60`), preservando palpites com correspondência real.
     - Desafios de item (`kind = "word"`) concluem com aprovação imediata assim que `s >= passScore`.


### L46. Roda de Seleção Radial ('V'), Portão Pedagógico de Voz Estrito, HUD Visual no Parceiro e Desvinculação do Iris 'R'
- **Armadilha:**
  1. O mod Iris vinculava por padrão o recarregamento de shaders à tecla 'R' (`key_iris.keybind.reload:key.keyboard.r`), congelando o jogo ao acidentalmente pressionar a tecla durante movimentação ou bate-papo.
  2. Modificar arquivos de configuração como `options.txt` com scripts que salvam arquivos em UTF-8 com BOM (`\uFEFF`) corrompe a leitura de opções do Minecraft, fazendo o jogo resetar todas as configurações para o padrão vanilla.
  3. Enviar ações cooperativas diretamente pela interface sem praticar a pronúncia quebrava a premissa pedagógica de aprendizado imersivo de russo.
  4. Avisos cooperativos puramente textuais no chat passavam despercebidos durante a exploração ou combate conjunto.
- **Solução Implementada:**
  1. **Desvinculação Segura do Atalho Iris 'R':**
     - Em `options.txt` do cliente e nas instâncias do Launcher, a linha foi alterada para `key_iris.keybind.reload:key.keyboard.unknown` em ASCII puro estrito (Zero-BOM).
     - Adicionado guard programático em `SayItClient` no startup para garantir que a tecla 'R' não reative o recarregamento de shaders.
  2. **Roda de Seleção Radial ('V') com Portão de Voz Estrito (`RadialMenuScreen` & `ChallengeSession`):**
     - Criada a `RadialMenuScreen` (sem pausa de jogo, permitindo multiplayer fluido) com 6 setores circulares mapeados trigonometricamente (`wood`, `food`, `help`, `lava`, `follow`, `waypoint`).
     - A seleção da ação inicia obrigatoriamente a sessão de desafio de fala (`ChallengeSession.startCoopChallenge`). O pacote de rede `Packets.CoopInvoke` só é despachado se a pronúncia alcançar nota $\ge 85\%$. Em caso de cancelamento, silêncio ou nota insuficiente, nenhum pacote é enviado ao parceiro.
  3. **HUD Visual no Receptor (`ChallengeHud`):**
     - Ao receber `Packets.CoopCue`, o cliente renderiza uma notificação gráfica destacada contendo a cabeça/skin do jogador que solicitou via `PlayerFaceRenderer.method_44443` (com fallback offline seguro via `DefaultSkinHelper`), acompanhada do ícone do item solicitado e um ponto de interrogação `'?'` sobreposto via `DrawContext.method_51432`.
  4. **Mods de Vida Cotidiana e Dicionário de Móveis:**
     - Integrados simetricamente os mods *Farmer's Respite* (chaleira, café, chás) e *Comforts* (sacos de dormir e redes) no cliente e servidor dedicado.
     - Adicionadas 109 chaves de tradução bilíngues (ru_ru e pt_br) em KubeJS e resourcepack, e registradas anotações fonéticas pedagógicas em `russian_tooltips.js`.

### L47. Generalização da Roda Radial para Qualquer Item: Tela de Busca Dinâmica, Desafio de Pronúncia e Verificação Cooperativa
- **Armadilha:**
  - O setor 0 da roda radial era limitado a uma solicitação estática de madeira ("Мне нужно дерево!"). Em jogabilidade de sobrevivência real, jogadores precisam pedir os mais variados itens (minérios, ferramentas, itens cotidianos de mods, blocos).
  - Além disso, abrir uma tela GUI no Minecraft por padrão pode acidentalmente pausar o jogo em modo singleplayer ou LAN se `shouldPause()` / `method_25421()` retornar `true`.
- **Solução Implementada:**
  1. **Tela de Busca de Itens (ItemSearchScreen.java):**
     - Tela de busca sem pausa de jogo (`method_25421() = false`), integrada diretamente ao setor 0 da roda radial.
     - Caching sob demanda de todo o catálogo do Minecraft e de mods (`class_7923.field_41178`), com busca em tempo real em português, russo ou identificador técnico (`class_342`).
     - Botão de atalho rápido para pedir o item atualmente segurado na mão principal (`[Pedir item na mão: ...]`), além de atalhos rápidos para recursos frequentes.
  2. **Desafio de Voz e Protocolo de Rede Dinâmicos:**
     - Ao selecionar qualquer item, o jogo inicia o desafio de voz com a frase russa correspondente (`Мне нужно: <NomeRusso>!`) e pronúncia avaliada pelo `Scorer` e Vosk.
     - Ao atingir >= 85%, envia `Packets.CoopInvoke("item:<id>", "")`.
  3. **Reconhecimento no HUD do Receptor e Servidor:**
     - `ChallengeHud.resolveActionStack` decodifica `item:<id>` e exibe o ícone exato do item solicitado com o distintivo '?' ao lado da cabeça do jogador solicitante.
     - O servidor (`ChallengeManager$CoopManager`) rastreia o objetivo `REQUEST_ITEM` com verificação de proximidade (<= 6 blocos) e presença do item específico no inventário do parceiro (`hasItem`), concedendo +20 pontos cooperativos para ambos ao concluir.
### L48. Expansão Imersiva: Fauna Realista (Critters & Ecologics), Ruínas/Vilas (Towns & Towers, MVS) e Interação Física (Exposure & Carry On)
- **Armadilha:**
  - A versão mais recente do mod Critters and Companions (2.7.1) no Modrinth possui dependência declarada incorreta com `fabricloader >= 0.19.3`, impedindo a inicialização no Fabric Loader 0.16.14 com erro fatal de resolução de dependências (`HARD_DEP_NO_CANDIDATE`).
  - Além disso, mods de geração de mundo e entidades precisam ser rigorosamente sincronizados em ambas as instâncias (cliente e servidor dedicado) para evitar descompassos de chunk e desync de pacotes de rede.
- **Solução Implementada:**
  1. **Fauna da Vida Real (Critters & Companions 2.3.5 + Ecologics 2.2.7):**
     - Instalada a versão estável 2.3.5 de Critters & Companions (compatível com Fabric Loader >= 0.15) e Ecologics 2.2.7 em cliente e servidor dedicado.
     - Adicionadas criaturas da vida real com vocabulário eslavo autêntico: Lontras (Выдра), Furões (Хорёк), Libélulas (Стрекоза), Esquilos (Белка), Pinguins (Пингвин) e Caranguejos (Клешня краба / Пальмовый вор).
  2. **Ruínas Rústicas e Vilas Vivas (Towns and Towers 1.12 + Moog's Voyager Structures 5.1.3):**
     - Vilas com arquitetura e portos adaptados para cada bioma, além de ruínas abandonadas e postos de viajantes na natureza selvagem. Suportadas por Cristel Lib e Moog's Structure Lib em ambas as instâncias.
  3. **Interação Física e Registro Imersivo (Exposure 1.9.24 + Carry On 2.1.2.7):**
     - Exposure: Câmera fotográfica analógica funcional com filmes preto-e-branco (Чёрно-белая плёнка) e colorido (Цветная плёнка), revelação em câmara escura e montagem de álbuns de fotos (Фотоальбом).
     - Carry On: Permite levantar pequenos animais e baús nos braços com Shift + Botão Direito.
  4. **Anotações Pedagógicas Bilíngues (ussian_tooltips.js):**
     - Adicionadas 14 novas entradas completas no currículo bilíngue com pronúncia fonética e notas gramaticais de gênero/caso.

### L49. Desconexao em Sessao LAN/VPN: Limite de Tempo Netty (30s Read Timeout) e Tamanho de Pacotes
- **Armadilha:**
  - Em conexoes peer-to-peer (via Radmin VPN ou tuneis WAN), o outro jogador sofria desconexoes repetidas com as mensagens "lost connection: Tempo esgotado" (Timed Out) e "java.net.SocketException: Connection reset".
  - O Minecraft vanilla possui um temporizador rigido de leitura Netty (ReadTimeoutHandler) fixado em apenas 30 segundos. Durante geracao intensiva de chunks (dripstones, vilas do Towns & Towers), picos de jitter de rede na VPN ou sincronizacao de NBT/avancos de mods, a ausencia temporaria de pacotes por 30s provocava a queda imediata do jogador.
  - Alem disso, pacotes grandes de sincronizacao de mods excedendo o limite de buffer vanilla provocavam reset subito de socket TCP no cliente.
- **Solucao Implementada:**
  1. **Instalacao do Mod Packet Fixer (v3.3.2):**
     - Integrado packetfixer-3.3.2-1.18-1.20.4-merged.jar em Instancia_Cliente\mods e Servidor_Dedicado\mods.
     - Criado config/packetfixer.properties elevando o timeout para 180 segundos (timeout=180) e ativando allSizesUnlimited=true com suporte ampliado a NBT (200MB) e VarInt/VarLong.
  2. **Alternativa Direta com e4all/e4mc:**
     - Utilizar o dominio gerado pelo mod e4all (ex: consent-rental.cl.e4mc.link exibido no chat ao abrir para LAN) em vez do IP virtual do Radmin VPN elimina completamente os problemas de MTU e bloqueio de firewall do Radmin VPN.
  3. **Otimizacao de Renderizacao do Host:**
     - Reduzir a distancia de renderizacao (View Distance) do host de 12 para 8 ou 10 durante partidas LAN diminui sensivelmente o volume de pacotes transmitidos pela rede.

### L50. Desafios de Frase Cooperativa e Prevencao de Concorrencia/Sobreposicao de Itens de Inventario
- **Armadilha:**
  1. No motor de pronuncia (Scorer), a correspondencia de palavras constituintes (t.size() >= 2) elevava indevidamente o escore para 85%-95% ao reconhecer apenas uma unica palavra isolada (ex: "Мне"), aprovando a frase antes mesmo do jogador pronunciar o item ou o restante da solicitacao.
  2. No servidor, a rotina de inventario (scanInventory) e a fila de repeticao espacada disparavam desafios de itens a cada poucos segundos sem intervalo de respiro. Quando o jogador abria a Roda Radial ou a Tela de Busca para pedir um item ao amigo, a chegada de um pacote Packets.Challenge do servidor executava ChallengeSession.stop(), sobrepondo e destruindo o pedido ativo.
- **Solucao Implementada:**
  1. **Cobertura Integral de Frases (Scorer.java & ChallengeSession.java):**
     - Removido o boost isolado de palavras constituintes em alvos multi-palavras, confiando no calculo de wordCoverage (proporcional ao percentual real de palavras articuladas).
     - No cliente (ChallengeSession.offer e finish), a aprovacao de qualquer frase exige estritamente que a quantidade de palavras ouvidas seja compativel com o alvo (heardWords.size() >= targetWords.size()) e que o escore alcance o limiar exigido (s >= passThreshold).
  2. **Protocolo de Bloqueio Concorrente (coop_busy:true / coop_busy:false):**
     - Ao abrir a Roda Radial ('V') ou a Tela de Busca de Itens, o cliente despacha CoopInvoke("coop_busy:true").
     - O servidor pausa imediatamente as checagens de inventario para o jogador e, caso haja um desafio de palavra em andamento, o reagenda na fila sem aplicar penalidades.
     - Apos concluir o pedido de fala cooperativa ou fechar as telas, o cliente despacha coop_busy:false.
  3. **Guarda Defensiva no Cliente (ChallengeSession.start):**
     - Rejeita qualquer pacote de desafio entrante enquanto pendingCoopAction != null ou enquanto as telas de selecao de acao estiverem visiveis.
  4. **Intervalo de Respiro no Servidor (ChallengeManager.tick):**
     - Fixado intervalo minimo de 15 segundos (300 ticks) entre desafios automatizados de inventario, eliminando a sensacao de bombardeio ininterrupto de itens.