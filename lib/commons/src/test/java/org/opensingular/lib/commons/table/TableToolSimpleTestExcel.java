package org.opensingular.lib.commons.table;

import org.junit.Before;
import org.junit.Test;
import org.opensingular.internal.lib.commons.test.SingularTestUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TableToolSimpleTestExcel extends TableToolSimpleTestBase {

    private TableOutputExcel tableOutputExcel;
    private boolean isOpenEnabled = false;

    @Before
    public void setUp() throws Exception {
        tableOutputExcel = new TableOutputExcel("dummy");
    }

    @Test
    @Override
    public void testSimpleTable() {
        testSimpleTable_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_withInvisibleColumn() {
        testSimpleTable_withInvisibleColumn_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_dontShowTitle() {
        testSimpleTable_dontShowTitle_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_empty1() {
        testSimpleTable_empty1_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_empty2() {
        testSimpleTable_empty2_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_withSuperTitle() {
        testSimpleTable_withSuperTitle_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_withTotalizationLine1() {
        testSimpleTable_withTotalizationLine1_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_withTotalizationLine2() {
        testSimpleTable_withTotalizationLine2_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    @Test
    @Override
    public void testSimpleTable_withSuperTitleAndTotalization() {
        testSimpleTable_withSuperTitleAndTotalization_build().generate(tableOutputExcel);
        writeAndOpenIfEnabled();
    }

    public void writeAndOpenIfEnabled() {
        try {
            File xlsx = File.createTempFile("test", ".xlsx");
            FileOutputStream fos = new FileOutputStream(xlsx);
            tableOutputExcel.writeResult(fos);
            if (isOpenEnabled) {
                SingularTestUtil.showFileOnDesktopForUser(xlsx);
            } else {
                xlsx.deleteOnExit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
