# Original User Request

## Approved Task Prompt — 2026-11-04T11:51:26Z

Expandir o mod SayIt (Minecraft 1.20.1 Fabric) com mecânicas cooperativas multiplayer para incentivar os jogadores a jogarem juntos, ajustar o HUD de feedback de fala, calibrar o ritmo de desbloqueio do alfabeto cirílico e integrar mods adicionais de dinamismo e qualidade de vida (QoL).

Working directory: F:\Jogos\Mine
Integrity mode: development

## Requirements

### R1. Mecânicas Cooperativas Multiplayer e Invocação de Frases
Implementar mecânicas que instiguem os jogadores a cooperarem e jogarem juntos. Isso inclui a invocação de frases práticas (como pedir item, apontar lugar, pedir para seguir), minidesafios dinâmicos/kamikaze iniciáveis livremente por qualquer jogador (estilo "o chão é lava"), diálogos cooperativos aleatórios e um sistema de ranking/pontuação. O sistema deve fornecer uma forma compreensível para que o outro jogador entenda a instrução invocada enquanto também aprende o idioma.

### R2. Ajuste do HUD de Feedback de Fala
Restringir a exibição da caixa de feedback de fala ("o que eu disse") para aparecer exclusivamente ao pronunciar frases completas, não sendo exibida em desafios de itens de palavra única. A caixa visual deve ter tamanho reduzido e utilizar estritamente o cabeçalho "disse: ".

### R3. Calibração da Progressão do Alfabeto Cirílico
Ajustar a progressão de substituição do alfabeto para que letras cirílicas não-idênticas ao alfabeto latino não sejam fixadas de forma definitiva precocemente após ações básicas iniciais (como coletar madeira). Letras visualmente idênticas (como A, O, E) podem manter a substituição natural, mas letras com grafias diferentes devem exigir avanço gradual antes da fixação definitiva.

### R4. Integração de Mods Adicionais de Dinâmica e Qualidade de Vida (QoL)
Adicionar mods para tornar a jogabilidade mais dinâmica e menos vanilla. A equipe deve avaliar a complexidade técnica e selecionar mods apropriados, priorizando utilitários de qualidade de vida e desempenho (como correr mais rápido, organizar baús, depositar itens em baús próximos e otimizações) ou mods de conteúdo compatíveis que não sobrecarreguem excessivamente o vocabulário de aprendizado.

### R5. Compilação, Implantação e Testes
Garantir que as modificações compilem sem erros e sejam implantadas nas instâncias de cliente e servidor dedicado (`Instancia_Cliente\mods\` e `Servidor_Dedicado\mods\`). A suíte de testes E2E existente deve ser estendida para cobrir as novas funcionalidades e passar integralmente com sucesso.

## Acceptance Criteria

### Cooperação Multiplayer & Frases
- [ ] Jogadores conseguem invocar frases cooperativas (pedir item, apontar lugar, seguir) e desafios dinâmicos (estilo "o chão é lava") durante a sessão multiplayer.
- [ ] O jogador que recebe uma instrução ou desafio visualiza uma indicação compreensível do objetivo ou comando.
- [ ] Há um sistema de ranking/pontuação ativo que registra e reflete os desafios cooperativos completados.

### HUD de Fala
- [ ] A caixa de feedback de fala não é exibida em desafios de itens individuais (palavra única).
- [ ] A caixa de fala é exibida apenas em frases completas, em tamanho reduzido e com cabeçalho exclusivo "disse: ".

### Progressão do Alfabeto
- [ ] Letras cirílicas não-idênticas não se tornam permanentes após ações básicas iniciais (como pegar madeira pela primeira vez).
- [ ] Letras com caracteres idênticos permanecem funcionando sem regressões.

### Estabilidade, Mods e Verificação
- [ ] Mods adicionais selecionados carregam corretamente no Fabric 1.20.1 sem conflitos ou falhas de inicialização no cliente e no servidor.
- [ ] Os arquivos JAR atualizados do mod são gerados e implantados em `Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` e `Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`.
- [ ] A execução de `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict` é concluída com todos os testes passando e código de saída 0.
