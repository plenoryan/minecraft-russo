# ðŸŽ® Guia de InstalaÃ§Ã£o e ConexÃ£o Multiplayer â€” Minecraft Russo ("Melty Voice")

Bem-vindo ao **Modpack Minecraft Russo**! Este pacote turnkey foi preparado para vocÃª aprender russo na prÃ¡tica enquanto joga sobrevivÃªncia cooperativa com seus amigos.

Toda vez que vocÃª pegar um novo item no jogo, o mod **SayIt** entrarÃ¡ em aÃ§Ã£o com o desafio de voz: pronuncie o nome do item em russo pelo seu microfone antes do tempo acabar! Se acertar a pronÃºncia, vocÃª fica com o item. Se o tempo esgotar, o item Ã© dropado no chÃ£o ou perdido.

O jogo funciona perfeitamente com **contas offline (cracked/nÃ£o-originais)** e **contas originais**, sem exigÃªncia de login da Microsoft ou Mojang.

---

## ðŸš€ 1. Como ComeÃ§ar (InstalaÃ§Ã£o e ExecuÃ§Ã£o em 1 Clique)

1. Extraia o pacote completo recebido em uma pasta rÃ¡pida do seu computador (recomendado: `F:\Jogos\Mine` ou `C:\Jogos\Mine`).
2. DÃª dois cliques no arquivo:
   ðŸ‘‰ **`Jogar.bat`**
3. O inicializador portÃ¡til abrirÃ¡ automaticamente:
   - Se pedir para adicionar conta, clique em **Adicionar Conta** -> Escolha **Offline** -> Digite o seu **Nome/Apelido** desejado.
   - O perfil **"Minecraft_Russo"** (Fabric 1.20.1) jÃ¡ estarÃ¡ prÃ©-selecionado.
4. Clique em **Jogar** (ou **Iniciar**)! O jogo inicializarÃ¡ com todos os mods de aprendizado, otimizaÃ§Ã£o grÃ¡fica para AMD RDNA 2 e reconhecimento de voz prontos.

---

## ðŸŽ­ 2. Como Colocar Sua PrÃ³pria Skin (Mesmo em Conta Offline)

VocÃª e seus amigos nÃ£o precisam jogar com a skin padrÃ£o do Steve! O modpack vem com o **CustomSkinLoader** configurado com suporte a dois mÃ©todos fÃ¡ceis:

### MÃ©todo A: Skin Local no Seu Computador (100% Offline, Sem Internet)
1. Pegue a imagem da sua skin (arquivo `.png` no formato de skin de Minecraft, 64x64).
2. Renomeie o arquivo com o seu **Nome de Jogador** exato.  
   *Exemplo:* Se seu apelido offline Ã© `AmigoRusso`, renomeie o arquivo para `AmigoRusso.png`.
3. Copie o arquivo para a pasta:
   `Instancia_Cliente\CustomSkinLoader\LocalSkin\skins\`  
   *(Ou dentro da pasta `Launcher\instances\Minecraft_Russo\.minecraft\CustomSkinLoader\LocalSkin\skins\`)*
4. Pronto! Ao entrar no jogo, sua skin serÃ¡ carregada localmente para vocÃª e renderizada no jogo.

### MÃ©todo B: Usando o Perfil Gratuito Ely.by (Skin Online sem Conta Microsoft)
1. Crie uma conta gratuita no site [https://ely.by/](https://ely.by/) com o mesmo apelido que vocÃª usa no jogo.
2. FaÃ§a o upload da sua skin favorita no painel do Ely.by.
3. O modpack puxarÃ¡ sua skin automaticamente da nuvem do Ely.by quando vocÃª entrar em qualquer mundo!

---

## ðŸŽ™ï¸ 3. Configurando o Microfone e o Reconhecimento de Voz

O reconhecimento de voz roda com o motor de inteligÃªncia artificial **Vosk** instalado **localmente no seu computador** (`Instancia_Cliente\vosk_models\`), sem enviar nenhum Ã¡udio para a internet e com latÃªncia inferior a 50ms!

1. Dentro do jogo, pressione a tecla de atalho:
   ðŸ‘‰ **`U`** (Abre o menu de configuraÃ§Ãµes do mod **SayIt**).
2. Na opÃ§Ã£o de Microfone, selecione o seu dispositivo de Ã¡udio principal (ex: *Microfone do Headset USB* ou *Microfone Realtek*).
3. Fale algo no microfone e observe a barrinha de captaÃ§Ã£o de voz se mover para confirmar o volume de entrada.
4. Certifique-se de que o idioma de reconhecimento estÃ¡ definido como **Russian (ru)**.
5. **Ajuste de TolerÃ¢ncia da PronÃºncia**: O padrÃ£o exige 70% de proximidade fonÃ©tica. Se tiver dificuldades com o sotaque no comeÃ§o, vocÃª pode ajustar a sensibilidade para 60% nessa mesma tela.
6. **Fallback de EmergÃªncia (Se o microfone falhar)**: Se o seu microfone desconectar no meio de um desafio tenso, aperte a tecla **`Y`** para abrir o teclado virtual de emergÃªncia e digitar a palavra em russo para nÃ£o perder o seu item!

---

## ðŸŒ 4. Como Jogar Multiplayer com os Amigos

O modpack oferece duas opÃ§Ãµes prÃ¡ticas para jogar junto com os amigos:

### OPÃ‡ÃƒO 1: Modo P2P RÃ¡pido via `e4all` (O AnfitriÃ£o abre o Singleplayer)
*A forma mais fÃ¡cil e instantÃ¢nea! NÃ£o precisa configurar roteador, nÃ£o precisa abrir portas e nÃ£o precisa instalar Hamachi.*

1. **O AnfitriÃ£o (quem vai criar e liderar a sessÃ£o)**:
   - Entra no seu mundo singleplayer normalmente pelo `Jogar.bat`.
   - Pressiona **ESC** e clica em **Abrir para LAN** (Open to LAN).
   - **CONFIGURAÃ‡ÃƒO CRUCIAL PARA AMIGOS OFFLINE**:
     - Localize a opÃ§Ã£o **Modo Online (Online Mode)** fornecida pelo mod `e4all`.
     - Altere para **[DESLIGADO / OFF]**.
   - Clique em **Iniciar Mundo LAN**.
   - O mod `e4all` gerarÃ¡ um link pÃºblico de conexÃ£o exibido diretamente no chat (ex: `mine-amigos-7.e4mc.link:25565`).
   - Copie esse link e envie para os seus amigos no Discord ou WhatsApp.

2. **Os Amigos (quem vai entrar na partida)**:
   - Abrem o Minecraft pelo `Jogar.bat`.
   - VÃ£o em **Multijogador** -> **ConexÃ£o Direta** (Direct Connection).
   - Colam o endereÃ§o fornecido pelo anfitriÃ£o e clicam em **Entrar no Servidor**.
   - O mod `e4all` faz o tÃºnel reverso direto, com suporte automÃ¡tico ao proxy de voz UDP na porta **24454** para bate-papo de proximidade!

---

### OPÃ‡ÃƒO 2: Servidor Dedicado 24/7 (`Servidor_Dedicado`)
*Ideal para um mundo permanente de sobrevivÃªncia onde qualquer amigo pode entrar mesmo se o anfitriÃ£o nÃ£o estiver jogando.*

1. **Quem vai hospedar o Servidor**:
   - DÃª dois cliques no arquivo:
     ðŸ‘‰ **`Iniciar_Servidor.bat`**
   - O script iniciarÃ¡ o servidor Fabric 1.20.1 usando o Java 17 embutido e as otimizaÃ§Ãµes Aikar G1GC.
   - O arquivo `server.properties` jÃ¡ vem configurado com:
     - `online-mode=false` (Permite contas offline e originais)
     - `enforce-secure-profile=false` (Evita erros de chat signature)
     - `motd=Â§aServidor Minecraft Russo - Melty Voice [1.20.1 Fabric]`
   - Aguarde o console exibir a mensagem: `Done! For help, type "help"`.

2. **Liberando ConexÃ£o Externa para os Amigos (Sem Port Forwarding)**:
   Como a maioria das conexÃµes residenciais no Brasil utiliza CGNAT, utilize uma das opÃ§Ãµes gratuitas:
   - **Playit.gg (Recomendado)**: Baixe o agente leve do [Playit.gg](https://playit.gg/), execute-o no computador do servidor e gere um domÃ­nio customizado (ex: `russo-amigos.gl.joinmc.link:25565`). Os amigos entram direto pelo domÃ­nio sem instalar nada!
   - **Radmin VPN**: Crie uma rede no Radmin VPN (ex: `Minecraft-Russo-Amigos`, senha: `123`). Seus amigos entram na rede e conectam direto pelo IP do Radmin: `26.x.x.x:25565`.

3. **Gerenciamento de Skins no Servidor**:
   O servidor dedicado inclui o mod **FabricTailor**. Qualquer jogador pode digitar comandos no chat para alterar sua skin em tempo real:
   - `/skin set <NomeDeJogadorOriginal>` (Copia a skin de qualquer conta original pÃºblica)
   - `/skin set url <LinkPNG>` (Aplica skin diretamente de um link de imagem na internet)
   - `/skin clear` (Restaura a skin padrÃ£o)

---

## ðŸ› ï¸ 5. ResoluÃ§Ã£o de Problemas Frequentes (Troubleshooting)

### P1: "Invalid Session" / "SessÃ£o InvÃ¡lida" ao tentar conectar
- **Causa**: O anfitriÃ£o esqueceu de marcar o modo online como desligado.
- **SoluÃ§Ã£o**: No modo P2P, o anfitriÃ£o deve abrir o menu ESC -> Abrir para LAN -> mudar **Modo Online** para **[OFF]** antes de iniciar. No servidor dedicado, confirme que no `Servidor_Dedicado\server.properties` consta `online-mode=false`.

### P2: "Missing or invalid chat signature" / "Perfil seguro obrigatÃ³rio"
- **Causa**: O servidor estÃ¡ cobrando assinaturas de chat criptografadas da Mojang que clientes offline nÃ£o possuem.
- **SoluÃ§Ã£o**: No arquivo `server.properties`, confirme que `enforce-secure-profile=false` estÃ¡ definido. O modpack jÃ¡ vem com essa linha configurada por padrÃ£o.

### P3: O microfone nÃ£o capta o som ou a barra de voz nÃ£o se move
1. Abra as **ConfiguraÃ§Ãµes do Windows** -> **Privacidade e SeguranÃ§a** -> **Microfone**.
2. Certifique-se de que a opÃ§Ã£o *"Permitir que os aplicativos da Ã¡rea de trabalho acessem seu microfone"* estÃ¡ **Ativada**.
3. No jogo, aperte **`U`** e confirme se o microfone selecionado Ã© o aparelho correto e se o volume nÃ£o estÃ¡ mudo no Windows.

### P4: O jogo ou o servidor nÃ£o abre ao clicar no `.bat`
- O pacote inclui uma versÃ£o portÃ¡til do OpenJDK 17 na pasta `Java17\`. Se por algum motivo a pasta foi movida ou apagada, instale o **Java 17 de 64 bits (Eclipse Temurin)** atravÃ©s do site oficial: [https://adoptium.net/](https://adoptium.net/).

### P5: ConexÃ£o bloqueada pelo Firewall do Windows
- Na primeira vez que iniciar o jogo ou o servidor, o Windows Defender Firewall exibirÃ¡ uma janela de aviso. Marque as caixas de **Redes Privadas** e **Redes PÃºblicas** e clique em **Permitir Acesso**.