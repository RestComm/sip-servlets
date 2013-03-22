$(document).ready(function() {
	var factory = new JmxChartsFactory();	
	factory.create([
		{
			type: 'read',
			name: 'java.lang:type=Memory',
			attribute: 'HeapMemoryUsage',
			path: 'committed'
		},
		{
			type: 'read',
			name: 'java.lang:type=Memory',
			attribute: 'HeapMemoryUsage',
			path: 'used'
		}
	]);
	factory.create({
			type: 'read',
			name: 'java.lang:type=OperatingSystem',
			attribute: 'SystemLoadAverage'
	});
	factory.create({
		type:		'read',
		name:		'java.lang:type=Threading',
		attribute:	'ThreadCount'
	});
});

function JmxChartsFactory(keepHistorySec, pollInterval, columnsCount) {
	var series = [];
	var monitoredMbeans = [];
	var chartsCount = 0;

	// if not given a value for number of columns, use what fits.
	columnsCount = columnsCount || Math.floor($(window).width()/$(".column").width());
	pollInterval = pollInterval || 1000;
	var keepPoints = 30;

	setupPortletsContainer(columnsCount);

	setInterval(function() {
		pollAndUpdateCharts();
	}, pollInterval);

	this.create = function(mbeans) {
		mbeans = $.makeArray(mbeans);
		series = series.concat(createChart(mbeans).series);
		monitoredMbeans = monitoredMbeans.concat(mbeans);
	};

	function pollAndUpdateCharts() {
		var requests = prepareBatchRequest();
		var responses = jolokia.request(requests);
		updateCharts(responses);
	}

	function createNewPortlet(name) {
		return $('#portlet-template')
				.clone(true)
				.appendTo($('.column')[chartsCount++ % columnsCount])
				.removeAttr('id')
				.find('.title').text((name.length > 50 ? '...' : '') + name.substring(name.length - 50, name.length)).end()
				.find('.portlet-content')[0];
	}

	function setupPortletsContainer() {
		var column = $('.column');
		for(var i = 1; i < columnsCount; ++i){
			column.clone().appendTo(column.parent());
		}
		$(".column").sortable({
			connectWith: ".column"
		});

		$(".portlet-header .ui-icon").click(function() {
			$(this).toggleClass("ui-icon-minusthick").toggleClass("ui-icon-plusthick");
			$(this).parents(".portlet:first").find(".portlet-content").toggle();
		});
		$(".column").disableSelection();
	}

	function prepareBatchRequest() {
		return $.map(monitoredMbeans, function(mbean) {
			switch(mbean.type) {
				case 'read':
					return {
						type: mbean.type,
						opts: mbean.args,
						mbean: mbean.name,
						attribute: mbean.attribute,
						path: mbean.path
					};
					break;
				case 'exec':
					return {
						type: mbean.type,
						arguments: mbean.args,
						mbean: mbean.name,
						operation: mbean.operation,
						path: mbean.path
					};
					break;
			}
		});
	}

	function updateCharts(responses) {
		var curChart = 0;
		$.each(responses, function() {
			var point = {
				x: this.timestamp * 1000,
				y: parseFloat(this.value)
			};
			var curSeries = series[curChart++];
			curSeries.addPoint(point, true, curSeries.data.length >= keepPoints);
		});
	}

	function createChart(mbeans) {
		return new Highcharts.Chart({
			chart: {
				renderTo: createNewPortlet(mbeans[0].name),
				height: 250,
				animation: true,
				defaultSeriesType: 'spline',
				shadow: true
			},
			title: { text: null },
			xAxis: { type: 'datetime' },
			yAxis: {
				title: { text: mbeans[0].attribute || mbeans[0].operation }
			},
			legend: {
				enabled: true,
				borderWidth: 0
			},
			credits: {enabled: false},
			exporting: { enabled: true },
			plotOptions: {
				area: {
					marker: {
						enabled: false
					}
				}
			},
			series: $.map(mbeans, function(mbean) {
				return {
					data: [],
					name: mbean.path || mbean.attribute || mbean.args
				}
			})
		})
	}
}