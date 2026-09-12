package com.example.data

import com.example.model.ComponentType
import com.example.model.PCComponent

object ComponentCatalog {
    val allComponents = listOf(
        // === GPU ===
        PCComponent(
            id = "gpu_gtx1050ti",
            name = "NVIDIA GeForce GTX 1050 Ti 4GB",
            type = ComponentType.GPU,
            priceUsd = 65.0,
            hashRateMh = 13.5,
            powerWatts = 75,
            tier = 1,
            description = "Бюджетная легенда для первого старта в гараже."
        ),
        PCComponent(
            id = "gpu_rx580",
            name = "AMD Radeon RX 580 8GB",
            type = ComponentType.GPU,
            priceUsd = 110.0,
            hashRateMh = 31.0,
            powerWatts = 135,
            tier = 1,
            description = "Рабочая лошадка эпохи майнинг-бума. Отличный хэшрейт за свои деньги."
        ),
        PCComponent(
            id = "gpu_gtx1660s",
            name = "NVIDIA GeForce GTX 1660 Super 6GB",
            type = ComponentType.GPU,
            priceUsd = 175.0,
            hashRateMh = 31.8,
            powerWatts = 85,
            tier = 2,
            description = "Очень холодная и энергоэффективная карта."
        ),
        PCComponent(
            id = "gpu_rtx2060s",
            name = "NVIDIA GeForce RTX 2060 Super 8GB",
            type = ComponentType.GPU,
            priceUsd = 240.0,
            hashRateMh = 43.5,
            powerWatts = 125,
            tier = 2,
            description = "Хороший запас видеопамяти и лучи для игр в свободное время."
        ),
        PCComponent(
            id = "gpu_rtx3060ti",
            name = "NVIDIA GeForce RTX 3060 Ti 8GB",
            type = ComponentType.GPU,
            priceUsd = 360.0,
            hashRateMh = 60.5,
            powerWatts = 130,
            tier = 3,
            description = "Золотой стандарт крипто-майнинга. Высокая доходность."
        ),
        PCComponent(
            id = "gpu_rtx3070",
            name = "NVIDIA GeForce RTX 3070 8GB GDDR6",
            type = ComponentType.GPU,
            priceUsd = 450.0,
            hashRateMh = 63.0,
            powerWatts = 135,
            tier = 3,
            description = "Надёжная и быстрая видеокарта с отличным охлаждением."
        ),
        PCComponent(
            id = "gpu_rtx3080",
            name = "NVIDIA GeForce RTX 3080 10GB GDDR6X",
            type = ComponentType.GPU,
            priceUsd = 690.0,
            hashRateMh = 101.0,
            powerWatts = 230,
            tier = 4,
            description = "Монстр хэшрейта. Требует мощного охлаждения и отдельного внимания к термопасте!"
        ),
        PCComponent(
            id = "gpu_rtx4090",
            name = "NVIDIA GeForce RTX 4090 24GB",
            type = ComponentType.GPU,
            priceUsd = 1750.0,
            hashRateMh = 148.0,
            powerWatts = 330,
            tier = 5,
            description = "Флагманский зверь. 24 ГБ памяти, огромный доход, но требует мощнейшего БП."
        ),

        // === CPU ===
        PCComponent(
            id = "cpu_celeron",
            name = "Intel Celeron G3930",
            type = ComponentType.CPU,
            priceUsd = 25.0,
            hashRateMh = 1.2,
            powerWatts = 35,
            tier = 1,
            description = "Дешёвая 'затычка' для майнинг фермы."
        ),
        PCComponent(
            id = "cpu_i3",
            name = "Intel Core i3-10100F",
            type = ComponentType.CPU,
            priceUsd = 65.0,
            hashRateMh = 4.5,
            powerWatts = 65,
            tier = 2,
            description = "4 ядра, 8 потоков. Стабильная работа рига."
        ),
        PCComponent(
            id = "cpu_ryzen5",
            name = "AMD Ryzen 5 3600",
            type = ComponentType.CPU,
            priceUsd = 95.0,
            hashRateMh = 9.0,
            powerWatts = 65,
            tier = 3,
            description = "Умеет майнить процессоро-зависимые монеты на алгоритме RandomX."
        ),
        PCComponent(
            id = "cpu_ryzen9",
            name = "AMD Ryzen 9 5950X 16-Core",
            type = ComponentType.CPU,
            priceUsd = 360.0,
            hashRateMh = 24.0,
            powerWatts = 145,
            tier = 4,
            description = "16 мощных ядер для гибридного CPU+GPU майнинга."
        ),

        // === MOTHERBOARDS ===
        PCComponent(
            id = "mb_basic",
            name = "H81 Pro BTC (2x PCIe)",
            type = ComponentType.MOTHERBOARD,
            priceUsd = 40.0,
            powerWatts = 20,
            maxGpuSlots = 2,
            maxRamSlots = 2,
            tier = 1,
            description = "Плата начального уровня на 2 видеокарты."
        ),
        PCComponent(
            id = "mb_b250_expert",
            name = "B250 Mining Expert (6x PCIe)",
            type = ComponentType.MOTHERBOARD,
            priceUsd = 90.0,
            powerWatts = 30,
            maxGpuSlots = 6,
            maxRamSlots = 2,
            tier = 2,
            description = "Специализированная плата с раздельным питанием на 6 GPU."
        ),
        PCComponent(
            id = "mb_z390_pro",
            name = "BTC-D37 Industrial Server (8x PCIe)",
            type = ComponentType.MOTHERBOARD,
            priceUsd = 160.0,
            powerWatts = 45,
            maxGpuSlots = 8,
            maxRamSlots = 2,
            tier = 3,
            description = "Серверная моноплата со встроенными райзерами на 8 видеокарт без переходников."
        ),

        // === RAM ===
        PCComponent(
            id = "ram_4gb",
            name = "DDR4 4GB 2400MHz",
            type = ComponentType.RAM,
            priceUsd = 15.0,
            powerWatts = 4,
            tier = 1,
            description = "Минимум для загрузки операционной системы фермы."
        ),
        PCComponent(
            id = "ram_8gb",
            name = "DDR4 8GB 3200MHz",
            type = ComponentType.RAM,
            priceUsd = 25.0,
            powerWatts = 5,
            tier = 2,
            description = "Оптимально для DAG-файлов и быстрой обработки очередей."
        ),
        PCComponent(
            id = "ram_16gb",
            name = "DDR4 16GB (2x8GB) Dual Channel",
            type = ComponentType.RAM,
            priceUsd = 45.0,
            powerWatts = 8,
            tier = 3,
            description = "Быстрая двухканальная память для максимального КПД."
        ),

        // === PSU (Блоки питания) ===
        PCComponent(
            id = "psu_500w",
            name = "Aerocool VX 500W",
            type = ComponentType.PSU,
            priceUsd = 30.0,
            psuWattsCapacity = 500,
            tier = 1,
            description = "Бюджетный блок питания. Не перегружать выше 400W!"
        ),
        PCComponent(
            id = "psu_750w_gold",
            name = "Chieftec Polaris 750W Gold",
            type = ComponentType.PSU,
            priceUsd = 75.0,
            psuWattsCapacity = 750,
            tier = 2,
            description = "Надёжный золотой сертификат 80 PLUS. Держит до 3 видеокарт."
        ),
        PCComponent(
            id = "psu_1200w_plat",
            name = "Corsair HX1200 1200W Platinum",
            type = ComponentType.PSU,
            priceUsd = 160.0,
            psuWattsCapacity = 1200,
            tier = 3,
            description = "Платиновый КПД 94%. Идеален для фермы на 4-6 карт."
        ),
        PCComponent(
            id = "psu_2000w_server",
            name = "HP Server Mining PSU 2000W",
            type = ComponentType.PSU,
            priceUsd = 240.0,
            psuWattsCapacity = 2000,
            tier = 4,
            description = "Промышленный серверный блок с распайкой проводов для 8 мощных карт."
        ),

        // === COOLING ===
        PCComponent(
            id = "cool_box",
            name = "Стандартный кулер + 1x 120mm вентилятор",
            type = ComponentType.COOLING,
            priceUsd = 12.0,
            coolingCapacityWatts = 180,
            powerWatts = 6,
            tier = 1,
            description = "Базовый обдув. Хватает для одной слабой видеокарты."
        ),
        PCComponent(
            id = "cool_delta_rig",
            name = "Комплект Delta 4x 120mm (5000 RPM)",
            type = ComponentType.COOLING,
            priceUsd = 45.0,
            coolingCapacityWatts = 550,
            powerWatts = 30,
            tier = 2,
            description = "Турбинные серверные вентиляторы. Громкие, но сдувают всю жару."
        ),
        PCComponent(
            id = "cool_industrial",
            name = "Промышленная вытяжная система и фильтры",
            type = ComponentType.COOLING,
            priceUsd = 130.0,
            coolingCapacityWatts = 1200,
            powerWatts = 60,
            tier = 3,
            description = "Мощная приточно-вытяжная вентиляция с фильтром тонкой очистки от пыли."
        ),
        PCComponent(
            id = "cool_immersion",
            name = "Иммерсионная ванна охлаждения",
            type = ComponentType.COOLING,
            priceUsd = 420.0,
            coolingCapacityWatts = 3200,
            powerWatts = 90,
            tier = 4,
            description = "Погружное охлаждение в диэлектрической жидкости. Ноль пыли и абсолютная тишина!"
        )
    )

    // === БАРАХОЛКА AVINTO (Б/У Комплектующие со скидкой) ===
    val usedComponents = listOf(
        PCComponent(
            id = "used_gpu_gtx1050ti",
            name = "[Б/У Avinto] GeForce GTX 1050 Ti 4GB",
            type = ComponentType.GPU,
            priceUsd = 38.0,
            hashRateMh = 13.0,
            powerWatts = 78,
            tier = 1,
            description = "Продавец Санёк: 'Стояла у сестры в компе, не майнила!'. Износ 20%.",
            isUsed = true,
            baseDurability = 80f
        ),
        PCComponent(
            id = "used_gpu_rx580",
            name = "[Б/У Avinto] Radeon RX 580 8GB Nitro+",
            type = ComponentType.GPU,
            priceUsd = 68.0,
            hashRateMh = 30.5,
            powerWatts = 140,
            tier = 1,
            description = "Продавец Михалыч: 'Прошитый биос под эфир, кулера смазаны'. Износ 25%.",
            isUsed = true,
            baseDurability = 75f
        ),
        PCComponent(
            id = "used_gpu_gtx1660s",
            name = "[Б/У Avinto] GeForce GTX 1660 Super 6GB",
            type = ComponentType.GPU,
            priceUsd = 108.0,
            hashRateMh = 31.0,
            powerWatts = 88,
            tier = 2,
            description = "С офисного ПК. Пломбы на месте, термопаста родная. Износ 18%.",
            isUsed = true,
            baseDurability = 82f
        ),
        PCComponent(
            id = "used_gpu_rtx2060s",
            name = "[Б/У Avinto] GeForce RTX 2060 Super 8GB",
            type = ComponentType.GPU,
            priceUsd = 155.0,
            hashRateMh = 42.0,
            powerWatts = 130,
            tier = 2,
            description = "С домашнего игрового ПК. Отличный вариант удвоить хэшрейт фермы.",
            isUsed = true,
            baseDurability = 80f
        ),
        PCComponent(
            id = "used_gpu_rtx3070",
            name = "[Б/У Avinto] GeForce RTX 3070 8GB",
            type = ComponentType.GPU,
            priceUsd = 280.0,
            hashRateMh = 61.0,
            powerWatts = 145,
            tier = 3,
            description = "Продавец 'Крипто-Ликвидация': 'Распродажа домашней фермы'. Износ 22%.",
            isUsed = true,
            baseDurability = 78f
        ),
        PCComponent(
            id = "used_gpu_rtx3080",
            name = "[Б/У Avinto] GeForce RTX 3080 10GB GDDR6X",
            type = ComponentType.GPU,
            priceUsd = 450.0,
            hashRateMh = 98.0,
            powerWatts = 240,
            tier = 4,
            description = "Монстр хэшрейта. Термопрокладки заменены на медные пластины.",
            isUsed = true,
            baseDurability = 75f
        )
    )

    // === КРЕДИТНЫЙ БАНК (СберБит / Кредиты из Colorbit) ===
    val loanOptions = listOf(
        com.example.model.VirtualLoan(
            id = "loan_micro",
            title = "Микрозайм 'Быстрый старт'",
            amountUsd = 150.0,
            interestRatePercent = 8.0,
            totalToRepayUsd = 162.0,
            remainingDebtUsd = 162.0
        ),
        com.example.model.VirtualLoan(
            id = "loan_starter",
            title = "Кредит 'Студент-майнер'",
            amountUsd = 500.0,
            interestRatePercent = 10.0,
            totalToRepayUsd = 550.0,
            remainingDebtUsd = 550.0
        ),
        com.example.model.VirtualLoan(
            id = "loan_pro",
            title = "Бизнес-кредит 'Гаражный масштаб'",
            amountUsd = 2000.0,
            interestRatePercent = 12.0,
            totalToRepayUsd = 2240.0,
            remainingDebtUsd = 2240.0
        ),
        com.example.model.VirtualLoan(
            id = "loan_corporate",
            title = "Синдицированный заём 'ЦОД и Недвижимость'",
            amountUsd = 10000.0,
            interestRatePercent = 15.0,
            totalToRepayUsd = 11500.0,
            remainingDebtUsd = 11500.0
        )
    )
}
