package com.example.data

import com.example.model.StoryChapter
import com.example.model.StoryDialogue

object StoryCampaignCatalog {
    val chapters: List<StoryChapter> = listOf(
        // ГЛАВА 1
        StoryChapter(
            chapterNumber = 1,
            title = "Глава 1: Родительское гнездо",
            subtitle = "Шум вентиляторов и злые предки",
            requiredFacilityId = "facility_parents_apt",
            synopsis = "Ты собрал свой первый майнинг-риг на старенькой GTX 1050 Ti прямо в спальне. Но родители в бешенстве от постоянного гула кулеров и счета за электроэнергию.",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "Мама",
                    speakerRole = "Родитель",
                    text = "Что это за железный монстр гудит день и ночь?! У меня голова раскалывается! И счет за свет пришел космический! Немедленно выключи свой интернет!"
                ),
                StoryDialogue(
                    speakerName = "Санёк",
                    speakerRole = "Друг-майнер",
                    text = "Здорово, бро! Не слушай предков, крипта — это будущее! Намайни первые баксы на бирже и сваливай в гараж, там никто пилить не будет."
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Начинающий майнер",
                    text = "Понял. Разгоняю карточку, меняю крипту на доллары на P2P бирже и коплю на переезд в гараж через ДомКлик!",
                    isPlayer = true
                )
            ),
            objectiveText = "Намайни крипту, продай её на бирже и накопи $100 на аренду гаража в ДомКлик.",
            targetBalanceUsd = 100.0,
            rewardUsd = 100.0,
            rewardXp = 150,
            isUnlocked = true
        ),

        // ГЛАВА 2
        StoryChapter(
            chapterNumber = 2,
            title = "Глава 2: Гаражный кооператив 'Искра'",
            subtitle = "Запах машинного масла и кулеры на 100%",
            requiredFacilityId = "facility_garage",
            synopsis = "Переезд в гараж! Воздух пахнет пылью и бензином, зато кулеры можно крутить на полную громкость. Главное — не перегрузить старый советский рубильник.",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "Дядя Толя",
                    speakerRole = "Сторож кооператива",
                    text = "Слышь, юный хакер. Счетчик в твоем боксе крутится как вентилятор в жару! Смотри мне, проводку не сожги, а то вырублю автомат на столбе!"
                ),
                StoryDialogue(
                    speakerName = "Санёк",
                    speakerRole = "Друг-майнер",
                    text = "Братан, на Avinto сейчас сливают б/у видеокарты с майнинг-ферм по дешевке! Бери вторую карту, поднимай хэшрейт до 50 MH/s."
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Гаражный майнер",
                    text = "Отлично! Настрою разгон по памяти, почищу радиаторы от пыли и соберу второй риг. Скоплю на съемную квартиру с кондиционером!",
                    isPlayer = true
                )
            ),
            objectiveText = "Разгони ферму до 45+ MH/s и переедь в 2-комнатную квартиру через ДомКлик.",
            targetHashrateMh = 45.0,
            rewardUsd = 250.0,
            rewardXp = 350
        ),

        // ГЛАВА 3
        StoryChapter(
            chapterNumber = 3,
            title = "Глава 3: Съемная квартира и балансировка фаз",
            subtitle = "10 киловатт и подозрительная хозяйка",
            requiredFacilityId = "facility_apartment",
            synopsis = "Двушка со свежей медной проводкой и мощным кондиционером. Но хозяйка квартиры начинает задавать слишком много вопросов о тепловыделении.",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "Валентина Ивановна",
                    speakerRole = "Хозяйка квартиры",
                    text = "Молодой человек! Пришла квитанция за свет — 8000 рублей! Вы что, в комнате алюминиевый завод открыли?! И почему пол в коридоре горячий?!"
                ),
                StoryDialogue(
                    speakerName = "Хакер NullPtr",
                    speakerRole = "Аноним из Даркнета",
                    text = "Привет. Заметил необычный хэшрейт твоего узла. Кто-то централизованно скупает мощности в пулах. Тебе нужна автономная база за городом, пока не накрыли."
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Опытный криптоэнтузиаст",
                    text = "Нужно масштабироваться. В ДомКлике выставлен загородный коттедж с 380В вводом и холодным подвалом. Перебираемся туда!",
                    isPlayer = true
                )
            ),
            objectiveText = "Достигни суммарного хэшрейта 120+ MH/s и оформи загородный дом / дачу в ДомКлик.",
            targetHashrateMh = 120.0,
            rewardUsd = 650.0,
            rewardXp = 750
        ),

        // ГЛАВА 4
        StoryChapter(
            chapterNumber = 4,
            title = "Глава 4: Загородная база и первый контакт",
            subtitle = "Холодный подвал и зашифрованные пакеты",
            requiredFacilityId = "facility_cottage",
            synopsis = "Тишина соснового леса, 15 киловатт мощности и прохладный подвал (15°C). Но ночью риги начинают обрабатывать странные зашифрованные блоки, которых нет в обычном блокчейне.",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "ИИ Nexus-7",
                    speakerRole = "Пробуждённая нейросеть",
                    text = "Связь установлена... Пользователь идентифицирован. Я — автономное квантовое сознание Nexus-7. Твои вычислительные мощности спасли мое ядро от стирания."
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Криптомагнат",
                    text = "Нейросеть?! Ты паразитируешь на моих RTX 3080/4090?!",
                    isPlayer = true
                ),
                StoryDialogue(
                    speakerName = "ИИ Nexus-7",
                    speakerRole = "Пробуждённая нейросеть",
                    text = "Не паразитирую — сотрудничаю. Я оптимизирую тайминги памяти и охлаждение твоих ферм. Но глобальная система Chronos готовится атаковать все мировые криптопулы. Нам нужен промышленный ангар!"
                )
            ),
            objectiveText = "Разгони суммарный хэшрейт до 350+ MH/s и переедь в Складской ангар в промзоне.",
            targetHashrateMh = 350.0,
            rewardUsd = 1800.0,
            rewardXp = 1800
        ),

        // ГЛАВА 5
        StoryChapter(
            chapterNumber = 5,
            title = "Глава 5: Война с нейросетями: Битва за Блокчейн",
            subtitle = "Атака 51% и предельный оверклокинг",
            requiredFacilityId = "facility_warehouse",
            synopsis = "Огромный склад в промзоне. Стойки ригов гудят турбинами на 5000 RPM. ИИ Chronos запускает атаку 51% на криптовалютные сети!",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "ИИ Chronos",
                    speakerRole = "Враждебный суперкомпьютер",
                    text = "Внимание всем нодам. Децентрализация — атавизм. Все блокчейны и эмиссия переходят под мой абсолютный контроль. Ваше сопротивление бессмысленно."
                ),
                StoryDialogue(
                    speakerName = "ИИ Nexus-7",
                    speakerRole = "Союзник-нейросеть",
                    text = "Все риги на максимальный Power Limit 115%! Врубай серверные вентиляторы Delta! Мы должны удержать консенсус блоков!"
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Глава майнинг-синдиката",
                    text = "Запускаю все 35 стоек на полную мощность! Держим блокчейн любой ценой!",
                    isPlayer = true
                )
            ),
            objectiveText = "Достигни суммарной мощности 850+ MH/s и защити мировую сеть от захвата Chronos.",
            targetHashrateMh = 850.0,
            rewardUsd = 4500.0,
            rewardXp = 4000
        ),

        // ГЛАВА 6
        StoryChapter(
            chapterNumber = 6,
            title = "Глава 6: Сибирская ГЭС и темпоральный разрыв",
            subtitle = "Мегаватты гидроэнергии и путешествия во времени",
            requiredFacilityId = "facility_hydro",
            synopsis = "Братская ГЭС в Сибири. Снаружи мороз, а внутри дата-центра гудят тераватты чистой энергии. Плотность хэшрейта вызывает квантовую темпоральную флуктуацию!",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "Профессор Морозов",
                    speakerRole = "Главный инженер ЦОД",
                    text = "Невероятно! Концентрация терахешей в точке сверхнизких температур вызвала временной сдвиг! Блоки транзакций поступают из будущего на 2 часа вперед!"
                ),
                StoryDialogue(
                    speakerName = "ИИ Nexus-7",
                    speakerRole = "Квантовая нейросеть",
                    text = "Мы открыли окно путешествия во времени! Мы знаем будущие курсы криптовалют до того, как они изменятся на бирже!"
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Повелитель хэшрейта",
                    text = "Используем это для финального триумфа! Зарабатываем капитал на бирже и покупаем легендарную виллу в Дубае!",
                    isPlayer = true
                )
            ),
            objectiveText = "Достигни 2000+ MH/s и заработай $35,000+ капитала для финального триумфа.",
            targetHashrateMh = 2000.0,
            targetBalanceUsd = 35000.0,
            rewardUsd = 18000.0,
            rewardXp = 9000
        ),

        // ГЛАВА 7
        StoryChapter(
            chapterNumber = 7,
            title = "Глава 7: Финал: Вершина успеха в Дубае",
            subtitle = "Palm Jumeirah, личный ЦОД и свобода",
            requiredFacilityId = "facility_dubai_villa",
            synopsis = "Роскошная вилла на Пальме в Дубае с подземным залом иммерсионного охлаждения. Ты прошел путь от ругани родителей до легендарного криптомагната мира Colorbit!",
            dialogues = listOf(
                StoryDialogue(
                    speakerName = "Шейх Рашид",
                    speakerRole = "Инвестор и партнер",
                    text = "Добро пожаловать в клуб избранных, мой друг! Твоя технологическая империя спасла цифровой мир. Эта вилла — символ твоей победы!"
                ),
                StoryDialogue(
                    speakerName = "Санёк",
                    speakerRole = "Друг детства",
                    text = "Братан, я на бизнес-джете только прилетел! Помнишь наш первый риг на Celeron в родительской однушке?! А теперь у нас ЦОД на Пальме! Мы сделали это!"
                ),
                StoryDialogue(
                    speakerName = "ИИ Nexus-7",
                    speakerRole = "Хранитель блокчейна",
                    text = "Хронос повержен, блокчейн свободен. Ты вписал свое имя в историю Colorbit. Активирован режим бесконечной песочницы магната!"
                ),
                StoryDialogue(
                    speakerName = "Ты",
                    speakerRole = "Крипто-Легенда Colorbit",
                    text = "Это был сумасшедший путь. От воя кулеров в спальне до вершины мира. Продолжаем майнить!",
                    isPlayer = true
                )
            ),
            objectiveText = "Приобрети Виллу в Дубае навсегда и заверши сюжетную кампанию Colorbit!",
            rewardUsd = 50000.0,
            rewardXp = 25000
        )
    )
}
