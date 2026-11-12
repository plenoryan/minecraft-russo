/**
 * Minecraft Russo: SayIt Modpack - Client-Side Application
 * Pure Vanilla ES6 JavaScript (Zero External Dependencies)
 */

document.addEventListener("DOMContentLoaded", () => {
  initNavigation();
  initAudioPlayer();
  initAudioTabs();
  initCreditsTable();
  initClipboard();
  initSimulator();
  initRadialWheel();
  initDownloadLinks();
  initParallax();
});

// Authoritative Metadata for all 43 Curated Mod JARs
const MODS_DATA = [
    {
        "Jar":  "AmbientSounds_FABRIC_v6.3.11_mc1.20.1.jar",
        "Id":  "ambientsounds",
        "Name":  "AmbientSounds",
        "Version":  "6.3.11",
        "Authors":  "CreativeMD, AriaFreeze",
        "License":  "LGPL-3.0-only",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "",
        "Description":  "Expands minecraft\u0027s ambient sounds."
    },
    {
        "Jar":  "appleskin-fabric-mc1.20.1-2.5.2.jar",
        "Id":  "appleskin",
        "Name":  "AppleSkin",
        "Version":  "2.5.2+mc1.20.1",
        "Authors":  "squeek502",
        "License":  "Unlicense",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "https://minecraft.curseforge.com/projects/appleskin",
        "Description":  "Adds various food-related HUD improvements"
    },
    {
        "Jar":  "architectury-9.2.14-fabric.jar",
        "Id":  "architectury",
        "Name":  "Architectury",
        "Version":  "9.2.14",
        "Authors":  "shedaniel",
        "License":  "LGPL-3",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://architectury.github.io/architectury-documentations/",
        "Description":  "A intermediary api aimed to ease developing multiplatform mods."
    },
    {
        "Jar":  "carryon-fabric-1.20.1-2.1.2.7.jar",
        "Id":  "carryon",
        "Name":  "Carry On",
        "Version":  "2.1.2.7",
        "Authors":  "Tschipp, Purplicious_Cow, cy4n",
        "License":  "GNU LGPLv3",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "https://tschipp.ch/",
        "Description":  "Carry On is a simple mod that improves game interaction by allowing players to pick up, carry, and place single block Tile Entities using only their empty hands."
    },
    {
        "Jar":  "comforts-fabric-6.4.0+1.20.1.jar",
        "Id":  "comforts",
        "Name":  "Comforts",
        "Version":  "6.4.0+1.20.1",
        "Authors":  "Illusive Soulworks",
        "License":  "LGPL-3.0-or-later",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "",
        "Description":  "Adds sleeping bags and hammocks for, respectively, portability and turning day to night, without setting new spawns.\nComes in 16 different colors!"
    },
    {
        "Jar":  "CreativeCore_FABRIC_v2.12.41_mc1.20.1.jar",
        "Id":  "creativecore",
        "Name":  "CreativeCore",
        "Version":  "2.12.41",
        "Authors":  "CreativeMD, AriaFreeze",
        "License":  "LGPL-3.0-only",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "",
        "Description":  "A simple but powerful coremod."
    },
    {
        "Jar":  "cristellib-1.1.5-fabric.jar",
        "Id":  "cristellib",
        "Name":  "Cristel Lib",
        "Version":  "1.1.5",
        "Authors":  "Cristelknight999",
        "License":  "CC BY-NC-ND 4.0",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://modrinth.com/mod/cristel-lib",
        "Description":  "A Library mod for easy structure config and runtime datapacks."
    },
    {
        "Jar":  "crittersandcompanions-fabric-1.20.1-2.3.5.jar",
        "Id":  "crittersandcompanions",
        "Name":  "Critters and Companions",
        "Version":  "1.20.1-2.3.5",
        "Authors":  "Joosh, EterDelta",
        "License":  "CC0-1.0",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "",
        "Description":  "Critters and Companions is a vanilla-style creature mod which fills the overworld with new animals."
    },
    {
        "Jar":  "CustomSkinLoader_Universal-15.1.jar",
        "Id":  "customskinloader-bootstrap",
        "Name":  "CustomSkinLoader Bootstrap",
        "Version":  "15.1",
        "Authors":  "xfl03, JLChnToZ, ZekerZhayard",
        "License":  "GPL-3.0-only",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://github.com/xfl03/MCCustomSkinLoader",
        "Description":  "Custom Skin Loader for Minecraft"
    },
    {
        "Jar":  "e4all-fabric-1.6.4.jar",
        "Id":  "e4all",
        "Name":  "e4all",
        "Version":  "1.6.4",
        "Authors":  "JustPixel",
        "License":  "MIT",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://github.com/Synquox/e4all",
        "Description":  "Open a LAN server to anyone, anywhere, anytime. Enhanced with Online/Offline Mode support."
    },
    {
        "Jar":  "ecologics-fabric-1.20.1-2.2.7.jar",
        "Id":  "ecologics",
        "Name":  "Ecologics",
        "Version":  "2.2.7",
        "Authors":  "SameButDifferent, Zero_DSRS_VX, Drigonis, Crispytwig, Irishjevil, Foquito Azul",
        "License":  "MIT",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/ecologics",
        "Description":  "Simple vanilla biome updates, with fun mobs, blocks, and more!"
    },
    {
        "Jar":  "entityculling-fabric-1.11.3-mc1.20.1.jar",
        "Id":  "entityculling",
        "Name":  "EntityCulling",
        "Version":  "1.11.3",
        "Authors":  "tr7zw",
        "License":  "tr7zw Protective License",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://modrinth.com/mod/entityculling",
        "Description":  "This mod uses async path-tracing to hide Tiles/Entities that are not visible."
    },
    {
        "Jar":  "exposure-fabric-1.20.1-1.9.24.jar",
        "Id":  "exposure",
        "Name":  "Exposure",
        "Version":  "1.9.24",
        "Authors":  "mortuusars",
        "License":  "MIT",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/exposure",
        "Description":  "Camera mod with focus on process and aesthetics."
    },
    {
        "Jar":  "fabric-api-0.92.12+1.20.1.jar",
        "Id":  "fabric-api",
        "Name":  "Fabric API",
        "Version":  "0.92.12+1.20.1",
        "Authors":  "FabricMC",
        "License":  "Apache-2.0",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://fabricmc.net",
        "Description":  "Core API module providing key hooks and intercompatibility features."
    },
    {
        "Jar":  "FarmersDelight-1.20.1-2.1.1+refabricated.jar",
        "Id":  "farmersdelight",
        "Name":  "Farmer\u0027s Delight",
        "Version":  "1.20.1-2.1.1+refabricated",
        "Authors":  "MehVahdJukaar, MerchantPug",
        "License":  "MIT License",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/farmers-delight-refabricated",
        "Description":  "A cozy expansion to farming and cooking!"
    },
    {
        "Jar":  "Farmersrespite-Fabric-3.0.2.jar",
        "Id":  "farmersrespite",
        "Name":  "Farmer\u0027s Respite",
        "Version":  "3.0.2",
        "Authors":  "StevenPlayzz, JustinPlayzz, Umpaz, HoboJoe, SoyTutta, MrSterner",
        "License":  "MIT",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "",
        "Description":  "An addon created by Umpaz, HoboJoe, and SoyTutta for the Farmer\u0027s Delight mod by vectorwing. Attempts to add tea and ways to brew it, as well as some other foods!"
    },
    {
        "Jar":  "ferritecore-6.0.1-fabric.jar",
        "Id":  "ferritecore",
        "Name":  "FerriteCore",
        "Version":  "6.0.1",
        "Authors":  "malte0811",
        "License":  "MIT",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/ferritecore-fabric",
        "Description":  "Reduces memory usage"
    },
    {
        "Jar":  "ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar",
        "Id":  "forgeconfigapiport",
        "Name":  "Forge Config API Port",
        "Version":  "8.0.3",
        "Authors":  "Fuzs",
        "License":  "MPL-2.0",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://github.com/Fuzss/forgeconfigapiport",
        "Description":  "Forge\u0027s whole config system provided to the Fabric ecosystem. Designed for a multiloader architecture."
    },
    {
        "Jar":  "geckolib-fabric-1.20.1-4.8.4.jar",
        "Id":  "geckolib",
        "Name":  "GeckoLib 4",
        "Version":  "4.8.4",
        "Authors":  "Gecko, Eliot, AzureDoom, DerToaster, Tslat, Witixin",
        "License":  "MIT",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/geckolib",
        "Description":  "GeckoLib is an animation engine for Minecraft Mods, with support for complex 3D keyframe-based animations, 30+ easings, concurrent animation support, sound and particle keyframes, event keyframes, and more."
    },
    {
        "Jar":  "handcrafted-fabric-1.20.1-3.0.6.jar",
        "Id":  "handcrafted",
        "Name":  "Handcrafted",
        "Version":  "3.0.6",
        "Authors":  "Alex Nijjar, Kekie6",
        "License":  "Terrarium Licence",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "",
        "Description":  "Epic Furniture mod!"
    },
    {
        "Jar":  "Heracles-fabric-1.20.1-1.1.13.jar",
        "Id":  "heracles",
        "Name":  "Heracles",
        "Version":  "1.1.13",
        "Authors":  "ThatGravyBoat",
        "License":  "MIT",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "",
        "Description":  "Heracles is a comprehensive questing mod that allows pack makers to create tree style quests that can be distributed via the config folder for users to play and complete."
    },
    {
        "Jar":  "heraclesbutton-1.0.0.jar",
        "Id":  "heraclesbutton",
        "Name":  "Heracles Button",
        "Version":  "1.0.0",
        "Authors":  "kyber-6, Pixel Dream Studios",
        "License":  "MIT",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://modrinth.com/project/heracles-button",
        "Description":  "Access your Quests from your Inventory"
    },
    {
        "Jar":  "ImmediatelyFast-Fabric-1.5.5+1.20.4.jar",
        "Id":  "immediatelyfast",
        "Name":  "ImmediatelyFast",
        "Version":  "1.5.5+1.20.4",
        "Authors":  "RK_01",
        "License":  "LGPL-3.0-or-later",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://github.com/RaphiMC/ImmediatelyFast",
        "Description":  "Speed up and optimize immediate mode rendering in Minecraft"
    },
    {
        "Jar":  "InventorySorter-1.9.0-1.20.jar",
        "Id":  "inventorysorter",
        "Name":  "Inventory Sorter",
        "Version":  "1.9.0-1.20",
        "Authors":  "kyrptonaught",
        "License":  "MIT",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/inventory-sorting",
        "Description":  "Quickly and easily sort inventorys"
    },
    {
        "Jar":  "iris-1.7.6+mc1.20.1.jar",
        "Id":  "iris",
        "Name":  "Iris",
        "Version":  "1.7.6+mc1.20.1",
        "Authors":  "coderbot, IMS212, Justsnoopy30, FoundationGames",
        "License":  "LGPL-3.0-only",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://github.com/IrisShaders/Iris",
        "Description":  "A modern shaders mod for Minecraft intended to be compatible with existing OptiFine shader packs"
    },
    {
        "Jar":  "Jade-1.20-Fabric-11.13.3.jar",
        "Id":  "jade",
        "Name":  "Jade",
        "Version":  "11.13.3+fabric",
        "Authors":  "Snownee",
        "License":  "CC BY-NC-SA 4.0",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/jade",
        "Description":  "Minecraft mod shows what you are looking at. (Hwyla fork)"
    },
    {
        "Jar":  "kubejs-fabric-2001.6.5-build.26.jar",
        "Id":  "kubejs",
        "Name":  "KubeJS",
        "Version":  "2001.6.5-build.26",
        "Authors":  "LatvianModder",
        "License":  "GNU LGPLv3",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://github.com/KubeJS-Mods/KubeJS",
        "Description":  "Customize your modpack or server with JavaScript!"
    },
    {
        "Jar":  "language-reload-1.7.5+1.20.1.jar",
        "Id":  "languagereload",
        "Name":  "Language Reload",
        "Version":  "1.7.5+1.20.1",
        "Authors":  "Jerozgen",
        "License":  "MIT",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "https://modrinth.com/mod/language-reload",
        "Description":  "Reduces load times and adds fallbacks for languages."
    },
    {
        "Jar":  "lithium-fabric-mc1.20.1-0.11.4.jar",
        "Id":  "lithium",
        "Name":  "Lithium",
        "Version":  "0.11.4",
        "Authors":  "JellySquid, 2No2Name",
        "License":  "LGPL-3.0-only",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://jellysquid.me/projects/",
        "Description":  "Lithium is a free and open-source optimization mod for Minecraft which makes a wide range of performance improvements to the game."
    },
    {
        "Jar":  "modernfix-fabric-5.25.2+mc1.20.1.jar",
        "Id":  "modernfix",
        "Name":  "ModernFix",
        "Version":  "5.25.2+mc1.20.1",
        "Authors":  "embeddedt",
        "License":  "LGPL-3.0",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://modrinth.com/mod/modernfix",
        "Description":  "Egregious, yet effective performance improvements for modern Minecraft"
    },
    {
        "Jar":  "ModernUI-Fabric-1.20.1-3.12.0.1-universal.jar",
        "Id":  "modernui",
        "Name":  "Modern UI",
        "Version":  "3.12.0.1",
        "Authors":  "BloCamLimb",
        "License":  "LGPL-3.0-or-later",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://curseforge.com/minecraft/mc-mods/modern-ui",
        "Description":  "Modern desktop framework from low-level 3D graphics API to high-level view model, for development of 2D/3D rendering software or game engine, with internationalization support and many new technologies.\n    Modern UI is a desktop application framework designed for standalone 2D and 3D rendering software development. It makes use of modern 3D graphical APIs and technologies to provide high real-time rendering performance. For good measure, Modern UI improves and optimizes a set of features used by Android and its own set of internationalization supporting text layout engine meeting Unicode specification.\n    This version is an official version that extends to Minecraft and Fabric, which combines Modern UI with Minecraft and provides a large number of additional features and modding APIs."
    },
    {
        "Jar":  "MoogsStructureLib-fabric-1.20-3.4.2.jar",
        "Id":  "moogs_structures",
        "Name":  "Moog\u0027s Structure Lib",
        "Version":  "3.4.2+fabric",
        "Authors":  "FinnDog",
        "License":  "GNU Lesser General Public License v3.0",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/moogs-structure-lib",
        "Description":  "A library mod for generating structures"
    },
    {
        "Jar":  "MoogsVoyagerStructures-universal-1.20-5.1.3.jar",
        "Id":  "mvs",
        "Name":  "MoogsVoyagerStructures",
        "Version":  "5.1.3",
        "Authors":  "FinnDog",
        "License":  "GNU Lesser General Public License v3.0",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/moogs-voyager-structures",
        "Description":  "160+ structures made with vanilla blocks and entities bringing life to your world! Discover loot, enemies, \u0026 villagers along the way!"
    },
    {
        "Jar":  "naturalist-fabric-4.0.3-1.20.1.jar",
        "Id":  "naturalist",
        "Name":  "Naturalist",
        "Version":  "4.0.3",
        "Authors":  "Starfish Studios",
        "License":  "MIT",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/naturalist",
        "Description":  "Enhances the overworld by adding new wildlife with unique behavior!"
    },
    {
        "Jar":  "omnimovement-fabric-1.0+mc1.20.1.jar",
        "Id":  "omnimovement",
        "Name":  "Omnimovement",
        "Version":  "1.0",
        "Authors":  "RhysR6S",
        "License":  "All-Rights-Reserved",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "",
        "Description":  "An Omnimovement mod."
    },
    {
        "Jar":  "PresenceFootsteps-1.10.1+1.20.1.jar",
        "Id":  "presencefootsteps",
        "Name":  "Presence Footsteps",
        "Version":  "1.10.1+1.20.1",
        "Authors":  "Hurricaaane (Ha3), Sollace",
        "License":  "MIT",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://github.com/Sollace/Presence-Footsteps",
        "Description":  "An overly complicated sound mod."
    },
    {
        "Jar":  "resourcefullib-fabric-1.20.1-2.1.29.jar",
        "Id":  "resourcefullib",
        "Name":  "Resourceful Lib",
        "Version":  "2.1.29",
        "Authors":  "ThatGravyBoat, Epic_Oreo",
        "License":  "MIT",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://resourcefulbees.com",
        "Description":  "Library Mod by Team Resourceful"
    },
    {
        "Jar":  "rhino-fabric-2001.2.3-build.10.jar",
        "Id":  "rhino",
        "Name":  "Rhino",
        "Version":  "2001.2.3-build.10",
        "Authors":  "LatvianModder, Mozilla",
        "License":  "MPL-2.0",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://github.com/KubeJS-Mods/Rhino",
        "Description":  "A fork of Mozilla\u0027s Rhino library, modified for use in mods"
    },
    {
        "Jar":  "sayitenglish-fabric-1.20.1-1.1.0.jar",
        "Id":  "sayitenglish",
        "Name":  "SayIt",
        "Version":  "1.1.0",
        "Authors":  "SpicesFire",
        "License":  "MIT",
        "Category":  "SayIt \u0026 IA",
        "Homepage":  "",
        "Description":  "Learn a language while you play: say every new item\u0027s name out loud (or type it) in English, German, Spanish, French, Italian, Portuguese, Russian or Turkish before you can keep it, and answer sentence challenges as you craft, fight and explore. Offline speech recognition (Vosk)."
    },
    {
        "Jar":  "sodium-fabric-0.5.13+mc1.20.1.jar",
        "Id":  "sodium",
        "Name":  "Sodium",
        "Version":  "0.5.13+mc1.20.1",
        "Authors":  "JellySquid (jellysquid3)",
        "License":  "Polyform-Shield-1.0.0",
        "Category":  "Otimização \u0026 Visual",
        "Homepage":  "https://github.com/CaffeineMC/sodium-fabric",
        "Description":  "Sodium is a powerful rendering engine for Minecraft which greatly improves frame rates and micro-stutter, while fixing many graphical issues"
    },
    {
        "Jar":  "stack-to-nearby-chests-mc1.20.1-0.5.10.jar",
        "Id":  "stack-to-nearby-chests",
        "Name":  "Stack to Nearby Chests",
        "Version":  "0.5.10",
        "Authors":  "xiaocihua",
        "License":  "LGPL-3.0",
        "Category":  "Jogabilidade \u0026 QoL",
        "Homepage":  "https://github.com/xiaocihua",
        "Description":  "A client-side ONLY mod, adds the quick stacking and restocking features from Terraria into Minecraft."
    },
    {
        "Jar":  "Towns-and-Towers-1.12-Fabric+Forge.jar",
        "Id":  "t_and_t",
        "Name":  "Towns and Towers",
        "Version":  "1.12",
        "Authors":  "Kubek and Biban_Auriu, Cristelknight999",
        "License":  "https://creativecommons.org/licenses/by-nc-sa/4.0/",
        "Category":  "Conteúdo \u0026 Mobília",
        "Homepage":  "https://www.curseforge.com/minecraft/mc-mods/towns-and-towers",
        "Description":  "Spice up your world with new villages, pillager outposts, and even new ships!"
    },
    {
        "Jar":  "yet_another_config_lib_v3-3.6.6+1.20.1-fabric.jar",
        "Id":  "yet_another_config_lib_v3",
        "Name":  "YetAnotherConfigLib",
        "Version":  "3.6.6+1.20.1-fabric",
        "Authors":  "isXander",
        "License":  "LGPL-3.0-or-later",
        "Category":  "Core \u0026 Bibliotecas",
        "Homepage":  "https://isxander.dev",
        "Description":  "YetAnotherConfigLib (yacl) is just that. A builder-based configuration library for Minecraft."
    }
]
;

// Additional Core Infrastructure Attributions
const INFRA_DATA = [
  {
    Name: "Vosk Offline Neural Speech Recognition",
    Id: "vosk-model-small-ru-0.22",
    Authors: "Alpha Cephei Inc.",
    License: "Apache-2.0",
    Category: "SayIt & IA",
    Homepage: "https://alphacephei.com/vosk/",
    Description: "Modelo de rede neural acústica leve em russo operando 100% offline em tempo real."
  },
  {
    Name: "UltimMC Portable Launcher",
    Id: "ultimmc",
    Authors: "UltimMC / Prism Launcher Contributors",
    License: "GPL-3.0-only",
    Category: "Core & Bibliotecas",
    Homepage: "https://github.com/UltimMC/Launcher",
    Description: "Inicializador autônomo e portátil baseado no Prism Launcher com suporte a instâncias isoladas."
  }
];

let activeAudio = null;
let currentPlayingId = null;

/**
 * Interactive Audio Player Engine
 */
function initAudioPlayer() {
  const playButtons = document.querySelectorAll(".btn-play-audio");

  playButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      const card = btn.closest(".audio-card");
      const audioSrc = btn.getAttribute("data-audio") || (card && card.getAttribute("data-audio-src"));
      if (!audioSrc) return;

      const cardId = (card && card.getAttribute("data-id")) || audioSrc;

      if (currentPlayingId === cardId && activeAudio && !activeAudio.paused) {
        activeAudio.pause();
        resetAudioUI();
        return;
      }

      if (activeAudio) {
        activeAudio.pause();
        activeAudio.currentTime = 0;
        resetAudioUI();
      }

      const audio = new Audio(audioSrc);
      activeAudio = audio;
      currentPlayingId = cardId;

      if (card) {
        card.classList.add("active-playback");
      }
      btn.innerHTML = '<span class="play-icon">⏸</span> Pausar';

      audio.play().catch(err => {
        console.warn("[SayIt-WebAudio] Reprodução bloqueada:", err);
        showToast("Clique para autorizar a reprodução de áudio.");
        resetAudioUI();
      });

      audio.addEventListener("ended", () => {
        resetAudioUI();
      });

      audio.addEventListener("error", () => {
        console.error("[SayIt-WebAudio] Falha ao carregar áudio:", audioSrc);
        showToast("Áudio indisponível neste momento.");
        resetAudioUI();
      });
    });
  });
}

function resetAudioUI() {
  document.querySelectorAll(".audio-card").forEach(c => c.classList.remove("active-playback"));
  document.querySelectorAll(".btn-play-audio").forEach(b => {
    b.innerHTML = '<span class="play-icon">▶</span> Ouvir Pronúncia';
  });
  currentPlayingId = null;
}

/**
 * Tab Switcher for Audio Preview Cards
 */
function initAudioTabs() {
  const tabs = document.querySelectorAll(".audio-tabs .tab-btn");
  const sections = {
    radial: document.getElementById("tab-content-radial"),
    survival: document.getElementById("tab-content-survival")
  };

  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      const target = tab.getAttribute("data-tab");
      if (target === "radial") {
        if (sections.radial) sections.radial.style.display = "grid";
        if (sections.survival) sections.survival.style.display = "none";
      } else if (target === "survival") {
        if (sections.radial) sections.radial.style.display = "none";
        if (sections.survival) sections.survival.style.display = "grid";
      } else {
        if (sections.radial) sections.radial.style.display = "grid";
        if (sections.survival) sections.survival.style.display = "grid";
      }
    });
  });
}

/**
 * In-Game HUD Challenge Simulator
 */
function initSimulator() {
  const btnVoice = document.getElementById("btn-simulate-voice");
  const waveBars = document.getElementById("sim-wave-bars");
  const resultBadge = document.getElementById("sim-result-badge");
  const timerDisplay = document.getElementById("sim-timer-display");
  const ruText = document.getElementById("sim-ru-text");
  const phoneticText = document.getElementById("sim-phonetic-text");
  const ptText = document.getElementById("sim-pt-text");
  const slots = document.querySelectorAll(".mc-slot[data-item]");

  const itemChallenges = {
    wood: {
      ru: "Мне нужно дерево!",
      phonetic: "[Mnye nuzhno dye-rye-va!]",
      pt: '"Preciso de madeira!"',
      audio: "assets/audio/phrase_item.mp3"
    },
    bread: {
      ru: "Мне нужна еда!",
      phonetic: "[Mnye nuzhna ye-da!]",
      pt: '"Preciso de comida / Pão!"',
      audio: "assets/audio/phrase_food.mp3"
    },
    diamond: {
      ru: "Мне нужен алмаз!",
      phonetic: "[Mnye nuzhen al-maz!]",
      pt: '"Preciso de diamante!"',
      audio: "assets/audio/vocab_diamond.mp3"
    },
    kettle: {
      ru: "Мне нужен чайник!",
      phonetic: "[Mnye nuzhen chay-nik!]",
      pt: '"Preciso de uma chaleira!"',
      audio: "assets/audio/phrase_kettle.mp3"
    },
    camera: {
      ru: "Мне нужна камера!",
      phonetic: "[Mnye nuzhna ka-mye-ra!]",
      pt: '"Preciso da câmera fotográfica!"',
      audio: "assets/audio/phrase_camera.mp3"
    },
    net: {
      ru: "Мне нужен сачок!",
      phonetic: "[Mnye nuzhen sa-chok!]",
      pt: '"Preciso do puçá de insetos!"',
      audio: "assets/audio/phrase_net.mp3"
    }
  };

  let currentItem = "wood";
  let simTimer = null;
  let simAudio = null;

  slots.forEach(slot => {
    slot.addEventListener("click", () => {
      slots.forEach(s => s.classList.remove("active"));
      slot.classList.add("active");
      const itemKey = slot.getAttribute("data-item");
      if (itemChallenges[itemKey]) {
        currentItem = itemKey;
        const data = itemChallenges[itemKey];
        if (ruText) ruText.textContent = data.ru;
        if (phoneticText) phoneticText.textContent = data.phonetic;
        if (ptText) ptText.textContent = data.pt;
        if (resultBadge) resultBadge.classList.remove("visible");
        if (waveBars) waveBars.classList.remove("active");
        if (timerDisplay) timerDisplay.textContent = "[12.0s]";
      }
    });
  });

  if (btnVoice) {
    btnVoice.addEventListener("click", () => {
      const data = itemChallenges[currentItem] || itemChallenges.wood;
      if (waveBars) waveBars.classList.add("active");
      if (resultBadge) resultBadge.classList.remove("visible");

      if (simAudio) {
        simAudio.pause();
        simAudio.currentTime = 0;
      }
      simAudio = new Audio(data.audio);
      simAudio.play().catch(() => {});

      let timeLeft = 12.0;
      clearInterval(simTimer);
      simTimer = setInterval(() => {
        timeLeft -= 0.1;
        if (timerDisplay) timerDisplay.textContent = "[" + Math.max(0, timeLeft).toFixed(1) + "s]";
        if (timeLeft <= 9.8) {
          clearInterval(simTimer);
          if (waveBars) waveBars.classList.remove("active");
          if (resultBadge) {
            resultBadge.textContent = "[OK] Aprovado: 98% • +20 Pontos Coop • Item Preservado!";
            resultBadge.classList.add("visible");
          }
          showToast("[OK] Reconhecimento Vosk: \"" + data.ru + "\" (98% precisão)");
        }
      }, 100);
    });
  }
}

/**
 * Interactive Radial Wheel Showcase Controller
 */
function initRadialWheel() {
  const sectorCards = document.querySelectorAll(".sector-card");
  const panelTitle = document.getElementById("radial-panel-title");
  const panelDesc = document.getElementById("radial-panel-desc");
  const panelRu = document.getElementById("radial-panel-ru");
  const panelPhonetic = document.getElementById("radial-panel-phonetic");
  const centerHub = document.getElementById("wheel-center-hub");

  const actionAudios = {
    item: "assets/audio/phrase_item.mp3",
    food: "assets/audio/phrase_food.mp3",
    help: "assets/audio/phrase_help.mp3",
    lava: "assets/audio/phrase_lava.mp3",
    follow: "assets/audio/phrase_follow.mp3",
    waypoint: "assets/audio/phrase_waypoint.mp3"
  };

  let radialAudio = null;

  function activateSector(card, playSound) {
    if (!card) return;
    sectorCards.forEach(c => c.classList.remove("active"));
    card.classList.add("active");

    const titleEl = card.querySelector(".sector-card-title, .sector-title");
    const title = titleEl ? titleEl.textContent : "Ação";
    const ru = card.getAttribute("data-ru") || "";
    const phonetic = card.getAttribute("data-phonetic") || "";
    const pt = card.getAttribute("data-pt") || "";
    const action = card.getAttribute("data-action") || "item";

    if (panelTitle) panelTitle.textContent = title;
    if (panelDesc) panelDesc.textContent = pt;
    if (panelRu) panelRu.textContent = ru;
    if (panelPhonetic) panelPhonetic.textContent = phonetic;

    if (centerHub) {
      centerHub.classList.remove("pulse");
      void centerHub.offsetWidth;
      centerHub.classList.add("pulse");
    }

    if (playSound && actionAudios[action]) {
      if (radialAudio) {
        radialAudio.pause();
        radialAudio.currentTime = 0;
      }
      radialAudio = new Audio(actionAudios[action]);
      radialAudio.play().catch(() => {});
    }
  }

  sectorCards.forEach(card => {
    card.addEventListener("click", () => {
      activateSector(card, true);
    });
  });

  window.addEventListener("keydown", (e) => {
    if (e.target && (e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA")) return;
    if (e.key === "v" || e.key === "V") {
      const activeIdx = Array.from(sectorCards).findIndex(c => c.classList.contains("active"));
      const nextIdx = (activeIdx + 1) % (sectorCards.length || 1);
      activateSector(sectorCards[nextIdx], true);
    }
  });
}

/**
 * Searchable & Filterable Mod Credits Table
 */
function initCreditsTable() {
  const tbody = document.getElementById("credits-tbody");
  const searchInput = document.getElementById("mod-search-input");
  const filterPills = document.querySelectorAll(".filter-pill");
  const countDisplay = document.getElementById("credits-count-display");

  if (!tbody) return;

  const allMods = [...MODS_DATA, ...INFRA_DATA];
  let currentCategory = "all";
  let searchQuery = "";

  function renderTable() {
    tbody.innerHTML = "";

    const filtered = allMods.filter(mod => {
      const name = mod.Name || mod.name || "";
      const id = mod.Id || mod.id || "";
      const authors = mod.Authors || mod.authors || "Comunidade";
      const category = mod.Category || mod.category || "Geral";
      const license = mod.License || mod.license || "Open Source";
      const description = mod.Description || mod.description || "";

      const matchesCat = currentCategory === "all" || category === currentCategory;
      const textToSearch = `${name} ${id} ${authors} ${license} ${description}`.toLowerCase();
      const matchesSearch = !searchQuery || textToSearch.includes(searchQuery.toLowerCase());
      return matchesCat && matchesSearch;
    });

    if (filtered.length === 0) {
      const emptyRow = document.createElement("tr");
      emptyRow.innerHTML = '<td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--text-dim);">Nenhum mod encontrado para os critérios de busca.</td>';
      tbody.appendChild(emptyRow);
      if (countDisplay) countDisplay.textContent = "Mostrando 0 de " + allMods.length + " mods";
      return;
    }

    filtered.forEach(mod => {
      const name = mod.Name || mod.name || "Mod";
      const id = mod.Id || mod.id || "";
      const authors = mod.Authors || mod.authors || "Comunidade";
      const category = mod.Category || mod.category || "Geral";
      const license = mod.License || mod.license || "Open Source";
      const homepage = mod.Homepage || mod.homepage || "";
      const description = mod.Description || mod.description || "";

      const tr = document.createElement("tr");
      const linkHtml = homepage
        ? `<a href="${homepage}" target="_blank" rel="noopener noreferrer" title="Visitar página oficial" class="mod-link-btn">↗ Visitar</a>`
        : '<span style="color: var(--text-dim);">-</span>';

      tr.innerHTML = `
        <td>
          <div class="mod-title-cell">
            <span class="mod-name">${name}</span>
            <span class="mod-id-code">${id}</span>
          </div>
        </td>
        <td><span class="mod-category-badge">${category}</span></td>
        <td><span class="mod-authors-text">${authors}</span></td>
        <td><span class="mod-license-badge">${license}</span></td>
        <td><span class="mod-desc-text">${description}</span></td>
        <td>${linkHtml}</td>
      `;
      tbody.appendChild(tr);
    });

    if (countDisplay) {
      countDisplay.textContent = `Mostrando ${filtered.length} de ${allMods.length} mods e componentes`;
    }
  }

  filterPills.forEach(pill => {
    pill.addEventListener("click", () => {
      filterPills.forEach(p => p.classList.remove("active"));
      pill.classList.add("active");
      currentCategory = pill.getAttribute("data-category");
      renderTable();
    });
  });

  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      searchQuery = e.target.value.trim();
      renderTable();
    });
  }

  renderTable();
}

/**
 * Copy to Clipboard with Feedback Toast
 */
function initClipboard() {
  const copyButtons = document.querySelectorAll("[data-copy], #btn-copy-sha");

  copyButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      let textToCopy = btn.getAttribute("data-copy");
      let label = btn.getAttribute("data-copy-label") || "Texto";

      if (btn.id === "btn-copy-sha" || !textToCopy) {
        const hashEl = document.getElementById("sha256-val");
        textToCopy = hashEl ? hashEl.textContent.trim() : "7dacabded7f319811a467e3085d8d80db2da6b8be7d240a10ed9506a78c4c347";
        label = "Checksum SHA-256";
      }

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(textToCopy).then(() => {
          showToast(`[OK] ${label} copiado para a área de transferência!`);
        }).catch(() => fallbackCopy(textToCopy, label));
      } else {
        fallbackCopy(textToCopy, label);
      }
    });
  });
}

function fallbackCopy(text, label) {
  const ta = document.createElement("textarea");
  ta.value = text;
  ta.style.position = "fixed";
  ta.style.left = "-9999px";
  document.body.appendChild(ta);
  ta.focus();
  ta.select();
  try {
    document.execCommand("copy");
    showToast(`[OK] ${label} copiado!`);
  } catch (err) {
    showToast(`Falha ao copiar: ${text}`);
  }
  document.body.removeChild(ta);
}

/**
 * Toast Notification Controller
 */
function showToast(message) {
  let container = document.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = "toast";
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => toast.classList.add("show"), 10);

  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => {
      if (toast.parentNode) toast.parentNode.removeChild(toast);
    }, 300);
  }, 3200);
}

/**
 * Smooth Navigation & Mobile Menu
 */
function initNavigation() {
  const menuToggle = document.querySelector(".menu-toggle");
  const navMenu = document.querySelector(".nav-menu");
  const navLinks = document.querySelectorAll(".nav-link");

  if (menuToggle && navMenu) {
    menuToggle.addEventListener("click", () => {
      navMenu.classList.toggle("open");
    });

    navLinks.forEach(link => {
      link.addEventListener("click", () => {
        navMenu.classList.remove("open");
      });
    });
  }

  const sections = document.querySelectorAll("section[id]");
  window.addEventListener("scroll", () => {
    let current = "";
    const scrollY = window.pageYOffset;

    sections.forEach(section => {
      const sectionHeight = section.offsetHeight;
      const sectionTop = section.offsetTop - 120;
      if (scrollY >= sectionTop && scrollY < sectionTop + sectionHeight) {
        current = section.getAttribute("id");
      }
    });

    navLinks.forEach(link => {
      link.classList.remove("active");
      if (link.getAttribute("href") === `#${current}`) {
        link.classList.add("active");
      }
    });
  });
}

function initDownloadLinks() {
  if (window.location.hostname.endsWith('github.io')) {
    const downloadBtns = document.querySelectorAll('a[href$="Minecraft_Russo_Client_Setup.zip"]');
    const releaseUrl = 'https://github.com/plenoryan/minecraft-russo/releases/download/v1.1.0/Minecraft_Russo_Client_Setup.zip';
    downloadBtns.forEach(btn => {
      btn.href = releaseUrl;
    });
  }
}


/**
 * Subtle Minecraft Parallax Gutters
 * Coordinates subtle mouse drift for ambient pixel-art particles in desktop gutters
 */
function initParallax() {
  const canvas = document.querySelector(".mc-parallax-canvas");
  if (!canvas) return;

  if (window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
    return;
  }

  const particles = canvas.querySelectorAll(".mc-particle");
  if (!particles.length) return;

  let mouseX = 0;
  let mouseY = 0;
  let ticking = false;

  window.addEventListener("mousemove", (e) => {
    // Offset from window center: -0.5 to +0.5
    mouseX = (e.clientX / window.innerWidth) - 0.5;
    mouseY = (e.clientY / window.innerHeight) - 0.5;

    if (!ticking) {
      window.requestAnimationFrame(() => {
        particles.forEach((p) => {
          const speed = parseFloat(p.getAttribute("data-speed") || "20");
          const px = (mouseX * speed).toFixed(1);
          const py = (mouseY * speed).toFixed(1);
          p.style.setProperty("--px", px + "px");
          p.style.setProperty("--py", py + "px");
        });
        ticking = false;
      });
      ticking = true;
    }
  }, { passive: true });
}