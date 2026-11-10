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
