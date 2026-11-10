// Russian Pedagogical Tooltips Script for Fabric 1.20.1 (KubeJS)
// Executed client-side with zero multiplayer server impact.

ItemEvents.tooltip(event => {
    var Words = null;
    var Romanizer = null;
    var ChallengeSession = null;
    var Alphabet = null;

    try {
        Alphabet = Java.tryLoadClass('com.spicesfire.sayitenglish.Alphabet');
    } catch (e) {}

    try {
        Words = Java.tryLoadClass('com.spicesfire.sayitenglish.client.Words');
    } catch (e) {}

    try {
        Romanizer = Java.tryLoadClass('com.spicesfire.sayitenglish.client.Romanizer');
    } catch (e) {}

    try {
        ChallengeSession = Java.tryLoadClass('com.spicesfire.sayitenglish.client.ChallengeSession');
        if (ChallengeSession && (!ChallengeSession.language || ChallengeSession.language === 'en')) {
            ChallengeSession.language = 'ru';
        }
    } catch (e) {}

    function getPlayerAlphabetLevel() {
        if (!Alphabet || !ChallengeSession || !ChallengeSession.scores) return 0;
        var totalStars = 0;
        var scoresList = ChallengeSession.scores;
        for (var i = 0; i < scoresList.size(); i++) {
            var s = scoresList.get(i);
            if (s > 0) totalStars += s;
        }
        return Alphabet.calculateLevel(totalStars);
    }

    function getUnlockedLetters() {
        if (!Alphabet) return null;
        var wordScores = {};
        if (ChallengeSession && ChallengeSession.words && ChallengeSession.scores) {
            for (var i = 0; i < ChallengeSession.words.size() && i < ChallengeSession.scores.size(); i++) {
                wordScores[ChallengeSession.words.get(i)] = ChallengeSession.scores.get(i);
            }
        }
        return Alphabet.computeUnlockedLetters(wordScores, Alphabet.CLIENT_HEAT);
    }

    var dynamicWordCache = {};

    var CYR_SPELL = {
        'а': 'a', 'б': 'b', 'в': 'v', 'г': 'g', 'д': 'd', 'е': 'ye', 'ё': 'yo',
        'ж': 'zh', 'з': 'z', 'и': 'i', 'й': 'y', 'к': 'k', 'л': 'l', 'м': 'm',
        'н': 'n', 'о': 'o', 'п': 'p', 'р': 'r', 'с': 's', 'т': 't', 'у': 'u',
        'ф': 'f', 'х': 'kh', 'ц': 'ts', 'ч': 'ch', 'ш': 'sh', 'щ': 'shch',
        'ъ': '-', 'ы': 'y', 'ь': '`', 'э': 'e', 'ю': 'yu', 'я': 'ya'
    };

    var FALSE_FRIENDS = {
        'в': 'В=V (vitória)',
        'н': 'Н=N (navio)',
        'р': 'Р=R (roda)',
        'с': 'С=S (sol)',
        'у': 'У=U (urso)',
        'х': 'Х=Kh (aspirado)',
        'ь': 'Ь=amolece a consoante',
        'ъ': 'Ъ=pausa seca'
    };

    var LUSO_MAP = {
        'v': 'в', 'V': 'В',
        'n': 'н', 'N': 'Н',
        'r': 'р', 'R': 'Р',
        's': 'с', 'S': 'С',
        'u': 'у', 'U': 'У',
        'd': 'д', 'D': 'Д',
        'p': 'п', 'P': 'П',
        'm': 'м', 'M': 'М',
        't': 'т', 'T': 'Т',
        'a': 'а', 'A': 'А',
        'o': 'о', 'O': 'О',
        'e': 'е', 'E': 'Е'
    };

    function spellWord(w) {
        if (!w) return { spelling: '', mnem: '' };
        var clean = String(w).replace(/[\u0300-\u036f]/g, '');
        var parts = [];
        var mnems = {};
        for (var i = 0; i < clean.length; i++) {
            var ch = clean.charAt(i);
            var low = ch.toLowerCase();
            if (CYR_SPELL.hasOwnProperty(low)) {
                parts.push(ch + ' [' + CYR_SPELL[low] + ']');
                if (FALSE_FRIENDS.hasOwnProperty(low)) {
                    mnems[FALSE_FRIENDS[low]] = true;
                }
            } else if (ch === ' ') {
                parts.push(' ');
            }
        }
        return {
            spelling: parts.join(' • '),
            mnem: Object.keys(mnems).join(' | ')
        };
    }

    function lusoSub(pt) {
        if (!pt) return '';
        var out = '';
        var s = String(pt);
        for (var i = 0; i < s.length; i++) {
            var c = s.charAt(i);
            out += LUSO_MAP.hasOwnProperty(c) ? LUSO_MAP[c] : c;
        }
        return out;
    }

    const ruCurriculum = {
        'minecraft:oak_log': {
            ru: 'Дéрево',
            phon: '[DYE-rye-va]',
            pt: 'Árvore / Madeira',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Significa primariamente Árvore e também Madeira vegetal. A primeira sílaba é tônica (ДÉ-ре-во). A vogal \'o\' átona final sofre redução vocálica (ákanie), soando como [a].'
        },
        'minecraft:oak_wood': {
            ru: 'Дéрево',
            phon: '[DYE-rye-va]',
            pt: 'Árvore / Madeira',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Bloco de madeira vegetal. Mesma raiz de Дерево.'
        },
        'minecraft:stripped_oak_log': {
            ru: 'Дéрево',
            phon: '[DYE-rye-va]',
            pt: 'Árvore / Madeira Descascada',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Tronco descascado com machado. Mesma raiz de Дерево.'
        },
        'minecraft:spruce_log': {
            ru: 'Дéрево (Ель)',
            phon: '[DYE-rye-va (YEL\')]',
            pt: 'Árvore / Madeira (Pinheiro)',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Tronco/madeira de pinheiro (Ель).'
        },
        'minecraft:birch_log': {
            ru: 'Дéрево (Берёза)',
            phon: '[DYE-rye-va (bye-RYO-za)]',
            pt: 'Árvore / Madeira (Bétula)',
            gen: 'Neutro (с.р.)',
            tier: 1,
            note: 'Tronco/madeira de bétula/eucalipto (Берёза).'
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
            pt: 'Machado de Madeira',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'O primeiro \'o\' átono soa como [a] (ákanie). Acento na última sílaba.'
        },
        'minecraft:wooden_pickaxe': {
            ru: 'Ки́рка',
            phon: '[KEER-ka]',
            pt: 'Picareta de Madeira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino regular com acento na raiz.'
        },
        'minecraft:wooden_shovel': {
            ru: 'Лопáта',
            phon: '[la-PAH-ta]',
            pt: 'Pá de Madeira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Substantivo feminino regular em -а. Acento na segunda sílaba.'
        },
        'minecraft:wooden_sword': {
            ru: 'Меч',
            phon: '[MYECH]',
            pt: 'Espada de Madeira',
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
        },
        'farmersrespite:kettle': {
            ru: 'Чáйник',
            phon: '[CHAY-neek]',
            pt: 'Chaleira',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Utensílio para ferver água e preparar infusões. Raiz Чай (chá) com sufixo instrumental -ник.'
        },
        'farmersrespite:coffee': {
            ru: 'Кóфе',
            phon: '[KOH-fye]',
            pt: 'Café',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Substantivo indeclinável de gênero masculino. Bebida quente energizante.'
        },
        'farmersrespite:green_tea': {
            ru: 'Зелëный чай',
            phon: '[zye-LYO-niy CHAY]',
            pt: 'Chá Verde',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Adjetivo + substantivo. A vogal \'ë\' é sempre tônica na pronúncia russa.'
        },
        'farmersrespite:black_tea': {
            ru: 'Чёрный чай',
            phon: '[CHYOR-niy CHAY]',
            pt: 'Chá Preto',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Infusão clássica. Adjetivo Чёрный (preto) com \'ë\' tônico acompanhando Чай.'
        },
        'farmersrespite:yellow_tea': {
            ru: 'Жёлтый чай',
            phon: '[ZHOL-tiy CHAY]',
            pt: 'Chá Amarelo',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Chá raro de cura suave. Adjetivo Жёлтый (amarelo) com inicial dura \'ж\'.'
        },
        'comforts:sleeping_bag_white': {
            ru: 'Спáльный мешóк',
            phon: '[SPAHL-niy mye-SHOK]',
            pt: 'Saco de Dormir Branco',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Composto: Спальный (para dormir) + Мешок (saco). Permite dormir sem alterar o ponto de renascimento.'
        },
        'comforts:hammock_white': {
            ru: 'Гамáк',
            phon: '[ga-MAHK]',
            pt: 'Rede Branca',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Rede de descanso suspensa. Permite avançar o dia e descansar sob a sombra.'
        },
        'comforts:rope_and_nail': {
            ru: 'Верëвка и гвоздь',
            phon: '[vye-RYOV-ka ee GVOST\']',
            pt: 'Corda e Prego',
            gen: 'Feminino + Masculino',
            tier: 2,
            note: 'Suporte de fixação da rede. A palavra Гвоздь tem consoante final suave palatizada \'дь\'.'
        },
        'handcrafted:chair': {
            ru: 'Стул',
            phon: '[STOOL]',
            pt: 'Cadeira',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Assento individual de madeira. Plural irregular: Стулья (STOOL-ya).'
        },
        'handcrafted:oak_chair': {
            ru: 'Стул',
            phon: '[STOOL]',
            pt: 'Cadeira de Carvalho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Mobília básica de assento individual. Substantivo masculino monossilábico regular.'
        },
        'handcrafted:table': {
            ru: 'Стол',
            phon: '[STOL]',
            pt: 'Mesa',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Móvel central da casa eslava. Plural regular: Столы.'
        },
        'handcrafted:oak_table': {
            ru: 'Стол',
            phon: '[STOL]',
            pt: 'Mesa de Carvalho',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Mesa de apoio e refeições. \'O\' aberto e tônico. Plural: Столы (sta-LI).'
        },
        'crittersandcompanions:otter_spawn_egg': {
            ru: 'Вы́дра',
            phon: '[VI-dra]',
            pt: 'Lontra',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Mamífero semiaquático ágil dos rios. Acento na primeira sílaba.'
        },
        'crittersandcompanions:ferret_spawn_egg': {
            ru: 'Хорёк',
            phon: '[kha-RYOK]',
            pt: 'Furão',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Pequeno carnívoro doméstico e curioso. \'Ё\' é sempre tônico.'
        },
        'crittersandcompanions:dragonfly_spawn_egg': {
            ru: 'Стрекозá',
            phon: '[strye-ka-ZAH]',
            pt: 'Libélula',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Inseto voador predador de rios. Acento final com redução vocálica (ákanie).'
        },
        'crittersandcompanions:clam': {
            ru: 'Моллю́ск',
            phon: '[ma-LYOOSK]',
            pt: 'Marisco / Molusco',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Criatura marinha bivalve. \'О\' inicial átono soa como [a].'
        },
        'ecologics:coconut': {
            ru: 'Кокóс',
            phon: '[ka-KOHS]',
            pt: 'Coco',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Fruto da palmeira costeira. Acento no segundo \'о\'.'
        },
        'ecologics:crab_claw': {
            ru: 'Клешня́ крáба',
            phon: '[klye-SHNYAH KRAH-ba]',
            pt: 'Garra de Caranguejo',
            gen: 'Feminino + Genitivo',
            tier: 2,
            note: 'Клешня (garra/pinça) no nominativo + Краба (do caranguejo) no caso genitivo.'
        },
        'ecologics:crab_meat': {
            ru: 'Мя́со крáба',
            phon: '[MYAH-sa KRAH-ba]',
            pt: 'Carne de Caranguejo',
            gen: 'Neutro + Genitivo',
            tier: 2,
            note: 'Мясо (carne) substantivo neutro terminando em -о átono [a] + Краба no genitivo.'
        },
        'ecologics:penguin_feather': {
            ru: 'Перó пингви́на',
            phon: '[pye-ROH peen-GVEE-na]',
            pt: 'Pena de Pinguim',
            gen: 'Neutro + Genitivo',
            tier: 2,
            note: 'Перо (pena, gênero neutro) + Пингвина (do pinguim no caso genitivo).'
        },
        'ecologics:walnut': {
            ru: 'Грéцкий орéх',
            phon: '[GREHT-skeey a-RYEKH]',
            pt: 'Noz',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Literalmente \'noz grega\'. Adjetivo Грецкий acompanhando o substantivo Орех.'
        },
        'exposure:camera': {
            ru: 'Кáмера',
            phon: '[KAH-mye-ra]',
            pt: 'Câmera Fotográfica',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Aparelho óptico analógico para registrar momentos e animais no mundo.'
        },
        'exposure:album': {
            ru: 'Фотоальбóм',
            phon: '[fa-ta-al-BOHM]',
            pt: 'Álbum de Fotos',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Composto: Фото (foto) + Альбом (álbum). Guarda recordações de viagens.'
        },
        'exposure:black_and_white_film': {
            ru: 'Чёрно-бéлая плëнка',
            phon: '[CHYOR-na BYE-la-ya PLYON-ka]',
            pt: 'Filme Preto e Branco',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Filme clássico fotossensível. Adjetivo composto Чёрно-белая com dois radicais.'
        },
        'exposure:color_film': {
            ru: 'Цветнáя плëнка',
            phon: '[tsvyet-NAH-ya PLYON-ka]',
            pt: 'Filme Colorido',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Filme colorido analógico para capturar biomas vívidos. \'Ё\' é tônico em Плёнка.'
        },
        'exposure:photograph': {
            ru: 'Фотогрáфия',
            phon: '[fa-ta-GRAH-fee-ya]',
            pt: 'Fotografia',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Imagem revelada em papel fotográfico. Pode ser emoldurada em paredes.'
        },
        'naturalist:bug_net': {
            ru: 'Сачóк',
            phon: '[sa-CHOK]',
            pt: 'Rede de Insetos / Puçá',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Rede manual para captura de insetos e borboletas. Diminutivo clássico com sufixo -ок.'
        },
        'naturalist:butterfly': {
            ru: 'Бáбочка',
            phon: '[BAH-bach-ka]',
            pt: 'Borboleta',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Inseto voador de asas coloridas. Etimologicamente ligado a Бабушка (antiga crença de almas ancestrais).'
        },
        'naturalist:moth': {
            ru: 'Мотылëк',
            phon: '[ma-ti-LYOK]',
            pt: 'Mariposa',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Inseto noturno atraído pela luz. \'Ё\' é sempre tônico com som [yo].'
        },
        'naturalist:caterpillar': {
            ru: 'Гýсеница',
            phon: '[GOO-sye-nee-tsa]',
            pt: 'Lagarta',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Estágio larval dos lepidópteros. Acento no \'у\' inicial.'
        },
        'naturalist:bass': {
            ru: 'О́кунь',
            phon: '[OH-koon\']',
            pt: 'Robalo / Truta-achigã',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Peixe predador de água doce. Sinal brando \'ь\' final palatiza a consoante \'н\'.'
        },
        'naturalist:catfish': {
            ru: 'Сом',
            phon: '[SOM]',
            pt: 'Bagre / Peixe-gato',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Peixe de fundo de rio com longos barbilhões táteis. Substantivo masculino monossilábico.'
        },
        'naturalist:duck': {
            ru: 'У́тка',
            phon: '[OOT-ka]',
            pt: 'Pato',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Ave aquática palmípede. Diminutivo comum carinhoso: Уточка (OOT-ach-ka).'
        },
        'naturalist:worm': {
            ru: 'Червь',
            phon: '[CHYERV\']',
            pt: 'Minhoca / Verme',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Anelídeo de solo usado como isca de pesca. Terminação com sinal brando \'ь\'.'
        },
        'crittersandcompanions:koi_fish': {
            ru: 'Карп кóи',
            phon: '[KARP KOH-ee]',
            pt: 'Carpa Koi',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Peixe ornamental de lagoas e jardins. Карп é declinável, Кои indeclinável.'
        },
        'crittersandcompanions:pearl': {
            ru: 'Жéмчуг',
            phon: '[ZHEHM-chook]',
            pt: 'Pérola',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Gema marinha preciosa formada em moluscos. O \'г\' final sofre ensurdecimento e soa [k].'
        },
        'crittersandcompanions:silk': {
            ru: 'Шёлк',
            phon: '[SHOLK]',
            pt: 'Seda',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Fibra têxtil suave e nobre. A letra \'ё\' após \'ш\' soa [o] com o chiante duro.'
        },
        'crittersandcompanions:grappling_hook': {
            ru: 'Крюк-кóшка',
            phon: '[KRYOOK KOHSH-ka]',
            pt: 'Gancho de Escalada',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Instrumento de ascensão e escalada. Substantivo composto hifenizado.'
        },
        'ecologics:prickly_pear': {
            ru: 'Опýнция',
            phon: '[a-POON-tsee-ya]',
            pt: 'Figo-da-Índia',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Fruto comestível e doce de cactos desérticos. Terminação suave em -ия.'
        },
        'ecologics:coconut_slice': {
            ru: 'Дóлька кокóса',
            phon: '[DOL\'-ka ka-KOH-sa]',
            pt: 'Fatia de Coco',
            gen: 'Feminino + Genitivo',
            tier: 1,
            note: 'Долька (pedaço/fatia) com sinal brando + Кокоса (genitivo de Кокос).'
        },
        'farmersrespite:rose_hip_tea': {
            ru: 'Чай из шипóвника',
            phon: '[CHAY eez shee-POV-nee-ka]',
            pt: 'Chá de Rosa Mosqueta',
            gen: 'Masculino + Genitivo',
            tier: 2,
            note: 'Bebida rica em vitamina C. Preposição Из (de) exige caso genitivo Шиповника.'
        },
        'farmersrespite:coffee_berries': {
            ru: 'Кофéйные я́годы',
            phon: '[ka-FYEY-ni-ye YAH-ga-di]',
            pt: 'Frutos de Café',
            gen: 'Plural (мн.ч.)',
            tier: 2,
            note: 'Frutos frescos colhidos do cafeeiro. Adjetivo Кофейные concordando com Ягоды.'
        },
        'farmersrespite:coffee_cake': {
            ru: 'Кофéйный пирóг',
            phon: '[ka-FYEY-niy pee-ROHK]',
            pt: 'Bolo de Café',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Bolo confeitado aromático. O \'г\' final de Пирог ensurdece para [k].'
        },
        'handcrafted:bench': {
            ru: 'Скамéйка',
            phon: '[ska-MYEY-ka]',
            pt: 'Banco de Madeira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Assento coletivo longo. Raiz eslava clássica com ditongo suave -ей-.'
        },
        'handcrafted:couch': {
            ru: 'Дивáн',
            phon: '[dee-VAHN]',
            pt: 'Sofá / Divã',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Móvel estofado confortável para descanso na sala. Empréstimo linguístico oriental comum.'
        },
        'handcrafted:cupboard': {
            ru: 'Сервáнт',
            phon: '[syer-VAHNT]',
            pt: 'Armário / Louceiro',
            gen: 'Masculino (м.р.)',
            tier: 1,
            note: 'Armário com prateleiras e portas para louças e utensílios culinários.'
        },
        'handcrafted:drawer': {
            ru: 'Тýмба',
            phon: '[TOOM-ba]',
            pt: 'Gaveteiro / Criado-mudo',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Móvel de cabeceira com gavetas para armazenar pequenos objetos.'
        },
        'handcrafted:shelf': {
            ru: 'Пóлка',
            phon: '[POHL-ka]',
            pt: 'Prateleira',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Superfície horizontal fixada na parede para suporte de livros e decorações.'
        },
        'handcrafted:counter': {
            ru: 'Столéшница',
            phon: '[sta-LYE-shnee-tsa]',
            pt: 'Balcão / Bancada',
            gen: 'Feminino (ж.р.)',
            tier: 1,
            note: 'Bancada de preparo e apoio culinário. Derivado de Стол (mesa).'
        },
        'naturalist:reptile_hide': {
            ru: 'Шкýра репти́лии',
            phon: '[SHKOO-ra ryeep-TEE-lee-ee]',
            pt: 'Couro de Réptil',
            gen: 'Feminino + Genitivo',
            tier: 2,
            note: 'Couro escamoso resistente obtido de jacarés e lagartos.'
        },
        'naturalist:glow_goop': {
            ru: 'Светя́щаяся слизь',
            phon: '[svye-TYAH-shcha-ya-sya SLEES\']',
            pt: 'Visgo Brilhante',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Substância luminescente coletada de vaga-lumes e criaturas da noite.'
        },
        'naturalist:snail_shell_block': {
            ru: 'Блок из рáковины',
            phon: '[BLOK eez RAH-ka-vee-ni]',
            pt: 'Bloco de Conchas',
            gen: 'Masculino + Genitivo',
            tier: 2,
            note: 'Material de construção calcário ornamental composto de conchas de caracóis.'
        },
        'farmersrespite:green_tea_cookie': {
            ru: 'Печéнье с зелëным чáем',
            phon: '[pye-CHYE-n\'ye s zye-LYO-nim CHAH-yem]',
            pt: 'Biscoito de Chá Verde',
            gen: 'Neutro + Instrumental',
            tier: 2,
            note: 'Biscoito aromático matinal assado com folhas trituradas de chá verde.'
        },
        'farmersrespite:black_cod': {
            ru: 'Чёрная трескá',
            phon: '[CHYOR-na-ya tryes-KAH]',
            pt: 'Bacalhau-preto / Peixe-carvão',
            gen: 'Feminino (ж.р.)',
            tier: 2,
            note: 'Peixe nobre de águas profundas e gélidas. Adjetivo Чёрная concordando com Треска.'
        },
        'farmersrespite:dandelion_tea': {
            ru: 'Чай из одувáнчиков',
            phon: '[CHAY eez a-doo-VAHN-chee-kaf]',
            pt: 'Chá de Dente-de-Leão',
            gen: 'Masculino + Genitivo Plural',
            tier: 2,
            note: 'Infusão campestre depurativa amarela feita com pétalas silvestres.'
        },
        'comforts:sleeping_bag_blue': {
            ru: 'Си́ний спáльный мешóк',
            phon: '[SEE-neey SPAHL-niy mye-SHOK]',
            pt: 'Saco de Dormir Azul',
            gen: 'Masculino (м.р.)',
            tier: 2,
            note: 'Equipamento de acampamento portátil na cor azul-celeste.'
        }
    };

    Object.keys(ruCurriculum).forEach(function(itemId) {
        var entry = ruCurriculum[itemId];
        event.addAdvanced(itemId, function(item, advanced, text) {
            var unlocked = getUnlockedLetters();
            var hybrid = Alphabet ? String(Alphabet.toSmartHybrid(entry.ru, unlocked, true)) : entry.ru;
            var wordLetters = Alphabet ? Alphabet.getLettersOrderedByDifficulty(entry.ru) : [];
            var masteredCount = 0;
            for (var i = 0; i < wordLetters.size(); i++) {
                if (unlocked && unlocked.contains(wordLetters.get(i))) masteredCount++;
            }

            var stars = 0;
            try {
                if (ChallengeSession && ChallengeSession.words && ChallengeSession.scores) {
                    var idx = ChallengeSession.words.indexOf(itemId);
                    if (idx >= 0 && idx < ChallengeSession.scores.size()) {
                        stars = ChallengeSession.scores.get(idx);
                    }
                }
            } catch (e) {}

            if (stars >= 5) {
                text.add(Text.gold('★ Russo Fluente: ').append(Text.white(entry.ru)).append(Text.gray(' ' + entry.phon)));
            } else {
                text.add(Text.gold('★ Treino Russo: ').append(Text.of(hybrid)).append(Text.gray(' (' + masteredCount + '/' + wordLetters.size() + ' letras)')));
                text.add(Text.darkGray('  (Dourado = Russo | Branco = Português)'));
            }
            text.add(Text.yellow('  Português: ').append(Text.white(entry.pt)));
            text.add(Text.aqua('  Gênero: ').append(Text.white(entry.gen)).append(Text.gray(' | Nível ' + entry.tier)));

            if (stars > 0) {
                var starsFilled = ''; for (var s = 0; s < stars; s++) starsFilled += '★';
                var starsEmpty = ''; for (var s = stars; s < 5; s++) starsEmpty += '☆';
                text.add(Text.gold('  Domínio: ').append(Text.yellow(starsFilled)).append(Text.darkGray(starsEmpty)).append(Text.gray(' (' + stars + '/5 estrelas)')));
            }

            if (event.shift) {
                if (stars < 5) {
                    text.add(Text.gold('  Meta Fluente: ').append(Text.white(entry.ru)).append(Text.gray(' ' + entry.phon)));
                }
                if (wordLetters.size() > 0 && Alphabet) {
                    var heatParts = [];
                    for (var i = 0; i < wordLetters.size(); i++) {
                        var c = wordLetters.get(i);
                        var isAct = unlocked && unlocked.contains(c);
                        var h = Alphabet.getHeat(c);
                        var thresh = Alphabet.getHeatThreshold(c);
                        heatParts.push(c + ': ' + (isAct ? '\u00a7a\u2714\u00a77' : '\u00a7e' + h + '/' + thresh + 'x\u00a77'));
                    }
                    text.add(Text.gold('  Calor das Letras: ').append(Text.gray(heatParts.join(' | '))));
                }
                text.add(Text.green('  Nota Gramatical: ').append(Text.gray(entry.note)));
                var spell = spellWord(entry.ru);
                if (spell.spelling) {
                    text.add(Text.gold('  Soletrando: ').append(Text.white(spell.spelling)));
                }
                if (spell.mnem) {
                    text.add(Text.red('  Mnemônica: ').append(Text.yellow(spell.mnem)));
                }
                var sub = lusoSub(entry.pt);
                if (sub && sub !== entry.pt) {
                    text.add(Text.lightPurple('  Ponte Luso-Cirílica: ').append(Text.gray(sub)));
                }
            } else {
                text.add(Text.darkGray('  [Segure SHIFT para decomposição do alfabeto e nota]'));
            }
        });
    });

    // Universal dynamic tooltip fallback for ALL other items prompted by SayIt
    event.addAdvancedToAll(function(item, advanced, text) {
        if (!item || !item.id) {
            return;
        }

        var itemId = String(item.id);
        if (ruCurriculum.hasOwnProperty(itemId)) {
            return;
        }

        if (dynamicWordCache.hasOwnProperty(itemId)) {
            var cached = dynamicWordCache[itemId];
            if (cached) {
                appendDynamicTooltip(text, cached, itemId);
            }
            return;
        }

        if (!Words) {
            return;
        }

        try {
            if (ChallengeSession && (!ChallengeSession.language || ChallengeSession.language === 'en')) {
                ChallengeSession.language = 'ru';
            }
            var prompt = Words.word(itemId);
            if (!prompt) {
                dynamicWordCache[itemId] = null;
                return;
            }

            var ru = prompt.target();
            if (!ru || ru === itemId) {
                dynamicWordCache[itemId] = null;
                return;
            }

            // Ensure the translation contains Cyrillic characters (proof of valid Russian translation)
            if (!/[\u0400-\u04FF]/.test(ru)) {
                dynamicWordCache[itemId] = null;
                return;
            }

            var phon = null;
            if (Romanizer) {
                try {
                    phon = Romanizer.of(ru, 'ru');
                } catch (e) {}
            }

            var pt = prompt.nativeText();
            if (!pt || pt === itemId || pt.indexOf('item.') === 0 || pt.indexOf('block.') === 0) {
                try {
                    pt = item.name ? item.name.getString() : '';
                } catch (e) {}
            }

            var entry = {
                ru: String(ru),
                phon: phon ? ('[' + phon + ']') : '',
                pt: (pt && pt !== ru) ? String(pt) : ''
            };

            dynamicWordCache[itemId] = entry;
            appendDynamicTooltip(text, entry, itemId);
        } catch (e) {
            dynamicWordCache[itemId] = null;
        }
    });

    function appendDynamicTooltip(text, entry, itemId) {
        var phonSuffix = entry.phon ? (' ' + entry.phon) : '';
        var unlocked = getUnlockedLetters();
        var hybrid = Alphabet ? String(Alphabet.toSmartHybrid(entry.ru, unlocked, true)) : entry.ru;
        var wordLetters = Alphabet ? Alphabet.getLettersOrderedByDifficulty(entry.ru) : [];
        var masteredCount = 0;
        for (var i = 0; i < wordLetters.size(); i++) {
            if (unlocked && unlocked.contains(wordLetters.get(i))) masteredCount++;
        }

        var stars = 0;
        try {
            if (ChallengeSession && ChallengeSession.words && ChallengeSession.scores) {
                var idx = ChallengeSession.words.indexOf(itemId);
                if (idx >= 0 && idx < ChallengeSession.scores.size()) {
                    stars = ChallengeSession.scores.get(idx);
                }
            }
        } catch (e) {}

        if (stars >= 5) {
            text.add(Text.gold('★ Russo Fluente: ').append(Text.white(entry.ru)).append(Text.gray(phonSuffix)));
        } else {
            text.add(Text.gold('★ Treino Russo: ').append(Text.of(hybrid)).append(Text.gray(' (' + masteredCount + '/' + wordLetters.size() + ' letras)')));
            text.add(Text.darkGray('  (Dourado = Russo | Branco = Português)'));
        }
        if (entry.pt) {
            text.add(Text.yellow('  Português: ').append(Text.white(entry.pt)));
        }

        if (stars > 0) {
            var starsFilled = ''; for (var s = 0; s < stars; s++) starsFilled += '★';
            var starsEmpty = ''; for (var s = stars; s < 5; s++) starsEmpty += '☆';
            text.add(Text.gold('  Domínio: ').append(Text.yellow(starsFilled)).append(Text.darkGray(starsEmpty)).append(Text.gray(' (' + stars + '/5 estrelas)')));
        }

        if (event.shift) {
            if (stars < 5) {
                text.add(Text.gold('  Meta Fluente: ').append(Text.white(entry.ru)).append(Text.gray(phonSuffix)));
            }
            if (wordLetters.size() > 0 && Alphabet) {
                var heatParts = [];
                for (var i = 0; i < wordLetters.size(); i++) {
                    var c = wordLetters.get(i);
                    var isAct = unlocked && unlocked.contains(c);
                    var h = Alphabet.getHeat(c);
                    var thresh = Alphabet.getHeatThreshold(c);
                    heatParts.push(c + ': ' + (isAct ? '\u00a7a\u2714\u00a77' : '\u00a7e' + h + '/' + thresh + 'x\u00a77'));
                }
                text.add(Text.gold('  Calor das Letras: ').append(Text.gray(heatParts.join(' | '))));
            }
            var spell = spellWord(entry.ru);
            if (spell.spelling) {
                text.add(Text.gold('  Soletrando: ').append(Text.white(spell.spelling)));
            }
            if (spell.mnem) {
                text.add(Text.red('  Mnemônica: ').append(Text.yellow(spell.mnem)));
            }
            if (entry.pt) {
                var sub = lusoSub(entry.pt);
                if (sub && sub !== entry.pt) {
                    text.add(Text.lightPurple('  Ponte Luso-Cirílica: ').append(Text.gray(sub)));
                }
            }
        } else {
            text.add(Text.darkGray('  [Segure SHIFT para decomposição do alfabeto]'));
        }
    }
});
