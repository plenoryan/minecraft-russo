## 2026-11-07T05:52:54Z

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
