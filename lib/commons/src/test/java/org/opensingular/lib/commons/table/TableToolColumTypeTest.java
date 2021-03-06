/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.lib.commons.table;

import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Testa o comportamento de diferentes tipos de colunas
 *
 * @author Daniel C. Bordin on 23/04/2017.
 */
public class TableToolColumTypeTest {

    @Test
    public void testNumber_allZeros() {
        TableTool table = createTableWithNumber(null);
        TableOutputSimulated output = generate(table, p -> {
            p.insertLine(0, 0, 0, 0, 0);
        });

        output.getResult().assertLinesSize(2);
        output.getResult().assertLine(0, "Number", "Integer", "Money", "Percent", "Hour");
        output.getResult().assertLine(1, null, null, null, null, null);
    }

    @Test
    public void testNumber_allZeros_withShowZero() {
        TableTool table = createTableWithNumber(c -> c.setShowZero());
        TableOutputSimulated output = generate(table, p -> {
            p.insertLine(0, 0, 0, 0, 0);
        });

        output.getResult().assertLinesSize(2);
        output.getResult().assertLine(0, "Number", "Integer", "Money", "Percent", "Hour");
        output.getResult().assertLine(1, "0,00", "0", "0,00", "0,0%", "0:00");
    }

    @Test
    public void testNumber_setPrecision() {
        TableTool table = createTableWithNumber(c -> c.setFractionDigits(3));
        TableOutputSimulated output = generate(table, p -> {
            p.insertLine(1.2345, 1.2345, 1.2345, 1.2345, 1.2345);
        });

        output.getResult().assertLinesSize(2);
        output.getResult().assertLine(0, "Number", "Integer", "Money", "Percent", "Hour");
        output.getResult().assertLine(1, "1,234", "1,234", "1,234", "123,450%", "0:01");
    }

    @Test
    public void testNumber_setBigNumber() {
        TableTool table = createTableWithNumber(null);
        TableOutputSimulated output = generate(table, p -> {
            p.insertLine(1234.5, 1234.5, 1234.5, 1234.5, 1234.5);
        });

        output.getResult().assertLinesSize(2);
        output.getResult().assertLine(0, "Number", "Integer", "Money", "Percent", "Hour");
        output.getResult().assertLine(1, "1.234,50", "1.234", "1.234,50", "123.450,0%", "20:35");
    }

    @Test
    public void testBoolean() {
        assertOneCellResult(ColumnType.BOOLEAN, null, Boolean.TRUE, "Sim");
        assertOneCellResult(ColumnType.BOOLEAN, null, Boolean.FALSE, "Não");
        assertOneCellResult(ColumnType.BOOLEAN, null, null, null);
        assertOneCellResult(ColumnType.BOOLEAN, null, "X", "X");
    }

    @Test
    @Ignore
    public void testDate() {
        assertOneCellResult(ColumnType.DATE, null, new Date(), "Sim");
    }

    private TableOutputSimulated assertOneCellResult(ColumnType type, Consumer<Column> configColumn,
            Object valueToBeSet, Object expectedResult) {
        TableOutputSimulated output = createOnColumTable(type, configColumn, valueToBeSet);
        output.getResult().debug();
        output.getResult().assertLinesSize(2);
        output.getResult().assertLine(0, type.toString());
        output.getResult().assertLine(1, expectedResult);
        return output;
    }

    private TableOutputSimulated createOnColumTable(ColumnType type, Consumer<Column> configColumn, Object value) {
        TableTool table = new TableTool();
        table.addColumn(type, type.toString());
        if (configColumn != null) {
            configColumn.accept(table.getColumn(0));
        }
        return generate(table, p -> p.insertLine(value));
    }

    @Nonnull
    private TableOutputSimulated generate(TableTool table, Consumer<TablePopulator> populatorCode) {
        TablePopulator p = new TablePopulator(table);
        populatorCode.accept(p);

        TableOutputSimulated output = new TableOutputSimulated();
        table.setReaderByTree(p.asTreeLineReader());
        table.generate(output);
        return output;
    }

    private TableTool createTableWithNumber(Consumer<Column> action) {
        TableTool table = new TableTool();
        table.addColumn(ColumnType.NUMBER, "Number");
        table.addColumn(ColumnType.INTEGER, "Integer");
        table.addColumn(ColumnType.MONEY, "Money");
        table.addColumn(ColumnType.PERCENT, "Percent");
        table.addColumn(ColumnType.HOUR, "Hour");
        if (action != null) {
            table.getColumns().forEach(action);
        }
        return table;
    }
}
