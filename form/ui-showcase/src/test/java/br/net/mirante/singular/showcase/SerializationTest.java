package br.net.mirante.singular.showcase;

import br.net.mirante.singular.form.SIComposite;
import br.net.mirante.singular.form.document.RefType;
import br.net.mirante.singular.form.document.SDocumentFactory;
import br.net.mirante.singular.form.io.FormSerializationUtil;
import br.net.mirante.singular.showcase.dao.form.ShowcaseTypeLoader;
import br.net.mirante.singular.showcase.view.page.form.examples.ExamplePackage;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

//@RunWith(value = Parameterized.class)
public class SerializationTest {
//
//    TemplateRepository.TemplateEntry entry;
//
//    public SerializationTest(TemplateRepository.TemplateEntry entry){
//        this.entry = entry;
//    }
//
//    @Before public void setResolver(){
//        TemplateRepository.setDefault(TemplateRepository.get());
//    }

//    @Parameterized.Parameters(name = "{index}: serializeAndDeserialize({0})")
//    public static Iterable<Object[]> data1() {
//        Collection<TemplateRepository.TemplateEntry> entries = TemplateRepository.get().getEntries();
//        return entries.stream().map( (x) -> new Object[]{x}).collect(Collectors.toList());
//    }
//
//    @Test public void serializeAndDeserialize(){
//        MInstancia instance = entry.getType().novaInstancia();
//        FormSerializationUtil.fillInstance(FormSerializationUtil.toSerializedObject(instance));
//    }

    @Test public void serializeAndDeserialize(){
//        GenericApplicationContext ctx = new GenericApplicationContext();
//        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
//        xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
//        ctx.refresh();
//        ctx.getBean(ShowcaseTypeLoader.class);
//
//        ShowcaseTypeLoader repo = ctx.getBean(ShowcaseTypeLoader.class);
////        TemplateRepository.setDefault(TemplateRepository.get());
//        z pacote = null;
//        for(ShowcaseTypeLoader.TemplateEntry entry: repo.getEntries()){
//            if(entry.getType().getName().equals(ExamplePackage.Types.ORDER.name)){
//                pacote = (ExamplePackage) entry.getType().getPackage();
//            }
//        }
//
//        RefType refType = repo.loadRefTypeOrException(pacote.order.getName());
//        SIComposite order = (SIComposite) SDocumentFactory.empty().createInstance(refType);
//
//        order.setValue(pacote.orderNumber.getNameSimple(),1);
//        FormSerializationUtil.toInstance(FormSerializationUtil.toSerializedObject(order));
    }

}
