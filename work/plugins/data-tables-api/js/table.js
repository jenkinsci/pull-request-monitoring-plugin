/* global jQuery3, luxon, tableDataProxy */
jQuery3(document).ready(function () {
    /**
     * Binds all tables that have the class 'data-table' to a new JQuery DataTables instance.
     */
    function bindTables() {
        /**
         * Creates the data table instance for the specified table element.
         */
        function createDataTable(table) {
            const defaultConfiguration = {
                language: {
                    emptyTable: 'Loading - please wait ...'
                },
                deferRender: true,
                pagingType: 'numbers', // page number button only
                order: [[1, 'asc']], // default order, if not persisted yet
                columnDefs: [
                    {
                        targets: 'nosort', // All columns with the '.nosort' class in the <th>
                        orderable: false
                    },
                    {
                        targets: 'text-right', // All columns with the '.text-right' class in the <th>
                        className: 'text-right'
                    },
                    {
                        targets: 'date', // All columns with the '.date' class in the <th>
                        render: function (data, type, _row, _meta) {
                            if (type === 'display') {
                                if (data === 0) {
                                    return '-';
                                }
                                var dateTime = luxon.DateTime.fromMillis(data * 1000);
                                return '<span data-toggle="tooltip" data-placement="bottom" title="'
                                    + dateTime.toLocaleString(luxon.DateTime.DATETIME_SHORT) + '">'
                                    + dateTime.toRelative({locale: 'en'}) + '</span>';
                            }
                            else {
                                return data;
                            }
                        }
                    },
                    {
                        targets: 'hidden', // All columns with the '.hidden' class in the <th>
                        visible: false,
                        searchable: true,
                        orderable: false // There is no point in allowing sort by this column if it's not visible.
                    }
                ],
                columns: JSON.parse(table.attr('data-columns-definition'))
            };
            const tableConfiguration = JSON.parse(table.attr('data-table-configuration'));
            // overwrite/merge the default configuration with values from the provided table configuration
            const dataTable = table.DataTable(Object.assign(defaultConfiguration, tableConfiguration));

            // add the buttons to the top of the table
            if (tableConfiguration.buttons) {
                dataTable
                    .buttons()
                    .container()
                    .addClass('float-none mb-3')
                    .prependTo(jQuery3(dataTable.table().container()).closest('.table-responsive'));
            }

            return dataTable;
        }

        /**
         * Loads the content for the specified table element via an Ajax call.
         */
        function loadTableData(table, dataTable) {
            if (!table[0].hasAttribute('isLoaded')) {
                table.attr('isLoaded', 'true');
                tableDataProxy.getTableRows(table.attr('id'), function (t) {
                    (function () {
                        const model = JSON.parse(t.responseObject());
                        dataTable.rows.add(model).draw();
                        jQuery3('[data-toggle="tooltip"]').tooltip();
                    })();
                });
            }
        }

        const allTables = jQuery3('table.data-table');

        function toggleDetailsColumnIcon(tr, from, to) {
            const svg = tr.find("use");
            const current = svg.attr("href");
            svg.attr("href", current.replace(from, to))
        }

        allTables.each(function () {
            const table = jQuery3(this);
            const id = table.attr('id');
            const dataTable = createDataTable(table);

            // Add event listener for opening and closing details
            table.on('click', 'div.details-control', function () {
                const tr = jQuery3(this).parents('tr');
                const row = dataTable.row(tr);

                if (row.child.isShown()) {
                    row.child.hide();
                    tr.removeClass('shown');
                    toggleDetailsColumnIcon(tr, "minus-circle", "plus-circle");
                }
                else {
                    row.child(jQuery3(this).data('description')).show();
                    tr.addClass('shown');
                    toggleDetailsColumnIcon(tr, "plus-circle", "minus-circle");
                }
            });

            if (table.is(":visible")) {
                loadTableData(table, dataTable);
            }
            else {
                table.on('becameVisible', function () {
                    loadTableData(table, dataTable);
                });
            }

            // Add event listener that stores the order a user selects
            table.on('order.dt', function () {
                const order = table.DataTable().order();
                localStorage.setItem(id + '#orderBy', order[0][0]);
                localStorage.setItem(id + '#orderDirection', order[0][1]);
            });

            /**
             * Restores the order of every table by reading the local storage of the browser.
             * If no order has been stored yet, the table is skipped.
             * Also saves the default length of the number of table columns.
             */
            const orderBy = localStorage.getItem(id + '#orderBy');
            const orderDirection = localStorage.getItem(id + '#orderDirection');
            if (orderBy && orderDirection) {
                const order = [orderBy, orderDirection];
                try {
                    dataTable.order(order).draw();
                }
                catch (ignore) { // TODO: find a way to determine the number of columns here
                    dataTable.order([[1, 'asc']]).draw();
                }
            }

            // Store paging size
            table.on('length.dt', function (e, settings, len) {
                localStorage.setItem(id + '#table-length', len);
            });
            const storedLength = localStorage.getItem(id + '#table-length');
            if (jQuery3.isNumeric(storedLength)) {
                dataTable.page.len(storedLength).draw();
            }
        });
    }

    bindTables();
});
