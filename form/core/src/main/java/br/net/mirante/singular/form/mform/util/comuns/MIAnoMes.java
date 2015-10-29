package br.net.mirante.singular.form.mform.util.comuns;

import java.time.YearMonth;

import br.net.mirante.singular.form.mform.core.MIComparable;

public class MIAnoMes extends MIComparable<YearMonth> {

    public MIAnoMes() {
    }

    public YearMonth getJavaYearMonth() {
        if (isEmptyOfData()) {
            return null;
        }

        return YearMonth.of(getAno(), getMes());
    }

    public Integer getAno() {
        if (isEmptyOfData()) {
            return null;
        }
        return getValor().getYear();
    }

    public Integer getMes() {
        if (isEmptyOfData()) {
            return null;
        }
        return getValor().getMonthValue();
    }

    @Override
    public MTipoAnoMes getMTipo() {
        return (MTipoAnoMes) super.getMTipo();
    }
}
