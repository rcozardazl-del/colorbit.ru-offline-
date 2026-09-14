(function() {
    console.log('[Colorbit] Offline Renderer Starting...');

    function getRelativeShopUrl(slug) {
        var isShopsDir = window.location.pathname.indexOf('/shops/') !== -1;
        if (isShopsDir) {
            return slug + '.html';
        } else {
            return 'shops/' + slug + '.html';
        }
    }

    function initRenderer() {
        var appEl = document.getElementById('app');
        if (!appEl) {
            console.warn('[Colorbit] No #app element found');
            return;
        }

        var dataPageAttr = appEl.getAttribute('data-page');
        if (!dataPageAttr) {
            console.warn('[Colorbit] No data-page attribute found on #app');
            return;
        }

        var pageData;
        try {
            pageData = JSON.parse(dataPageAttr);
        } catch(e) {
            console.error('[Colorbit] JSON.parse error:', e);
            try {
                // Try unescaping HTML entities if needed
                var txt = document.createElement('textarea');
                txt.innerHTML = dataPageAttr;
                pageData = JSON.parse(txt.value);
            } catch(e2) {
                console.error('[Colorbit] Unescaping fallback failed:', e2);
                return;
            }
        }

        console.log('[Colorbit] Rendering component:', pageData.component);
        renderPage(appEl, pageData);
    }

    function renderPage(container, page) {
        var comp = page.component || '';
        var props = page.props || {};
        var shop = props.shop || null;
        var shopsList = props.shopsList || null;

        var html = '<div class="cb-root">';
        html += renderNavbar(shop ? shop.slug : '');

        if (shop) {
            html += renderShop(shop);
        } else if (shopsList) {
            html += renderAllShops(shopsList);
        } else if (comp.indexOf('Location') !== -1 || props.locationShop) {
            html += renderLocations(props);
        } else if (props.leaders) {
            html += renderLeaderboard(props.leaders);
        } else {
            html += renderGenericPage(comp, props);
        }

        html += renderFooter();
        html += '</div>';

        // Modal and Toast containers
        html += '<div id="cb-toast-container"></div>';

        container.innerHTML = html;
        attachEvents();
    }

    function renderNavbar(currentSlug) {
        var shops = [
            { slug: 'dhs', name: 'DHS (DNS)' },
            { slug: 'aliexprezz', name: 'Aliexprezz' },
            { slug: 'softportalcom', name: 'SoftPortal' },
            { slug: 'gertruda-karkas', name: 'Гертруда' },
            { slug: 'onlyfans', name: 'OnlyFans' },
            { slug: 'domklik', name: 'ДомКлик' },
            { slug: 'shops', name: 'Все Магазины' },
            { slug: 'locations', name: 'Локации' }
        ];

        var isShopsDir = window.location.pathname.indexOf('/shops/') !== -1;
        var navHtml = '<header class="cb-header">' +
            '<div class="cb-header-top">' +
                '<div class="cb-brand">' +
                    '<span class="cb-brand-icon">⚡</span>' +
                    '<span class="cb-brand-title">COLORBIT MINING</span>' +
                    '<span class="cb-badge-offline">ОФЛАЙН HTML</span>' +
                '</div>' +
                '<div class="cb-actions">' +
                    '<button type="button" class="cb-btn-avito" id="btn-open-avito">Авито P2P</button>' +
                    '<button type="button" class="cb-btn-game" id="btn-close-web">В игру ✕</button>' +
                '</div>' +
            '</div>' +
            '<div class="cb-shop-pills">';

        for (var i = 0; i < shops.length; i++) {
            var s = shops[i];
            var isActive = (s.slug === currentSlug) || (s.slug === 'shops' && currentSlug === '');
            var activeClass = isActive ? ' cb-pill-active' : '';
            var url = getRelativeShopUrl(s.slug);
            navHtml += '<a href="' + url + '" class="cb-pill' + activeClass + '">' + s.name + '</a>';
        }

        navHtml += '</div></header>';
        return navHtml;
    }

    function renderShop(shop) {
        var name = shop.name || 'Магазин';
        var desc = shop.description || 'Официальный каталог оборудования и комплектующих Colorbit';
        var warranty = shop.warranty !== undefined ? shop.warranty : 0;
        var delivery = shop.delivery_time !== undefined ? shop.delivery_time : 0;
        var goodsObj = shop.goods || {};
        var goodsList = goodsObj.data || (Array.isArray(goodsObj) ? goodsObj : []);
        var totalGoods = goodsObj.total || goodsList.length;

        var out = '<div class="cb-container">';
        out += '<div class="cb-shop-banner">' +
            '<div class="cb-shop-badge">МАГАЗИН ОБОРУДОВАНИЯ</div>' +
            '<h1 class="cb-shop-title">' + escapeHtml(name) + '</h1>' +
            '<p class="cb-shop-desc">' + escapeHtml(desc) + '</p>' +
            '<div class="cb-meta-row">' +
                '<span class="cb-meta-tag">🛡️ Гарантия: ' + (warranty > 0 ? warranty + ' дн.' : 'Нет') + '</span>' +
                '<span class="cb-meta-tag">🚚 Доставка: ' + (delivery > 0 ? delivery + ' дн.' : 'Мгновенно') + '</span>' +
                '<span class="cb-meta-tag">📦 В наличии: ' + totalGoods + ' позиций</span>' +
            '</div>' +
        '</div>';

        // Filter and Search
        out += '<div class="cb-filter-bar">' +
            '<input type="text" id="cb-search-input" class="cb-search" placeholder="🔍 Поиск по названию, характеристикам..." />' +
        '</div>';

        // Categories Row
        out += '<div class="cb-cat-row">' +
            '<button type="button" class="cb-cat-btn cb-cat-active" data-cat="">Все</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="gpu">Видеокарты</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="cpu">Процессоры</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="ram">ОЗУ</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="motherboard">Материнки</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="power_supply">БП</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="storage">Диски</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="fan">Охлаждение</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="case">Корпуса</button>' +
            '<button type="button" class="cb-cat-btn" data-cat="software">Софт</button>' +
        '</div>';

        // Products Grid
        out += '<div class="cb-grid" id="cb-products-grid">';
        for (var i = 0; i < goodsList.length; i++) {
            out += renderProductCard(goodsList[i], i);
        }
        out += '</div>';

        if (goodsList.length === 0) {
            out += '<div class="cb-empty">В данном магазине сейчас нет товаров на складе. Перейдите в другой магазин через верхнее меню.</div>';
        }

        out += '</div>';
        return out;
    }

    function renderProductCard(item, idx) {
        var g = item.good || {};
        var name = g.name || 'Комплектующее';
        var price = item.price || 0;
        var currency = item.currency || 'USD';
        var symbol = currency === 'RUB' ? '₽' : '$';
        var type = g.type || 'item';
        var rarity = g.rarityName || 'Обычное';
        var desc = g.description || '';
        var props = g.properties || {};

        var specTags = '';
        if (props.hashrate) specTags += '<span class="cb-spec-chip cb-spec-green">⚡ ' + props.hashrate + ' MH/s</span>';
        if (props.TDP || props.watts) specTags += '<span class="cb-spec-chip cb-spec-orange">🔥 ' + (props.TDP || props.watts) + 'W</span>';
        if (props.vram || props.size) specTags += '<span class="cb-spec-chip cb-spec-cyan">💾 ' + (props.vram || props.size) + (props.size && props.size >= 1024 ? 'МБ' : 'ГБ') + '</span>';
        if (props.socket) specTags += '<span class="cb-spec-chip">🔌 ' + escapeHtml(props.socket) + '</span>';
        if (props.effects) {
            if (props.effects.hashrateMultiplier) specTags += '<span class="cb-spec-chip cb-spec-green">Хешрейт x' + props.effects.hashrateMultiplier + '</span>';
            if (props.effects.CPUPerformanceMultiplier) specTags += '<span class="cb-spec-chip cb-spec-cyan">CPU x' + props.effects.CPUPerformanceMultiplier + '</span>';
        }

        var rarityClass = 'cb-rarity-common';
        if (rarity === 'Необычное') rarityClass = 'cb-rarity-uncommon';
        if (rarity === 'Редкое') rarityClass = 'cb-rarity-rare';
        if (rarity === 'Легендарное') rarityClass = 'cb-rarity-legendary';

        var imgHtml = g.image ? 
            '<img src="' + g.image + '" class="cb-card-img" onerror="this.style.display=\'none\'" alt="' + escapeHtml(name) + '" />' :
            '<div class="cb-card-img-placeholder">⚙️</div>';

        var safeName = escapeHtml(name);
        return '<div class="cb-card" data-name="' + safeName.toLowerCase() + '" data-type="' + type.toLowerCase() + '" data-desc="' + escapeHtml(desc).toLowerCase() + '">' +
            '<div class="cb-card-header">' +
                '<span class="cb-card-rarity ' + rarityClass + '">' + escapeHtml(rarity) + '</span>' +
                '<span class="cb-card-type">' + escapeHtml(g.typeName || type) + '</span>' +
            '</div>' +
            '<div class="cb-card-body">' +
                imgHtml +
                '<h3 class="cb-card-name">' + safeName + '</h3>' +
                '<div class="cb-specs">' + specTags + '</div>' +
                (desc ? '<p class="cb-card-desc">' + escapeHtml(desc) + '</p>' : '') +
            '</div>' +
            '<div class="cb-card-footer">' +
                '<div class="cb-price-col">' +
                    '<span class="cb-price-label">Цена:</span>' +
                    '<span class="cb-price-value">' + price + ' ' + symbol + '</span>' +
                '</div>' +
                '<button type="button" class="cb-btn-buy" data-buy-name="' + encodeURIComponent(name) + '" data-buy-price="' + price + '" data-buy-type="' + type + '">Купить</button>' +
            '</div>' +
        '</div>';
    }

    function renderAllShops(shopsList) {
        var shops = shopsList.data || (Array.isArray(shopsList) ? shopsList : []);
        var out = '<div class="cb-container">';
        out += '<div class="cb-shop-banner">' +
            '<div class="cb-shop-badge">ОФЛАЙН КАТАЛОГ</div>' +
            '<h1 class="cb-shop-title">Каталог магазинов Colorbit</h1>' +
            '<p class="cb-shop-desc">Официальные торговые точки и поставщики оборудования для майнинг-ферм</p>' +
        '</div>';

        out += '<div class="cb-shops-grid">';
        for (var i = 0; i < shops.length; i++) {
            var s = shops[i];
            var url = getRelativeShopUrl(s.slug);
            var isAvito = s.slug === 'avinto';
            out += '<a href="' + url + '" class="cb-shop-card">' +
                '<div class="cb-shop-card-badge">' + (s.type === 'used_market' ? 'Вторичный рынок' : 'Официальный ритейл') + '</div>' +
                '<h2 class="cb-shop-card-name">' + escapeHtml(s.name) + '</h2>' +
                '<p class="cb-shop-card-meta">Гарантия: ' + (s.warranty > 0 ? s.warranty + ' дн.' : 'Нет') + ' • Доставка: ' + (s.delivery_time > 0 ? s.delivery_time + ' дн.' : 'Мгновенно') + '</p>' +
                '<span class="cb-shop-card-link">' + (isAvito ? 'Открыть Авито P2P &rarr;' : 'Перейти в магазин &rarr;') + '</span>' +
            '</a>';
        }
        out += '</div></div>';
        return out;
    }

    function renderLocations(props) {
        var locs = [
            { name: 'Гараж', slots: '2 рига', power: '3.5 кВт', price: 'Включено' },
            { name: 'Подвал', slots: '5 ригов', power: '10.0 кВт', price: '$500' },
            { name: 'Складской бокс', slots: '15 ригов', power: '30.0 кВт', price: '$2,500' },
            { name: 'Ангар в промзоне', slots: '50 ригов', power: '100.0 кВт', price: '$10,000' }
        ];

        var out = '<div class="cb-container">';
        out += '<div class="cb-shop-banner">' +
            '<div class="cb-shop-badge">НЕДВИЖИМОСТЬ</div>' +
            '<h1 class="cb-shop-title">Локации и Помещения</h1>' +
            '<p class="cb-shop-desc">Пространства для расширения ваших майнинг-ферм с мощным электропитанием и вентиляцией</p>' +
        '</div>';

        out += '<div class="cb-shops-grid">';
        for (var i = 0; i < locs.length; i++) {
            var l = locs[i];
            out += '<div class="cb-shop-card">' +
                '<div class="cb-shop-card-badge">ЛОКАЦИЯ #' + (i + 1) + '</div>' +
                '<h2 class="cb-shop-card-name">' + l.name + '</h2>' +
                '<p class="cb-shop-card-meta">Вместимость: ' + l.slots + ' • Питание: ' + l.power + '</p>' +
                '<div style="display:flex; justify-content:space-between; align-items:center; margin-top:8px;">' +
                    '<span style="color:#39FF14; font-weight:bold;">' + l.price + '</span>' +
                    '<button type="button" class="cb-btn-buy" data-buy-name="' + encodeURIComponent(l.name) + '" data-buy-price="0" data-buy-type="location">Арендовать</button>' +
                '</div>' +
            '</div>';
        }
        out += '</div></div>';
        return out;
    }

    function renderLeaderboard(leaders) {
        var list = leaders || [];
        var out = '<div class="cb-container">';
        out += '<div class="cb-shop-banner">' +
            '<div class="cb-shop-badge">РЕЙТИНГ</div>' +
            '<h1 class="cb-shop-title">Таблица лидеров Colorbit</h1>' +
            '<p class="cb-shop-desc">Топ игроков по накопленному капиталу и мощности майнинга</p>' +
        '</div>';

        out += '<div style="background:#1C1C1E; border:1px solid #2B2B2B; border-radius:12px; padding:12px;">';
        for (var i = 0; i < list.length; i++) {
            var u = list[i];
            var rank = i + 1;
            out += '<div style="display:flex; justify-content:space-between; align-items:center; padding:10px; border-bottom:1px solid #282828;">' +
                '<div><span style="color:#39FF14; font-weight:bold; margin-right:8px;">#' + rank + '</span>' +
                '<span style="color:#FFF; font-weight:600;">' + escapeHtml(u.name || ('Игрок ' + rank)) + '</span></div>' +
                '<div style="color:#00E5FF; font-weight:bold;">$' + (u.balance || (1000000 - i * 50000)) + '</div>' +
            '</div>';
        }
        if (list.length === 0) {
            out += '<div class="cb-empty">Таблица лидеров доступна в онлайн режиме.</div>';
        }
        out += '</div></div>';
        return out;
    }

    function renderGenericPage(comp, props) {
        return '<div class="cb-container">' +
            '<div class="cb-shop-banner">' +
                '<div class="cb-shop-badge">COLORBIT СИМУЛЯТОР</div>' +
                '<h1 class="cb-shop-title">Автономный Офлайн Каталог</h1>' +
                '<p class="cb-shop-desc">Вы просматриваете оригинальные веб-страницы магазинов Colorbit без подключения к интернету.</p>' +
            '</div>' +
            '<div style="text-align:center; padding:20px 0;">' +
                '<a href="' + getRelativeShopUrl('dhs') + '" class="cb-btn-buy" style="display:inline-block; padding:12px 24px; text-decoration:none; font-size:14px;">Открыть DHS (DNS) Каталог</a>' +
            '</div>' +
        '</div>';
    }

    function renderFooter() {
        return '<footer class="cb-footer">' +
            '<p>Colorbit Mining Simulator &copy; 2026 • Автономный офлайн режим</p>' +
        '</footer>';
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function attachEvents() {
        var currentFilterCat = '';

        // Category filter buttons
        var catButtons = document.querySelectorAll('.cb-cat-btn');
        for (var i = 0; i < catButtons.length; i++) {
            catButtons[i].addEventListener('click', function(e) {
                var btn = e.currentTarget;
                for (var j = 0; j < catButtons.length; j++) catButtons[j].classList.remove('cb-cat-active');
                btn.classList.add('cb-cat-active');
                currentFilterCat = btn.getAttribute('data-cat') || '';
                filterItems();
            });
        }

        // Search input
        var searchInput = document.getElementById('cb-search-input');
        if (searchInput) {
            searchInput.addEventListener('input', function() {
                filterItems();
            });
        }

        function filterItems() {
            var search = (searchInput && searchInput.value ? searchInput.value : '').toLowerCase().trim();
            var cards = document.querySelectorAll('.cb-card');
            for (var k = 0; k < cards.length; k++) {
                var c = cards[k];
                var name = c.getAttribute('data-name') || '';
                var type = c.getAttribute('data-type') || '';
                var desc = c.getAttribute('data-desc') || '';

                var matchesSearch = !search || name.indexOf(search) !== -1 || desc.indexOf(search) !== -1;
                var matchesCat = !currentFilterCat || type.indexOf(currentFilterCat) !== -1;

                c.style.display = (matchesSearch && matchesCat) ? 'flex' : 'none';
            }
        }

        // Buy buttons
        var buyButtons = document.querySelectorAll('.cb-btn-buy[data-buy-name]');
        for (var b = 0; b < buyButtons.length; b++) {
            buyButtons[b].addEventListener('click', function(e) {
                var btn = e.currentTarget;
                var encName = btn.getAttribute('data-buy-name') || '';
                var price = parseFloat(btn.getAttribute('data-buy-price') || '0');
                var type = btn.getAttribute('data-buy-type') || 'item';
                var name = decodeURIComponent(encName);

                if (window.AndroidBridge && typeof window.AndroidBridge.buyItem === 'function') {
                    window.AndroidBridge.buyItem(name, price, type);
                } else {
                    showToast('Товар "' + name + '" заказан за $' + price + '!');
                }
            });
        }

        // Action buttons
        var avitoBtn = document.getElementById('btn-open-avito');
        if (avitoBtn) {
            avitoBtn.addEventListener('click', function() {
                if (window.AndroidBridge && typeof window.AndroidBridge.openAvito === 'function') {
                    window.AndroidBridge.openAvito();
                } else {
                    window.location.href = getRelativeShopUrl('avinto');
                }
            });
        }

        var closeBtn = document.getElementById('btn-close-web');
        if (closeBtn) {
            closeBtn.addEventListener('click', function() {
                if (window.AndroidBridge && typeof window.AndroidBridge.close === 'function') {
                    window.AndroidBridge.close();
                } else {
                    window.history.back();
                }
            });
        }
    }

    function showToast(msg) {
        var container = document.getElementById('cb-toast-container');
        if (!container) return;
        var toast = document.createElement('div');
        toast.className = 'cb-toast';
        toast.textContent = msg;
        container.appendChild(toast);
        setTimeout(function() {
            toast.classList.add('cb-toast-show');
        }, 50);
        setTimeout(function() {
            toast.classList.remove('cb-toast-show');
            setTimeout(function() { toast.remove(); }, 300);
        }, 2800);
    }

    window.initRenderer = initRenderer;

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initRenderer);
    } else {
        initRenderer();
    }
})();
