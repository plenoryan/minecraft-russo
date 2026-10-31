// Russian Pedagogical Tooltips Script for Fabric 1.20.1 (KubeJS)
// Executed client-side with zero multiplayer server impact.

ItemEvents.tooltip(event => {
    const ruCurriculum = {
        'minecraft:oak_log': {
            ru: 'Дéрево',
            phon: '[DYE-rye-va]',
            pt: 'Madeira / Árvore',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'A primeira sílaba é tônica (ДЕ-ре-во). A vogal \'o\' átona final sofre redução vocálica (ákanie), soando como [a].'
        },
        'minecraft:oak_planks': {
            ru: 'Дóски',
            phon: '[DOS-kee]',
            pt: 'Tábuas de Carvalho',
            gen: 'Plural (мн.ч.)',
            tier: 1,
            note: '\'O\' tônico longo e aberto. Substantivo no plural com terminação regular em -и.'
        },
        'minecraft:stick': {
            ru: 'Пáлка',
            phon: '[PAHL-ka]',
            pt: 'Graveto',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Terminação regular feminina em -а. Acento tônico na primeira sílaba.'
        },
        'minecraft:stone': {
            ru: 'Кáмень',
            phon: '[KAH-myen\']',
            pt: 'Pedra',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'O sinal brando final (ь) suaviza a consoante dental \'н\', tornando-a palatizada [n\'].'
        },
        'minecraft:cobblestone': {
            ru: 'Булӹжник',
            phon: '[boo-LIZH-neek]',
            pt: 'Pedregulho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Contém a vogal gutural posterior dura \'ы\'. A sílaba tônica é -лы-.'
        },
        'minecraft:dirt': {
            ru: 'Земл́я',
            phon: '[zyem-LYAH]',
            pt: 'Terra',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino oxítono terminado em -я. A primeira sílaba \'зем-\' é suave.'
        },
        'minecraft:sand': {
            ru: 'Песóк',
            phon: '[pye-SOK]',
            pt: 'Areia',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'A vogal \'e\' inicial é branda [pye]. Acento oxítono no \'o\' final.'
        },
        'minecraft:wooden_axe': {
            ru: 'Топóр',
            phon: '[ta-POR]',
            pt: 'Machado de Madeira',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Clássica redução vocálica russa: o primeiro \'o\' átono reduz para som de [a].'
        },
        'minecraft:wooden_pickaxe': {
            ru: 'Киркá',
            phon: '[keer-KAH]',
            pt: 'Picareta de Madeira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino oxítono com tônica na última sílaba (-КА).'
        },
        'minecraft:wooden_shovel': {
            ru: 'Лопáта',
            phon: '[la-PAH-ta]',
            pt: 'Pá de Madeira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'O primeiro \'o\' reduz para [a]. Tônica paroxítona em -ПА-.'
        },
        'minecraft:torch': {
            ru: 'Фáкел',
            phon: '[FAH-kyel]',
            pt: 'Tocha',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Origem germânica integrada ao russo. Tônica em ФА-. O \'е\' final é suave.'
        },
        'minecraft:crafting_table': {
            ru: 'Верстáк',
            phon: '[vyer-STAHK]',
            pt: 'Bancada de Trabalho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Consoante final dura \'к\'. Tônica oxítona na sílaba final -СТАК.'
        },
        'minecraft:furnace': {
            ru: 'Печь',
            phon: '[Pyech\']',
            pt: 'Fornalha',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo da 3ª declinação feminina terminado em sinal brando (ь). \'Ч\' é sempre suave.'
        },
        'minecraft:chest': {
            ru: 'Сундýк',
            phon: '[soon-DOOK]',
            pt: 'Baú',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Vogal posterior \'у\' tônica na última sílaba. Consoante \'к\' dura no final.'
        },
        'minecraft:oak_door': {
            ru: 'Дверь',
            phon: '[Dvyer\']',
            pt: 'Porta',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Monossílabo feminino com \'р\' palatizado pelo sinal brando final.'
        },
        'minecraft:glass': {
            ru: 'Стеклó',
            phon: '[styek-LO]',
            pt: 'Vidro',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Oxítona neutra terminada em -о pleno. O encontro \'ст\' é sonoro.'
        },
        'minecraft:bread': {
            ru: 'Хлеб',
            phon: '[Khlyep]',
            pt: 'Pão',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Dessonorização da consoante final em russo: a letra \'б\' em fim de palavra soa como [p].'
        },
        'minecraft:water_bucket': {
            ru: 'Водá',
            phon: '[va-DAH]',
            pt: 'Balde com Água / Água',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Exemplo supremo de redução vocálica (ákanie): o \'о\' átono vira [a], a tônica é no -ДА.'
        },
        'minecraft:apple': {
            ru: 'Я́блоко',
            phon: '[YAH-bla-ka]',
            pt: 'Maçã',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: 'Inicia com a vogal iotizada \'я\' [ya]. As duas letras \'о\' seguintes são reduzidas para som de [a].'
        },
        'minecraft:wheat': {
            ru: 'Пшенúца',
            phon: '[pshe-NEE-tsa]',
            pt: 'Trigo',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Encontro consonantal \'пш\' duro. A letra \'ц\' soa como [ts].'
        },
        'minecraft:beef': {
            ru: 'Мя́со',
            phon: '[MYAH-sa]',
            pt: 'Carne Crua',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: 'Substantivo neutro. \'М\' palatizado por \'я\'. O \'о\' final reduz para [a].'
        },
        'minecraft:cooked_beef': {
            ru: 'Говя́дина',
            phon: '[ga-VYA-dee-na]',
            pt: 'Carne Bovina / Bife',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Sufixo de carne animal -ина. \'О\' inicial reduzido para [a], tônica em -ВЯ-.'
        },
        'minecraft:porkchop': {
            ru: 'Свинúна',
            phon: '[svee-NEE-na]',
            pt: 'Carne de Porco',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Derivado de свинья (porco) com o sufixo -ина. Tônica em -НИ-.'
        },
        'minecraft:cod': {
            ru: 'Р́ыба',
            phon: '[RY-ba]',
            pt: 'Peixe',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'A vogal dura \'ы\' requer posicionamento posterior da língua. Tônica em РЫ-.'
        },
        'minecraft:white_wool': {
            ru: 'Шерсть',
            phon: '[Sherst\']',
            pt: 'Lã',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'A consoante \'ш\' é sempre dura no russo. O sinal brando final suaviza o \'т\'.'
        },
        'minecraft:white_bed': {
            ru: 'Кровáть',
            phon: '[kra-VAHT\']',
            pt: 'Cama',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Substantivo feminino da 3ª declinação. \'О\' átono vira [a], \'т\' palatizado no fim.'
        },
        'minecraft:coal': {
            ru: 'У́голь',
            phon: '[OO-gahl\']',
            pt: 'Carvão',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Tônica na primeira sílaba (У-голь). O \'л\' é suavizado pelo sinal brando [l\'].'
        },
        'minecraft:bucket': {
            ru: 'Ведрó',
            phon: '[vye-DRO]',
            pt: 'Balde',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: 'Substantivo neutro oxítono terminado em -о pleno. Primeira sílaba suave [vye].'
        },
        'minecraft:cow_spawn_egg': {
            ru: 'Корóва',
            phon: '[ka-RO-va]',
            pt: 'Vaca',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Primeiro \'o\' reduzido para [a]. Tônica na sílaba central -РО-.'
        },
        'minecraft:sheep_spawn_egg': {
            ru: 'Овцá',
            phon: '[af-TSAH]',
            pt: 'Ovelha',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: '\'О\' inicial átono vira [a]. Letra \'ц\' africada sonora [ts]. Tônica final.'
        },
        'minecraft:pig_spawn_egg': {
            ru: 'Свинь́я',
            phon: '[sveen-YAH]',
            pt: 'Porco',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Uso do sinal brando separador (разделительный мягкий знак) antes da vogal \'я\'.'
        },
        'minecraft:chicken': {
            ru: 'Кýрица',
            phon: '[KOO-ree-tsa]',
            pt: 'Frango / Galinha',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Terminação típica russa em -ица para fêmeas de animais. Tônica na primeira sílaba.'
        },
        'minecraft:carrot': {
            ru: 'Моркóвь',
            phon: '[mar-KOF\']',
            pt: 'Cenoura',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Dessonorização da consoante \'в\' final diante de sinal brando, soando como [f\'].'
        },
        'minecraft:potato': {
            ru: 'Картóфель',
            phon: '[kar-TO-fyel\']',
            pt: 'Batata',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Substantivo coletivo masculino derivado do alemão. Tônica em -ТО-. \'Л\' brando final.'
        },
        'minecraft:iron_ingot': {
            ru: 'Желéзо',
            phon: '[zhe-LYE-za]',
            pt: 'Barra de Ferro',
            gen: 'Neutro (с.р.)',
            tier: 3,
            note: 'A consoante \'ж\' é sempre dura. O \'е\' seguinte é brando [lye], e o \'о\' final reduz.'
        },
        'minecraft:gold_ingot': {
            ru: 'Зóлото',
            phon: '[ZO-la-ta]',
            pt: 'Barra de Ouro',
            gen: 'Neutro (с.р.)',
            tier: 3,
            note: 'Três letras \'о\', mas apenas a primeira é tônica! As duas últimas reduzem para som de [a].'
        },
        'minecraft:diamond': {
            ru: 'Алмáз',
            phon: '[ahl-MAHS]',
            pt: 'Diamante',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Dessonorização final: a consoante sonora \'з\' em fim de palavra soa como o surdo [s].'
        },
        'minecraft:iron_sword': {
            ru: 'Меч',
            phon: '[Myech]',
            pt: 'Espada de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Monossílabo masculino forte. A letra \'ч\' é pronunciada com suavidade inerente.'
        },
        'minecraft:shield': {
            ru: 'Щит',
            phon: '[Shcheet]',
            pt: 'Escudo',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'A letra russa \'щ\' representa uma consoante fricativa palatal longa [ɕː] (shch suave).'
        },
        'minecraft:bow': {
            ru: 'Лук',
            phon: '[Look]',
            pt: 'Arco',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Monossílabo curto. Curiosidade: \'лук\' também significa \'cebola\' em russo!'
        },
        'minecraft:arrow': {
            ru: 'Стрелá',
            phon: '[strye-LAH]',
            pt: 'Flecha',
            gen: 'Feminino (ж.р.)',
            tier: 3,
            note: 'Oxítona feminina. Encontro consonantal \'стр\' dinâmico e tônica em -ЛА.'
        },
        'minecraft:iron_helmet': {
            ru: 'Шлем',
            phon: '[Shlyem]',
            pt: 'Capacete de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: '\'Ш\' sempre duro seguido de \'л\' suavizado por \'е\'.'
        },
        'minecraft:iron_chestplate': {
            ru: 'Нагрýдник',
            phon: '[na-GROOD-neek]',
            pt: 'Peitoral de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Formado pelo prefixo na- (sobre) + грудь (peito) + sufixo de objeto -ник.'
        },
        'minecraft:iron_leggings': {
            ru: 'Пóножи',
            phon: '[PO-na-zhee]',
            pt: 'Calças de Ferro',
            gen: 'Plural (мн.ч.)',
            tier: 3,
            note: 'Termo de armadura clássica russa. \'Ж\' sempre duro, sufixo plural em -и.'
        },
        'minecraft:iron_boots': {
            ru: 'Ботúнки',
            phon: '[ba-TEEN-kee]',
            pt: 'Botas de Ferro',
            gen: 'Plural (мн.ч.)',
            tier: 3,
            note: 'Substantivo plural regular para calçados de cano médio. Tônica em -ТИН-.'
        },
        'minecraft:zombie_head': {
            ru: 'Зóмби',
            phon: '[ZOM-bee]',
            pt: 'Zumbi',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Substantivo de origem estrangeira indeclinável (несклоняемое существительное).'
        },
        'minecraft:skeleton_skull': {
            ru: 'Скелéт',
            phon: '[skye-LYET]',
            pt: 'Esqueleto',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Oxítona terminada em dental dura \'т\'. Consoante \'л\' branda antes de \'е\'.'
        },
        'minecraft:spider_eye': {
            ru: 'Паýк',
            phon: '[pa-OOK]',
            pt: 'Aranha / Olho de Aranha',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Hiato a-ú. A tônica recai com clareza sobre o \'у\'.'
        },
        'minecraft:gunpowder': {
            ru: 'Крúпер',
            phon: '[KREE-pyer]',
            pt: 'Creeper / Pólvora',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Adaptação fonética direta ao cirílico do monstro ícone do Minecraft.'
        },
        'minecraft:bone': {
            ru: 'Кость',
            phon: '[Kost\']',
            pt: 'Osso',
            gen: 'Feminino (ж.р.)',
            tier: 3,
            note: 'Encontro consonantal \'ст\' palatizado pelo sinal brando final. Substantivo feminino.'
        },
        'minecraft:string': {
            ru: 'Нить',
            phon: '[Neet\']',
            pt: 'Linha / Fio',
            gen: 'Feminino (ж.р.)',
            tier: 3,
            note: 'Monossílabo com vogal \'и\' palatizante e sinal brando final.'
        },
        'minecraft:lead': {
            ru: 'Поводóк',
            phon: '[pa-va-DOK]',
            pt: 'Laço',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Dupla redução vocálica: os dois primeiros \'o\' átonos viram [a], restando o último tônico.'
        },
        'minecraft:obsidian': {
            ru: 'Обсидиáн',
            phon: '[ap-see-dee-AHN]',
            pt: 'Obsidiana',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Redução do \'о\' inicial átono para [a]. Tônica na penúltima sílaba -ДИАН.'
        },
        'minecraft:lava_bucket': {
            ru: 'Лáва',
            phon: '[LAH-va]',
            pt: 'Balde de Lava / Lava',
            gen: 'Feminino (ж.р.)',
            tier: 4,
            note: 'Paroxítona feminina suave. O primeiro \'а\' é aberto e tônico.'
        },
        'minecraft:potion': {
            ru: 'Зéлье',
            phon: '[ZYE-lye]',
            pt: 'Poção',
            gen: 'Neutro (с.р.)',
            tier: 4,
            note: 'Substantivo neutro com sinal brando separador (ь) antes da vogal \'е\'.'
        },
        'minecraft:brewing_stand': {
            ru: 'Вáрочная стóйка',
            phon: '[VAH-rach-na-ya STOY-ka]',
            pt: 'Suporte de Poções',
            gen: 'Feminino (ж.р.)',
            tier: 4,
            note: 'Expressão composta com concordância de adjetivo feminino terminado em -ая.'
        },
        'minecraft:book': {
            ru: 'Кнúга',
            phon: '[KNEE-ga]',
            pt: 'Livro',
            gen: 'Feminino (ж.р.)',
            tier: 4,
            note: 'Substantivo feminino clássico com \'г\' sonoro brando antes de -а.'
        },
        'minecraft:enchanting_table': {
            ru: 'Стол зачаровáния',
            phon: '[Stol za-cha-ra-VAH-nee-ya]',
            pt: 'Mesa de Encantamentos',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Regência do caso genitivo neutro: зачарование torna-se зачарования.'
        },
        'minecraft:redstone': {
            ru: 'Крáсный кáмень',
            phon: '[KRAHS-nyy KAH-myen\']',
            pt: 'Redstone (Pedra Vermelha)',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Concordância do adjetivo masculino -ый com o substantivo masculino камень.'
        },
        'minecraft:piston': {
            ru: 'Пóршень',
            phon: '[POR-shyen\']',
            pt: 'Pistão',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Consoante \'ш\' dura seguida de \'e\' e sinal brando no \'н\'.'
        },
        'minecraft:dispenser': {
            ru: 'Раздáтчик',
            phon: '[raz-DAHT-cheek]',
            pt: 'Ejetor / Distribuidor',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Prefixo raz- (indica dispersão/distribuição) + radical do verbo dвать (dar).'
        },
        'minecraft:ender_eye': {
            ru: 'О́ко Э́ндера',
            phon: '[O-ka EN-dye-ra]',
            pt: 'Olho do Ender',
            gen: 'Neutro (с.р.)',
            tier: 4,
            note: '\'Око\' é o vocábulo poético eslavo arcaico para \'olho\'. \'Эндера\' está no caso genitivo.'
        },
        'minecraft:netherite_ingot': {
            ru: 'Незерúт',
            phon: '[nye-zye-REET]',
            pt: 'Barra de Netherita',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Substantivo mineral moderno com acentuação oxítona em -ИТ.'
        },
        'minecraft:ghast_tear': {
            ru: 'Слезá гáста',
            phon: '[slye-ZAH GAH-sta]',
            pt: 'Lágrima de Ghast',
            gen: 'Feminino (ж.р.)',
            tier: 4,
            note: '\'Слеза\' (lágrima) é oxítono; \'гаста\' é o genitivo do mob masculino Гаст.'
        },
        'minecraft:blaze_rod': {
            ru: 'Стержéнь ифрúта',
            phon: '[styer-ZHEN\' eef-REE-ta]',
            pt: 'Vara de Blaze (Ifrit)',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'No Minecraft em russo, o Blaze é oficialmente traduzido como \'Ифрит\' (gênio do fogo).'
        },
        'minecraft:dragon_breath': {
            ru: 'Дыхáние дракона',
            phon: '[dy-KHAH-nee-ye dra-KO-na]',
            pt: 'Bafo de Dragão',
            gen: 'Neutro (с.р.)',
            tier: 4,
            note: 'Substantivo neutro abstrato terminado em -ие. \'Дракона\' é genitivo de Дракон.'
        },
        'minecraft:anvil': {
            ru: 'Наковáльня',
            phon: '[na-ka-VAHL\'-nya]',
            pt: 'Bigorna',
            gen: 'Feminino (ж.р.)',
            tier: 4,
            note: 'Derivado do verbo ковать (forjar). Prefixo na- e terminação instrumental feminina -льня.'
        },
        'minecraft:experience_bottle': {
            ru: 'Пузырёк óпыта',
            phon: '[poo-zy-RYOK O-py-ta]',
            pt: 'Frasco de Experiência',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'A letra \'ё\' é SEMPRE tônica em russo (пузы-РЁК). \'Опыта\' é genitivo de опыт.'
        },
    };

    for (let id in ruCurriculum) {
        let e = ruCurriculum[id];
        event.addAdvanced(id, (item, advanced, text) => {
            text.add(1, Text.of([RU] ).gold().bold(true));
            text.add(2, Text.of([Pronúncia] ).yellow());
            text.add(3, Text.of([PT]  • ).aqua());
            text.add(4, Text.of([Nível] Tier  (Fale em russo!)).darkGreen().italic(true));
            if (e.note) {
                text.add(5, Text.of([Regra] ).gray());
            }
        });
    }
});