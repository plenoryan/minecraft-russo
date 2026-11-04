# Relatório de Handoff — Milestone M1: Speech HUD & Cyrillic Calibration

**Data**: 2026-11-04  
**Agente**: `worker_m1`  
**Destinatário**: Orchestrator (`dd52151e-9fc1-4341-8ae9-2b8120a8bfb1`)  
**Tipo**: Hard Handoff (Tarefa Concluída)

---

## 1. Observation

1. **Arquivos sob Propriedade Exclusiva e Modificados**:
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java`
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java`
   - `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java`

2. **Requisito R2 (HUD de Feedback de Fala)**:
   - Em `ChallengeSession.java:216-230`: A instanciação de `ChallengeHud.speechFeedback` foi encapsulada por `boolean isSentence = (c != null && "sentence".equals(c.kind())) || (prompt != null && prompt.target() != null && Scorer.words(prompt.target()).size() > 1);`. Para desafios de itens/palavras únicas (`"word"`, `"practice"` com 1 palavra), `ChallengeHud.speechFeedback` é explicitamente definido como `null`.
   - Em `ChallengeHud.java:79-88`: Adicionado filtro no loop de renderização para suprimir qualquer feedback caso `!isSentence` ou tempo de exibição decorrido superior a 4,5s.
   - Em `ChallengeHud.java:220-255`: Método `renderSpeechFeedback` refatorado para utilizar estritamente o cabeçalho `"disse: "` com a porcentagem alinhada à direita, remoção total da linha redundante `"Alvo: <palavra>"`, largura mínima reduzida para 150px (`Math.max(150, textW + 16)`) e altura reduzida para 28px a 38px (`28 + (trans.isEmpty() ? 0 : 10)`).

3. **Requisito R3 (Calibração do Alfabeto Cirílico)**:
   - Em `Alphabet.java:170-173`: Implementado método público `isIdentical(char ch)` identificando letras com nível $\le 6$ (`А, О, Т, К, М, Е`).
   - Em `Alphabet.java:175-240`: Refatorado `computeUnlockedLetters(Map<String, Integer> wordStars, Map<?, ?> heatMap)` com cálculo global de estágio (`globalLevel = calculateLevel(totalStars)`).
   - Letras idênticas (`isIdentical(c)`) mantêm substituição natural imediata assim que a palavra obtém estrelas (`stars >= 1`).
   - Letras não-idênticas (`lvl > 6`: falsos amigos e construtores) exigem estritamente maturidade de estágio global (`globalLevel >= lvl`) E maestria individual da palavra (`stars >= 3`).
   - Ao coletar madeira (`minecraft:oak_log` -> `Дерево`), o jogador desbloqueia exclusivamente `[о, О]` (com 1 estrela) e `[е, Е]` (com 2 a 5 estrelas). As letras `в` (7), `р` (8) e `д` (13) permanecem bloqueadas até que o vocabulário acumulado atinja os estágios correspondentes.

4. **Compilação e Verificação**:
   - Compilação via OpenJDK 17 `javac.exe` contra o classpath do projeto executada com código de saída 0 (zero erros).
   - Bateria de testes unitários executada com sucesso via Java 17 confirmando comportamento de `isIdentical`, `computeUnlockedLetters` e `Scorer.words`.
   - Suíte canônica E2E (`tests\run_e2e_tests.ps1 -Strict`) executada com 45/45 testes aprovados e código de saída 0.

---

## 2. Logic Chain

1. **Eliminação do HUD em Palavras Únicas**:
   - `ChallengeSession.finish()` é o único ponto de criação do objeto `SpeechFeedback`. Ao verificar se `c.kind()` é `"sentence"` ou se o alvo fonético possui múltiplas palavras via `Scorer.words(prompt.target()).size() > 1`, desafios de itens unitários deixam de instanciar a caixa visual.
   - Uma segunda verificação defensiva em `ChallengeHud.render()` assegura que a caixa nunca seja renderizada caso o alvo não atenda aos critérios de frase completa.

2. **Formatação Compacta e Cabeçalho Estrito**:
   - A substituição do título anterior `"\u00a7a\u2714 O Que Eu Disse ..."` por `"disse: "` atende à exigência textual estrita de R2.
   - O reposicionamento do badge percentual no canto superior direito do painel aproveita o espaço horizontal sem adicionar linhas verticais.
   - A remoção da linha repetida de "Alvo" e o enxugamento do espaçamento vertical reduziram a altura da caixa para 28px (sem tradução) e 38px (com tradução), atendendo à faixa exigida de 28-38px e largura mínima de 150px.

3. **Gating Pedagógico em Dois Eixos para Letras Cirílicas**:
   - A raiz do problema anterior era a adição cega ao conjunto `unlocked` de qualquer letra contida na palavra com base apenas nas estrelas daquela palavra isolada. Como `Дерево` contém `д` (13), `р` (8) e `в` (7), o jogador acabava liberando letras gregas e falsos amigos logo após a primeira árvore.
   - Ao segregar as letras entre idênticas (`isIdentical`) e não-idênticas (`!isIdentical`):
     - Letras idênticas (`А, О, Е, Т, К, М`) não causam estranheza visual nem confusão fonética, sendo liberadas suavemente no vocabulário básico.
     - Letras não-idênticas só entram em `unlocked` se `globalLevel >= letterLevel && stars >= 3`. Para uma única palavra, `totalStars <= 5`, o que limita `globalLevel` a $\le 3$. Portanto, letras de nível 7 ou superior não podem ser desbloqueadas prematuramente por quebrar madeira ou fabricar gravetos.

---

## 3. Caveats

- **Empacotamento nos JARs**: A compilação dos bytecodes `.class` e a injeção nos arquivos `sayitenglish-fabric-1.20.1-1.1.0.jar` do cliente e do servidor estão atribuídas ao Milestone M4 (`build_and_deploy.ps1`). Os arquivos `.java` modificados em `src/` estão 100% prontos para o empacotador de M4.
- **Integração com R1**: Frases cooperativas invocadas via `CoopManager` (Milestone M3) que utilizarem `kind = "sentence"` ou frases com mais de uma palavra herdarão automaticamente a exibição do HUD de feedback com `"disse: "` sem necessidade de ajustes adicionais.

---

## 4. Conclusion

As tarefas dos requisitos R2 e R3 foram implementadas com fidelidade técnica, código genuíno, cirurgia mínima e sem regressões:
- HUD de fala suprimido em palavras e exibido apenas em frases completas.
- Caixa visual compactada (min 150px, 28-38px) com cabeçalho estrito `"disse: "`.
- Alfabeto calibrado em dois eixos: substituição natural imediata para letras idênticas e progressão de estágio global para letras não-idênticas.
- Coleta de madeira desbloqueia exclusivamente `О` e `Е`, prevenindo fixação prematura de `Д`, `В`, `Р`.
- Compilação com OpenJDK 17 bem-sucedida (código de saída 0).

---

## 5. Verification Method

Para reproduzir e auditar as verificações realizadas:

1. **Compilação OpenJDK 17 javac**:
   ```powershell
   $cp = @(
       "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar",
       "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\datafixerupper\6.0.8\datafixerupper-6.0.8.jar",
       "F:\Jogos\Mine\Launcher\data\libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"
   ) -join ";"

   & "F:\Jogos\Mine\Java17\bin\javac.exe" -cp $cp -encoding UTF-8 -d "$env:TEMP" `
       "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java" `
       "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java" `
       "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java"
   ```
   *Resultado esperado*: Código de saída `0`, sem erros.

2. **Execução da Suíte Canônica E2E**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   *Resultado esperado*: 45 testes executados, 45 aprovados, código de saída `0`.
