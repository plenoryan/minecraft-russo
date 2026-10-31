# 🇷🇺 Minecraft Russo: Pacote de Aprendizado com Reconhecimento de Voz ("Melty Voice")

Bem-vindo ao **Minecraft Russo**, um modpack e ecossistema multiplayer estruturado para aprender a língua russa enquanto joga sobrevivência cooperativa no Minecraft 1.20.1 Fabric.

Inspirado na dinâmica de jogos como *Melty*, ao adquirir qualquer novo item no jogo você é desafiado a falar o nome correspondente em russo pelo microfone. Se pronunciar corretamente antes do tempo limite, você mantém o item; caso contrário, o item é descartado ou perdido!

---

## ⚡ Recursos Principais
- **Minecraft 1.20.1 Fabric:** A versão mais estável e com melhor suporte a mods da era moderna.
- **Reconhecimento de Fala Offline (Vosk STT):** Motor neural leve em russo (`vosk-model-small-ru-0.22`), executado localmente sem enviar áudio para servidores externos.
- **100% Compatível com Contas Offline (Cracked):** Inicialização sem necessidade de login na conta Microsoft/Mojang.
- **Otimização de Alto Desempenho:** Sodium, Lithium, FerriteCore, ModernFix e ImmediatelyFast pré-configurados para taxas máximas de quadros (FPS) em GPUs AMD Radeon e gráficos integrados.
- **Pronto para Multiplayer:** Suporte a túnel LAN P2P (`e4all`) com zero encaminhamento de portas no roteador e servidor dedicado integrado.
- **Zero Poluição do Sistema:** Java 17 (OpenJDK) portátil embutido diretamente na pasta do jogo.

---

## 📁 Estrutura de Diretórios

```
F:\Jogos\Mine\
├── Jogar.bat                    # Atalho de 1 clique para iniciar o cliente
├── AGENTS.md                    # Governança operacional e lições aprendidas
├── README.md                    # Este documento explicativo
├── .gitignore                   # Exclusão de arquivos temporários, logs e caches
│
├── Java17\                      # OpenJDK 17 portátil (sem necessidade de instalar Java no Windows)
│   ├── bin\
│   │   ├── java.exe
│   │   └── javaw.exe
│   └── lib\
│
├── Launcher\                    # Lançador portátil UltimMC / Prism Launcher
│   ├── UltimMC.exe              # Executável do lançador com suporte a contas offline
│   ├── data\                    # Dados do perfil, contas offline e configurações
│   └── instances\               # Perfil pré-configurado do modpack
│       └── Minecraft_Russo\
│           ├── instance.cfg     # Configurações de memória e Java da instância
│           └── mmc-pack.json    # Manifesto Fabric 1.20.1
│
├── Instancia_Cliente\           # Instância mestre do jogo
│   ├── instance.cfg             # Configuração da instância cliente
│   ├── mmc-pack.json            # Manifesto de componentes
│   ├── options.txt              # Opções do Minecraft (idioma pré-configurado em russo: ru_ru)
│   ├── mods\                    # Modificações Fabric 1.20.1
│   │   ├── fabric-api-*.jar     # Fabric API
│   │   ├── sodium-*.jar         # Renderizador de alto desempenho
│   │   ├── lithium-*.jar        # Otimização de física e IA
│   │   ├── ferritecore-*.jar     # Redução de consumo de memória RAM
│   │   ├── modernfix-*.jar      # Correções de engine e inicialização rápida
│   │   └── ImmediatelyFast-*.jar# Otimização de HUD e fontes cirílicas
│   └── config\                  # Configurações de mods
│
├── verify_m1.ps1                # Script de validação automatizada do Milestone M1
└── install_java17.ps1           # Script instalador/atualizador do Java 17 portátil
```

---

## 🚀 Como Jogar (Início Rápido)

1. Dê dois cliques no arquivo:
   ```cmd
   Jogar.bat
   ```
2. O lançador portátil abrirá carregando o perfil **Minecraft_Russo**.
3. Na tela de contas, selecione a conta offline pré-configurada ou adicione seu nome de jogador.
4. Clique em **Jogar**! O jogo já iniciará com interface em russo e mods de otimização ativos.

---

## 🛠️ Validação Técnica Automatizada

Para verificar a integridade da instalação M1, execute no terminal PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\verify_m1.ps1"
```
O teste valida a presença do Java 17 portátil, o status do executável, os manifestos de instância, as configurações de idioma cirílico (`ru_ru`) e todos os mods de base do Fabric.
