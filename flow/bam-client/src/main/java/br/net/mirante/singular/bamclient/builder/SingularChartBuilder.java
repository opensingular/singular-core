package br.net.mirante.singular.bamclient.builder;


import br.net.mirante.singular.bamclient.builder.amchart.AmSerialChartBuilder;

public class SingularChartBuilder extends AbstractJSONBuilder<SingularChartBuilder> {

    public SingularChartBuilder() {
        super(new JSONBuilderContext());
    }

    public AmSerialChartBuilder newSerialChart() {
        context.getjWriter().object();
        return new AmSerialChartBuilder(context);
    }

    @Override
    public SingularChartBuilder self() {
        return this;
    }
}
