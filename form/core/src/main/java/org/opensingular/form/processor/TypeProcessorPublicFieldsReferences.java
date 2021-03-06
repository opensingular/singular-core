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

package org.opensingular.form.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opensingular.form.SScope;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.STypeList;
import org.opensingular.form.SingularFormException;

import com.google.common.collect.ImmutableMap;

/**
 * Garante que os campos publicos nas classes derivadas de {@link STypeComposite} são corretamente configurados e
 * preenchidos, na primeira definição do tipo, e garante que os campos públicos são preenchidos corretamente  quanto
 * das extenssão do tipo.
 *
 * @author Daniel C. Bordin
 */
public class TypeProcessorPublicFieldsReferences implements TypeProcessorPosRegister {

    public final static TypeProcessorPublicFieldsReferences INSTANCE = new TypeProcessorPublicFieldsReferences();

    @Override
    public void processTypePreOnLoadTypeCall(SType<?> type) {
        if (isNotDerivedClassOfSTypeComposite(type)) {
            return;
        }
        STypeComposite composite = (STypeComposite) type;
        CompositePublicInfo info = getPublicInfo(composite.getClass());
        propagatePublicFieldsToExtendedComposite(composite, info, true);
    }

    @Override
    public void processTypePosRegister(SType<?> type, boolean onLoadCalled) {
        if (isNotDerivedClassOfSTypeComposite(type)) {
            return;
        }
        STypeComposite composite = (STypeComposite) type;
        CompositePublicInfo info = getPublicInfo(composite.getClass());
        if (info.isNotEmpty()) {
            if (onLoadCalled) {
                if (!info.isPublicFieldsMatched()) {
                    verifyIfAllCompositeFieldsArePublicJavaFields(composite, info);
                    verifyIfAllPublicFieldsAreValid(composite, info);
                    info.setPublicFieldsMatched();
                }
                if (info.isNotEmpty()) {
                    verifyAllFieldsCorrectedFilled(composite, info);
                }
            } else {
                propagatePublicFieldsToExtendedComposite(composite, info, false);
            }
        }
    }

    private boolean isNotDerivedClassOfSTypeComposite(SType<?> type) {
        return !type.isComposite() || type.getClass() == STypeComposite.class;
    }

    private void propagatePublicFieldsToExtendedComposite(STypeComposite composite, CompositePublicInfo info,
            boolean preOnLoad) {
        info.checkSanity(composite.getClass());
        for (PublicFieldRef ref : info) {
            SType<?> newFieldValue = composite.getField(ref.getName());
            if (newFieldValue == null) {
                if (composite.getSuperType().getClass() == composite.getClass() || info.isFieldFromSuperType(composite,
                        ref)) {
                    newFieldValue = copyFieldValueFromSuperType(composite, ref);
                }
            }

            if (newFieldValue != null) {
                setJavaField(composite, ref, newFieldValue);
            } else if (!preOnLoad) {
                throw new SingularFormException(erroValue(composite, null, ref, null,
                        "Erro tentando setar valor na instância extendida de " + composite +
                                " pois não foi encontrado o valor para atribuir ao campo " + ref.getName()));
            }
        }
    }

    /**
     * Copia o valor de um field public do tipo pai do composite para o composite atual já fazendo a devida correção de
     * apontamento para o subtipo. Tipicamente copia um campo que foi declarado no pai para não deixar o field no tipo
     * extendido null.
     */
    private SType<?> copyFieldValueFromSuperType(STypeComposite composite, PublicFieldRef ref) {
        SType<?> parentValue;
        try {
            parentValue = (SType<?>) ref.getField().get(composite.getSuperType());
        } catch (IllegalAccessException e) {
            throw new SingularFormException(erroValue(composite, null, ref, null,
                    "Erro tentando ler valor do campo Java " + ref.getName() + " em " +
                            composite.getSuperType() + ", que é a instância pai de " + composite), e);
        }
        if (parentValue == null) {
            throw new SingularFormException(erroValue(composite, null, ref, null,
                    "O valor do campo Java " + ref.getName() + " está null em " +
                            composite.getSuperType() + ", que é a instância pai de " + composite + ""));
        }
        SType<?> newFieldValue = tryToFindInHierarchy(composite, parentValue.getParentScope(),parentValue.getNameSimple());
        if (newFieldValue != null) {
            //Verificação de sanidade do resultado
            if (newFieldValue.getSuperType() != parentValue) {
                throw new SingularFormException(erroValue(composite, null, ref, null,
                        "O valor encontrado para atribuir ao campo Java '" + ref.getName() +
                                "' em " + composite + " foi\n       encontrado: " + newFieldValue +
                                "\ne esse não é uma extensão da referência do pai\n             pai: " +
                                parentValue));
            }
        }
        return newFieldValue;
    }

    /** Seta o valor do field Java com o valor informado. */
    private void setJavaField(STypeComposite composite, PublicFieldRef ref, SType<?> newFieldValue) {
        try {
            ref.getField().set(composite, newFieldValue);
        } catch (IllegalAccessException e) {
            throw new SingularFormException(erroValue(composite, newFieldValue, ref, null,
                    "Erro tentando setar valor na instância extendida de " + composite.getClass().getName()), e);
        }
    }

    /** Resolve o valor encontrado em referencia ao tipo destino, mesmo se for um sub campo de um sub campo. */
    private SType<?> tryToFindInHierarchy(STypeComposite composite, SScope scope, String nextType) {
        if (!(scope instanceof SType)) {
            return null;
        }
        SType<?> parent = (SType<?>) scope;
        if (composite.getSuperType() == parent) {
            return composite.getLocalType(nextType);
        }
        SType<?> newParent = tryToFindInHierarchy(composite, parent.getParentScope(), parent.getNameSimple());
        return (newParent == null) ? null : newParent.getLocalType(nextType);
    }

    private void verifyAllFieldsCorrectedFilled(STypeComposite<?> composite, CompositePublicInfo info) {
        for (PublicFieldRef ref : info) {
            SType<?> currentValue = ref.getCurrentFieldValue(composite);
            SType<?> expectedType = composite.getField(ref.getName());
            if (expectedType == null) {
                if (currentValue == null) {
                    throw new SingularFormException(
                            erroValue(composite, null, ref, null, "O campo java deveria ter um valor, mas está null"));
                } else if (!isTypeChildrenOf(composite, currentValue)) {
                    throw new SingularFormException(erroValue(composite, null, ref, currentValue,
                            "O campo java tem um tipo que não é filho direto (ou indireto) de " + composite));
                }
            } else if (currentValue == null) {
                setJavaField(composite, ref, expectedType);
            } else if (currentValue != expectedType) {
                String expectedTypeNameSimple = expectedType.getNameSimple();
                throw new SingularFormException(erroValue(composite, expectedType, ref, currentValue,
                        "O field java público '" + expectedTypeNameSimple +
                                "' da classe " + composite.getClass().getSimpleName() +
                                " deveria ter o valor do atributo '" + expectedTypeNameSimple + "' do type"));
            }
        }
    }

    private boolean isTypeChildrenOf(STypeComposite<?> composite, SType<?> currentValue) {
        for (SScope parent = currentValue.getParentScope(); parent != null; parent = parent.getParentScope()) {
            if (parent == composite) {
                return true;
            }
        }
        return false;
    }

    private void verifyIfAllCompositeFieldsArePublicJavaFields(STypeComposite<?> composite, CompositePublicInfo info) {
        for (SType<?> field : composite.getFields()) {
            PublicFieldRef ref = info.getPublicField(field.getNameSimple());
            if (ref == null) {
                throw new SingularFormException(errorMsg(composite, field, null, true,
                        "Não foi encontrado o campo publico esperado"));
            }

            if (!(ref.getField().getType().isAssignableFrom(field.getClass()))) {
                throw new SingularFormException(errorMsg(composite, field, ref, true,
                        "Foi encontrado o campo na classe mas o mesmo não é da classe " + field.getClass()));
            }
            if (Modifier.isStatic(ref.getField().getModifiers())) {
                throw new SingularFormException(errorMsg(composite, field, ref, true,
                        "Foi encontrado o campo na classe mas o mesmo não pode ser static"));

            } else if (!Modifier.isPublic(ref.getField().getModifiers())) {
                throw new SingularFormException(errorMsg(composite, field, ref, true,
                        "Foi encontrado o campo na classe mas o mesmo têm que ser public e não é"));
            }
        }
    }

    private void verifyIfAllPublicFieldsAreValid(STypeComposite composite, CompositePublicInfo info) {
        for (PublicFieldRef ref : info) {
            SType<?> type = composite.getField(ref.getName());
            //if (type == null && ! isInsideChildrenTypes(composite, ref.getName())) {
            //    throw new SingularFormException(errorMsg(composite, null, ref, true,
            //            "Foi encontrado um campo na classe para o qual não existe campo na estrutura de dados do " +
            //                   "composite\n Esperado  : que existisse o campo '" + ref.getName() + "' em
            // " +
            //                    composite + " ou em um campo filho"));
            //}
            if (!SType.class.isAssignableFrom(ref.getField().getType()) &&
                    (type == null || ref.getField().getType().isAssignableFrom(type.getClass()))) {
                throw new SingularFormException(errorMsg(composite, type, ref, type != null,
                        "Foi encontrado um campo na classe o qual se esperava que fosse de um tipo derivado de SType," +
                                " mas em vez disso é do tipo " + ref.getField().getType()));

            }
        }
    }

    private boolean isInsideChildrenTypes(SType<?> type, String typeName) {
        if (type.getNameSimple().equals(typeName)) {
            return true;
        } else if (type.isComposite()) {
            for (SType<?> children : ((STypeComposite<?>) type).getFields()) {
                if (isInsideChildrenTypes(children, typeName)) {
                    return true;
                }
            }
        } else if (type.isList()) {
            return isInsideChildrenTypes(((STypeList<?, ?>) type).getElementsType(), typeName);
        }
        return false;
    }

    private String errorMsg(STypeComposite composite, SType<?> fieldType, PublicFieldRef ref, boolean showExpected,
            String msg) {
        String m = "Na classe " + composite.getClass().getName();
        if (fieldType != null) {
            m += " para o campo do composite '" + fieldType + "'";
        }
        m += ": " + msg;
        if (showExpected) {
            if (fieldType == null) {
                m += "\n Esperado  : 'nenhum field Java de nome '" + ref.getName() + "'";
            } else {
                m += "\n Esperado  : public " + fieldType.getClass().getSimpleName() + " " + fieldType.getNameSimple() +
                        ";";
            }
        }
        if (ref != null) {
            m += "\n Encontrado: " + getFieldDescription(ref);
        }
        return m;
    }

    private String erroValue(STypeComposite composite, SType<?> expectedType, PublicFieldRef ref, SType<?> currentValue,
            String msg) {
        String m = "Erro no atributo público da classe do composite: " + msg;
        m += "\n SType           : " + composite;
        m += "\n Campo composite : " + expectedType;
        m += "\n Classe          : " + composite.getClass();
        m += "\n Field Java      : " + getFieldDescription(ref);
        m += "\n Valor Field Java: " + currentValue;
        return m;
    }

    private String getFieldDescription(PublicFieldRef ref) {
        return Modifier.toString(ref.getField().getModifiers()) + " " + ref.getField().getType().getSimpleName() + " " +
                ref.getName() + ";";
    }

    private static CompositePublicInfo getPublicInfo(Class<?> typeClass) {
        return ClassInspectionCache.getInfo(typeClass, ClassInspectionCache.CacheKey.PUBLIC_INFO,
                TypeProcessorPublicFieldsReferences::readPublicFields);
    }

    private static CompositePublicInfo readPublicFields(Class<?> aClass) {
        CompositePublicInfo info = new CompositePublicInfo(aClass);
        Field[] fields = aClass.getFields();
        for (Field field : fields) {
            int mods = field.getModifiers();
            if (Modifier.isPublic(mods) && !(Modifier.isStatic(mods) || Modifier.isFinal(mods) ||
                    !SType.class.isAssignableFrom(field.getType()))) {
                info.add(new PublicFieldRef(field));
            }
        }
        info.finisheLoad();

        return info;
    }

    /** Verifica se a classe é do usuário ou da implementação core. */
    private static boolean isNotCoreClass(Class<?> aClass) {
        if (aClass.getPackage() == SType.class.getPackage()) {
            return !(aClass.getSuperclass() == SType.class || aClass == SType.class);
        }
        return true;
    }

    private static class CompositePublicInfo implements Iterable<PublicFieldRef> {

        private final Class<?> targetClass;
        private Map<String, PublicFieldRef> refs;

        /**
         * Indica que a classe já foi especionada com sucesso uma vez.
         */
        private boolean publicFieldsMatched;
        private Class<?> superTypeClass;

        public CompositePublicInfo(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

        public boolean isPublicFieldsMatched() {
            return publicFieldsMatched;
        }

        public void setPublicFieldsMatched() {publicFieldsMatched = true;}

        private void add(PublicFieldRef ref) {
            if (refs == null) {
                refs = new HashMap<>();
            }
            refs.put(ref.field.getName(), ref);
        }

        final void finisheLoad() {
            refs = refs == null ? Collections.emptyMap() : ImmutableMap.copyOf(refs);
        }

        @Override
        public Iterator<PublicFieldRef> iterator() {
            return refs == null ? Collections.emptyIterator() : refs.values().iterator();
        }

        public PublicFieldRef getPublicField(String nameSimple) {
            return refs == null ? null : refs.get(nameSimple);
        }

        public boolean isNotEmpty() {
            return !refs.isEmpty();
        }

        // Garante a carga das informações da classe do super tipo foram lidas
        public void ensureSuperTypeInfoWasReaded(SType<?> target) {
            Class<? extends SType> superClass = target.getSuperType().getClass();
            if (superTypeClass == null) {
                if (isNotEmpty() && isNotCoreClass(superClass)) {
                    CompositePublicInfo superInfo = getPublicInfo(superClass);
                    for(PublicFieldRef refSuper : superInfo) {
                        getPublicField(refSuper.getName()).setFieldCameFromSuperType(true);
                    }
                }
                superTypeClass = superClass;
            } else {
                checkClassSanity(superClass, superTypeClass);
            }
        }

        /** Verifica se o field Java em questão deve ser lido dos fields da classe do super tipo */
        public boolean isFieldFromSuperType(STypeComposite composite, PublicFieldRef ref) {
            ensureSuperTypeInfoWasReaded(composite);
            return ref.isFieldFromSuperType();
        }

        /** Verifica se a info atual se refere a classe do tipo informado como esperado. */
        private void checkSanity(Class<?> expectedTargetClass) {
            checkClassSanity(expectedTargetClass, targetClass);
        }
    }

    /** Verifica se a info atual se refere a classe do tipo informado como esperado. */
    private static void checkClassSanity(Class<?> expectedClass, Class<?> currentClass) {
        if (currentClass != expectedClass) {
            throw new SingularFormException(
                    "Era esperado que o metadado fosse para a classe " + expectedClass.getName() +
                            " mas na verdade o metadado é da classe " + currentClass.getName());
        }
    }


    /**
     * Representa um field público da classe
     */
    private static class PublicFieldRef {

        private final Field field;
        private boolean fieldCameFromSuperType;

        private PublicFieldRef(Field field) {this.field = field;}

        /**
         * Retorna, via reflection, o valor atualmente atribuido ao campo público.
         */
        public SType<?> getCurrentFieldValue(STypeComposite composite) {
            try {
                return (SType<?>) field.get(composite);
            } catch (IllegalAccessException e) {
                throw new SingularFormException("Erro lendo campo '" + field + " em " + composite, e);
            }
        }

        public Field getField() {
            return field;
        }

        public void setFieldCameFromSuperType(boolean fieldCameFromSuperType) {
            this.fieldCameFromSuperType = fieldCameFromSuperType;
        }

        public boolean isFieldFromSuperType() {
            return fieldCameFromSuperType;
        }

        public String getName() {
            return field.getName();
        }
    }
}
