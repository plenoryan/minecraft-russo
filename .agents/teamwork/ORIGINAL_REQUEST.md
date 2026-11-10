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


## Approved Task Prompt — 2026-11-07T05:51:29Z

Implementar uma roda de seleção radial com ícones para invocar ações cooperativas com ativação estritamente condicionada ao sucesso no desafio de fala em russo, exibir notificações visuais marcantes na tela do parceiro com ícone de cabeça e símbolo indicativo do pedido com interrogação, desvincular o atalho de recarga de sombreador da tecla 'R' e integrar mods de móveis e cotidiano (Handcrafted, Farmer's Delight, Farmer's Respite e Comforts) com vocabulário adaptado.

Working directory: F:\Jogos\Mine
Integrity mode: development

## Requirements

### R1. Roda de Seleção Radial de Ações com Gatilho Pedagógico de Voz
Implementar uma interface de roda de seleção radial visual acessível por tecla de atalho configurável (padrão 'V') contendo setores com ícones representativos para invocar ações cooperativas (ex: pedir madeira, pedir comida, pedir ajuda, seguir, apontar/waypoint, desafio lava). Ao selecionar uma ação na roleta, o sistema deve iniciar obrigatoriamente a sessão de desafio de fala para o jogador pronunciar a respectiva frase em russo, e a notificação cooperativa para o outro jogador só deve ser disparada se a pronúncia for validada com sucesso.

### R2. Notificação Visual Cooperativa no Receptor
Exibir uma notificação visual na tela do jogador que recebe o pedido cooperativo, contendo o ícone/cabeça do jogador solicitante acompanhado de um ícone sugestivo do pedido com ponto de interrogação (ex: madeira com '?', comida com '?').

### R3. Desvinculação do Atalho de Recarregar Sombreador
Remover a atribuição da tecla 'R' para o recarregamento de shaders (Iris / `key.iris.reloadShaders`), desativando o atalho para evitar travamentos acidentais durante a movimentação ou digitação.

### R4. Integração de Mods de Móveis e Cotidiano com Vocabulário Adequado
Integrar os mods de vida cotidiana Farmer's Respite (chá, café, chaleira) e Comforts (sacos de dormir e redes) juntamente com Handcrafted e Farmer's Delight já presentes na instância, adequando o vocabulário, palavras, tooltips e suporte de pronúncia para os blocos e itens correspondentes.

## Verification Resources
- Script de compilação e deploy: `F:\Jogos\Mine\build_and_deploy.ps1`
- Suíte de testes automatizados: `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`

## Acceptance Criteria

### Roleta Radial e Portão de Voz
- [ ] A interface radial é aberta ao pressionar a tecla de atalho configurada (padrão 'V') e exibe setores com ícones para as ações cooperativas.
- [ ] A seleção de qualquer ação na roleta inicia imediatamente a sessão de desafio de voz para pronúncia da frase correspondente.
- [ ] O envio do pacote de notificação cooperativa ao parceiro ocorre única e exclusivamente após a aprovação da pronúncia no desafio de voz; pronúncia incorreta ou cancelamento não envia o aviso ao parceiro.

### Interface e Notificação Visual
- [ ] Ao receber um pedido cooperativo bem-sucedido, o jogador receptor visualiza na tela uma notificação com a cabeça/skin do emissor e o ícone representativo do pedido acompanhado de um ponto de interrogação.
- [ ] Pressionar a tecla 'R' no jogo não aciona o recarregamento de shaders do Iris nem causa congelamentos por reload de shader.

### Mods de Cotidiano, Vocabulário e Testes Automatizados
- [ ] Os mods Farmer's Respite e Comforts (Fabric 1.20.1) estão instalados e operacionais na instância ao lado de Handcrafted e Farmer's Delight.
- [ ] O vocabulário e tooltips em russo contemplam itens de mobília e situações cotidianas dos mods Handcrafted, Farmer's Delight, Farmer's Respite e Comforts.
- [ ] O script `F:\Jogos\Mine\build_and_deploy.ps1` executa sem erros de compilação ou empacotamento.
- [ ] A suíte de testes `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict` passa com 100% de sucesso, incluindo novos testes automatizados para a validação dos fluxos da roleta, portão de voz e desvinculação da tecla 'R'.


## Approved Task Prompt — 2026-11-10T17:48:04Z

Revisar e completar as traduções em russo (ru_ru), implementar referência fonética com áudio de pronúncia no mod/cliente SayIt (Minecraft 1.20.1 Fabric) e criar um instalador portátil acompanhado de repositório com site GitHub Pages para distribuição.

Working directory: F:\Jogos\Mine
Integrity mode: development

## Requirements

### R1. Auditoria e Conclusão das Traduções em Russo (ru_ru)
Realizar uma auditoria completa de traduções em russo (`ru_ru`) no modpack, eliminando termos não traduzidos e lacunas nos mods adicionados recentemente (Towns and Towers, Moog's Voyager Structures, Critters and Companions, Ecologics, Exposure, Carry On, Farmer's Respite, Comforts, Handcrafted, Naturalist), itens, blocos, entidades e nos tooltips pedagógicos em `Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js`.

### R2. Referência Fonética com Áudio ("Pressione para Ouvir")
Implementar recurso de referência fonética com áudio no mod SayIt e/ou cliente, permitindo ao jogador acionar a reprodução sonora da pronúncia russa das palavras ou itens antes de falar (estilo "pressione para ouvir" / TTS ou reprodução sonora). A equipe tem autonomia para selecionar a melhor implementação técnica no cliente/mod.

### R3. Empacotador / Instalador Executável Portátil
Criar uma solução de empacotamento ou instalador executável portátil que torne o repasse e instalação do cliente (incluindo o inicializador portátil UltimMC em `Launcher`) simples e direto para distribuição entre amigos, sem necessidade de configurações manuais complexas. A equipe tem autonomia para decidir a abordagem mais prática e robusta.

### R4. Repositório e Site GitHub Pages
Criar um repositório estruturado contendo um site estático moderno (HTML/CSS/JS) para o GitHub Pages. O site deve incluir seções claras de créditos aos criadores originais dos mods, lista detalhada de alterações do usuário e botão direto de download do instalador.

### R5. Pipeline de Compilação e Testes Automatizados E2E
Compilar e injetar os bytecodes nas instâncias de cliente e servidor via `F:\Jogos\Mine\build_and_deploy.ps1`. Estender a suíte canônica de testes `F:\Jogos\Mine\tests\run_e2e_tests.ps1` com testes automatizados para validar a ausência de termos sem tradução em russo e o correto empacotamento do instalador e ativos, garantindo aprovação estrita total (código de saída 0).

## Acceptance Criteria

### Traduções em Russo
- [ ] Não restam itens, blocos, entidades ou tooltips dos mods recentes sem tradução para russo (`ru_ru`) ou chaves pendentes em `russian_tooltips.js`.

### Referência Fonética
- [ ] O jogador consegue acionar a reprodução sonora da pronúncia em russo de termos/itens através do cliente/mod SayIt antes de tentar falar.

### Instalador Portátil
- [ ] O pacote de distribuição/instalador portátil gerado é funcional, autossuficiente e permite a execução direta do cliente Minecraft com o inicializador UltimMC.

### Repositório e GitHub Pages
- [ ] Os arquivos do site estático para o GitHub Pages estão criados e estruturados com seção de créditos aos criadores, lista de alterações e botão funcional de download.

### Compilação e Testes E2E
- [ ] O pipeline `F:\Jogos\Mine\build_and_deploy.ps1` compila com sucesso sem erros.
- [ ] A execução de `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict` passa integralmente com todos os testes (incluindo os novos testes adicionados) com código de saída 0.
