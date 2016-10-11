package org.opensingular.flow.test.mssql;

import org.junit.BeforeClass;
import org.springframework.test.context.ActiveProfiles;

import org.opensingular.lib.commons.base.SingularPropertiesImpl;
import org.opensingular.flow.test.InstanciaDefinicaoComVariavelTest;

@ActiveProfiles("mssql")
public class InstanciaDefinicaoComVariavelMssqlTest extends InstanciaDefinicaoComVariavelTest {

    @BeforeClass
    public static void configProperties() {
        SingularPropertiesImpl.get().reloadAndOverrideWith(ClassLoader.getSystemClassLoader().getResource(
                "singular-mssql.properties"));
    }
}