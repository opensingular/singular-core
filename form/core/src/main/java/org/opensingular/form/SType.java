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

package org.opensingular.form;

import org.opensingular.form.aspect.AspectEntry;
import org.opensingular.form.aspect.AspectRef;
import org.opensingular.form.builder.selection.SelectionBuilder;
import org.opensingular.form.calculation.SimpleValueCalculation;
import org.opensingular.form.calculation.SimpleValueCalculationInstanceOptional;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.provider.SimpleProvider;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.type.core.SPackageCore;
import org.opensingular.form.validation.InstanceValidator;
import org.opensingular.form.validation.ValidationEntry;
import org.opensingular.form.validation.ValidationErrorLevel;
import org.opensingular.form.view.SView;
import org.opensingular.form.view.SViewSelectionBySelect;
import org.opensingular.internal.form.util.ArrUtil;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.commons.util.Loggable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@SInfoType(name = "SType", spackage = SPackageCore.class)
public class SType<I extends SInstance> extends SScopeBase implements SAttributeEnabled, Loggable {

    private static final Logger LOGGER = Logger.getLogger(SType.class.getName());

    @Nullable
    private AttributeDefinitionManager attributesDefined;

    @Nullable
    private AttributeValuesManagerForSType attributes;
    /**
     * Classe a  ser usada para criar as instâncias do tipo atual. Pode ser null, indicado que o tipo atual é abstrato.
     */
    @Nullable
    private final Class<? extends I> instanceClass;
    /**
     * contabiliza a quantidade de instancias desse tipo.
     */
    long instanceCount;

    private SimpleName nameSimple;
    private String nameFull;
    private SDictionary dictionary;
    /**
     * Representa um identficado único de cada tipo dentro de um dicionário.
     */
    private int typeId;
    private SScope scope;
    private List<ValidationEntry<I>> validators;
    private Set<SType<?>> dependentTypes;
    private AttrInternalRef attrInternalRef;
    /**
     * Representa o tipo ao qual o tipo atual extende. Pode ou não ser da mesma classe do tipo atual.
     */
    private SType<I> superType;

    /** Represents a second parent type in case o multiple inheritance. */
    private SType<?> complementarySuperType;

    private SView view;

    /**
     * List os aspectes implementations registered locally to the type.
     */
    private AspectEntry<?, ?>[] aspects;

    /**
     * Indica se o tipo está no meio da execução do seu método {@link #onLoadType(TypeBuilder)}.
     */
    private boolean callingOnLoadType;

    /**
     * It's the package that owns this type.
     */
    private SPackage pkg;

    protected SType() {
        this(null, null);
    }

    protected SType(@Nullable Class<? extends I> instanceClass) {
        this(null, instanceClass);
    }

    protected SType(@Nullable String simpleName, @Nullable Class<? extends I> instanceClass) {
        if (simpleName == null) {
            this.nameSimple = SFormUtil.getTypeSimpleName(getClass());
        } else {
            this.nameSimple = new SimpleName(simpleName);
        }
        this.instanceClass = instanceClass;
    }

    private static boolean isEqualsStart(String name, String prefixo) {
        return name.startsWith(prefixo) && name.length() > prefixo.length() && name.charAt(prefixo.length()) == '.';
    }

    /**
     * Esse método é chamado quando do primeiro registro de um classe Java que extende SType. Esse método é chamado
     * apenas uma única vez para o registro de cada classe. Se forem criadas extensões do mesmo tipo sem criar uma nova
     * classe Java, esse método não será chamado para o tipos filhos.
     * <p>ATENÇÃO: A implementação não deve chamar super.onLoadType(), pois pode deixar o tipo incosistente</p>
     *
     * @param tb Classe utilitária de apoio na configuração do tipo
     */
    protected void onLoadType(@Nonnull TypeBuilder tb) {
        throw new SingularFormException("As implementações de onLoadType() não devem chamar super.onLoadType()", this);
        // Esse método será implementado nas classes derevidas se precisarem
    }

    /** Extends the current type creating a new one with the current type as super type (parent type). */
    @Nonnull
    @SuppressWarnings("unchecked")
    final <S extends SType<?>> S extend(@Nullable SimpleName simpleName, @Nullable SType<?> complementarySuperType) {
        SimpleName nameResolved = SFormUtil.resolveName(simpleName, this);

        S newType = (S) SFormUtil.newInstance(getClass());
        ((SType<?>) newType).nameSimple = nameResolved;
        ((SType<I>) newType).superType = this;
        ((SType<?>) newType).complementarySuperType = complementarySuperType;
        return newType;
    }

    /**
     * Metodo invocado após um tipo ser extendido, de modo, que o mesmo possa extender sub referências que possua.
     */
    protected void extendSubReference() {
        //Se necessário, a classes derivadas vão implementar esse método
    }

    @SuppressWarnings("unchecked")
    final void resolveSuperType(SDictionary dictionary) {
        if (superType == null && getClass() != SType.class) {
            Class<? extends SType> c = getClass();
            do {
                c = (Class<SType>) c.getSuperclass();
            } while (Modifier.isAbstract(c.getModifiers()));
            superType = dictionary.getType(c);
        }
    }

    @Override
    @Nonnull
    public String getName() {
        if (nameFull == null) {
            this.nameFull = getParentScope().getName() + '.' + nameSimple;
        }
        return nameFull;
    }

    @Nonnull
    public String getNameSimple() {
        return nameSimple.get();
    }

    @Nonnull
    final SimpleName getNameSimpleObj() {
        return nameSimple;
    }

    /** Return the super type (parent type) of the current type. Returns null only for the parent of all types. */
    @Nullable
    public SType<I> getSuperType() {
        if (superType == null && getClass() != SType.class) {
            throw new SingularFormException("This type isn't ready to use", this);
        }
        return superType;
    }

    /**
     * Returns a reference to the secondary super type, if it exists. This secondary super type is use for a source o
     * complementary attributes.
     */
    @Nonnull
    public Optional<SType<?>> getComplementarySuperType() {
        return Optional.ofNullable(complementarySuperType);
    }

    @SuppressWarnings("unchecked")
    public Class<I> getInstanceClass() {
        return (Class<I>) instanceClass;
    }

    @SuppressWarnings("unchecked")
    private Class<I> getInstanceClassResolved() {
        if (instanceClass == null && superType != null) {
            return superType.getInstanceClassResolved();
        }
        return (Class<I>) instanceClass;
    }

    final void setScope(SScope scope) {
        this.scope = scope;
    }

    @Override
    @Nonnull
    public SScope getParentScope() {
        if (scope == null) {
            throw new SingularFormException("O escopo do tipo ainda não foi configurado. \n" +
                    "Se você estiver tentando configurar o tipo no construtor do mesmo, " +
                    "dê override no método onLoadType() e mova as chamada de configuração para ele.", this);
        }
        return scope;
    }

    @Nonnull
    @Override
    public final SPackage getPackage() {
        if (pkg == null) {
            pkg = getParentScope().getPackage();
        }
        return pkg;
    }

    @Override
    @Nonnull
    public SDictionary getDictionary() {
        if (dictionary == null) {
            dictionary = getPackage().getDictionary();
        }
        return dictionary;
    }

    public boolean isSelfReference() {
        return false;
    }

    /**
     * <p> Verificar se o tipo atual é do tipo informado, diretamente ou se é um tipo extendido. Para isso percorre toda
     * a hierarquia de derivação do tipo atual verificando se encontra parentTypeCandidate na hierarquia. </p> <p> Ambos
     * o tipo tem que pertencer à mesma instância de dicionário para serem considerado compatíveis, ou seja, se dois
     * tipo forem criados em dicionário diferentes, nunca serão considerado compatíveis mesmo se proveniente da mesma
     * classe de definição. </p>
     *
     * @return true se o tipo atual for do tipo informado.
     */
    public boolean isTypeOf(@Nonnull SType<?> parentTypeCandidate) {
        SFormUtil.verifySameDictionary(this, parentTypeCandidate);
        SType<I> current = this;
        while (current != null) {
            if (current == parentTypeCandidate ||
                    (current.complementarySuperType != null && current.complementarySuperType.isTypeOf(
                            parentTypeCandidate))) {
                return true;
            }
            current = current.superType;
        }
        return false;
    }

    /**
     * Verificar se o tipo é um tipo lista ({@link STypeList}).
     */
    public boolean isList() {
        return this instanceof STypeList;
    }

    /**
     * Verificar se o tipo é um tipo composto ({@link STypeComposite}).
     */
    public boolean isComposite() {
        return this instanceof STypeComposite;
    }

    private AttrInternalRef getAttrInternalRef() {
        return attrInternalRef;
    }

    final void setAttrInternalRef(AttrInternalRef attrInternalRef) {
        if (this.attrInternalRef != null) {
            throw new SingularFormException("Internal Error: this method shouldn't be called twice");
        }
        this.attrInternalRef = attrInternalRef;
    }

    @Nonnull
    final SInstance newAttributeInstanceFor(@Nonnull SType<?> typeToBeAppliedAttribute) {
        checkIfIsAttribute();
        SInstance attrInstance;
        if (attrInternalRef.isSelfReference()) {
            attrInstance = typeToBeAppliedAttribute.newInstance(getDictionary().getInternalDicionaryDocument());
        } else {
            attrInstance = newInstance(getDictionary().getInternalDicionaryDocument());
        }
        attrInstance.setAsAttribute(attrInternalRef, typeToBeAppliedAttribute);
        return attrInstance;
    }

    /**
     * Informa se o tipo é a definição de um atributo (de outro tipo ou de instância) ou se é um tipo comum.
     */
    public final boolean isAttribute() {
        return attrInternalRef != null;
    }

    @Nonnull
    final AttrInternalRef checkIfIsAttribute() {
        if (attrInternalRef == null) {
            throw new SingularFormException("O tipo '" + getName() + "' não é um tipo atributo", this);
        }
        return attrInternalRef;
    }

    final void addAttribute(@Nonnull SType<?> attributeDef) {
        getAttributesDefinitions().add(this, attributeDef);
    }

    /**
     * Returns the enclosing type of the current type. For example, if the current type is a field in a {@link
     * STypeComposite}, the composite will the parent of field.
     */
    @Nonnull
    public final Optional<SType> getParent() {
        if (scope instanceof SType) {
            return Optional.of((SType<?>) scope);
        }
        return Optional.empty();
    }

    /**
     * <p>
     * Retorna o path da instancia atual relativa ao elemento raiz, ou seja, não
     * inclui o nome da instância raiz no path gerado.
     * </p>
     * Exemplos, supundo que enderecos e experiencias estao dentro de um
     * elemento raiz chamado cadastro:
     * </p>
     * <p>
     * <pre>
     *     "enderecos/rua"
     *     "experiencias/empresa/nome"
     *     "experiencias/empresa/ramo"
     * </pre>
     *
     * @return Null se chamado em uma instância raiz.
     */
    @Nullable
    public final String getPathFromRoot() {
        return SFormUtil.generatePath(this, i -> !i.getParent().isPresent());
    }

    /**
     * <p>
     * Retorna o path da instancia atual desde o raiz, incluindo o nome da
     * instancia raiz.
     * </p>
     * Exemplos, supundo que enderecos e experiencias estao dentro de um
     * elemento raiz chamado cadastro:
     * </p>
     * <p>
     * <pre>
     *     "/cadastro/enderecos/rua"
     *     "/cadastro/experiencias/experiencia/empresa/nome"
     *     "/cadastro/experiencias/experiencia/empresa/ramo"
     * </pre>
     */
    @Nonnull
    public final String getPathFull() {
        return SFormUtil.generatePath(this, Objects::isNull);
    }

    @Nullable
    @Deprecated
    public final SAttributeEnabled getParentAttributeContext() {
        return getSuperType();
    }

    @Nonnull
    private AttributeDefinitionManager getAttributesDefinitions() {
        if (attributesDefined == null) {
            attributesDefined = new AttributeDefinitionManager();
        }
        return attributesDefined;
    }

    @Nullable
    final SType<?> getAttributeDefinedLocally(@Nonnull AttrInternalRef ref) {
        return AttributeDefinitionManager.staticGetAttributeDefinedLocally(attributesDefined, ref);
    }

    @Nullable
    public final <M extends SInstance> M findAttributeInstance(@Nonnull AtrRef<?, M, ?> atr) {
        //TODO retirar o public desse método e colocar como visibilidade de pacote
        Class<M> instanceAttributeClass;
        if (atr.isSelfReference()) {
            instanceAttributeClass = (Class<M>) getInstanceClassResolved();
            if (instanceAttributeClass == null) {
                throw new SingularFormException(
                        "O Atributo " + atr.getNameFull() + " é uma SELF reference ao tipo, mas o tipo é abstrato",
                        this);
            }
        } else {
            instanceAttributeClass = atr.getInstanceClass();
            if (instanceAttributeClass == null) {
                throw new SingularFormException(
                        "O Atributo " + atr.getNameFull() + " não define o tipo da instância do atributo", this);
            }
        }
        SInstance instance = findAttributeInstance(atr.getNameFull());
        return instanceAttributeClass.cast(instance);
    }

    @Nullable
    final SInstance findAttributeInstance(@Nonnull String fullName) {
        AttrInternalRef ref = getDictionary().getAttributeReferenceOrException(fullName);
        return SAttributeUtil.findAttributeInstance(this, ref);
    }

    @Override
    public final <V> void setAttributeValue(@Nonnull AtrRef<?, ?, V> atr, @Nullable V value) {
        getAttributesMap().setAttributeValue(atr, value);
    }

    @Override
    public void setAttributeValue(@Nonnull String attributeFullName, @Nullable String subPath, @Nullable Object value) {
        getAttributesMap().setAttributeValue(attributeFullName, subPath, value);
    }

    final void setAttributeValueSavingForLatter(@Nonnull String attributeName, @Nullable String value) {
        AttrInternalRef ref = getDictionary().getAttribureRefereceOrCreateLazy(attributeName);
        getAttributesMap().setAttributeValue(ref, null, value);
    }

    final <V> void setAttributeValue(@Nonnull AttrInternalRef ref, @Nullable V value) {
        getAttributesMap().setAttributeValue(ref, null, value);
    }

    @Override
    public final <V> void setAttributeCalculation(@Nonnull AtrRef<?, ?, V> atr,
                                                  @Nullable SimpleValueCalculation<V> value) {
        getAttributesMap().setAttributeCalculation(atr, value);
    }

    public final <V> void setAttributeCalculationInstanceOptional(@Nonnull AtrRef<?, ?, V> atr,
            @Nullable SimpleValueCalculationInstanceOptional<V> value) {
        getAttributesMap().setAttributeCalculation(atr, value);
    }

    //    @Override
    //    public <V> void setAttributeCalculation(@Nonnull String attributeFullName, @Nullable String subPath,
    //                                            @Nullable SimpleValueCalculation<V> valueCalculation) {
    //        getAttributesMap().setAttributeCalculation(attributeFullName, subPath, valueCalculation);
    //    }

    @Nonnull
    private AttributeValuesManagerForSType getAttributesMap() {
        if (attributes == null) {
            attributes = new AttributeValuesManagerForSType(this);
        }
        return attributes;
    }

    /**
     * Lista todos os atributos com valor associado diretamente ao tipo atual. Não retorna os atributos consolidado do
     * tipo pai.
     */
    @Override
    @Nonnull
    public Collection<SInstance> getAttributes() {
        return AttributeValuesManager.staticGetAttributes(attributes);
    }

    /**
     * Retorna a instancia do atributo se houver uma associada diretamente ao objeto atual. Não procura o atributo na
     * hierarquia.
     */
    @Nonnull
    final Optional<SInstance> getAttributeDirectly(@Nonnull String fullName) {
        return AttributeValuesManager.staticGetAttributeDirectly(this, attributes, fullName);
    }

    /**
     * Retorna a instancia do atributo se houver uma associada diretamente ao objeto atual.
     */
    @Nullable
    final SInstance getAttributeDirectly(@Nonnull AttrInternalRef ref) {
        return AttributeValuesManager.staticGetAttributeDirectly(attributes, ref);
    }

    @Nullable
    public final <T> T getAttributeValue(@Nonnull AtrRef<?, ?, ?> atr, @Nullable Class<T> resultClass) {
        return getAttributeValue(getDictionary().getAttributeReferenceOrException(atr), resultClass);
    }

    @Nullable
    public final <V> V getAttributeValue(@Nonnull AtrRef<?, ?, V> atr) {
        return getAttributeValue(getDictionary().getAttributeReferenceOrException(atr), atr.getValueClass());
    }

    final boolean hasAttributeValueDirectly(@Nonnull AtrRef<?, ?, ?> atr) {
        AttrInternalRef ref = getDictionary().getAttributeReferenceOrException(atr);
        return AttributeValuesManager.staticGetAttributeDirectly(attributes, ref) != null;
    }

    @Override
    @Nullable
    public final <V> V getAttributeValue(@Nonnull String attributeFullName, @Nullable Class<V> resultClass) {
        return getAttributeValue(getDictionary().getAttributeReferenceOrException(attributeFullName), resultClass);
    }

    @Nullable
    private <V> V getAttributeValue(@Nonnull AttrInternalRef ref, @Nullable Class<V> resultClass) {
        return SAttributeUtil.getAttributeValueInTheContextOf(this, null, ref, resultClass);
    }

    public SType<I> with(AtrRef<?, ?, ?> attribute, Object value) {
        setAttributeValue((AtrRef<?, ?, Object>) attribute, value);
        return this;
    }

    public SType<I> withDefaultValueIfNull(Object value) {
        return with(SPackageBasic.ATR_DEFAULT_IF_NULL, value);
    }

    public Object getAttributeValueOrDefaultValueIfNull() {
        if (Objects.equals(nameSimple, SPackageBasic.ATR_DEFAULT_IF_NULL.getNameSimple())) {
            return null;
        }
        return getAttributeValue(SPackageBasic.ATR_DEFAULT_IF_NULL);
    }

    public <V> V getAttributeValueOrDefaultValueIfNull(Class<V> resultClass) {
        if (Objects.equals(nameSimple, SPackageBasic.ATR_DEFAULT_IF_NULL.getNameSimple())) {
            return null;
        }
        return getAttributeValue(SPackageBasic.ATR_DEFAULT_IF_NULL, resultClass);
    }

    public final Boolean isRequired() {
        return getAttributeValue(SPackageBasic.ATR_REQUIRED);
    }

    public final boolean exists() {
        return !Boolean.FALSE.equals(getAttributeValue(SPackageBasic.ATR_EXISTS));
    }

    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> targetClass) {
        return (T) STranslatorForAttribute.of(this, (Class<STranslatorForAttribute>) targetClass);
    }

    @Override
    public <T> T as(Function<SAttributeEnabled, T> aspectFactory) {
        return aspectFactory.apply(this);
    }

    /**
     * Faz cast do tipo atual para a classe da variável que espera receber o valor ou dispara ClassCastException se o
     * destino não for compatível. É um método de conveniência para a interface fluente de construção do tipo, mas com o
     * porem de acusar o erro de cast apenas em tempo de execução.
     */
    public <T> T cast() {
        // TODO (from Daniel) decidir se esse método fica. Quem que devater o
        // assunto?
        return (T) this;
    }

    public final <T extends SView> SType<I> withView(Supplier<T> factory) {
        withView(factory.get());
        return this;
    }

    @SafeVarargs
    public final <T extends SView> SType<I> withView(T mView, Consumer<T>... initializers) {
        for (Consumer<T> initializer : initializers) {
            initializer.accept(mView);
        }
        setView(mView);
        return this;
    }

    /**
     * Listener é invocado quando o campo do qual o tipo depende
     * é atualizado ( a dependencia é expressa via depends on)
     */
    public SType<I> withUpdateListener(IConsumer<I> consumer) {
        asAtr().setAttributeValue(SPackageBasic.ATR_UPDATE_LISTENER, consumer);
        return this;
    }

    private <T extends SView> T setView(Supplier<T> factory) {
        T v = factory.get();
        setView(v);
        return v;
    }

    public void resetToDefaultView() {
        this.withView(SView.DEFAULT);
    }

    public SView getView() {
        return this.view;
    }

    private void setView(SView view) {
        if (view.isApplicableFor(this)) {
            this.view = view;
        } else {
            throw new SingularFormException("A view '" + view.getClass().getName() + "' não é aplicável ao tipo", this);
        }
    }

    public Set<SType<?>> getDependentTypes() {
        Set<SType<?>> result = getDependentTypesInternal();
        return result == null ? Collections.emptySet() : Collections.unmodifiableSet(result);
    }

    private Set<SType<?>> getDependentTypesInternal() {
        if (dependentTypes == null) {
            return superType == null ? null : superType.getDependentTypes();
        }
        Set<SType<?>> resultSuper = superType.getDependentTypesInternal();
        if (resultSuper == null) {
            return dependentTypes;
        }
        Set<SType<?>> sum = new LinkedHashSet<>(resultSuper.size() + dependentTypes.size());
        sum.addAll(resultSuper);
        sum.addAll(dependentTypes);
        return sum;
    }

    public final SType<I> addDependentType(SType<?> type) {
        if (!isDependentType(type)) {
            if (dependentTypes == null) {
                dependentTypes = new LinkedHashSet<>();
            }
            dependentTypes.add(type);
        }
        return this;
    }

    public final boolean isDependentType(SType<?> type) {
        if (dependentTypes != null) {
            for (SType<?> d : dependentTypes) {
                if (type.isTypeOf(d)) {
                    return true;
                }
            }
        }
        return superType != null && superType.isDependentType(type);
    }

    public boolean dependsOnAnyType() {
        return Optional.ofNullable(getAttributeValue(SPackageBasic.ATR_DEPENDS_ON_FUNCTION)).map(Supplier::get).map(
                it -> !it.isEmpty()).orElse(Boolean.FALSE);
    }

    public SType<I> addInstanceValidator(InstanceValidator<I> validator) {
        return addInstanceValidator(ValidationErrorLevel.ERROR, validator);
    }

    public SType<I> addInstanceValidator(ValidationErrorLevel level, InstanceValidator<I> validator) {
        if (validators == null) {
            validators = new ArrayList<>(2);
        }
        validators.add(new ValidationEntry<>(validator, level));
        return this;
    }

    public final boolean hasValidator(InstanceValidator<?> validator) {
        if (validators != null && validators.stream().map(ValidationEntry::getValidator).anyMatch(validator::equals)) {
            return true;
        } else if (superType != null) {
            return superType.hasValidator(validator);
        }
        return false;
    }

    public final List<ValidationEntry<I>> getValidators() {
        List<ValidationEntry<I>> list = superType == null ? Collections.emptyList() : superType.getValidators();
        if (validators != null && !validators.isEmpty()) {
            if (list.isEmpty()) {
                list = new ArrayList<>(validators);
            } else {
                list.addAll(validators);
            }
        }
        return list;
    }

    /**
     * Criar uma nova instância do tipo atual e executa os códigos de inicialização dos tipos
     * se existirem (ver {@link #withInitListener(IConsumer)}}).
     */
    public final I newInstance() {
        return newInstance(true, new SDocument());
    }

    /**
     * Criar uma nova instância do tipo atual. Esse método deve se evitado o uso e preferencialmente usar
     * {@link #newInstance()} sem parâmetros.
     *
     * @param executeInstanceInitListeners Indica se deve executar executa os códigos de inicialização dos tipos se
     *                                     existirem (ver {@link #withInitListener(IConsumer)}}).
     */
    final I newInstance(boolean executeInstanceInitListeners, @Nonnull SDocument owner) {
        I instance = newInstance(this, owner);
        owner.setRoot(instance);
        if (executeInstanceInitListeners) {
            instance.init();
        }
        return instance;
    }

    /**
     * Cria uma nova instância pertencente ao documento informado.
     */
    I newInstance(SDocument owner) {
        return newInstance(this, owner);
    }

    public SIList<?> newList() {
        return SIList.of(this);
    }

    private I newInstance(SType<?> original, SDocument owner) {
        if (instanceClass == null) {
            if (superType != null) {
                return superType.newInstance(original, owner);
            }
            throw new SingularFormException(
                    "O tipo '" + original.getName() + (original == this ? "" : "' que é do tipo '" + getName()) +
                            "' não pode ser instanciado por esse ser abstrato (classeInstancia==null)", this);
        }
        I newInstance = SFormUtil.newInstance(instanceClass);
        newInstance.setDocument(owner);
        newInstance.setType(this);
        SFormUtil.inject(newInstance);
        instanceCount++;
        return newInstance;
    }

    /**
     * Run initialization code for new created instance. Recebe uma referência que
     * pode ser de inicialização lazy.
     */
    @SuppressWarnings("unchecked")
    void init(Supplier<I> instanceRef) {
        IConsumer<I> initListener = asAtr().getAttributeValue(SPackageBasic.ATR_INIT_LISTENER);
        if (initListener != null) {
            initListener.accept(instanceRef.get());
        }
    }

    /**
     * Verifica se a instância informada é do tipo atual, senão dispara exception.
     */
    void checkIfIsInstanceOf(@Nonnull SInstance instance) {
        SFormUtil.verifySameDictionary(this, instance);
        if (!instance.isTypeOf(this)) {
            throw new SingularFormException(
                    "A instância " + instance + " é do tipo " + instance.getType() + ", mas era esperada ser do tipo " +
                            this, instance);
        }
    }

    @Override
    void debug(Appendable appendable, int level) {
        try {
            pad(appendable, level);
            debugTypeHeader(appendable);
            debugAttributes(appendable);
            appendable.append('\n');

            if (this instanceof STypeSimple && asAtrProvider().getProvider() != null) {
                pad(appendable, level + 2).append("selection of ").append(asAtrProvider().getProvider().toString())
                        .append('\n');
            }

            debugAttributesDefined(appendable, level);

            super.debug(appendable, level + 1);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void debugTypeHeader(Appendable appendable) throws IOException {
        if (isAttribute()) {
            debugTypeHeaderAttribute(appendable);
        } else {
            debugTypeHeaderNormalType(appendable);
        }
        if (superType != null && (!isAttribute() || !isSelfReference())) {
            appendable.append(" extend ");
            appendNameAndId(appendable, superType);
            if (complementarySuperType != null) {
                appendable.append(", ");
                appendNameAndId(appendable, complementarySuperType);
            }
            if (this.isList()) {
                STypeList<?, ?> list = (STypeList<?, ?>) this;
                if (list.getElementsType() != null) {
                    appendable.append(" of ");
                    appendNameAndId(appendable, list.getElementsType());
                }
            }
        }
    }

    private void debugTypeHeaderAttribute(Appendable appendable) throws IOException {
        appendable.append("defAtt ").append(getNameSimple()).append('@').append(Integer.toString(getTypeId()));
        SType<?> owner = getAttrInternalRef().getOwner();
        if (owner != null && owner != getParentScope()) {
            appendable.append(" for ");
            appendNameAndId(appendable, owner);
        }
        if (isSelfReference()) {
            appendable.append(" (SELF)");
        }
    }

    private void debugTypeHeaderNormalType(Appendable appendable) throws IOException {
        appendable.append("def ").append(getNameSimple()).append('@').append(Integer.toString(getTypeId()));
        if (superType == null || superType.getClass() != getClass()) {
            appendable.append(" (").append(getClass().getSimpleName());
            if (instanceClass != null && (superType == null || !instanceClass.equals(superType.instanceClass))) {
                appendable.append(":").append(instanceClass.getSimpleName());
            }
            appendable.append(")");
        }
    }

    private void debugAttributes(Appendable appendable) {
        try {
            Collection<SInstance> attrs = getAttributes();
            if (!attrs.isEmpty()) {
                appendable.append(" {");
                attrs.forEach(attr -> debugAttribute(appendable, attr));
                appendable.append("}");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void debugAttribute(Appendable appendable, SInstance attr) {
        try {
            if (!attr.getAttributeInstanceInfo().getRef().isResolved()) {
                appendable.append('?');
            }
            appendable.append(suppressPackage(attr.getAttributeInstanceInfo().getName(), true)).append("=")
                    .append(attr.toStringDisplay()).append("; ");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void debugAttributesDefined(Appendable appendable, int level) {
        AttributeDefinitionManager.getStaticAttributes(attributesDefined).stream().filter(att -> !getLocalTypeOptional(
                att.getNameSimple()).isPresent()).forEach(att -> {
            try {
                pad(appendable, level + 1).append("att ").append(suppressPackage(att.getName())).append(':').append(
                        suppressPackage(att.getSuperType().getName())).append(att.isSelfReference() ? " SELF" : "")
                        .append('\n');
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        });
    }

    private void appendNameAndId(Appendable appendable, SType<?> type) throws IOException {
        appendable.append(suppressPackage(type.getName())).append('@').append(Integer.toString(type.getTypeId()));
    }

    private String suppressPackage(String name) {
        return suppressPackage(name, false);
    }

    private String suppressPackage(String name, boolean aggressive) {
        String thisName = getName();
        if (isEqualsStart(name, thisName)) {
            return name.substring(thisName.length() + 1);
        } else {
            String parentScopeName = getParentScope().getName();
            if (isEqualsStart(name, parentScopeName)) {
                return name.substring(parentScopeName.length() + 1);
            } else if (isEqualsStart(name, SPackageCore.NAME)) {
                String v = name.substring(SPackageCore.NAME.length() + 1);
                if (aggressive && isEqualsStart(v, "SType")) {
                    v = v.substring(6);
                }
                return v;
            } else if (aggressive && isEqualsStart(name, SPackageBasic.NAME)) {
                return name.substring(SPackageBasic.NAME.length() + 1);
            } else if (isEqualsStart(name, getPackage().getName())) {
                return name.substring(getPackage().getName().length() + 1);
            }
        }
        return name;
    }

    @Nullable
    public <T> T convert(@Nullable Object value, @Nonnull Class<T> resultClass) {
        throw new SingularFormException("Método não suportado", this);
    }

    public boolean hasValidation() {
        return isRequired() ||
                getAttributeValue(SPackageBasic.ATR_REQUIRED_FUNCTION) != null ||
                hasValidationInternal();
    }

    private boolean hasValidationInternal() {
        return (validators != null && !validators.isEmpty()) ||
                (superType != null && superType.hasValidationInternal());
    }

    /**
     * Looks for the best match implementation of the aspect being request.
     * <p>To understand the registration and retrieval process see {@link AspectRef}.</p>
     */
    @Nonnull
    public <T> Optional<T> getAspect(@Nonnull AspectRef<T> aspectRef) {
        return getDictionary().getMasterAspectRegistry().getAspect(this, aspectRef);
    }

    /**
     * Returns, if available, the associated implementation of the aspect registered in the local map os aspects. It's
     * only for internal use.
     */
    @Nullable
    final AspectEntry<?, ?> getAspectDirect(int index) {
        return ArrUtil.arrayGet(aspects, index);
    }

    /**
     * Binds locally a specific implementation factory for the aspect. This method should be typically called during the
     * type configuration into {@link SType#onLoadType(TypeBuilder)}.
     * <p>To understand the registration and retrieval process see {@link AspectRef}.</p>
     */
    public <T> SType<I> setAspect(@Nonnull AspectRef<T> aspectRef, @Nonnull Supplier<T> factory) {
        Objects.requireNonNull(aspectRef);
        Objects.requireNonNull(factory);
        Integer index = getDictionary().getMasterAspectRegistry().getIndex(aspectRef);
        AspectEntry<T, Object> entry = new AspectEntry<>(null, factory);
        aspects = ArrUtil.arraySet(aspects, index, entry, AspectEntry.class, 1);
        return this;
    }

    /**
     * Binds locally a specific implementation factory for the aspect. It's the same of
     * {@link SType#setAspect(AspectRef, Supplier)}, but set a instantiated implementation. This method should be
     * typically called during the
     * type configuration into {@link SType#onLoadType(TypeBuilder)}.
     * <p>To understand the registration and retrieval process see {@link AspectRef}.</p>
     */
    public <T> void setAspectFixImplementation(@Nonnull AspectRef<T> aspectRef, @Nonnull T implementation) {
        setAspect(aspectRef, () -> implementation);
    }

    /**
     * Lambda para inicialização da {@link SInstance} desse {@link SType}
     * Esse listener é executa somente no momento em que o tipo é instanciado a primeira vez.
     * Quando a {@link SInstance} persistence é carregada o listener não é executado novamente.
     */
    public SType<I> withInitListener(IConsumer<I> initListener) {
        this.asAtr().setAttributeValue(SPackageBasic.ATR_INIT_LISTENER, initListener);
        return this;
    }

    public SelectionBuilder typelessSelection() {
        this.setView(SViewSelectionBySelect::new);
        return new SelectionBuilder<>(this);
    }

    public <SP extends SimpleProvider<?, ?>> void withSelectionFromProvider(Class<? extends SP> providerClass) {
        this.typelessSelection().selfIdAndDisplay().provider(providerClass);
    }

    public void withSelectionFromProvider(String providerName) {
        this.typelessSelection().selfIdAndDisplay().provider(providerName);
    }

    @SuppressWarnings("unchecked")
    public SType<I> withSelectionFromSimpleProvider(SimpleProvider<?, I> provider) {
        this.typelessSelection().selfIdAndDisplay().simpleProvider(provider);
        return this;
    }

    /**
     * Retorna um identificador único do tipo dentro de um mesmo dicionário.
     */
    final int getTypeId() {
        return this.typeId;
    }

    final void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public String toString() {
        String n = nameFull;
        if (n == null) {
            if (scope == null) {
                n = nameSimple == null ? null : nameSimple.get();
            } else {
                n = scope.getName() + '.' + nameSimple;
            }
        }
        return n + '(' + getClass().getSimpleName() + '@' + typeId + ')';
    }

    final boolean isCallingOnLoadType() {
        return callingOnLoadType;
    }

    final void setCallingOnLoadType(boolean callingOnLoadType) {
        this.callingOnLoadType = callingOnLoadType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return typeId;
    }
}
