/**
 * @author Simon Symhoven
 */

let domLoaded;
let grid;

/**
 *
 * @param configuration
 */
function initDashboard(configuration) {

    const defaultConfig = JSON.parse(configuration);

    loadGrid(defaultConfig);
    sortGrid();

}

/**
 *
 */
document.addEventListener('DOMContentLoaded', function () {

    let docElem = document.documentElement;
    let demo = document.querySelector('.grid-demo');
    let gridElement = demo.querySelector('.grid');
    let addItemsElement = document.querySelector('.add-more-items');

    /**
     *
     */
    function initGrid() {

        grid = new Muuri(gridElement, {
            layoutDuration: 400,
            layoutEasing: 'ease',
            dragEnabled: true,
            dragSortInterval: 50,
            dragContainer: document.body,
            dragStartPredicate: function (item, event) {
                const isDraggable = true;
                const isRemoveAction = elementMatches(event.target, '.card-remove, .card-remove i');
                return isDraggable && !isRemoveAction ? Muuri.ItemDrag.defaultStartPredicate(item, event) : false;
            },
            dragReleaseDuration: 400,
            dragReleaseEasing: 'ease'
        })
            .on('dragStart', function () {
                docElem.classList.add('dragging');
            })
            .on('dragEnd', function () {
                docElem.classList.remove('dragging');
            })
            .on('move', function () {
                const gridOrder = grid.getItems().map(item => item.getElement().getAttribute('data-id'));
                localStorage.setItem(getLocalStorageId(), JSON.stringify(gridOrder));
            });

        addItemsElement.addEventListener('click', addItem);
        gridElement.addEventListener('click', function (e) {
            if (elementMatches(e.target, '.card-remove, .card-remove i')) {
                removeItem(e);
            }
        });

        domLoaded = true;

    }

    if (!domLoaded) {
        initGrid();
    }

});

/**
 *
 * @param values
 * @returns {*}
 */
function getCheckedValue(values) {

    let checked;
    for (const v of values) {
        if (v.checked) {
            checked = v.value;
            break;
        }
    }
    return checked;

}

/**
 *
 */
function addItem() {

    const bricks = document.querySelectorAll('input[name="brick"]');
    const colors = document.querySelectorAll('input[name="color"]');
    const plugins = document.querySelectorAll('input[name="plugin"]');

    const size = getCheckedValue(bricks).split(",");
    const color = getCheckedValue(colors);
    const plugin = getCheckedValue(plugins);

    const newElem = generateItem(size, color, plugin);

    grid.add(newElem);
    const modal = document.getElementById('modalClose');
    modal.click();

}

/**
 *
 * @param e
 */
function removeItem(e) {

    const items = grid.getItems();
    const elem = elementClosest(e.target, '.muuri-item');
    const index = items.findIndex(e => e._element === elem);
    const elemToRemove = items.slice(index, index + 1);

    grid.hide(elemToRemove, {onFinish: function (items) {
            grid.remove(items, {removeElements: true});
        }});

}

/**
 *
 * @param size
 * @param color
 * @param plugin
 * @returns {ChildNode}
 */
function generateItem(size, color, plugin) {

    const id = plugin;
    const title = plugin;
    const width = size[0];
    const height = size[1];
    const itemElem = document.createElement('div');
    itemElem.innerHTML =
        '<div class="muuri-item h' + height + ' w' + width + ' ' + color + '" data-id="' + id + '" data-color="' + color + '" data-title="' + title + '">' +
            '<div class="muuri-item-content">' +
                '<div class="card">' +
                    '<div class="plugin-card-title">' + plugin + '</div>' +
                    '<div class="card-remove"><i class="material-icons">&#xE5CD;</i></div>' +
                '</div>' +
            '</div>' +
        '</div>';

    return itemElem.firstChild;

}

/**
 *
 * @param element
 * @param selector
 * @returns {boolean}
 */
function elementMatches(element, selector) {

    const p = Element.prototype;
    return (p.matches || p.matchesSelector || p.webkitMatchesSelector || p.mozMatchesSelector || p.msMatchesSelector || p.oMatchesSelector).call(element, selector);

}

/**
 *
 * @param element
 * @param selector
 * @returns {*|null}
 */
function elementClosest(element, selector) {

    if (window.Element && !Element.prototype.closest) {
        let isMatch = elementMatches(element, selector);
        while (!isMatch && element && element !== document) {
            element = element.parentNode;
            isMatch = element && element !== document && elementMatches(element, selector);
        }
        return element && element !== document ? element : null;
    }
    else {
        return element.closest(selector);
    }

}

/**
 *
 * @param defaultConfig
 */
function loadGrid(defaultConfig) {

    Object.keys(defaultConfig.plugins).forEach(function(key) {
        const plugin = defaultConfig.plugins[key];
        const item = generateItem([plugin.width, plugin.height], plugin.color, key);
        grid.add(item, { active: false });
    });

}

/**
 *
 */
function sortGrid() {

    const hasConfigStored = localStorage.getItem(getLocalStorageId()) !== null;

    if (hasConfigStored) {
        const currentItems = grid.getItems();
        const currentItemIds = JSON.parse(JSON.parse(localStorage.getItem(getLocalStorageId())));
        const sortedItems = [];

        currentItemIds.forEach(function (currentItemIdsKey, index) {
            let element = currentItems.find(item => {
                return item.getElement().getAttribute('data-id') === currentItemIdsKey;
            });

            sortedItems.push(element);
        });

        grid.sort(sortedItems);
    }

    grid.show(grid.getItems());

}

/**
 *
 * @returns {string}
 */
function getLocalStorageId() {

    return window.location.pathname.split('/').slice(3, -2).join('.') + '.monitoring-grid-order';

}