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
            phon: '[pyech\']',
            pt: 'Fornalha',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino da 3ª declinação terminado em sinal brando (ь). Som palatal suave [ch\'].'
        },
        'minecraft:chest': {
            ru: 'Сундýк',
            phon: '[soon-DOOK]',
            pt: 'Baú',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Origem túrquica eslavizada. Tônica oxítona fechada em -ДУК.'
        },
        'minecraft:oak_door': {
            ru: 'Дверь',
            phon: '[dvyer\']',
            pt: 'Porta de Carvalho',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Monossílabo com encontro consonantal inicial [dv] e consoante palatizada final [r\'].'
        },
        'minecraft:glass': {
            ru: 'Стеклó',
            phon: '[styek-LO]',
            pt: 'Vidro',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Terminação regular neutra em -о. Acento oxítono no -ЛО final.'
        },
        'minecraft:bread': {
            ru: 'Хлеб',
            phon: '[khlyep]',
            pt: 'Pão',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Desvozeamento da consoante final russa: o \'б\' soa como [p] no final da palavra.'
        },
        'minecraft:water_bucket': {
            ru: 'Водá',
            phon: '[va-DAH]',
            pt: 'Balde de Água',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Redução vocálica (ákanie): o primeiro \'о\' átono soa como [a]. Tônica no -ДА.'
        },
        'minecraft:apple': {
            ru: 'Я́блоко',
            phon: '[YAH-bla-ka]',
            pt: 'Maçã',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: 'Acento proparoxítono no Я-. As duas vogais \'o\' seguintes reduzem para [a].'
        },
        'minecraft:wheat': {
            ru: 'Пшенúца',
            phon: '[pshye-NEE-tsa]',
            pt: 'Trigo',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Encontro consonantal inicial complexo [psh]. Consoante \'ц\' é sempre dura em russo.'
        },
        'minecraft:beef': {
            ru: 'Мя́со',
            phon: '[MYAH-sa]',
            pt: 'Bife Cru / Carne',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: 'Substantivo neutro paroxítono. O \'о\' final reduz para som aberto [a].'
        },
        'minecraft:cooked_beef': {
            ru: 'Говя́дина',
            phon: '[ga-VYAH-dee-na]',
            pt: 'Bife Cozido',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Refere-se especificamente à carne bovina. Primeira sílaba reduz para [ga].'
        },
        'minecraft:porkchop': {
            ru: 'Свинúна',
            phon: '[svee-NEE-na]',
            pt: 'Costeleta de Porco',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Sufixo produtivo -ина denotando tipo de carne animal. Tônica em -НИ-.'
        },
        'minecraft:cod': {
            ru: 'Рӹба',
            phon: '[RY-ba]',
            pt: 'Bacalhau Cru / Peixe',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Vogal central fechada posterior \'ы\'. Requer tensão muscular recuada da língua.'
        },
        'minecraft:white_wool': {
            ru: 'Шерсть',
            phon: '[shyerst\']',
            pt: 'Lã Branca',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'A consoante \'ш\' é sempre dura, enquanto o final -ть é suave palatizado.'
        },
        'minecraft:white_bed': {
            ru: 'Кровáть',
            phon: '[kra-VAHT\']',
            pt: 'Cama Branca',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Substantivo feminino em sinal brando (ь). O primeiro \'o\' reduz para [a].'
        },
        'minecraft:coal': {
            ru: 'У́голь',
            phon: '[OO-gahl\']',
            pt: 'Carvão',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Tônica na primeira sílaba У-. O \'ль\' final é um \'l\' palatizado brando.'
        },
        'minecraft:bucket': {
            ru: 'Ведрó',
            phon: '[vye-DRO]',
            pt: 'Balde',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: 'Terminação neutra em -о com tônica oxítona. No plural torna-se вёдра (VYOD-ra).'
        },
        'minecraft:carrot': {
            ru: 'Моркóвь',
            phon: '[mar-KOV\']',
            pt: 'Cenoura',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Substantivo feminino singular coletivo. Terminação labiodental branda [v\'].'
        },
        'minecraft:potato': {
            ru: 'Картóфель',
            phon: '[kar-TO-fyel\']',
            pt: 'Batata',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Origem alemã (Kartoffel). Masculino singular coletivo com tônica paroxítona em -TO-.'
        },
        'minecraft:turtle_egg': {
            ru: 'Яйцó черепáхи',
            phon: '[yay-TSO chye-rye-PAH-khee]',
            pt: 'Ovo de Tartaruga',
            gen: 'Neutro (с.р.)',
            tier: 2,
            note: '\'Яйцо\' (ovo) é neutro com acento oxítono na sílaba final -ЦО. \'Черепахи\' está no caso genitivo feminino singular.'
        },
        'minecraft:turtle_spawn_egg': {
            ru: 'Черепáха',
            phon: '[chye-rye-PAH-kha]',
            pt: 'Tartaruga',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Acento tônico em -ПА- (чере-ПА-ха). Terminação feminina regular em -а.'
        },
        'minecraft:scute': {
            ru: 'Щитóк черепáхи',
            phon: '[shchee-TOK chye-rye-PAH-khee]',
            pt: 'Escudo / Casco de Tartaruga',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Diminutivo de \'щит\' (escudo). A consoante inicial \'щ\' soa como [shch].'
        },
        'minecraft:turtle_helmet': {
            ru: 'Черепáший пáнцирь',
            phon: '[chye-rye-PAH-sheey PAN-tseer\']',
            pt: 'Casco de Tartaruga (Elmo)',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Adjetivo possessivo \'черепаший\' concordando com o substantivo masculino \'панцирь\'.'
        },
        'minecraft:iron_ingot': {
            ru: 'Желéзо',
            phon: '[zhe-LYE-za]',
            pt: 'Barra de Ferro',
            gen: 'Neutro (с.р.)',
            tier: 3,
            note: 'A consoante \'ж\' é sempre dura. Acento na segunda sílaba -ЛЕ-.'
        },
        'minecraft:gold_ingot': {
            ru: 'Зóлото',
            phon: '[ZO-la-ta]',
            pt: 'Barra de Ouro',
            gen: 'Neutro (с.р.)',
            tier: 3,
            note: 'Pleofonia eslava (ol/olo). Tônica na primeira sílaba com dupla redução vocálica.'
        },
        'minecraft:diamond': {
            ru: 'Алмáз',
            phon: '[al-MAHS]',
            pt: 'Diamante',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Desvozeamento final: a consoante sonora \'з\' soa como [s] no final da palavra.'
        },
        'minecraft:wooden_sword': {
            ru: 'Меч',
            phon: '[myech\']',
            pt: 'Espada de Madeira',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Substantivo masculino monossilábico terminado na consoante suave \'ч\'.'
        },
        'minecraft:iron_sword': {
            ru: 'Меч',
            phon: '[myech\']',
            pt: 'Espada de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'No SayIt pedagógico, mantém o vocábulo base \'Меч\' para consolidação lexical.'
        },
        'minecraft:shield': {
            ru: 'Щит',
            phon: '[shcheet]',
            pt: 'Escudo',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Inicia com a consoante palatal fricativa longa \'щ\' [shch].'
        },
        'minecraft:bow': {
            ru: 'Лук',
            phon: '[look]',
            pt: 'Arco',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Vogal fechada posterior \'у\'. \'Л\' velar duro sem palatização.'
        },
        'minecraft:arrow': {
            ru: 'Стрелá',
            phon: '[strye-LAH]',
            pt: 'Flecha',
            gen: 'Feminino (ж.р.)',
            tier: 3,
            note: 'Acento oxítono final no -ЛА. O cluster inicial [str] requer dicção precisa.'
        },
        'minecraft:iron_helmet': {
            ru: 'Шлем',
            phon: '[shlyem]',
            pt: 'Elmo de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Monossílabo com transição de consoante dura \'ш\' para \'л\' palatizado [sh-lyem].'
        },
        'minecraft:iron_chestplate': {
            ru: 'Нагрýдник',
            phon: '[na-GROOD-neek]',
            pt: 'Peitoral de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Prefixo na- (sobre) + radical грудь (peito) + sufixo nominalizador -ник.'
        },
        'minecraft:iron_leggings': {
            ru: 'Пóножи',
            phon: '[PO-na-zhee]',
            pt: 'Calças de Ferro',
            gen: 'Plural (мн.ч.)',
            tier: 3,
            note: 'Termo arcaico para grevas/perneiras de armadura. Tônica na primeira sílaba ПО-.'
        },
        'minecraft:iron_boots': {
            ru: 'Ботúнки',
            phon: '[ba-TEEN-kee]',
            pt: 'Botas de Ferro',
            gen: 'Plural (мн.ч.)',
            tier: 3,
            note: 'Substantivo no plural com sufixo diminutivo -инки. A primeira sílaba reduz para [ba].'
        },
        'minecraft:bone': {
            ru: 'Кость',
            phon: '[kost\']',
            pt: 'Osso',
            gen: 'Feminino (ж.р.)',
            tier: 3,
            note: 'Substantivo feminino terminado em consoante dental palatizada branda [t\'].'
        },
        'minecraft:string': {
            ru: 'Нить',
            phon: '[neet\']',
            pt: 'Linha / Fio',
            gen: 'Feminino (ж.р.)',
            tier: 3,
            note: 'Primeira consoante dental branda [n\'] seguida de [ee] e final suave em -ть.'
        },
        'minecraft:lead': {
            ru: 'Поводóк',
            phon: '[pa-va-DOK]',
            pt: 'Laço / Coleira',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Dupla redução vocálica átona antes da tônica oxítona final [pa-va-DOK].'
        },
        'minecraft:obsidian': {
            ru: 'Обсидиáн',
            phon: '[ap-see-dee-AHN]',
            pt: 'Obsidiana',
            gen: 'Masculino (м.р.)',
            tier: 4,
            note: 'Desvozeamento assimilatório: o \'б\' antes de consoante surda \'с\' soa como [p].'
        },
        'minecraft:lava_bucket': {
            ru: 'Лáва',
            phon: '[LAH-va]',
            pt: 'Balde de Lava',
            gen: 'Feminino (ж.р.)',
            tier: 4,
            note: 'Paroxítono clássico com \'л\' duro velarizado inicial.'
        },
        'minecraft:potion': {
            ru: 'Зéлье',
            phon: '[ZYE-lye]',
            pt: 'Poção',
            gen: 'Neutro (с.р.)',
            tier: 4,
            note: 'O sinal brando separador (ь) introduz uma quebra palatal antes da vogal -е.'
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
            phon: '[styer-ZHEN\'] eef-REE-ta]',
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
        }
    };

    for (let id in ruCurriculum) {
        let e = ruCurriculum[id];
        event.addAdvanced(id, (item, advanced, text) => {
            text.add(1, Text.of("[RU] " + e.ru).gold().bold(true));
            text.add(2, Text.of("[Pronúncia] " + e.phon).yellow());
            text.add(3, Text.of("[PT] " + e.pt + " • " + e.gen).aqua());
            text.add(4, Text.of("[Nível] Tier " + e.tier + " (Fale em russo!)").darkGreen().italic(true));

            let fixText = "§7[Fixação] ☆☆☆☆☆ (0/5 - Não iniciado)";
            try {
                let ChallengeSession = Java.loadClass('com.spicesfire.sayitenglish.client.ChallengeSession');
                if (ChallengeSession && ChallengeSession.words && ChallengeSession.scores) {
                    let idx = ChallengeSession.words.indexOf(id);
                    if (idx >= 0 && idx < ChallengeSession.scores.size()) {
                        let lvl = ChallengeSession.scores.get(idx);
                        if (lvl >= 5) {
                            fixText = "§a[Fixação] ★★★★★ (5/5 - Mestre!)";
                        } else if (lvl === 4) {
                            fixText = "§b[Fixação] ★★★★☆ (4/5 - Avançado)";
                        } else if (lvl === 3) {
                            fixText = "§e[Fixação] ★★★☆☆ (3/5 - Intermediário)";
                        } else if (lvl === 2) {
                            fixText = "§e[Fixação] ★★☆☆☆ (2/5 - Básico)";
                        } else if (lvl === 1) {
                            fixText = "§6[Fixação] ★☆☆☆☆ (1/5 - Iniciante)";
                        }
                    }
                }
            } catch (err) {}
            text.add(5, Text.of(fixText));

            if (e.note) {
                text.add(6, Text.of("[Regra] " + e.note).gray());
            }
        });
    }
});
