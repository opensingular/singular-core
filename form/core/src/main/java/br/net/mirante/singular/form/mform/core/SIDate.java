package br.net.mirante.singular.form.mform.core;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

import br.net.mirante.singular.form.mform.SISimple;

public class SIDate extends SISimple<Date> implements SIComparable<Date> {
    public SIDate() {
    }

    public Date getDate() {
        return (Date) getValue();
    }

    public YearMonth getJavaYearMonth() {
        return YearMonth.from(getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

}
