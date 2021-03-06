/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opensingular.form.type.core.STypeDate;
import org.opensingular.form.type.core.STypeString;

@RunWith(Parameterized.class)
public class TestMFormUtilUserFriendlyPath extends TestCaseForm {

    private STypeComposite<? extends SIComposite>               _evento;
    private STypeString                                         _descricao;
    private STypeComposite<SIComposite>                         _periodo;
    private STypeDate                                           _dataInicial;
    private STypeDate                                           _dataFinal;
    private STypeList<STypeComposite<SIComposite>, SIComposite> _alertas;
    private STypeComposite<SIComposite>                         _alerta;
    private STypeDate                                           _alerta_data;

    private SIComposite evento;

    private SIList<SIComposite> alertas;

    public TestMFormUtilUserFriendlyPath(TestFormConfig testFormConfig) {
        super(testFormConfig);
    }

    @Before
    public void setUp() {
        PackageBuilder pb = createTestPackage();

        _evento = pb.createCompositeType("evento");
        _evento.asAtr().label("Evento");
        _descricao = _evento.addField("descricao", STypeString.class);
        _descricao.asAtr().label("Descrição");
        _periodo = _evento.addFieldComposite("periodo");
        _dataInicial = _periodo.addField("dataInicia", STypeDate.class);
        _dataInicial.asAtr().label("Data inicial");
        _dataFinal = _periodo.addField("dataFinal", STypeDate.class);
        _dataFinal.asAtr().label("Data final");
        _alertas = _periodo.addFieldListOfComposite("alertas", "alerta");
        _alerta = _alertas.getElementsType();
        _alerta_data = _alerta.addField("data", STypeDate.class);

        // _evento.asAtr().label("Evento"); // calculado
        _descricao.asAtr().label("Descrição");
        _periodo.asAtr().label(""); // *** período com label vazia
        _dataInicial.asAtr().label("Data inicial");
        _dataFinal.asAtr().label("Data final");
        _alertas.asAtr().label("Alertas");
        _alerta.asAtr().label("Alerta");
        _alerta_data.asAtr().label("Data");

        evento = _evento.newInstance();
        alertas = evento.findNearest(_alertas).get();
        alertas.addNew();
    }

    @Test
    public void testFindNearestFromSimpleTypeInstanceToSameInstance(){
        SInstance descricao = evento.findNearest(_descricao).get();
        Assert.assertEquals(descricao.findNearest(_descricao).get(), descricao);
    }

    @Test
    public void testFindNearestFromSimpleTypeInstanceToSameInstanceWithoutOptional(){
        SInstance descricao = evento.findNearestOrException(_descricao);
        Assert.assertEquals(descricao.findNearestOrException(_descricao), descricao);
    }


    @Test
    public void generateUserFriendlyPath_no_parentContext() {
        //@formatter:off
        Assert.assertEquals("Evento"                                , SFormUtil.generateUserFriendlyPath(evento));
        Assert.assertEquals("Evento > Descrição"                    , SFormUtil.generateUserFriendlyPath(evento.findNearest(_descricao  ).get()));
        Assert.assertEquals("Evento"                                , SFormUtil.generateUserFriendlyPath(evento.findNearest(_periodo    ).get()));
        Assert.assertEquals("Evento > Data inicial"                 , SFormUtil.generateUserFriendlyPath(evento.findNearest(_dataInicial).get()));
        Assert.assertEquals("Evento > Data final"                   , SFormUtil.generateUserFriendlyPath(evento.findNearest(_dataFinal  ).get()));
        //@formatter:on
    }

    @Test
    public void generateUserFriendlyPath_with_parentContext() {
        //@formatter:off
        Assert.assertNull  (                                      SFormUtil.generateUserFriendlyPath(evento, evento));
        Assert.assertEquals("Descrição"                         , SFormUtil.generateUserFriendlyPath(evento.findNearest(_descricao  ).get(), evento));
        Assert.assertNull  (                                      SFormUtil.generateUserFriendlyPath(evento.findNearest(_periodo    ).get(), evento));
        Assert.assertEquals("Data inicial"                      , SFormUtil.generateUserFriendlyPath(evento.findNearest(_dataInicial).get(), evento));
        Assert.assertEquals("Data final"                        , SFormUtil.generateUserFriendlyPath(evento.findNearest(_dataFinal  ).get(), evento));
        Assert.assertEquals("Alertas"                           , SFormUtil.generateUserFriendlyPath(evento.findNearest(_alertas    ).get(), evento));
        Assert.assertEquals("Alertas [1] > Alerta"              , SFormUtil.generateUserFriendlyPath(evento.findNearest(_alerta     ).get(), evento));
        Assert.assertEquals("Alertas [1] > Alerta > Data"       , SFormUtil.generateUserFriendlyPath(evento.findNearest(_alerta_data).get(), evento));
        
        Assert.assertEquals("Alerta"                            , SFormUtil.generateUserFriendlyPath(evento.findNearest(_alerta     ).get(), alertas));
        Assert.assertEquals("Alerta > Data"                     , SFormUtil.generateUserFriendlyPath(evento.findNearest(_alerta_data).get(), alertas));
        //@formatter:on
    }
}
