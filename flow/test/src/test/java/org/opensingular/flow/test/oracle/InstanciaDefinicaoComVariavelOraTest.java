package org.opensingular.flow.test.oracle;

import org.junit.BeforeClass;
import org.springframework.test.context.ActiveProfiles;

import org.opensingular.lib.commons.base.SingularPropertiesImpl;
import org.opensingular.flow.test.InstanciaDefinicaoComVariavelTest;

@ActiveProfiles("oracle")
public class InstanciaDefinicaoComVariavelOraTest extends InstanciaDefinicaoComVariavelTest {

    @BeforeClass
    public static void configProperties() {
        SingularPropertiesImpl.get().reloadAndOverrideWith(ClassLoader.getSystemClassLoader().getResource(
                "singular-ora.properties"));
    }
}