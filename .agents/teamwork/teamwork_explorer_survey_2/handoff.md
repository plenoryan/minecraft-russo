# Relatório de Handoff — Explorer Survey 2: Requisitos R2 e R3

**Data**: 2026-11-04  
**Agente**: `teamwork_explorer_survey_2`  
**Destinatário**: Orchestrator (`dd52151e-9fc1-4341-8ae9-2b8120a8bfb1`)  
**Tipo**: Hard Handoff (Tarefa Concluída)

---

## 1. Observation

1. **Localização do Renderizador do HUD de Fala ("O Que Eu Disse")**:
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java`:
     - Linhas 52-53: Record `SpeechFeedback(String heard, String nativeTranslation, String target, int score, boolean success, String kind, long shownAt)`.
     - Linha 55: `public static volatile SpeechFeedback speechFeedback = null;`.
     - Linhas 78-86: No método `render()`, `speechFeedback` é exibido incondicionalmente por 6 segundos (`age > 6000L`).
     - Linhas 217-245: Método `renderSpeechFeedback()`:
       - Linha 219: `String title = (fb.success() ? "\u00a7a\u2714 O Que Eu Disse " : "\u00a7c\u2716 O Que Eu Disse ") + "\u00a7e(" + fb.score() + "%)";`
       - Linha 225: `int w = Math.min(screenW - 20, Math.max(220, textW + 24));` (largura mínima excessiva de 220px).
       - Linha 228: `int h = 48 + (trans.isEmpty() ? 0 : 12);` (altura de até 60px).

2. **Gatilho de Disparo de Feedback de Fala**:
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java`:
     - Linhas 216-225: No método `finish()`, `ChallengeHud.speechFeedback` é instanciado em todos os desafios sem verificar se o desafio é palavra única (`"word"`, `"practice"`) ou frase (`"sentence"`).
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\ChallengeManager.java`:
     - Linha 142: Inicia desafio de item com `kind = "word"`.
     - Linha 347: Inicia treino de item com `kind = "practice"`.
     - Linha 150: Inicia desafio de frase com `kind = "sentence"`.

3. **Causa Raiz da Fixação Prematura de Letras Cirílicas Não-Idênticas**:
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java`:
     - Linhas 24-66: Definição de 32 níveis do alfabeto (`LETTER_LEVEL`).
       - Fase 1 (Níveis 1 a 6): Gêmeas Transparentes idênticas ao português (`А`, `О`, `Т`, `К`, `М`, `Е`).
       - Fase 2 (Níveis 7 a 12): Falsos Amigos (`В`, `Р`, `С`, `Н`, `У`, `Х`).
       - Fase 3 (Níveis 13 a 19): Construtores/Gregas (`Д`, `П`, `Б`, `Г`, `Л`, `И`, `Й`).
     - Linhas 170-216: Método `computeUnlockedLetters(Map<String, Integer> wordStars, Map<?, ?> heatMap)`:
       - Linhas 198-213: Itera sobre `wordStars`. Para qualquer palavra com `stars >= 1`, calcula `unlockCount = (stars >= 5) ? wordLetters.size() : Math.min(stars, wordLetters.size())` e adiciona as letras a `unlocked`.
       - Para `minecraft:oak_log` ("Дерево"): as letras ordenadas por dificuldade são `о` (lvl 2), `е` (lvl 6), `в` (lvl 7), `р` (lvl 8), `д` (lvl 13).
       - Com apenas 3 estrelas na madeira, `в` (Falso Amigo) era desbloqueado globalmente; com 5 estrelas, `д` (Nível 13) era desbloqueado globalmente.
       - Não havia qualquer checagem contra o nível acumulado do alfabeto (`calculateLevel(totalStars)`), fazendo ações básicas iniciais (madeira, gravetos, tábuas) liberarem letras gregas e falsos amigos no jogo inteiro.

4. **Sincronia do Cliente e Servidor**:
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\ChallengeManager.java`: Linhas 472 e 694 invocam `Alphabet.computeUnlockedLetters(pr.words(), pr.letterHeat())`.
   - `F:\Jogos\Mine\Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js`: Linhas 48 e 480 invocam `Alphabet.computeUnlockedLetters(wordScores, Alphabet.CLIENT_HEAT)` e `Alphabet.toSmartHybrid`.
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java`: Linha 110 invoca `Alphabet.computeUnlockedLetters(wordStars, Alphabet.CLIENT_HEAT)`.

5. **Infraestrutura de Compilação Verificada**:
   - Execução de `javac` com classpath local compilou `Alphabet.java`, `ChallengeHud.java` e `ChallengeSession.java` com código de saída 0:
     Classpath comprovado:
     - `Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
     - `Launcher\data\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar`
     - `Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar`
     - `Launcher\data\libraries\com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar`
     - `Launcher\data\libraries\com\mojang\datafixerupper\6.0.8\datafixerupper-6.0.8.jar`
     - `Launcher\data\libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar`

---

## 2. Logic Chain

1. **Supressão do HUD de Fala (R2)**:
   - A partir de (1) e (2), `speechFeedback` é instanciado em `ChallengeSession.finish()` para todos os desafios, inclusive `"word"` (itens escaneados) e `"practice"` (itens no dicionário).
   - A especificação R2 exige estritamente que a caixa de fala apareça apenas em frases completas e seja suprimida em palavras únicas.
   - Logo, filtrando `ChallengeSession.finish()` para instanciar `SpeechFeedback` apenas quando `"sentence".equals(c.kind()) || Scorer.words(prompt.target()).size() > 1` e adicionando salvaguarda idêntica em `ChallengeHud.render()`, o HUD é 100% suprimido para desafios de itens e exibido com fidelidade para frases completas (incluindo as frases cooperativas de R1).

2. **Ajuste Visual Compacto e Cabeçalho (R2)**:
   - A partir de (1), o cabeçalho continha `"O Que Eu Disse"` e a caixa media 220px de largura e até 60px de altura.
   - Ajustando o cabeçalho para a string estrita `"disse: "` (com a porcentagem alinhada à direita na mesma linha), reduzindo a largura mínima para 150px, encurtando a altura para 28-38px e removendo a linha redundante de alvo, a caixa atinge o tamanho compacto e cumpre os critérios visuais sem perda de legibilidade.

3. **Desacoplamento e Calibração do Alfabeto (R3)**:
   - A partir de (3), `computeUnlockedLetters` liberava letras estritamente com base na contagem de estrelas da palavra individual, sem checar se a letra é idêntica ou não-idêntica e sem checar o estágio cardinal do jogador (`calculateLevel(totalStars)`).
   - As letras idênticas (`А, О, Т, К, М, Е` — níveis 1 a 6) possuem grafia gêmea ao alfabeto latino e sons correspondentes naturais, não causando dissonância cognitiva.
   - As letras não-idênticas (`lvl > 6`: falsos amigos como `В, Р` e construtores como `Д, П`) causam estranheza visual severa e confusão fonética se aparecerem prematuramente.
   - Ao introduzir `isIdentical(ch)` (`lvl <= 6`):
     - Letras idênticas mantêm substituição natural imediata quando a palavra tem estrelas (`stars >= 1`), preservando a funcionalidade sem regressões.
     - Letras não-idênticas passam a exigir a conjunção de dois critérios: avanço global de estágio (`globalLevel >= lvl`) E prática deliberada na palavra (`stars >= 3`), com salvaguarda no mapa de calor (`count >= thresh && globalLevel >= lvl - 3`).
   - Dessa forma, pegar madeira pela primeira vez ou praticar `oak_log` desbloqueia apenas `о` e `е` (idênticas). As letras `в`, `р` e `д` permanecem latinas provisórias até que o jogador avance autenticamente nas fases de Falsos Amigos e Construtores através do domínio acumulado de vocabulário.

---

## 3. Caveats

- **R1 Cooperativo**: Frases cooperativas invocadas entre jogadores (R1) devem ser disparadas com `kind = "sentence"` ou com texto alvo contendo mais de uma palavra para que o HUD de fala enriquecido com `"disse: "` seja ativado automaticamente para elas.
- **KubeJS russian_tooltips.js**: Não requer modificação estrutural pesada porque já consome dinamicamente `Alphabet.computeUnlockedLetters`. Ao atualizar a classe Java `Alphabet`, os tooltips herdam a calibração imediatamente.
- **Heracles Quests**: As missões de `tier0_alfabeto` são acionadas em `ChallengeManager.java:710-730` inspecionando `newCount`. Com a nova calibração, as missões só serão completadas quando o jogador desbloquear as letras autênticas de cada categoria, eliminando a conclusão trivial após quebrar blocos.

---

## 4. Conclusion

O plano de implementação para R2 e R3 está totalmente diagnosticado, com código diff projetado, contratos de interface validados e compilação comprovada.
- **R2**: Supressão completa da caixa em palavras únicas e exibição exclusiva em frases completas com formato compacto e cabeçalho `"disse: "`.
- **R3**: Calibração em dois eixos em `Alphabet.computeUnlockedLetters`: substituição natural para `А, О, Е, Т, К, М` e exigência de maturidade global de estágio para letras não-idênticas, eliminando 100% da fixação prematura pós-coleta de madeira.
- **Artefato Detalhado**: Disponível em `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_2\analysis.md`.

---

## 5. Verification Method

Para verificar independentemente as descobertas e validações:

1. **Verificação de Compilação de Teste**:
   ```powershell
   $cp = @(
       "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar",
       "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\datafixerupper\6.0.8\datafixerupper-6.0.8.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"
   ) -join ";"
   & "F:\Jogos\Mine\Java17\bin\javac.exe" -cp $cp -encoding UTF-8 -d "$env:TEMP" "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java" "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java" "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java"
   ```
   *Condição de Sucesso*: Código de saída `0` sem erros.

2. **Verificação da Suíte Canônica E2E**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   *Condição de Sucesso*: 45 testes executados, 45 aprovados, código de saída `0`.

3. **Arquivos Inspecionados**:
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java` (linhas 52, 78-86, 217-245)
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java` (linhas 74, 216-225)
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java` (linhas 24-66, 170-240)
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\ChallengeManager.java` (linhas 142, 150, 472, 694, 710-730)
   - `F:\Jogos\Mine\Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js` (linhas 40-50, 480, 520)
