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
            note: 'A primeira sílaba é tônica (ДÉ-ре-во). A vogal \'o\' átona final sofre redução vocálica (ákanie), soando como [a].'
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
            ru: 'Булы́жник',
            phon: '[boo-LIZH-neek]',
            pt: 'Pedregulho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Contém a vogal gutural posterior dura \'ы\'. A sílaba tônica é -лы́-.'
        },
        'minecraft:dirt': {
            ru: 'Земля́',
            phon: '[zyem-LYAH]',
            pt: 'Terra',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Acento tônico no final (-я́). O \'е\' inicial soa levemente como [i] antes da tônica (íkanie).'
        },
        'minecraft:sand': {
            ru: 'Песóк',
            phon: '[pye-SOK]',
            pt: 'Areia',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Substantivo masculino incontável terminando em consoante dura.'
        },
        'minecraft:gravel': {
            ru: 'Грáвий',
            phon: '[GRAH-veey]',
            pt: 'Cascalho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Terminação em -ий (semivogal breve i-kratkoye).'
        },
        'minecraft:coal': {
            ru: 'У́голь',
            phon: '[OO-gahl\']',
            pt: 'Carvão',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'O \'ль\' final é suave. Substantivo masculino de 3ª declinação.'
        },
        'minecraft:iron_ingot': {
            ru: 'Желéзный сли́ток',
            phon: '[zhe-LYEZ-niy SLEE-tok]',
            pt: 'Barra de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Adjetivo + substantivo. A letra \'ж\' é sempre dura em russo.'
        },
        'minecraft:gold_ingot': {
            ru: 'Золотóй сли́ток',
            phon: '[za-la-TOY SLEE-tok]',
            pt: 'Barra de Ouro',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Terminação adjetival tônica em -ой. Os dois primeiros \'o\' sofrem redução para [a].'
        },
        'minecraft:diamond': {
            ru: 'Алмáз',
            phon: '[al-MAHS]',
            pt: 'Diamante',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'A consoante sonora final \'з\' sofre desvozeamento e soa como [s] ao final da palavra.'
        },
        'minecraft:wooden_axe': {
            ru: 'Топóр',
            phon: '[ta-POHR]',
            pt: 'Machado',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'O primeiro \'o\' átono soa como [a] (ákanie). Acento na última sílaba.'
        },
        'minecraft:wooden_pickaxe': {
            ru: 'Ки́рка',
            phon: '[KEER-ka]',
            pt: 'Picareta',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino regular com acento na raiz.'
        },
        'minecraft:wooden_shovel': {
            ru: 'Лопáта',
            phon: '[la-PAH-ta]',
            pt: 'Pá',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino regular em -а. Acento na segunda sílaba.'
        },
        'minecraft:wooden_sword': {
            ru: 'Меч',
            phon: '[MYECH]',
            pt: 'Espada',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Palavra monossilábica. A consoante \'ч\' é sempre suave em russo.'
        },
        'minecraft:torch': {
            ru: 'Фáкел',
            phon: '[FAH-kyel]',
            pt: 'Tocha',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Do alemão Fackel. Substantivo masculino com terminação dura.'
        },
        'minecraft:crafting_table': {
            ru: 'Верстáк',
            phon: '[vyer-STAHK]',
            pt: 'Bancada de Trabalho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Significa originalmente banco de carpinteiro. Acento na última sílaba.'
        },
        'minecraft:furnace': {
            ru: 'Печь',
            phon: '[PYECH\']',
            pt: 'Fornalha / Forno',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino de 3ª declinação terminado em sinal brando (ь). Elemento central da cultura russa (русская печь).'
        },
        'minecraft:chest': {
            ru: 'Сунду́к',
            phon: '[soon-DOOK]',
            pt: 'Baú',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Acento na segunda sílaba. Palavra de origem túrquica incorporada ao vocabulário russo clássico.'
        },
        'minecraft:apple': {
            ru: 'Я́блоко',
            phon: '[YAH-bla-ka]',
            pt: 'Maçã',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Acento na primeira sílaba (Я́). As duas vogais \'o\' átonas seguintes sofrem ákanie, soando como [a].'
        },
        'minecraft:bread': {
            ru: 'Хлеб',
            phon: '[KHLYEP]',
            pt: 'Pão',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'A consoante sonora final \'б\' sofre desvozeamento obrigatório em final de palavra, soando como [p].'
        },
        'minecraft:turtle_egg': {
            ru: 'Яйцó черепáхи',
            phon: '[yyeet-SO che-rye-PAH-khee]',
            pt: 'Ovo de Tartaruga',
            gen: 'Neutro (с.р.) + Genitivo',
            tier: 2,
            note: 'Яйцо (ovo) é neutro com acento no final. Черепаха está no caso genitivo (de tartaruga: черепахи).'
        },
        'minecraft:scute': {
            ru: 'Щитóк черепáхи',
            phon: '[shchee-TOK che-rye-PAH-khee]',
            pt: 'Placa de Tartaruga',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Diminutivo de щит (escudo). A letra \'щ\' tem som longo suave [shch].'
        },
        'minecraft:turtle_helmet': {
            ru: 'Черепáший пáнцирь',
            phon: '[che-rye-PAH-sheey PAHN-tseer\']',
            pt: 'Casco de Tartaruga',
            gen: 'Masculino (м.р.)',
            tier: 3,
            note: 'Черепаший é adjetivo possessivo (de tartaruga). Панцирь (carapaça/armadura) é masculino.'
        },
        'farmersdelight:cutting_board': {
            ru: 'Раздéлочная доскá',
            phon: '[raz-DYE-lach-na-ya das-KAH]',
            pt: 'Tábua de Corte',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Разделочная vem do verbo разделывать (cortar, preparar alimentos). Доска tem acento final.'
        },
        'farmersdelight:skillet': {
            ru: 'Сковородá',
            phon: '[ska-va-ra-DAH]',
            pt: 'Frigideira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Acento na última sílaba (-да). Os dois \'o\' anteriores reduzem para [a]. Item essencial de culinária cotidiana.'
        },
        'farmersdelight:cooking_pot': {
            ru: 'Кастрю́ля',
            phon: '[kas-TRYU-lya]',
            pt: 'Panela de Cozinha',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino suave em -я. Acento no -рю́-. Utilizada para fazer sopas e ensopados russos (борщ, щи).'
        },
        'farmersdelight:iron_knife': {
            ru: 'Желéзный нож',
            phon: '[zhe-LYEZ-niy NOZH]',
            pt: 'Faca de Ferro',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Нож é masculino monossilábico. A consoante \'ж\' é dura e soa quase como [ш] em final de palavra.'
        },
        'farmersdelight:stove': {
            ru: 'Плитá',
            phon: '[plee-TAH]',
            pt: 'Fogão',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino regular com acento na terminação.'
        },
        'farmersdelight:onion': {
            ru: 'Лук',
            phon: '[LOOK]',
            pt: 'Cebola',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Atenção aos homônimos: Лук significa tanto cebola quanto arco de flechas em russo!'
        },
        'farmersdelight:tomato': {
            ru: 'Помидóр',
            phon: '[pa-mee-DOHR]',
            pt: 'Tomate',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Do italiano pomodoro (maçã dourada). Acento na última sílaba.'
        },
        'farmersdelight:cabbage': {
            ru: 'Капу́ста',
            phon: '[ka-POOS-ta]',
            pt: 'Repolho',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Legume básico da alimentação eslava. Base tradicional da sopa Щи (shchi).'
        },
        'farmersdelight:rice': {
            ru: 'Рис',
            phon: '[REES]',
            pt: 'Arroz',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Monossílabo masculino incontável. Consoante inicial \'р\' é palatizada diante do \'и\'.'
        },
        'farmersdelight:apple_pie': {
            ru: 'Я́блочный пирóг',
            phon: '[YAH-blach-niy pee-ROHK]',
            pt: 'Torta de Maçã',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Пирог é o tradicional bolo/torta recheada russa. A consoante final \'г\' soa como [k].'
        },
        'naturalist:bear_fur': {
            ru: 'Медвéжья шку́ра',
            phon: '[meed-VYE-zhya SHKOO-ra]',
            pt: 'Pele de Urso',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Шкура é couro/pele de animal. Медвежья é adjetivo possessivo derivado de Медведь (urso).'
        },
        'naturalist:venison': {
            ru: 'Олéнина',
            phon: '[a-LYE-nee-na]',
            pt: 'Carne de Veado',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Substantivo culinário derivado de Олень (veado/cervo) com sufixo -ина (carne de caça).'
        },
        'naturalist:cooked_venison': {
            ru: 'Жáреная олéнина',
            phon: '[ZHAH-rye-na-ya a-LYE-nee-na]',
            pt: 'Carne de Veado Assada',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Жареная significa frita/assada/grelhada. Adjetivo feminino concordando com Оленина.'
        },
        'naturalist:snail_shell': {
            ru: 'Рáковина ули́тки',
            phon: '[RAH-ka-vee-na oo-LEET-kee]',
            pt: 'Concha de Caracol',
            gen: 'Feminino (ж.р.) + Genitivo',
            tier: 2,
            note: 'Раковина significa concha/carapaça. Улитки é o genitivo de Улитка (caracol).'
        },
        'naturalist:antler': {
            ru: 'Рог олéня',
            phon: '[ROHK a-LYE-nya]',
            pt: 'Chifre de Veado',
            gen: 'Masculino (м.р.) + Genitivo',
            tier: 2,
            note: 'Рог significa chifre/galhada (o \'г\' final soa como [k]). Оленя é o genitivo de Олень.'
        }
    };

    for (const [id, data] of Object.entries(ruCurriculum)) {
        event.addAdvanced(id, (item, advanced, text) => {
            text.add(Text.gold('★ Russo: ').append(Text.white(data.ru)).append(Text.gray(' ' + data.phon)));
            text.add(Text.yellow('  Português: ').append(Text.white(data.pt)));
            text.add(Text.aqua('  Gênero: ').append(Text.white(data.gen)).append(Text.gray(' | Nível ' + data.tier)));
            if (event.shift) {
                text.add(Text.green('  Nota Gramatical: ').append(Text.gray(data.note)));
            } else {
                text.add(Text.darkGray('  [Segure SHIFT para nota fonética/gramatical]'));
            }
        });
    }
});
