/**
 * @author Simon Symhoven
 */

let grid;
let configuration;

/**
 *
 * @param configuration
 */
function initDashboard(configuration) {

    this.configuration = JSON.parse(configuration);

    initGrid();

    loadGrid();

    updateConfigPanel();

}

/**
 *
 */
function initGrid() {

    let docElem = document.documentElement;
    let demo = document.querySelector('.grid-demo');
    let gridElement = demo.querySelector('.grid');
    let addItemsElement = document.querySelector('.add-more-items');

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
            updateLocalStorage();
        })
        .on('add', function () {
            updateLocalStorage();
        })
        .on('remove', function () {
            updateLocalStorage();
        });

    addItemsElement.addEventListener('click', addItem);
    gridElement.addEventListener('click', function (e) {
        if (elementMatches(e.target, '.card-remove, .card-remove i')) {
            removeItem(e);
        }
    });

}

/**
 *
 */
function updateLocalStorage() {
    localStorage.setItem(getLocalStorageId(), JSON.stringify(getCurrentConfig()));
    updateConfigPanel();
}

/**
 *
 */
function updateConfigPanel() {
    let badge = document.getElementById('badge-config');
    let description = document.getElementById('config-text');
    let config = document.getElementById('config');

    if (isLocalConfigEqualsJenkinsfile()) {
        localStorage.removeItem(getLocalStorageId());
        badge.innerHTML = 'Jenkinsfile';
        badge.classList.remove('badge-danger');
        badge.classList.add('badge-success');
        description.innerHTML = 'The configuration setting in your Jenkinsfile is up to date with local changes!';
        config.innerHTML = syntaxHighlight(JSON.stringify(this.configuration, null, 3));
    } else {
        badge.innerHTML = 'Local Storage';
        badge.classList.remove('badge-success');
        badge.classList.add('badge-danger');
        description.innerHTML = 'The configuration setting in your Jenkinsfile will be overwritten by your local changes.' +
            'To save this permanently, copy the json below and replace the <code>configuration</code> in the Jenkinsfile with it.';
        config.innerHTML = syntaxHighlight(JSON.stringify(getCurrentConfig(), null, 3));
    }
}

/**
 *
 * @param json
 * @returns {*}
 */
function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        let cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

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
 */
function loadGrid() {

    const hasConfigStored = localStorage.getItem(getLocalStorageId()) !== null;
    let items = hasConfigStored ? JSON.parse(localStorage.getItem(getLocalStorageId())).plugins : this.configuration.plugins;
    console.log(items);

    Object.keys(items).forEach(function(key) {
        const plugin = items[key];
        const item = generateItem([plugin.width, plugin.height], plugin.color, key);
        grid.add(item, { active: false });
    });

    grid.show(grid.getItems());

}

/**
 *
 * @returns {string}
 */
function getLocalStorageId() {
    const project = `${window.location.pathname.split('/').slice(3, 4)}.monitoring-grid-order`
        .toLowerCase();
    return decodeURI(project).replaceAll(" ", "-");
}

/**
 *
 * @returns {any}
 */
function getCurrentConfig() {

    let config = '{"plugins": {'

    grid.getItems().forEach(function(item, index) {
        const id = item.getElement().getAttribute('data-id');
        const width = Math.round(item.getWidth() / 120);
        const height = Math.round(item.getHeight() / 120);
        const color = item.getElement().getAttribute('data-color')

        config += `"${id}": {"width":${width},"height":${height},"color":"${color}"}`

        if (index !== grid.getItems().length - 1) {
            config += ', ';
        }
    });

    config += '}}';

    return JSON.parse(config);

}

/**
 *
 * @returns {boolean}
 */
function isLocalConfigEqualsJenkinsfile() {

    return JSON.stringify(this.configuration) === JSON.stringify(getCurrentConfig());

}


/**
 *
 */
function copyConfig() {

    navigator.clipboard.writeText(document.getElementById('config').innerText);

}

