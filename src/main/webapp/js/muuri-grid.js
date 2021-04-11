/**
 * Custom javascript method to handle the dashboard for each build action.
 *
 * @author Simon Symhoven
 */

let grid;
let configuration;

/**
 * Get the local storage id to store the configuration for based on selected build action.
 *
 * @returns {string}
 *          the local storage id of current build action.
 */
function getLocalStorageId() {
    const project = `${window.location.pathname.split('/').slice(3, 4)}.monitoring-grid-order`
        .toLowerCase();
    return decodeURI(project).replaceAll(" ", "-");
}

/**
 * Gets the current dashboard configuration as json.
 *
 * @returns {json}
 *          the dashboard configuration as json.
 */
function getCurrentConfig() {

    let config = '{"plugins": {';

    grid.getItems().forEach(function(item, index) {
        const id = item.getElement().getAttribute('data-id');
        const width = Math.round(item.getWidth() / 120);
        const height = Math.round(item.getHeight() / 120);
        const color = item.getElement().getAttribute('data-color');

        config += `"${id}": {"width":${width},"height":${height},"color":"${color}"}`;

        if (index !== grid.getItems().length - 1) {
            config += ', ';
        }
    });

    config += '}}';

    return JSON.parse(config);

}

/**
 * Check if the current dashboard configuration is equal the the configuration
 * from Jenkinsfile.
 *
 * @returns {boolean}
 *          true, if current dashboard configuration equals configuration from
 *          Jenkinsfile, false else.
 */
function isLocalConfigEqualsJenkinsfile() {

    return JSON.stringify(this.configuration) === JSON.stringify(getCurrentConfig());

}

/**
 * Copies the current dashboard configuration to clipboard.
 */
function copyConfig() {

    navigator.clipboard.writeText(document.getElementById('config').innerText);

}

/**
 * Find the closest element (parent node of one grid slot) based on a specific element.
 *
 * @param element
 *              the clicked element (remove button of one grid slot).
 *
 * @param selector
 *              the selector of parent node to find (e.g. '.muuri-item').
 *
 * @returns {*|null}
 *              the parent node with specific selector of the clicked element.
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

    return element.closest(selector);

}

/**
 * Checks if an element matches a specific selector (e.g. class selector).
 *
 * @param element
 *          the element to be checked.
 *
 * @param selector
 *          the selector that should match.
 *
 * @returns {boolean}
 *          true if element matches selector, false else.
 */
function elementMatches(element, selector) {

    const p = Element.prototype;
    return (p.matches || p.matchesSelector || p.webkitMatchesSelector || p.mozMatchesSelector || p.msMatchesSelector || p.oMatchesSelector).call(element, selector);

}

/**
 * Highlights a json with specific style set.
 *
 * @param json
 *          the json to highlight.
 *
 * @returns {*}
 *          the highlighted json.
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
 * Updated the config panel on the right side of a build action
 * with information about current configuration compared to the default
 * configuration from Jenkinsfile.
 */
function updateConfigPanel() {

    let badge = document.getElementById('badge-config');
    let description = document.getElementById('config-text');
    let config = document.getElementById('config');
    let configButton = document.getElementById('config-button');

    if (isLocalConfigEqualsJenkinsfile()) {
        localStorage.removeItem(getLocalStorageId());
        badge.innerHTML = 'Jenkinsfile';
        badge.classList.remove('badge-danger');
        badge.classList.add('badge-success');
        description.innerHTML = 'The configuration setting in your Jenkinsfile is up to date with local changes!';
        config.innerHTML = syntaxHighlight(JSON.stringify(this.configuration, null, 3));
        configButton.classList.remove('btn-danger');
        configButton.classList.add('btn-success');
    } else {
        badge.innerHTML = 'Local Storage';
        badge.classList.remove('badge-success');
        badge.classList.add('badge-danger');
        description.innerHTML = 'The configuration setting in your Jenkinsfile will be overwritten by your local changes.' +
            'To save this permanently, copy the json below and replace the <code>configuration</code> in the Jenkinsfile with it.';
        config.innerHTML = syntaxHighlight(JSON.stringify(getCurrentConfig(), null, 3));
        configButton.classList.add('btn-success');
        configButton.classList.remove('btn-success');
        configButton.classList.add('btn-danger');
    }

}

/**
 *  Updates the local storage with the current grid config form dashboard.
 */
function updateLocalStorage() {
    localStorage.setItem(getLocalStorageId(), JSON.stringify(getCurrentConfig()));
    updateConfigPanel();
}

/**
 * Get the checked value of a input selection wrapped in a form.
 *
 * @param values
 *          all possible input values.
 *
 * @returns {*}
 *          the checked value.
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
 * Removes an element from grid.
 *
 * @param e
 *          the clicked element (element to be deleted).
 */
function removeItem(e) {

    const items = grid.getItems();
    const elem = elementClosest(e.target, '.muuri-item');
    const index = items.findIndex((e) => { return e._element === elem });
    const elemToRemove = items.slice(index, index + 1);

    grid.hide(elemToRemove, {onFinish: function (items) {
            grid.remove(items, {removeElements: true});
        }});

}

/**
 * Generates the html code for one grid slot based on input.
 *
 * @param size
 *          the site of the slot as array of [width, height]
 *
 * @param color
 *          the color of the slot.
 *
 * @param plugin
 *          the name of the plugin.
 *
 * @returns {ChildNode}
 *          the generated grid slot (the first child node).
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
        '<div class="card-remove"><i class="material-icons icon">&#xE5CD;</i></div>' +
        '</div>' +
        '</div>' +
        '</div>';

    return itemElem.firstChild;

}

/**
 *  Event listener for 'Add item' button. Adds a new item to grid, based
 *  on user selection of the corresponding modal.
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
 * Initialise the grid. Adds all event listener to grid methods.
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
        dragStartPredicate: (item, event) => {
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
        .on('layoutEnd', function () {
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
 *  Load the grid slots either form default configuration or from local storage
 *  if present.
 */
function loadGrid() {

    const hasConfigStored = localStorage.getItem(getLocalStorageId()) !== null;
    let items = hasConfigStored ? JSON.parse(localStorage.getItem(getLocalStorageId())).plugins : this.configuration.plugins;

    Object.keys(items).forEach(function(key) {
        const plugin = items[key];
        const item = generateItem([plugin['width'], plugin['height']], plugin['color'], key);
        grid.add(item, { active: false });
    });

    grid.show(grid.getItems());

}

/**
 * Entry point for grid initialisation. Initialise the grid, load the grid slots and updates the
 * config panel on the right side of build action.
 *
 * @param configuration
 *          the default configuration from Jenkinsfile.
 */
function initDashboard(configuration) {

    this.configuration = JSON.parse(configuration);

    initGrid();

    loadGrid();

    updateConfigPanel();

}