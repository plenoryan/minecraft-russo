# Análise Técnica e Plano de Implementação: Requisitos R2 e R3

**Data**: 2026-11-04  
**Alvo**: SayIt Modpack (Minecraft 1.20.1 Fabric)  
**Módulos Analisados**: `com.spicesfire.sayitenglish` (Client & Server) e Scripts KubeJS  
**Status**: Investigação Concluída com Sucesso

---

## 1. Visão Geral Executiva

Esta investigação detalha o diagnóstico e o planejamento arquitetural para os requisitos:
- **R2 (Ajuste do HUD de Feedback de Fala)**: Restrição da caixa "O Que Eu Disse" para ser renderizada **exclusivamente** em desafios de frases completas (eliminando-a em desafios de itens individuais/palavra única), enxugamento de dimensões visuais para um formato compacto e adoção estrita do cabeçalho `"disse: "`.
- **R3 (Calibração da Progressão do Alfabeto Cirílico)**: Desacoplamento da fixação prematura de caracteres cirílicos não-idênticos (como `Д`, `В`, `Р`) resultante de ações básicas iniciais (ex: coletar madeira e praticar `Дерево`), estabelecendo um algoritmo calibrado em dois eixos: manutenção da substituição natural para caracteres visualmente idênticos (`А`, `О`, `Е`, `Т`, `К`, `М`) e progressão gradual multi-passo (estágio cardinal global + fixação por repetições) para caracteres não-idênticos (`lvl > 6`).

---

## 2. Requisito R2 — Ajuste do HUD de Feedback de Fala

### 2.1. Diagnóstico do Estado Atual

Na versão atual da classe `com.spicesfire.sayitenglish.client.ChallengeHud` e `ChallengeSession`:
1. **Instanciação Incondicional**: Em `ChallengeSession.finish()` (linhas 216-225 de `src/com/spicesfire/sayitenglish/client/ChallengeSession.java`), ao concluir qualquer desafio fonético, o campo estático `ChallengeHud.speechFeedback` é instanciado incondicionalmente:
   ```java
   String heardText = (bestHeard != null && !bestHeard.isEmpty()) ? bestHeard : (heard != null ? heard : "");
   ChallengeHud.speechFeedback = new ChallengeHud.SpeechFeedback(
       heardText,
       prompt != null ? prompt.nativeText() : "",
       prompt != null ? prompt.target() : "",
       best,
       ok,
       c.kind(),
       System.currentTimeMillis()
   );
   ```
2. **Renderização Cega**: Em `ChallengeHud.render(class_332 g, float partialTick)` (linhas 78-86 de `src/com/spicesfire/sayitenglish/client/ChallengeHud.java`), se `speechFeedback != null` e `age <= 6000L` (6 segundos), o método invoca `renderSpeechFeedback(g, mc, font, fb, age)` independentemente do tipo de desafio (`"word"`, `"practice"`, `"sentence"`).
3. **Layout Volumoso e Cabeçalho Informal**:
   Em `ChallengeHud.renderSpeechFeedback` (linhas 217-245):
   - O cabeçalho utiliza a string informal:
     `String title = (fb.success() ? "\u00a7a\u2714 O Que Eu Disse " : "\u00a7c\u2716 O Que Eu Disse ") + "\u00a7e(" + fb.score() + "%)";`
   - O painel possui largura mínima exagerada de 220px (`Math.max(220, textW + 24)`) e altura de 48px a 60px com 4 linhas verticais (Título, Fala com aspas, Tradução nativa, e Alvo redundante).
   - Isso polui a visão periférica superior direita do jogador durante tarefas rotineiras de sobrevivência (como pegar gravetos, blocos e pedras).

### 2.2. Discriminação entre Desafios de Palavra Única vs Frases Completas

No ciclo de vida do mod SayIt:
- **Desafio de Palavra Única (Item)**:
  - Disparado pelo servidor em `ChallengeManager.java:142` com `kind = "word"` (itens escaneados do inventário).
  - Disparado pelo dicionário/tecla de treino em `ChallengeManager.java:347` com `kind = "practice"` (itens já descobertos).
  - Em ambos os casos, `prompt = Words.word(c.subject())`. O alvo fonético (`prompt.target()`) consiste em um único substantivo (ex: `Дерево`, `Камень`, `Яблоко`, `Палка`).
- **Desafio de Frase Completa (Sentença / Coop)**:
  - Disparado pelo servidor em `ChallengeManager.java:150` com `kind = "sentence"` (eventos de ambiente: chuva, noite, mineração, combate, etc.).
  - Frases cooperativas do Requisito R1 (pedir item, apontar lugar, minidesafios dinâmicos como "o chão é lava", diálogos) enviadas com `kind = "sentence"` ou novo subtipo cooperativo.
  - Em todos esses casos, `prompt = Words.sentence(c.subject(), c.arg())` ou o alvo possui múltiplas palavras: `Scorer.words(prompt.target()).size() > 1`.

### 2.3. Plano de Supressão e Filtragem

A supressão do HUD em desafios de palavra única será aplicada em duas barreiras defensivas (Defense-in-Depth):

1. **Barreira 1 (Atribuição no Cliente - `ChallengeSession.finish`)**:
   - Avaliar se o desafio finalizado qualifica-se como frase completa:
     ```java
     boolean isSentence = "sentence".equals(c.kind()) 
         || (prompt != null && prompt.target() != null && Scorer.words(prompt.target()).size() > 1);
     ```
   - Se `isSentence` for falso, atribuir `ChallengeHud.speechFeedback = null`.
   - Se `isSentence` for verdadeiro, instanciar `SpeechFeedback`.

2. **Barreira 2 (Filtragem no Renderizador - `ChallengeHud.render`)**:
   - No loop gráfico de renderização:
     ```java
     SpeechFeedback fb = speechFeedback;
     if (fb != null) {
         long age = System.currentTimeMillis() - fb.shownAt();
         boolean isSentence = "sentence".equals(fb.kind()) 
             || (fb.target() != null && Scorer.words(fb.target()).size() > 1);
         if (age > 4500L || !isSentence) {
             speechFeedback = null;
         } else {
             ChallengeHud.renderSpeechFeedback(g, mc, font, fb, age);
         }
     }
     ```

### 2.4. Plano de Redesenho do Layout Visual

A caixa visual compacta atenderá estritamente às especificações:
1. **Cabeçalho Exclusivo**: O texto inicial deve ser rigorosamente `"disse: "` (sem "O Que Eu Disse", sem exclamações).
2. **Dimensões Compactas**:
   - Largura mínima reduzida de `220px` para `150px` (`int w = Math.min(screenW - 16, Math.max(150, textW + 16));`).
   - Altura reduzida de `60px` para `28px` a `38px` (`int h = 28 + (trans.isEmpty() ? 0 : 10);`).
   - Espaçamento de linhas condensado (passo de 10px ao invés de 12-14px).
   - Eliminação da linha repetitiva `"Alvo: <palavra>"`, mantendo apenas o essencial: cabeçalho + porcentagem, o que foi ouvido e a tradução nativa em português.

**Layout Proposto**:
```
+--------------------------------------------------------+---+
| disse:                                            85%  |   | <- Linha 1: "disse: " cinza/ouro + Score à direita
| "Ya rublyu derevo"                                     |   | <- Linha 2: Transcrição ouvida (branco)
| Tradução: Eu corto madeira                             |   | <- Linha 3 (se houver): Português (cinza)
+--------------------------------------------------------+---+
```

---

## 3. Requisito R3 — Calibração da Progressão do Alfabeto Cirílico

### 3.1. Causa Raiz da Fixação Prematura

Ao inspecionar `src/com/spicesfire/sayitenglish/Alphabet.java` (método `computeUnlockedLetters`, linhas 170-216), identificou-se o defeito exato:
```java
// 2. Individual word progress: stars on words unlock letters contained in those words
if (wordStars != null) {
    for (Map.Entry<String, Integer> entry : wordStars.entrySet()) {
        String wordOrId = entry.getKey();
        int stars = entry.getValue();
        if (stars <= 0) continue;

        String ru = getRuWord(wordOrId);
        List<Character> wordLetters = getLettersOrderedByDifficulty(ru);
        int unlockCount = (stars >= 5) ? wordLetters.size() : Math.min(stars, wordLetters.size());
        for (int i = 0; i < unlockCount; i++) {
            char c = wordLetters.get(i);
            unlocked.add(c);
            unlocked.add(Character.toUpperCase(c));
        }
    }
}
```

#### Mecânica da Falha:
1. Ao cortar uma árvore, o jogador obtém `minecraft:oak_log` (Madeira de Carvalho). A palavra correspondente no dicionário russo é `Дерево`.
2. As letras ordenadas por dificuldade (`getLettersOrderedByDifficulty`) em `Дерево` são:
   - `о` (Nível 2 — Gêmea Transparente / idêntica ao português)
   - `е` (Nível 6 — Gêmea Transparente / idêntica ao português)
   - `в` (Nível 7 — Falso Amigo / parece B, mas soa como V)
   - `р` (Nível 8 — Falso Amigo / parece P, mas soa como R)
   - `д` (Nível 13 — Construtor Grego / formato de cabana/domo)
3. No algoritmo legado:
   - Se o jogador atinge **Estrela 3** em `oak_log`, o loop libera `wordLetters[2]` (`в` — Nível 7).
   - Se o jogador atinge **Estrela 4**, o loop libera `wordLetters[3]` (`р` — Nível 8).
   - Se o jogador atinge **Estrela 5**, o loop libera `wordLetters[4]` (`д` — Nível 13).
4. O mesmo ocorre ao fabricar graveto (`minecraft:stick` = `Палка`):
   - Letras: `а` (1), `к` (4), `п` (14 — Construtor), `л` (17 — Construtor).
   - Com 3 ou 4 estrelas no graveto, os caracteres gregos `П` e `Л` entram imediatamente no conjunto global `unlocked`!
5. **Efeito Cascata Indesejado**:
   - O conjunto `unlocked` é global. Assim que `Д`, `П`, `Л`, `В`, `Р` entram em `unlocked`, a função `toSmartHybrid` substitui essas letras em **todos** os blocos, itens e tooltips do Minecraft.
   - Além disso, em `ChallengeManager.java:710-730`, o servidor avalia `newCount = Alphabet.getLetterCount(newUnlocked)`. Como `newCount` atinge rapidamente $\ge 6$ e $\ge 12$ apenas combinando madeira e gravetos, o servidor executava atomicamente os comandos `heracles complete 01_alfabeto_gemeas` e `heracles complete 02_alfabeto_falsos_amigos` precocemente.
   - O jogador via letras de nível 13 a 17 fixadas permanentemente no início do jogo, antes mesmo de dominar a fase introdutória!

### 3.2. Arquitetura da Calibração Pedagógica em Dois Eixos

Para resolver o problema sem regressão e com estrita fidelidade pedagógica, o algoritmo deve separar as letras em duas classes funcionais:

```
                          ALFABETO CIRÍLICO (33 Letras)
                                     |
         +---------------------------+---------------------------+
         |                                                       |
[EIXO 1: Letras Idênticas]                              [EIXO 2: Letras Não-Idênticas]
Gêmeas Transparentes (Níveis 1 a 6)                     Falsos Amigos, Construtores, Chiantes (7 a 32)
Letras: А, О, Т, К, М, Е                                Letras: В, Р, С, Н, У, Х, Д, П, Б, Л, И, Ж...
         |                                                       |
- Substituição natural imediata                         - Exigem progressão multi-passo:
- Desbloqueio suave por estrelas da palavra               1. Estágio global do jogador (globalLevel >= lvl)
- Zero atrito cognitivo                                   2. Fixação deliberada na palavra (stars >= 3)
- Sem regressões de compatibilidade                       3. Imunidade total após ações básicas iniciais
```

#### Regra do Eixo 1 — Caracteres Visualmente Idênticos:
- Definidos por `isIdentical(c)`: `LETTER_LEVEL.get(c) <= 6` (`а, о, т, к, м, е`).
- Mantêm desbloqueio natural: quando a palavra ganha estrelas (`stars >= 1`), suas letras idênticas são liberadas por ordem de dificuldade.
- Em `Дерево`, `о` (nível 2) e `е` (nível 6) desbloqueiam normalmente. O jogador vê `Derevо` e `Dеrеvо` em ouro cirílico, lendo naturalmente sem estranheza fonética.

#### Regra do Eixo 2 — Caracteres Não-Idênticos (Grafias Diferentes e Falsos Amigos):
- Definidos por `!isIdentical(c)`: `LETTER_LEVEL.get(c) > 6` (`в, р, с, н, у, х, д, п, б, г, л, и, й, ж, з, ч, ш, щ, ц, ф, я, ю, ё, ы, э, ь, ъ`).
- **Gating de Estágio Global**: O jogador só pode fixar permanentemente uma letra não-idêntica de nível $L$ se o seu nível geral de alfabeto acumulado (`globalLevel = calculateLevel(totalStars)`) for maior ou igual a $L$.
- **Gating de Fixação por Palavra**: A palavra específica deve ter alcançado pelo menos **3 estrelas** (`stars >= 3`), garantindo prática sólida e intencional.
- **Proteção do Heatmap**: No mapa de calor, caracteres não-idênticos exigem que o limiar de calor seja atingido e que o nível global esteja dentro do raio de alcance pedagógico (`globalLevel >= lvl - 3`), evitando vazamento de calor em exposições isoladas.

---

## 4. Contratos de Código e Diffs Propostos

### 4.1. Modificação em `src/com/spicesfire/sayitenglish/Alphabet.java`

```java
// =========================================================================
// NOVO MÉTODO AUXILIAR
// =========================================================================
public static boolean isIdentical(char ch) {
    Integer lvl = LETTER_LEVEL.get(Character.toLowerCase(ch));
    return lvl != null && lvl <= 6;
}

// =========================================================================
// REFORMULAÇÃO CALIBRADA DE computeUnlockedLetters
// =========================================================================
public static Set<Character> computeUnlockedLetters(Map<String, Integer> wordStars, Map<?, ?> heatMap) {
    Set<Character> unlocked = new HashSet<>();

    // Calcula o progresso global de estrelas acumuladas em todo o vocabulário
    int totalStars = 0;
    if (wordStars != null) {
        for (int s : wordStars.values()) {
            totalStars += Math.max(0, s);
        }
    }
    int globalLevel = calculateLevel(totalStars);

    // 1. Heatmap: letras vistas/ouvidas com frequência suficiente
    if (heatMap != null) {
        for (Map.Entry<?, ?> entry : heatMap.entrySet()) {
            char ch;
            Object key = entry.getKey();
            if (key instanceof Character) {
                ch = Character.toLowerCase((Character) key);
            } else {
                String s = String.valueOf(key);
                if (s.isEmpty()) continue;
                ch = Character.toLowerCase(s.charAt(0));
            }
            int count = 0;
            Object val = entry.getValue();
            if (val instanceof Number) {
                count = ((Number) val).intValue();
            }
            int lvl = LETTER_LEVEL.getOrDefault(ch, 99);
            int thresh = getHeatThreshold(ch);

            // Caracteres idênticos desbloqueiam no limiar; não-idênticos exigem maturidade de estágio
            if (count >= thresh) {
                if (lvl <= 6 || globalLevel >= Math.max(1, lvl - 3) || count >= thresh * 2) {
                    unlocked.add(ch);
                    unlocked.add(Character.toUpperCase(ch));
                }
            }
        }
    }

    // 2. Progresso por palavra: calibração em dois eixos (idênticos vs não-idênticos)
    if (wordStars != null) {
        for (Map.Entry<String, Integer> entry : wordStars.entrySet()) {
            String wordOrId = entry.getKey();
            int stars = entry.getValue();
            if (stars <= 0) continue;

            String ru = getRuWord(wordOrId);
            List<Character> wordLetters = getLettersOrderedByDifficulty(ru);
            int unlockCount = (stars >= 5) ? wordLetters.size() : Math.min(stars, wordLetters.size());

            for (int i = 0; i < unlockCount; i++) {
                char c = wordLetters.get(i);
                int lvl = LETTER_LEVEL.getOrDefault(c, 99);

                if (lvl <= 6) {
                    // EIXO 1: Caracteres idênticos (А, О, Т, К, М, Е) retêm substituição natural imediata
                    unlocked.add(c);
                    unlocked.add(Character.toUpperCase(c));
                } else {
                    // EIXO 2: Caracteres não-idênticos exigem avanço gradual no estágio global e maestria da palavra
                    if (globalLevel >= lvl && stars >= 3) {
                        unlocked.add(c);
                        unlocked.add(Character.toUpperCase(c));
                    }
                }
            }
        }
    }

    return unlocked;
}
```

### 4.2. Modificação em `src/com/spicesfire/sayitenglish/client/ChallengeSession.java`

```java
// Em finish() (linhas 214-226):
private static void finish() {
    Packets.Challenge c = current;
    ChallengeSession.release();
    boolean ok = best >= c.passScore();
    ClientPlatform.I.sendToServer((ModPacket)new Packets.Result(c.id(), best, bestHeard.length() > 200 ? bestHeard.substring(0, 200) : bestHeard));
    banner = new Banner(prompt.target(), best, ok, c.kind(), System.currentTimeMillis());
    
    // R2: Restringir speechFeedback exclusivamente para desafios de sentenças completas
    boolean isSentence = "sentence".equals(c.kind()) 
        || (prompt != null && prompt.target() != null && Scorer.words(prompt.target()).size() > 1);

    if (isSentence) {
        String heardText = (bestHeard != null && !bestHeard.isEmpty()) ? bestHeard : (heard != null ? heard : "");
        ChallengeHud.speechFeedback = new ChallengeHud.SpeechFeedback(
            heardText,
            prompt != null ? prompt.nativeText() : "",
            prompt != null ? prompt.target() : "",
            best,
            ok,
            c.kind(),
            System.currentTimeMillis()
        );
    } else {
        ChallengeHud.speechFeedback = null;
    }

    class_310 mc = class_310.method_1551();
    if (mc.field_1724 != null) {
        mc.field_1724.method_5783((class_3414)(ok ? class_3417.field_14725 : class_3417.field_14624).comp_349(), 0.8f, ok ? 1.2f : 0.6f);
    }
    current = null;
    prompt = null;
}
```

### 4.3. Modificação em `src/com/spicesfire/sayitenglish/client/ChallengeHud.java`

```java
// Em render() (linhas 78-86):
SpeechFeedback fb = speechFeedback;
if (fb != null) {
    long age = System.currentTimeMillis() - fb.shownAt();
    boolean isSentence = "sentence".equals(fb.kind()) 
        || (fb.target() != null && Scorer.words(fb.target()).size() > 1);
    if (age > 4500L || !isSentence) {
        speechFeedback = null;
    } else {
        ChallengeHud.renderSpeechFeedback(g, mc, font, fb, age);
    }
}

// Em renderSpeechFeedback() (linhas 217-245):
private static void renderSpeechFeedback(class_332 g, class_310 mc, class_327 font, SpeechFeedback fb, long age) {
    int screenW = mc.method_22683().method_4486();
    
    // R2: Cabeçalho estritamente "disse: " + badge de porcentagem
    String header = "disse: ";
    String scoreBadge = fb.score() + "%";
    String spoken = fb.heard().isEmpty() ? "\u00a77(sem \u00e1udio)" : "\u00a7f\"" + fb.heard() + "\"";
    String trans = fb.nativeTranslation().isEmpty() ? "" : "\u00a77" + fb.nativeTranslation();

    int textW = Math.max(font.method_1727(header) + font.method_1727(scoreBadge) + 16, Math.max(font.method_1727(spoken), font.method_1727(trans)));
    int w = Math.min(screenW - 16, Math.max(150, textW + 16));
    int x = screenW - w - 8;
    int y = 8;
    int h = 28 + (trans.isEmpty() ? 0 : 10);

    // Fundo semitransparente escuro
    ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + w), (int)(y + h), (int)-803726810);
    // Borda lateral de status (Verde para sucesso, Vermelha para erro)
    ClientCompat.fill((class_332)g, (int)(x + w - 2), (int)y, (int)(x + w), (int)(y + h), (int)(fb.success() ? -10755203 : -42406));

    int curY = y + 4;
    // Linha 1: "disse: " à esquerda e badge de pontuação à direita
    ClientCompat.text((class_332)g, (class_327)font, (String)header, (int)(x + 6), (int)curY, (int)-7697766);
    int badgeW = font.method_1727(scoreBadge);
    ClientCompat.text((class_332)g, (class_327)font, (String)scoreBadge, (int)(x + w - 6 - badgeW), (int)curY, (int)(fb.success() ? -10755203 : -42406));

    curY += 10;
    // Linha 2: Transcrição ouvida
    ClientCompat.text((class_332)g, (class_327)font, (String)spoken, (int)(x + 6), (int)curY, (int)-1);

    // Linha 3 (se presente): Tradução em português
    if (!trans.isEmpty()) {
        curY += 10;
        ClientCompat.text((class_332)g, (class_327)font, (String)trans, (int)(x + 6), (int)curY, (int)-4671288);
    }
}
```

---

## 5. Estratégia de Compilação, Empacotamento e Teste

### 5.1. Comando de Compilação Verificado
O compilador Java 17 local em `F:\Jogos\Mine\Java17\bin\javac.exe` foi testado e compilou as classes alvo com sucesso com a seguinte cadeia de classpath:
```powershell
$javaDir = "F:\Jogos\Mine\Java17\bin"
$javac = Join-Path $javaDir "javac.exe"

$cp = @(
    "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
    "F:\Jogos\Mine\Launcher\data\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar",
    "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
    "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar",
    "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\datafixerupper\6.0.8\datafixerupper-6.0.8.jar",
    "F:\Jogos\Mine\Launcher\data\libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"
) -join ";"

& $javac -cp $cp -encoding UTF-8 -d "F:\Jogos\Mine\src" `
    "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java" `
    "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java" `
    "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java"
```

### 5.2. Atualização Atômica dos JARs
Após compilar os `.class` para `F:\Jogos\Mine\src`:
Injetar os novos bytecodes dentro de:
1. `F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
2. `F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
3. Copiar para o cache de instâncias do Launcher se sincronizado.

### 5.3. Validação dos Testes E2E
Executar o comando oficial estrito:
```powershell
powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
```
Critério de aceitação: Todos os 45 testes passando com código de saída 0.
Adicionar novas asserções de verificação:
- Teste unitário para `Alphabet.computeUnlockedLetters`: dado `{"minecraft:oak_log": 1}`, verificar que `unlocked` contém `о`, mas NÃO contém `д`, `в` ou `р`.
- Teste para `ChallengeHud`: verificar ausência da string `"O Que Eu Disse"` e presença exclusiva do cabeçalho `"disse: "`.
