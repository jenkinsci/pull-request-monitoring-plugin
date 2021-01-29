/* global echarts, EChartsJenkinsApi */
/**
 * Renders a trend chart in the specified div using ECharts.
 *
 * @param {String} chartDivId - the ID of the div where the chart should be shown in
 * @param {String} enableLinks - determines if the chart is clickable. If the chart is clickable, then clicking on a
 *     chart will open the results of the selected build.
 * @param {Object} ajaxProxy - AJAX proxy of the endpoint in Jenkins Java model object
 */
EChartsJenkinsApi.prototype.renderTrendChart = function (chartDivId, enableLinks, ajaxProxy) {
    /**
     * Renders a trend chart in the specified div using ECharts.
     *
     * @param {HTMLElement} chartPlaceHolder - the div where the chart should be shown in
     * @param {Object} chart - the ECharts instance
     * @param {String} model - the line chart model
     * @param {Boolean} enableOnClickHandler - to enable clicking on the chart to see the results
     */
     function render(chartPlaceHolder, chart, model, enableOnClickHandler) { // eslint-disable-line no-unused-vars
        const chartModel = JSON.parse(model);
        let selectedBuild; // the tooltip formatter will change this value while hoovering

        if (enableOnClickHandler) {
            const urlName = chartPlaceHolder.getAttribute("tool");
            if (urlName) {
                chartPlaceHolder.onclick = function () {
                    if (urlName && selectedBuild > 0) {
                        window.location.assign(selectedBuild + '/' + urlName);
                    }
                };
            }
        }

        const textColor = getComputedStyle(document.body).getPropertyValue('--text-color') || '#333';

        const options = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                },
                formatter: function (params, ticket, callback) {
                    if (params.componentType === 'legend') {
                        selectedBuild = 0;
                        return params.name;
                    }

                    const builds = chartModel.buildNumbers;
                    const labels = chartModel.domainAxisLabels;
                    for (let i = 0; i < builds.length; i++) {
                        if (params[0].name === labels[i]) {
                            selectedBuild = builds[i];
                            break;
                        }
                    }

                    let text = 'Build ' + params[0].name.escapeHTML();
                    for (let i = 0, l = params.length; i < l; i++) {
                        text += '<br/>' + params[i].marker + params[i].seriesName + ' : ' + params[i].value;
                    }
                    text += '<br />';
                    return '<div style="text-align:left">' + text + '</div>';
                }
            },
            legend: {
                orient: 'horizontal',
                type: 'scroll',
                x: 'center',
                y: 'top',
                textStyle: {
                    color: textColor
                }
            },
            grid: {
                left: '20',
                right: '10',
                bottom: '10',
                top: '30',
                containLabel: true
            },
            xAxis: [{
                type: 'category',
                boundaryGap: false,
                data: chartModel.domainAxisLabels,
                axisLabel: {
                    color: textColor
                }
            }
            ],
            yAxis: [{
                type: 'value',
                axisLabel: {
                    color: textColor
                }
            }
            ],
            series: chartModel.series
        };
        chart.hideLoading();
        chart.setOption(options);
        chart.on('legendselectchanged', function (params) {
            selectedBuild = 0; // clear selection to avoid navigating to the selected build
        });
        chart.resize();
        window.onresize = function () {
            chart.resize();
        };
    }

    const chartPlaceHolder = document.getElementById(chartDivId);
    const chart = echarts.init(chartPlaceHolder);
    chart.showLoading();
    chartPlaceHolder.echart = chart;

    ajaxProxy.getBuildTrendModel(function (trendModel) {
        render(chartPlaceHolder, chart, trendModel.responseJSON, !!(enableLinks && enableLinks !== "false"));
    });
}
