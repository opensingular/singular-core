package org.opensingular.form;

import org.opensingular.form.PackageBuilder;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SPackage;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.STypeList;
import org.opensingular.form.type.core.SIString;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.type.country.brazil.STypeTelefoneNacional;

public class SPackageTesteContatos extends SPackage {

    public STypeComposite<?>                                   contato;
    public STypeList<STypeComposite<SIComposite>, SIComposite> enderecos;
    public STypeComposite<?>                                   identificacao;
    public STypeString                                         nome;
    public STypeString                                         sobrenome;
    public STypeComposite<?>                                   endereco;
    public STypeString                                         enderecoLogradouro;
    public STypeInteger                                        enderecoNumero;
    public STypeString                                         enderecoComplemento;
    public STypeString                                         enderecoCidade;
    public STypeString                                         enderecoEstado;
    public STypeList<STypeTelefoneNacional, SIString>          telefones;
    public STypeTelefoneNacional                               telefone;
    public STypeList<STypeString, SIString>                    emails;
    public STypeString                                         email;

    public SPackageTesteContatos() {
        super("mform.exemplo.squery");
    }

    @Override
    protected void onLoadPackage(PackageBuilder pb) {
        contato = pb.createCompositeType("Contato");

        identificacao = contato.addFieldComposite("identificacao");
        nome = identificacao.addFieldString("nome", true);
        sobrenome = identificacao.addFieldString("sobrenome");
//        sobrenome.as(MPacoteBasic.aspect())
//            .visible(i -> {
//                boolean visible = i.findAncestor(contato).get().findDescendant(nome).get().getValor() == null;
//                System.out.println(">>> " + visible);
//                return visible;
//            })
//            .enabled(i -> defaultString(i.findAncestor(contato).get().findDescendant(nome).get().getValor()).length() > 3);

        enderecos = contato.addFieldListOfComposite("enderecos", "endereco");
        endereco = enderecos.getElementsType();
        enderecoLogradouro = endereco.addFieldString("logradouro");
        enderecoComplemento = endereco.addFieldString("complemento");
        enderecoNumero = endereco.addFieldInteger("numero");
        enderecoCidade = endereco.addFieldString("cidade");
        enderecoEstado = endereco.addFieldString("estado");

        telefones = contato.addFieldListOf("telefones", STypeTelefoneNacional.class);
        telefone = telefones.getElementsType();
        emails = contato.addFieldListOf("emails", STypeString.class);
        email = emails.getElementsType();
    }
}