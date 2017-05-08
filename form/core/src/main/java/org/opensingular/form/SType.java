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

import org.apache.commons.lang3.StringUtils;
import org.opensingular.form.builder.selection.SelectionBuilder;
import org.opensingular.form.calculation.SimpleValueCalculation;
import org.opensingular.form.context.UIComponentMapper;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.provider.SimpleProvider;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.type.core.SPackageCore;
import org.opensingular.form.validation.IInstanceValidator;
import org.opensingular.form.validation.ValidationErrorLevel;
import org.opensingular.form.view.SView;
import org.opensingular.form.view.SViewSelectionBySelect;
import org.opensingular.lib.commons.lambda.IConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@SInfoType(name = "SType", spackage = SPackageCore.class)
public class SType<I extends SInstance> extends SScopeBase implements SScope, SAttributeEnabled {

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
    protected long instanceCount;
    @Nonnull
    private String nameSimple;
    private String nameFull;
    private SDictionary dictionary;
    /**
     * Representa um identficado único de cada tipo dentro de um dicionário.
     */
    private int typeId;
    private SScope scope;
    private Map<IInstanceValidator<I>, ValidationErrorLevel> instanceValidators;
    private Set<SType<?>> dependentTypes;
    private AttrInternalRef attrInternalRef;
    /**
     * Representa o tipo ao qual o tipo atual extende. Pode ou não ser da mesma classe do tipo atual.
     */
    private SType<I> superType;

    private SView view;

    /**
     * Indica se o tipo está no meio da execução do seu método {@link #onLoadType(TypeBuilder)}.
     */
    private boolean callingOnLoadType;

    public SType() {
        this(null, null);
    }

    protected SType(@Nullable Class<? extends I> instanceClass) {
        this(null, instanceClass);
    }

    protected SType(@Nullable String simpleName, @Nullable Class<? extends I> instanceClass) {
        if (simpleName == null) {
            simpleName = getInfoType().name();
            if (StringUtils.isEmpty(simpleName)) {
                simpleName = getClass().getSimpleName();
            }
        }
        SFormUtil.validateSimpleName(simpleName);
        this.nameSimple = simpleName;
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

    final SInfoType getInfoType() {
        return SFormUtil.getInfoType((Class<? extends SType<?>>) getClass());
    }

    final <S extends SType<?>> S extend(@Nullable String simpleName) {
        if (simpleName == null) {
            simpleName = nameSimple; //Extende usando o mesmo nome do tipo pai
        } else {
            SFormUtil.validateSimpleName(simpleName);
        }
        S newType = (S) MapByName.newInstance(getClass());
        ((SType<I>) newType).nameSimple = simpleName;
        ((SType<I>) newType).superType = this;

        return newType;
    }

    /**
     * Metodo invocado após um tipo ser extendido, de modo, que o mesmo possa extender sub referências que possua.
     */
    protected void extendSubReference() {
        //Se necessário, a classes derivadas vão implementar esse método
    }

    @SuppressWarnings("unchecked")
    final void resolvSuperType(SDictionary dictionary) {
        if (superType == null && getClass() != SType.class) {
            Class<SType> c = (Class<SType>) Objects.requireNonNull(getClass().getSuperclass());
            superType = dictionary.getType(c);
        }
    }

    @Override
    public String getName() {
        return nameFull;
    }

    public String getNameSimple() {
        return nameSimple;
    }

    public SType<I> getSuperType() {
        return superType;
    }

    public Class<I> getInstanceClass() {
        return (Class<I>) instanceClass;
    }

    private Class<I> getInstanceClassResolved() {
        if (instanceClass == null && superType != null) {
            return superType.getInstanceClassResolved();
        }
        return (Class<I>) instanceClass;
    }

    final void setScope(SScope packageScope) {
        this.scope = packageScope;
        this.nameFull = packageScope.getName() + '.' + nameSimple;
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
    public boolean isTypeOf(SType<?> parentTypeCandidate) {
        SType<I> atual = this;
        while (atual != null) {
            if (atual == parentTypeCandidate) {
                return true;
            }
            atual = atual.superType;
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

    final AttrInternalRef getAttrInternalRef() {
        return attrInternalRef;
    }

    final void setAttrInternalRef(AttrInternalRef attrInternalRef) {
        if (this.attrInternalRef != null) {
            throw new SingularFormException("Internal Error: this method shouldn't be called twice");
        }
        this.attrInternalRef = attrInternalRef;
    }

    final SInstance newAttributeInstanceFor(SType<?> typeToBeAppliedAttribute) {
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
        return ref == null ? null : AttributeValuesManagerForSType.findAttributeInstance(this, ref);
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

    @Override
    public <V> void setAttributeCalculation(@Nonnull String attributeFullName, @Nullable String subPath,
            @Nullable SimpleValueCalculation<V> valueCalculation) {
        getAttributesMap().setAttributeCalculation(attributeFullName, subPath, valueCalculation);
    }

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
    @Override
    @Nonnull
    public Optional<SInstance> getAttributeDirectly(@Nonnull String fullName) {
        return AttributeValuesManager.staticGetAttributeDirectly(this, attributes, fullName);
    }

    /** Retorna a instancia do atributo se houver uma associada diretamente ao objeto atual. */
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

    public final boolean hasAttribute(@Nonnull AtrRef<?, ?, ?> atr) {
        AttrInternalRef ref = getDictionary().getAttributeReferenceOrException(atr);
        return AttributeValuesManager.staticGetAttributeDirectly(attributes, ref) != null;
    }

    @Override
    @Nullable
    public final <V> V getAttributeValue(@Nonnull String attributeFullName, @Nullable Class<V> resultClass) {
        return getAttributeValue(getDictionary().getAttributeReferenceOrException(attributeFullName), resultClass);
    }

    @Nullable
    private final <V> V getAttributeValue(@Nonnull AttrInternalRef ref, @Nullable Class<V> resultClass) {
        return AttributeValuesManagerForSType.getAttributeValueInTheContextOf(this, null, ref, resultClass);
    }

    @Nonnull
    private AttrInternalRef mapAttributeName(@Nonnull String originalName) {
        return getDictionary().getAttributeReferenceOrException(originalName);
    }

    public SType<I> with(AtrRef<?, ?, ?> attribute, Object value) {
        setAttributeValue((AtrRef<?, ?, Object>) attribute, value);
        return this;
    }

    public SType<I> withInitialValue(Object value) {
        return with(SPackageBasic.ATR_INITIAL_VALUE, value);

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

    public Object getAttributeValueInitialValue() {
        return getAttributeValue(SPackageBasic.ATR_INITIAL_VALUE);
    }

    public SType<I> withRequired(boolean value) {
        return with(SPackageBasic.ATR_REQUIRED, value);
    }

    public final Boolean isRequired() {
        return getAttributeValue(SPackageBasic.ATR_REQUIRED);
    }

    public SType<I> withExists(Boolean value) {
        return with(SPackageBasic.ATR_EXISTS, value);
    }

    public SType<I> withExists(Predicate<I> predicate) {
        return with(SPackageBasic.ATR_EXISTS_FUNCTION, predicate);
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

    public final <T extends SView> T setView(Supplier<T> factory) {
        T v = factory.get();
        setView(v);
        return v;
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
            if (type.hasDirectOrInderectDependentType(this)) {
                throw new SingularFormException(
                        "Referência circular de dependência detectada ao tentar adicionar " + type +
                                " como dependente de " + this, this);
            }
            if (dependentTypes == null) {
                dependentTypes = new LinkedHashSet<>();
            }
            dependentTypes.add(type);
        }
        return this;
    }

    private boolean hasDirectOrInderectDependentType(SType<?> type) {
        if (dependentTypes != null) {
            for (SType<?> d : dependentTypes) {
                if (type.isTypeOf(d)) {
                    return true;
                } else if (d.hasDirectOrInderectDependentType(type)) {
                    return true;
                }
            }
        } else if (superType != null) {
            return superType.hasDirectOrInderectDependentType(type);
        }
        return false;
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

    public boolean hasDependentTypes() {
        return (dependentTypes != null) && (!dependentTypes.isEmpty());
    }

    public boolean dependsOnAnyType() {
        return Optional.ofNullable(getAttributeValue(SPackageBasic.ATR_DEPENDS_ON_FUNCTION)).map(Supplier::get).map(
                it -> !it.isEmpty()).orElse(Boolean.FALSE);
    }

    public boolean dependsOnAnyTypeInHierarchy() {
        return STypes.listAscendants(this, true).stream().anyMatch(SType::dependsOnAnyType);
    }

    public SType<I> addInstanceValidator(IInstanceValidator<I> validador) {
        return addInstanceValidator(ValidationErrorLevel.ERROR, validador);
    }

    public SType<I> addInstanceValidator(ValidationErrorLevel level, IInstanceValidator<I> validator) {
        if (instanceValidators == null) {
            instanceValidators = new LinkedHashMap<>();
        }
        instanceValidators.put(validator, level);
        return this;
    }

    public Collection<IInstanceValidator<I>> getValidators() {
        Collection<IInstanceValidator<I>> list =
                superType == null ? Collections.emptyList() : superType.getValidators();
        if (instanceValidators != null && !instanceValidators.isEmpty()) {
            if (list.isEmpty()) {
                list = instanceValidators.keySet();
            } else {
                if (!(list instanceof ArrayList)) {
                    ArrayList<IInstanceValidator<I>> list2 = new ArrayList<>(list.size() + instanceValidators.size());
                    list2.addAll(list);
                    list = list2;
                }
                list.addAll(instanceValidators.keySet());
            }
        }
        return list;
    }

    public ValidationErrorLevel getValidatorErrorLevel(IInstanceValidator<I> validator) {
        ValidationErrorLevel level = instanceValidators == null ? null : instanceValidators.get(validator);
        if (level == null && superType != null) {
            level = superType.getValidatorErrorLevel(validator);
        }
        return level;
    }

    @SuppressWarnings("unchecked")
    public I castInstance(SInstance instance) {
        // TODO verificar se essa é a verificação correta
        if (instance.getType() != this) {
            throw new SingularFormException("A instância " + instance + " não é do tipo " + this, this);
        }
        return (I) instance;
    }

    /**
     * Criar uma nova instância do tipo atual e executa os códigos de inicialização dos tipos
     * se existirem (ver {@link #withInitListener(IConsumer)}}).
     */
    public final I newInstance() {
        return newInstance(true);
    }

    /**
     * Criar uma nova instância do tipo atual. Esse método deve se evitado o uso e preferencialmente usar
     * {@link #newInstance()} sem parâmetros.
     *
     * @param executeInstanceInitListeners Indica se deve executar executa os códigos de inicialização dos tipos se
     *                                     existirem (ver {@link #withInitListener(IConsumer)}}).
     */
    public final I newInstance(boolean executeInstanceInitListeners) {
        SDocument owner    = new SDocument();
        I         instance = newInstance(this, owner);
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
        Class<? extends I> c = instanceClass;
        if (c == null && superType != null) {
            return superType.newInstance(original, owner);
        }
        if (instanceClass == null) {
            throw new SingularFormException(
                    "O tipo '" + original.getName() + (original == this ? "" : "' que é do tipo '" + getName()) +
                            "' não pode ser instanciado por esse ser abstrato (classeInstancia==null)", this);
        }
        try {
            I newInstance = instanceClass.newInstance();
            newInstance.setDocument(owner);
            newInstance.setType(this);
            if (newInstance instanceof SISimple) {
                Object valorInicial = original.getAttributeValueInitialValue();
                if (valorInicial != null) {
                    newInstance.setValue(valorInicial);
                }
            }
            instanceCount++;
            return newInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SingularFormException(
                    "Erro instanciando o tipo '" + getName() + "' para o tipo '" + original.getName() + '\'', e);
        }
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
    public void checkIfIsInstanceOf(SInstance instance) {
        if (getDictionary() != instance.getDictionary()) {
            throw new SingularFormException("O dicionário da instância " + instance + " não é o mesmo do tipo " + this +
                    ". Foram carregados em separado", instance);
        }
        for (SType<?> current = instance.getType(); current != null; current = current.getSuperType()) {
            if (current == this) {
                return;
            }
        }
        throw new SingularFormException(
                "A instância " + instance + " é do tipo " + instance.getType() + ", mas era esperada ser do tipo " +
                        this, instance);
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
            if (this.isList()) {
                STypeList<?, ?> lista = (STypeList<?, ?>) this;
                if (lista.getElementsType() != null) {
                    appendable.append(" of ");
                    appendNameAndId(appendable, lista.getElementsType());
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
                attrs.forEach(attr -> {
                    try {
                        if (! attr.getAttributeInstanceInfo().getRef().isResolved()) {
                            appendable.append('?');
                        }
                        appendable.append(suppressPackage(attr.getAttributeInstanceInfo().getName(), true)).append("=")
                                .append(attr.toStringDisplay()).append("; ");
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                });
                appendable.append("}");
            }
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
            }
        }
        return name;
    }

    public <T> T convert(Object value, Class<T> resultClass) {
        throw new SingularFormException("Método não suportado", this);
    }

    public boolean hasValidation() {
        return isRequired() ||
                getAttributeValue(SPackageBasic.ATR_REQUIRED_FUNCTION) != null ||
                hasValidationInternal();
    }

    private boolean hasValidationInternal() {
        return (instanceValidators != null && !instanceValidators.isEmpty()) ||
                (superType != null && superType.hasValidationInternal());
    }

    public <T extends UIComponentMapper> SType<I> withCustomMapper(T mapper) {
        this.asAtr().setAttributeValue(SPackageBasic.ATR_MAPPER, mapper);
        return this;
    }

    public UIComponentMapper getComponentMapper() {
        return this.asAtr().getAttributeValue(SPackageBasic.ATR_MAPPER);
    }

    @SuppressWarnings("unchecked")
    public IConsumer<SInstance> getUpdateListener() {
        return asAtr().getAttributeValue(SPackageBasic.ATR_UPDATE_LISTENER);
    }

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
        return nameFull + '(' + getClass().getSimpleName() + '@' + typeId + ')';
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
