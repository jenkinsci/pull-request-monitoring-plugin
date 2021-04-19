/**
 * Custom javascript method to handle the dashboard for each build action.
 *
 * @author Simon Symhoven
 */


/* global jQuery3 */
(function ($) {
    let grid;
    let configuration;

    /**
     * Trigger window resize event to resize echarts.
     */
    function resize() {

        $(document).ready(function() {
            $(window).trigger('resize');
        });

    }

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

        let plugins = grid.getItems().filter(function(item) {
            return item.isActive();
        }).map(function(item) {
            const id = item.getElement().getAttribute('data-id');
            const width = Math.round(item.getWidth() / 120);
            const height = Math.round(item.getHeight() / 120);
            const color = item.getElement().getAttribute('data-color');

            return `"${id}": {"width":${width},"height":${height},"color":"${color}"}`;
        }).join(', ');

        const config = `{"plugins": { ${plugins} }}`;

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

        return JSON.stringify(configuration) === JSON.stringify(getCurrentConfig());

    }

    /**
     * Copies the current dashboard configuration to clipboard.
     */
    function copyConfig() {

        navigator.clipboard.writeText(document.getElementById('config').innerText);

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
        return (p.matches || p.matchesSelector || p.webkitMatchesSelector || p.mozMatchesSelector
            || p.msMatchesSelector || p.oMatchesSelector).call(element, selector);

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
     * Highlights a json based on given ccs style.
     *
     * @param json
     *          the json to highlight.
     *
     * @returns {*}
     *          the html code of highlighted json.
     */
    function syntaxHighlight(json) {
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        const reg = /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g;
        return json.replace(reg, function (match) {
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
        let badge2 = document.getElementById('badge-config-type');
        let description = document.getElementById('config-text');
        let config = document.getElementById('config');
        let configButton = document.getElementById('config-button');

        if (Object.keys(configuration.plugins).length === 0) {
            badge2.innerHTML = 'default';
            badge2.classList.remove('badge-success');
            badge2.classList.add('badge-danger');
        } else {
            badge2.innerHTML = 'custom';
            badge2.classList.remove('badge-danger');
            badge2.classList.add('badge-success');
        }

        if (isLocalConfigEqualsJenkinsfile()) {
            localStorage.removeItem(getLocalStorageId());
            badge.innerHTML = 'Jenkinsfile';
            badge.classList.remove('badge-danger');
            badge.classList.add('badge-success');
            description.innerHTML = '<p>The configuration setting in your Jenkinsfile is up to date with local changes!</p>';
            config.innerHTML = syntaxHighlight(JSON.stringify(configuration, null, 3));
            configButton.classList.remove('btn-danger');
            configButton.classList.add('btn-success');
        } else {
            badge.innerHTML = 'Local Storage';
            badge.classList.remove('badge-success');
            badge.classList.add('badge-danger');
            description.innerHTML = '<p>The configuration setting in your Jenkinsfile will be overwritten ' +
                'by your local changes. To save this permanently, copy the json below and replace the ' +
                '<code>configuration</code> in the Jenkinsfile with it.</p>';
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
     * Disables or enables an input[name="plugin"] by its id.
     *
     * @param id
     *          the id of plugin to set the state.
     *
     * @param disabled
     *          the state to set.
     */
    function changeInput(id, disabled) {

        let plugins = document.querySelectorAll('input[name="plugin"]');
        Array.from(plugins).filter(function(item) {
            return item.value === id;
        }).map(function(item) {

            if (disabled === 'true') {
                item.setAttribute('disabled', disabled);
            } else {
                item.removeAttribute('disabled');
            }

        });

    }

    /**
     * Removes an element from grid.
     * @param e
     *          the clicked element (element to be deleted).
     */
    function removeItem(e) {

        const items = grid.getItems();
        const elem = elementClosest(e.target, '.muuri-item');
        const index = items.findIndex((e) => { return e._element === elem; });
        const elemToRemove = items.slice(index, index + 1);

        grid.hide(elemToRemove, {onFinish: () => {
                changeInput(elem.getAttribute('data-id'), 'false');
                updateLocalStorage();
            }});

    }

    /**
     *  Event listener for 'Add item' button. Adds a new item to grid, based
     *  on user selection of the corresponding modal.
     */
    function addItem() {

        const color = document.querySelector('input[name="color"]:checked').value;
        const width = document.querySelector('input[name="width"]').value;
        const height = document.querySelector('input[name="height"]').value;
        const plugin = document.querySelector('input[name="plugin"]:checked').value;

        let plugins = grid.getItems().filter(function(item) {
            const dataId = item.getElement().getAttribute('data-id');
            if (dataId === plugin) {
                const oldColor = item.getElement().getAttribute('data-color');
                item.getElement().classList.remove(oldColor);

                item.getElement().setAttribute('data-color', color);
                item.getElement().classList.add(color);

                item.getElement().classList.remove('w1', 'w2', 'w3', 'w4', 'w5');
                item.getElement().classList.add('w' + width);

                item.getElement().classList.remove('h1', 'h2', 'h3', 'h4', 'h5');
                item.getElement().classList.add('h' + height);

                return true;
            }

            return false;
        });

        changeInput(plugin, 'true');
        grid.show(plugins);

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
        let copy = document.getElementById('copy');
        let addItemsElement = document.querySelector('.add-more-items');

        grid = new Muuri(gridElement, {
            layoutDuration: 400,
            layoutEasing: 'ease',
            dragEnabled: true,
            dragSortInterval: 50,
            dragContainer: document.body,
            dragStartPredicate: (item, event) => {
                const isDraggable = true;
                const isRemoveAction = elementMatches(event.target, '.plugin-remove, .plugin-remove i');
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
                resize();
            });

        addItemsElement.addEventListener('click', addItem);
        gridElement.addEventListener('click', function (e) {
            if (elementMatches(e.target, '.plugin-remove, .plugin-remove i')) {
                removeItem(e);
            }
        });
        copy.addEventListener('click', copyConfig);

    }

    /**
     * Checks if a configuration is stored with the local storage id in local storage.
     *
     * @returns {boolean}
     *          if a configuration is stored, false else.
     */
    function isConfigStored() {
        return localStorage.getItem(getLocalStorageId()) !== null;
    }

    /**
     *  Load the grid slots either form default configuration or from local storage
     *  if present.
     */
    function loadGrid() {

        let config = isConfigStored() ?
            JSON.parse(localStorage.getItem(getLocalStorageId())).plugins : configuration.plugins;

        // Hide all elements
        grid.hide(grid.getItems(), {instant: true});

        // Remove html .hidden class
        let pluginsToHide = document.getElementsByClassName('muuri-item');

        for (let plugin of pluginsToHide) {
            plugin.classList.remove('hidden');
        }

        let plugins = [];

        Object.keys(config).forEach((id) => {
            let plugin = grid.getItems().find(function (item) {
                return item.getElement().getAttribute('data-id') === id;
            });

            const color = plugin.getElement().getAttribute('data-color');
            plugin.getElement().classList.remove(color);

            const newColor = config[String(id)].color;
            plugin.getElement().setAttribute('data-color', newColor);
            plugin.getElement().classList.add(newColor);

            const width = config[String(id)].width;
            plugin.getElement().classList.remove('w1', 'w2', 'w3', 'w4', 'w5');
            plugin.getElement().classList.add('w' + width);

            const height = config[String(id)].height;
            plugin.getElement().classList.remove('h1', 'h2', 'h3', 'h4', 'h5');
            plugin.getElement().classList.add('h' + height);

            changeInput(id, 'true');
            plugins.push(plugin);

        });

        // Merge DOM elements with elements to show and sort it
        let pluginsToSort = [...plugins];
        grid.getItems().forEach(function(item) {
            const dataId = item.getElement().getAttribute('data-id');

            let plugin = plugins.find(function (item) {
                return item.getElement().getAttribute('data-id') === dataId;
            });

            if (!plugin) {
                pluginsToSort.push(item);
            }
        });

        grid.sort(pluginsToSort);
        grid.show(plugins);

    }

    /**
     * Entry point for grid initialisation. Initialise the grid, load the grid slots and updates the
     * config panel on the right side of build action.
     *
     * @param config
     *          the default configuration from Jenkinsfile.
     */
    function initDashboard(config) {

        configuration = JSON.parse(config);

        initGrid();

        loadGrid();

        updateConfigPanel();

    }

    run.getConfiguration(function(config) {
        initDashboard(config.responseJSON);
    });

    /**
     * Form validation.
     */
    $(document).ready(function() {
        $('#submitButton').attr('disabled', 'disabled');

        $('input[name=plugin]').on('change', function() {

            const plugin = document.querySelector('input[name="plugin"]:checked');

            if (plugin.value !== 'default') {
                $('#submitButton').removeAttr("disabled");
            }
            else {
                $('#submitButton').attr('disabled', 'disabled');
            }
        });
    });

    /**
     * Range slider value changes.
     */
    $(document).ready(function() {
        let slider = $('.range-slider');
        let range = $('.range-slider-range');
        let value = $('.range-slider-value');

        slider.each(function(){

            value.each(function(){
                let value = jQuery3(this).prev().attr('value');
                jQuery3(this).html(value);
            });

            range.on('input', function(){
                jQuery3(this).next(value).html(this.value);
            });
        });
    });

})(jQuery3);