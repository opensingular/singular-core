/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.opensingular.form.helpers;

import org.assertj.core.api.Assertions;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.STypeList;
import org.opensingular.form.type.core.STypeDecimal;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Apoio a escrita de asserções referente a um {@link SType} e seu conteúdo.
 *
 * @author Daniel C. Bordin
 */
public class AssertionsSType extends AssertionsSAttributeEnabled<AssertionsSType, SType<?>> {

    public AssertionsSType(SType<?> type) {
        super(type);
    }

    /**
     * Retorna um novo objeto de assertiva para o tipo indicado pelo caminho informado.
     */
    public AssertionsSType field(String fieldPath) {
        //isInstanceOf(STypeComposite.class);
        return new AssertionsSType(getTarget().getLocalType(fieldPath));
    }

    /**
     * Verifica se o tipo é uma extensão direta do tipo informado. Para ser considerado uma extensão direta, deverá ser
     * da mesma classe do tipo (não pode ser derivado) e seu super tipo {@link SType#getSuperType()} deve ser igual o
     * tipo registrado no dicionário.
     */
    public AssertionsSType isDirectExtensionOf(Class<? extends SType<?>> typeClass) {
        return isDirectExtensionOf(typeClass, null);
    }

    /**
     * Verifica se o tipo encontrado no caminho indicado é uma extensão direta do tipo informado. Para ser considerado
     * uma extensão direta, deverá ser da mesma classe do tipo (não pode ser derivado) e seu super tipo {@link
     * SType#getSuperType()} deve ser igual o tipo registrado no dicionário.
     */
    public AssertionsSType isDirectExtensionOf(Class<? extends SType<?>> typeClass, String fieldPath) {
        SType<?> expectedSuperType = getTarget().getDictionary().getType(typeClass);
        if (fieldPath != null) {
            expectedSuperType = expectedSuperType.getLocalType(fieldPath);
        }
        return isDirectExtensionOf(expectedSuperType);
    }

    /**
     * Verifica se o tipo é uma extensão direta do tipo informado. Para ser considerado uma extensão direta, deverá ser
     * da mesma classe do tipo (não pode ser derivado) e seu super tipo {@link SType#getSuperType()} deve ser igual ao
     * tipo passado como parâmetro.
     */
    @Nonnull
    public AssertionsSType isDirectExtensionOf(@Nonnull SType<?> expectedSuperType) {
        isNotSameAs(expectedSuperType);
        if (getTarget().getSuperType() != expectedSuperType) {
            throw new AssertionError(errorMsg("Super tipo inválido", expectedSuperType, getTarget().getSuperType()));

        }
        checkCorrectJavaSuperClassDuringExtension(expectedSuperType);
        return this;
    }

    final void checkCorrectJavaSuperClassDuringExtension(@Nonnull SType<?> expectedSuperType) {
        if (!expectedSuperType.getClass().isAssignableFrom(getTarget().getClass())) {
            throw new AssertionError(errorMsg(
                    "A classe do tipo " + getTarget() + " deveria igual ou extender a classe " +
                            expectedSuperType.getClass().getName() + ", que e a classe de seu super tipo " +
                            expectedSuperType + ". Em vez disso, é uma classe " + getTarget().getClass().getName()));
        }
    }

    /** Verifies if the current type has the indicated type as its parent type. Otherwise, throws a exception. */
    @Nonnull
    public AssertionsSType isParent(@Nullable SType<?> expectedParent) {
        Assertions.assertThat(getTarget().getParent().orElse(null)).isSameAs(expectedParent);
        return this;
    }

    /**
     * Verify if the current type has a complementary extension ({@link SType#getComplementarySuperType()} of the
     * indicated type. Also verifies if this extension is correctly done. Throws a exception if the condicions are
     * not met.
     */
    @Nonnull
    public AssertionsSType isDirectComplementaryExtensionOf(@Nullable SType<?> expectedComplementarySuperType) {
        if (expectedComplementarySuperType == null) {
            Optional<SType<?>> complementary = getTarget().getComplementarySuperType();
            if (complementary.isPresent()) {
                throw new AssertionError(errorMsg("Complementary super type invalid", null, complementary.get()));
            }
            return this;
        }
        isNotSameAs(expectedComplementarySuperType);
        if (getTarget().getComplementarySuperType().orElse(null) != expectedComplementarySuperType) {
            throw new AssertionError(errorMsg("Complementary super type invalid", expectedComplementarySuperType,
                    getTarget().getComplementarySuperType().orElse(null)));
        }
        checkCorrectJavaSuperClassDuringExtension(expectedComplementarySuperType);
        return this;
    }

    /**
     * Verifies if the current type is directly or indirectly a type of the informed expected super type. Otherwise,
     * throws a exception.
     */
    @Nonnull
    public AssertionsSType isExtensionOf(@Nonnull SType<?> expectedSuperType) {
        isNotSameAs(expectedSuperType);
        if (!getTarget().isTypeOf(expectedSuperType)) {
            throw new AssertionError(errorMsg("Was expected to extend " + expectedSuperType));
        }
        checkCorrectJavaSuperClassDuringExtension(expectedSuperType);
        return this;
    }

    /**
     * Verifies if the current type is not directly or indirectly a type of the informed expected super type. Otherwise,
     * throws a exception.
     */
    @Nonnull
    public AssertionsSType isNotExtensionOf(@Nonnull SType<?> expectedSuperType) {
        isNotSameAs(expectedSuperType);
        if (getTarget().isTypeOf(expectedSuperType)) {
            throw new AssertionError(errorMsg("Shouldn't extend " + expectedSuperType));
        }
        return this;
    }

    /**
     * Veririca se o super tipo do composite pai do campo atual possui um campo de mesmo nome que é o super tipo do
     * campo atual, ou seja, verifica se o campo atual é uma extensão correta de um tipo que pertencia ao composite
     * original.
     */
    public AssertionsSType isExtensionOfParentCompositeFieldReference() {
        Assertions.assertThat(getTarget().getParentScope()).isInstanceOf(STypeComposite.class);
        STypeComposite parent = (STypeComposite) getTarget().getParentScope();
        new AssertionsSType(parent.getSuperType()).isInstanceOf(parent.getClass());
        STypeComposite parent2 = (STypeComposite) parent.getSuperType();
        SType<?> parentRef = parent2.getField(getTarget().getNameSimple());
        return isDirectExtensionOf(parentRef);
    }

    /**
     * Verifica se o tipo e seu tipos internos (se existirem) extendem corretamente o tipo pai. Faz uma analise
     * recursiva para os subtipos.
     */
    public AssertionsSType isExtensionCorrect() {
        SType<?> superType = getTarget().getSuperType();
        if (! (getTarget().isComposite() || getTarget().isList())) {
            throw new AssertionError(errorMsg("O tipo deve ser um composite"));
        }
        return isExtensionCorrect(superType);
    }

    /**
     * Verifica se o tipo e seu tipos internos (se existirem) extendem corretamente o tipo informado. Faz uma analise
     * recursiva para os subtipos.
     */
    public AssertionsSType isExtensionCorrect(Class<? extends SType> typeClass) {
        return isExtensionCorrect(getTarget().getDictionary().getType(typeClass));
    }

    /**
     * Verifica se o tipo e seu tipos internos (se existirem) extendem corretamente o tipo informado. Faz uma analise
     * recursiva para os subtipos.
     */
    @Nonnull
    public AssertionsSType isExtensionCorrect(@Nonnull SType<?> expectedSuperType) {
        isDirectExtensionOf(expectedSuperType);
        Assertions.assertThat(expectedSuperType.isRecursiveReference() && !getTarget().isRecursiveReference())
                .isFalse();
        if (getTarget().isComposite()) {
            Assertions.assertThat(expectedSuperType).isInstanceOf(STypeComposite.class);
            if (!getTarget().isRecursiveReference()) {
                for (SType<?> fieldSuper : ((STypeComposite<?>) expectedSuperType).getFields()) {
                    field(fieldSuper.getNameSimple()).isExtensionCorrect(fieldSuper);
                }
            }
        } else if (getTarget().isList()) {
            Assertions.assertThat(expectedSuperType).isInstanceOf(STypeList.class);
            listElementType().isExtensionCorrect(((STypeList<?,?>) expectedSuperType).getElementsType());
        }
        return this;
    }

    /**
     * Verifies is the current type is a direct complementary extension of the informed type (use {@link
     * #isDirectComplementaryExtensionOf(SType)} and if the internal structure of the extension is consistent.
     * Otherwise, throws a exception.
     */
    @Nonnull
    public AssertionsSType isComplementaryExtensionCorrect(@Nonnull SType<?> complementarySuperType) {
        isDirectComplementaryExtensionOf(complementarySuperType);
        if (getTarget().isComposite()) {
            Assertions.assertThat(complementarySuperType).isInstanceOf(STypeComposite.class);
            if (!getTarget().isRecursiveReference()) {
                for (SType<?> fieldSuper : ((STypeComposite<?>) complementarySuperType).getFields()) {
                    field(fieldSuper.getNameSimple()).isComplementaryExtensionCorrect(fieldSuper);
                }
            }
        } else if (getTarget().isList()) {
            Assertions.assertThat(complementarySuperType).isInstanceOf(STypeList.class);
            listElementType().isComplementaryExtensionCorrect(
                    ((STypeList<?, ?>) complementarySuperType).getElementsType());
        }
        return this;
    }

    private AssertionsSType is(Class<?> typeString, String fieldPath) {
        AssertionsSType assertions = this;
        if (fieldPath != null) {
            assertions = field(fieldPath);
        }
        return assertions.isInstanceOf(typeString);
    }

    /**
     * Verifica se o tipo atual é um {@link STypeComposite}.
     */
    public AssertionsSType isComposite() {
        return isInstanceOf(STypeComposite.class);
    }

    /**
     * Verifica se o tipo indicado pelo caminho informado é um {@link STypeComposite} e com a quantidade de campo
     * indicados.
     *
     * @return Retorna um novo objeto de assertiva para o tipo encontrado.
     */
    public AssertionsSType isComposite(String fieldPath, int expectedFieldsSize) {
        return isComposite(fieldPath).sizeIs(expectedFieldsSize);
    }

    /**
     * Verifica se o tipo indicado pelo caminho informado é um {@link STypeComposite}.
     *
     * @return Retorna um novo objeto de assertiva para o tipo encontrado.
     */
    private AssertionsSType isComposite(String fieldPath) {
        return is(STypeComposite.class, fieldPath);
    }

    private AssertionsSType sizeIs(int expectedFieldsSize) {
        compositeSize(expectedFieldsSize);
        return this;
    }

    /**
     * Verifica se o tipo indicado é um {@link STypeComposite} e com a quantidade de campo indicados.
     */
    public AssertionsSType isComposite(int expectedSize) {
        isComposite();
        compositeSize(expectedSize);
        return this;
    }

    private void compositeSize(int expectedSize) {
        Assertions.assertThat(getTarget(STypeComposite.class).getFields().size()).isEqualTo(expectedSize);
    }

    /**
     * Verifica se o tipo indicado pelo caminho informado é um {@link STypeList}.
     *
     * @return Retorna um novo objeto de assertiva para o tipo encontrado.
     */
    public AssertionsSType isList(String fieldPath) {
        return is(STypeList.class, fieldPath);
    }

    /**
     * Verifica se o tipo atual é uma {@link STypeList} e retorna um novo objketo de assertiva para o tipo de elementos
     * da lista.
     */
    public AssertionsSType listElementType() {
        return listElementType(null);
    }

    /**
     * Verifica se o tipo encontrado no caminho indicado é uma {@link STypeList} e retorna um novo objeto de assertiva
     * para o tipo de elementos da lista.
     */
    public AssertionsSType listElementType(String fieldPath) {
        AssertionsSType field = isList(fieldPath);
        SType<?> typeList = ((STypeList<?, ?>) field.getTarget()).getElementsType();
        if (typeList == null) {
            throw new AssertionError(errorMsg(
                    "era esperado que a lista tivesse um tipo, mas getElementsType() retornou null", "'Valor não nulo'",
                    null));
        }
        return new AssertionsSType(typeList);
    }

    /**
     * Verifica se o tipo atual não é um referência recursica {@link SType#isRecursiveReference()}.
     */
    public AssertionsSType isNotRecursiveReference() {
        if (getTarget().isRecursiveReference()) {
            throw new AssertionError(errorMsg("é uma referência recursiva"));
        }
        return this;
    }

    /**
     * Verifica se o tipo atual é um referência recursica {@link SType#isRecursiveReference()}.
     */
    public AssertionsSType isRecursiveReference() {
        if (!getTarget().isRecursiveReference()) {
            throw new AssertionError(errorMsg("não é uma referência recursiva"));
        }
        return this;
    }

    /**
     * Verifica se o tipo atual é um {@link STypeString}.
     */
    public AssertionsSType isString() {
        return isInstanceOf(STypeString.class);
    }

    /**
     * Verifica se o tipo indicado pelo caminho informado é um {@link STypeString}.
     *
     * @return Retorna um novo objeto de assertiva para o tipo encontrado.
     */
    public AssertionsSType isString(String fieldPath) {
        return is(STypeString.class, fieldPath);
    }

    /**
     * Verifica se o tipo indicado pelo caminho informado é um {@link STypeInteger}.
     *
     * @return Retorna um novo objeto de assertiva para o tipo encontrado.
     */
    public AssertionsSType isInteger(String fieldPath) {
        return is(STypeInteger.class, fieldPath);
    }

    /**
     * Verifica se o tipo indicado pelo caminho informado é um {@link STypeDecimal}.
     *
     * @return Retorna um novo objeto de assertiva para o tipo encontrado.
     */
    public AssertionsSType isDecimal(String fieldPath) {
        return is(STypeDecimal.class, fieldPath);
    }

    /**
     * Verifica se o tipo atual tem todos os tipos informados como campos dependentes e mais nenhum campo.
     */
    public AssertionsSType dependentsTypesAre(SType<?>... types) {
        Set<SType<?>> expectedSet = new LinkedHashSet<>(Arrays.asList(types));
        Set<SType<?>> currentSet = getTarget().getDependentTypes();
        isDependentType(types);
        for (SType<?> type : types) {
            if (!currentSet.contains(type)) {
                throw new AssertionError(errorMsg("A lista de dependente de " + getTarget() + " não contêm " + type));
            }
        }
        for (SType<?> type : currentSet) {
            if (!expectedSet.contains(type)) {
                throw new AssertionError(errorMsg(
                        "O tipo " + type + " foi encontrado como dependente de " + getTarget() +
                                ", mas isso não era esperado"));
            }
        }
        return this;
    }

    /** Verifica se o tipo atual tem todos os tipos informados como campos dependentes. */
    public AssertionsSType isDependentType(SType<?>... types) {
        for (SType<?> type : types) {
            if (!getTarget().isDependentType(type)) {
                throw new AssertionError(errorMsg("O tipo " + type + " não está como dependente de " + getTarget() +
                        " ( isDependentType(type) retornou false)"));
            }
        }
        return this;
    }

    /** Verifica se o tipo atual não possui nenhum dos tipos informados como campos dependentes. */
    public AssertionsSType isNotDependentType(SType<?>... types) {
        for (SType<?> type : types) {
            if (getTarget().isDependentType(type)) {
                throw new AssertionError(errorMsg("O tipo " + type + " está como dependente de " + getTarget() +
                        " ( isDependentType(type) retornou true)"));
            }
        }
        return this;
    }

    @Nonnull
    @Override
    protected Optional<String> generateDescriptionForCurrentTarget(@Nonnull Optional<SType<?>> current) {
        return current.map(type -> "No tipo '" + type);
    }
}
