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








