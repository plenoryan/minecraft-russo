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
        }
    };

    Object.keys(ruCurriculum).forEach(function(itemId) {
        var entry = ruCurriculum[itemId];
        event.addAdvanced(itemId, function(item, advanced, text) {
            var unlocked = getUnlockedLetters();
            var hybrid = Alphabet ? String(Alphabet.toSmartHybrid(entry.ru, unlocked, false)) : entry.ru;
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
                text.add(Text.gold('★ Treino Russo: ').append(Text.aqua(hybrid)).append(Text.gray(' (' + masteredCount + '/' + wordLetters.size() + ' letras)')));
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
        var hybrid = Alphabet ? String(Alphabet.toSmartHybrid(entry.ru, unlocked, false)) : entry.ru;
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
            text.add(Text.gold('★ Treino Russo: ').append(Text.aqua(hybrid)).append(Text.gray(' (' + masteredCount + '/' + wordLetters.size() + ' letras)')));
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
