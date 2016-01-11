package br.net.mirante.singular.bamclient.chart;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.net.mirante.singular.bamclient.builder.SingularChartBuilder;
import br.net.mirante.singular.bamclient.builder.amchart.AmChartCategoryAxis;
import br.net.mirante.singular.bamclient.builder.amchart.AmChartCursor;
import br.net.mirante.singular.bamclient.builder.amchart.AmChartGraph;
import br.net.mirante.singular.bamclient.builder.amchart.AmChartLegend;
import br.net.mirante.singular.bamclient.builder.amchart.AmChartValueAxes;
import br.net.mirante.singular.bamclient.builder.amchart.AmChartValueField;
import br.net.mirante.singular.bamclient.builder.amchart.AmSerialChartBuilder;
import br.net.mirante.singular.bamclient.portlet.PortletFilterContext;

public abstract class AbstractSerialChart implements SingularChart {

    final protected ChartDataProvider dataProvider;
    final protected String category;
    final protected List<AmChartValueField> values;

    protected boolean withLegend = false;

    public AbstractSerialChart(ChartDataProvider dataProvider, List<AmChartValueField> values, String category) {
        this.dataProvider = dataProvider;
        this.values = values;
        this.category = category;
    }

    @Override
    public String getDefinition(PortletFilterContext filter) {
        final AmSerialChartBuilder chartBuilder = new SingularChartBuilder()
                .newSerialChart()
                .theme("light")
                .dataProvider(dataProvider, filter)
                .valueAxes(Collections.singletonList(new AmChartValueAxes()
                        .gridColor("#FFFFFF")
                        .gridAlpha(0.2)
                        .dashLength(0)))
                .gridAboveGraphs(true)
                .startDuration(1)
                .graphs(getGraphs())
                .chartCursor(new AmChartCursor()
                        .categoryBalloonEnabled(false)
                        .cursorAlpha(0)
                        .zoomable(false))
                .categoryField(category)
                .categoryAxis(new AmChartCategoryAxis()
                        .gridPosition("start")
                        .gridAlpha(0)
                        .tickPosition("start")
                        .tickLength(20)
                        .autoWrap(true));

        if (withLegend) {
            chartBuilder.legend(new AmChartLegend().useGraphSettings(true));
        }

        return chartBuilder.finish();
    }

    public void withLegend() {
        withLegend = true;
    }

    protected abstract Collection<AmChartGraph> getGraphs();

}
