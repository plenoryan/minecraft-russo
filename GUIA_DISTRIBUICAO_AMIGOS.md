# 🎮 Guia de Instalação e Conexão Multiplayer — Minecraft Russo ("Melty Voice")

Bem-vindo ao **Modpack Minecraft Russo**! Este pacote turnkey foi preparado para você aprender russo na prática enquanto joga sobrevivência cooperativa com seus amigos.

Toda vez que você pegar um novo item no jogo, o mod **SayIt** entrará em ação com o desafio de voz: pronuncie o nome do item em russo pelo seu microfone antes do tempo acabar! Se acertar a pronúncia, você fica com o item. Se o tempo esgotar, o item é dropado no chão ou perdido.

O jogo funciona perfeitamente com **contas offline (cracked/não-originais)** e **contas originais**, sem exigência de login da Microsoft ou Mojang.

---

## 🚀 1. Como Começar (Instalação e Execução em 1 Clique)

1. Extraia o pacote completo recebido em uma pasta rápida do seu computador (recomendado: `F:\Jogos\Mine` ou `C:\Jogos\Mine`).
2. Dê dois cliques no arquivo:
   👉 **`Jogar.bat`**
3. O inicializador portátil abrirá automaticamente:
   - Se pedir para adicionar conta, clique em **Adicionar Conta** -> Escolha **Offline** -> Digite o seu **Nome/Apelido** desejado.
   - O perfil **"Minecraft_Russo"** (Fabric 1.20.1) já estará pré-selecionado.
4. Clique em **Jogar** (ou **Iniciar**)! O jogo inicializará com todos os mods de aprendizado, otimização gráfica para AMD RDNA 2 e reconhecimento de voz prontos.

---

## 🎭 2. Como Colocar Sua Própria Skin (Mesmo em Conta Offline)

Você e seus amigos não precisam jogar com a skin padrão do Steve! O modpack vem com o **CustomSkinLoader** configurado com suporte a dois métodos fáceis:

### Método A: Skin Local no Seu Computador (100% Offline, Sem Internet)
1. Pegue a imagem da sua skin (arquivo `.png` no formato de skin de Minecraft, 64x64).
2. Renomeie o arquivo com o seu **Nome de Jogador** exato.  
   *Exemplo:* Se seu apelido offline é `AmigoRusso`, renomeie o arquivo para `AmigoRusso.png`.
   *(Atenção: Nomes devem conter apenas letras, números e underlines, sem barras ou pontos).*
3. Copie o arquivo para a pasta:
   `Instancia_Cliente\CustomSkinLoader\LocalSkin\skins\`
4. Pronto! Ao entrar no jogo, sua skin será carregada localmente para você e renderizada no jogo.

### Método B: Usando o Perfil Gratuito Ely.by (Skin Online sem Conta Microsoft)
1. Crie uma conta gratuita no site [https://ely.by/](https://ely.by/) com o mesmo apelido que você usa no jogo.
2. Faça o upload da sua skin favorita no painel do Ely.by.
3. O modpack puxará sua skin automaticamente da nuvem do Ely.by quando você entrar em qualquer mundo!

---

## 🌐 3. Como Jogar em Multiplayer com Amigos

O modpack oferece **duas formas** fáceis de jogar juntos:

### Método 1: Hospedar pelo Mundo Local (P2P Sem Abrir Portas no Roteador — Recomendado)
1. Um dos amigos entra no seu mundo Singleplayer.
2. Pressione **ESC** e clique em **Abrir para LAN** (ou **Open to LAN**).
3. Na tela de LAN fornecida pelo mod **e4all**, você verá as opções:
   - **Online Mode:** Alterne para **[OFF]** (obrigatório para amigos de conta offline entrarem!).
   - **Porta:** 25565 (padrão).
4. Clique em **Iniciar Mundo LAN**.
5. O mod gerará um link público seguro no chat (exemplo: `mine-russo.e4mc.link:25565`). O túnel UDP da porta 24454 é integrado automaticamente para o chat de voz.
6. Copie esse endereço e envie para seus amigos no Discord ou WhatsApp!
7. Seus amigos só precisam ir em **Multijogador** -> **Conexão Direta**, colar o link e entrar!

### Método 2: Servidor Dedicado 24/7 (Para Jogar a Qualquer Momento)
1. Na pasta principal do jogo, clique duas vezes em:
   👉 **`Iniciar_Servidor.bat`**
2. O servidor dedicado Fabric 1.20.1 será inicializado automaticamente com flags otimizadas Aikar G1GC e autenticação offline (`online-mode=false`).
3. Para conectar amigos de fora da sua casa:
   - Use uma VPN leve para jogos como **Radmin VPN** ou **ZeroTier** (todos na mesma sala de rede entram pelo IP do Radmin).
   - Ou utilize o **Playit.gg** (basta rodar o executável do playit para gerar um túnel público automático para a porta 25565 e túnel UDP para a porta 24454 do chat de voz).

---

## 🎙️ 4. Como Funciona a Mecânica de Voz (Melty) e Microfone

1. Ao pegar qualquer novo item no jogo (madeira, pedra, maçã, diamante), um cronômetro e o nome em russo aparecerão no topo da tela:
   - Exemplo: Pegou carvalho -> Aparece **Дубовое бревно** [Dubovoye brevno].
2. Fale o nome em russo no microfone antes do cronômetro zerar!
3. O jogo utiliza a IA **Vosk offline** instalada na sua máquina, processando sua voz instantaneamente em menos de 50 milissegundos sem enviar nada para a internet.
4. Se acertar: Você ouve um som de sucesso e fica com o item!
5. Se errar ou o tempo acabar: O item cai no chão para você tentar novamente!
6. **Tecla de Emergência:** Se o seu microfone não funcionar ou estiver sem mic, pressione a tecla **Y** para digitar o nome em russo no chat como alternativa de acessibilidade.

---

## ❓ 5. Resolução de Problemas Frequentes

### P1: O microfone não reconhece minha voz
- Verifique se o microfone padrão do Windows está configurado corretamente em *Configurações do Windows > Som*.
- Certifique-se de que a permissão de microfone para aplicativos desktop está ativada em *Configurações > Privacidade e Segurança > Microfone*.

### P2: Meu amigo não consegue entrar no mundo LAN
- Certifique-se de que o anfitrião marcou **Online Mode: [OFF]** antes de clicar em Iniciar Mundo LAN.
- Se o firewall do Windows pedir autorização, clique em **Permitir Acesso** para redes privadas e públicas.

### P3: Minha skin não aparece
- Verifique se o nome do arquivo em `LocalSkin\skins\` bate exatamente com o seu apelido no jogo (respeitando maiúsculas e minúsculas).