/* global jQuery3, echarts, view */

(function($) {

    view.getId(function(id) {
        const chartDom = document.getElementById(id.responseObject());
        const myChart = echarts.init(chartDom);

        const data = [{
            name: 'All Severeties',
            children: [{
                name: 'Low-Prio\nWarning',
                value: 15,
                children: [{
                    name: 'New',
                    value: 1
                }, {
                    name: 'Outstanding',
                    value: 10
                }, {
                    name: 'Fixed',
                    value: 4
                }]
            }, {
                name: 'Normal-Prio\nWarning',
                value: 20,
                children: [{
                    name: 'New',
                    value: 1
                }, {
                    name: 'Outstanding',
                    value: 10
                }, {
                    name: 'Fixed',
                    value: 9
                }]
            }, {
                name: 'High-Prio\nWarning',
                value: 15,
                children: [{
                    name: 'New',
                    value: 10
                }, {
                    name: 'Outstanding',
                    value: 3
                }, {
                    name: 'Fixed',
                    value: 2
                }]
            }, {
                name: 'Errors',
                value: 10,
                children: [{
                    name: 'New',
                    value: 5
                }, {
                    name: 'Outstanding',
                    value: 3
                }, {
                    name: 'Fixed',
                    value: 2
                }]
            }]
        }];

        const option = {
            visualMap: {
                type: 'continuous',
                min: 0,
                max: 20,
                inRange: {
                    color: ['#A5D6A7', '#FFF59D', '#EF9A9A']
                }
            },
            series: {
                type: 'sunburst',
                data: data,
                radius: [0, '90%'],
                label: {
                    rotate: 'horizontal',

                }
            },
            tooltip: {
                trigger: 'item',
                formatter: '{b}: {c}'
            }
        };

        option && myChart.setOption(option);
    });

})(jQuery3);