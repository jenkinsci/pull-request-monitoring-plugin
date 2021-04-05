let domLoaded;
let grid;
let uuid;


function initDashboard(configuration) {

    const config = JSON.parse(configuration);
    Object.keys(config.plugins).forEach(function(key) {
        const plugin = config.plugins[key];
        console.log(plugin)
        const item = generateElement([plugin.width, plugin.height], plugin.color, key);
        grid.add(item);
    });

}

document.addEventListener('DOMContentLoaded', function () {

    let docElem = document.documentElement;
    let demo = document.querySelector('.grid-demo');
    let gridElement = demo.querySelector('.grid');
    let addItemsElement = document.querySelector('.add-more-items');

    function initGrid() {
        this.uuid = 0;

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
            .on('layoutEnd', function (items) {
                // todo: Save config
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

function addItem() {
    const bricks = document.querySelectorAll('input[name="brick"]');

    let selectedBrick;
    for (const b of bricks) {
        if (b.checked) {
            selectedBrick = b.value;
            break;
        }
    }
    const size = selectedBrick.split(",")

    const colors = document.querySelectorAll('input[name="color"]');

    let selectedColor;
    for (const c of colors) {
        if (c.checked) {
            selectedColor = c.value;
            break;
        }
    }
    const color = selectedColor;

    const plugins = document.querySelectorAll('input[name="plugin"]');

    let selectedPlugin;
    for (const p of plugins) {
        if (p.checked) {
            selectedPlugin = p.value;
            break;
        }
    }
    const plugin = selectedPlugin;

    const newElem = generateElement(size, color, plugin);

    grid.add(newElem);
    const modal = document.getElementById('modalClose');
    modal.click();

}

function removeItem(e) {
    const items = grid.getItems();
    const elem = elementClosest(e.target, '.muuri-item');
    const index = items.findIndex(e => e._element === elem);
    const elemToRemove = items.slice(index, index + 1);

    grid.hide(elemToRemove, {onFinish: function (items) {
            grid.remove(items, {removeElements: true});
        }});
}

function generateElement(size, color, plugin) {

    const id = ++this.uuid;
    const title = 'Title'
    const width = size[0];
    const height = size[1];
    const itemElem = document.createElement('div');
    itemElem.innerHTML = '' +
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

function elementMatches(element, selector) {

    const p = Element.prototype;
    return (p.matches || p.matchesSelector || p.webkitMatchesSelector || p.mozMatchesSelector || p.msMatchesSelector || p.oMatchesSelector).call(element, selector);

}

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