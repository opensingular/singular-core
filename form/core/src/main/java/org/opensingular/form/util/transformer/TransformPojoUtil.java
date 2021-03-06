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

package org.opensingular.form.util.transformer;

import org.opensingular.form.SIComposite;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.SingularFormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransformPojoUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformPojoUtil.class);

    private static Set<Class<?>> STOP_CRITERY = defineStopCritery();

    private static Set<Class<?>> defineStopCritery() {
        Set<Class<?>> stop = new HashSet<>();

        stop.add(Byte.class);
        stop.add(Integer.class);
        stop.add(Short.class);
        stop.add(Long.class);
        stop.add(Float.class);
        stop.add(Double.class);
        stop.add(String.class);
        stop.add(Boolean.class);

        stop.add(BigDecimal.class);
        stop.add(BigInteger.class);

        return stop;
    }

    /**
     * metodo para transformar um objeto em um map com os valores dele
     *
     * @param objectToConvert o objeto que deseja obter o map
     * @return retorna o Map com o objeto que se queria converter e todos os objetos pendurados a ele,
     * para recuperá-lo deve-se usar System.identityHashCode() passando o objectToConvert como argumento.
     * @throws RuntimeException quando o Objeto a ser mapeado possui um map dentro dele. Não foi implementado ainda.
     */
    public static Map<Integer, Map<String, Object>> pojoToMap(Object objectToConvert) {

        Map<Integer, Map<String, Object>> fieldsTST = new HashMap<>();

        fieldsTST.put(System.identityHashCode(objectToConvert), new HashMap<>());

        Map<String, Object> mapRootObj = new HashMap<>();
        fieldsTST.put(System.identityHashCode(objectToConvert), mapRootObj);

        Arrays.asList(objectToConvert.getClass().getDeclaredFields())
                .forEach(f -> convertObjectToMap(fieldsTST, f, objectToConvert));

        return fieldsTST;
    }

    private static void convertObjectToMap(Map<Integer, Map<String, Object>> mapMain, Field field, Object objectToConvert) {
        field.setAccessible(true);
        Map<String, Object> map = mapMain.get(System.identityHashCode(objectToConvert));
        if (map == null) {
            map = new HashMap<>();
            mapMain.put(System.identityHashCode(objectToConvert), map);
        }

        try {
            verifyTypeOfAField(mapMain, field, objectToConvert, map);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void verifyTypeOfAField(Map<Integer, Map<String, Object>> mapMain, Field field,
                                           Object objectToConvert, Map<String, Object> map) throws IllegalAccessException {
        Class<?> type = field.getType();
        // Verifica qual o tipo de campo
        if (Collection.class.isAssignableFrom(type)) {
            fieldIsACollection(mapMain, field, objectToConvert, map);
        } else if (Map.class.isAssignableFrom(type)) {
            fieldIsAMap(mapMain, field, objectToConvert, map);
        } else {
            fieldIsAObjectClass(mapMain, field, objectToConvert, map, type);
        }
    }

    // TODO Implementar futuramente
    private static void fieldIsAMap(Map<Integer, Map<String, Object>> mapMain, Field field, Object objectToConvert, Map<String, Object> map) {
        throw new UnsupportedOperationException("Não é suportado atualmente o mapeamento de map.");
    }

    private static void fieldIsAObjectClass(Map<Integer, Map<String, Object>> mapMain, Field field,
                                            Object objectToConvert, Map<String, Object> map, Class<?> type) throws IllegalAccessException {
        // caso contrario é um obj ou um tipo normal

        Object obj = field.get(objectToConvert);

        if (type.isPrimitive() || STOP_CRITERY.contains(type)) {
            map.put(field.getName(), obj);
        } else {
            fieldIsANonPrimitiveObject(mapMain, field, map, type, obj);
        }
    }

    private static void fieldIsANonPrimitiveObject(Map<Integer, Map<String, Object>> mapMain, Field field, Map<String, Object> map, Class<?> type, Object obj) {
        if (obj == null) {
            map.put(field.getName(), null);
        } else {
            // se ja existe, coloca-se apenas a referencia do obj
            if (mapMain.containsKey(System.identityHashCode(obj))) {
                map.put(field.getName(), "codRef=" + System.identityHashCode(obj));
            } else {
                // senao, gera-se o dados necessarios
                Map<String, Object> mapChildItem = new HashMap<>(); // cria o mapa do obj filho
                mapMain.put(System.identityHashCode(obj), mapChildItem); // add no mapa de referencia
                map.put(field.getName(), mapChildItem); // add no obj pai

                Arrays.asList(type.getDeclaredFields()).forEach(f -> {
                    try {
                        convertObjectToMap(mapMain, f, obj);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
            }
        }
    }

    private static void fieldIsACollection(Map<Integer, Map<String, Object>> mapMain, Field field,
                                           Object objectToConvert, Map<String, Object> map) throws IllegalAccessException {
        Collection<?> objCollection = (Collection<?>) field.get(objectToConvert);

        if (objCollection == null) {
            map.put(field.getName(), null);
        } else {
            Iterator<?> iterator = objCollection.iterator();
            if (iterator.hasNext()) {
                List<Object> colecao = populateCollection(mapMain, iterator);
                map.put(field.getName(), colecao);
            } else {
                map.put(field.getName(), null);
            }
        }
    }

    private static List<Object> populateCollection(Map<Integer, Map<String, Object>> mapMain, Iterator<?> iterator) {
        List<Object> colecao = new ArrayList<>();

        while (iterator.hasNext()) {
            Object item = iterator.next();
            if (item.getClass().isPrimitive() || STOP_CRITERY.contains(item.getClass())) {
                // maneira feita para garantir o tipo primitivo de ser colocado, e de se ter uma lista dele no SInstance
                Map<String, Object> itemMap = new HashMap<>();

                String[] value = item.getClass().getName().split("\\.");
                itemMap.put(value[value.length - 1], item);

                colecao.add(itemMap);
            } else {
                Map<String, Object> itemMap = mapMain.get(System.identityHashCode(item));
                if (itemMap == null) {
                    itemMap = new HashMap<>();
                    mapMain.put(System.identityHashCode(item), itemMap);
                }
                Arrays.asList(item.getClass().getDeclaredFields()).forEach(f -> convertObjectToMap(mapMain, f, item));
                colecao.add(itemMap);
            }
        }
        return colecao;
    }

    /**
     * @param pojoReferenceDataMap O map com os valores do Pojo e das subEntidades dele
     * @param pojo                 O objeto a ser colocado no SInstance
     * @param rootInstance         O SInstance a ser preenchido com o Pojo dado
     * @param strictMode           boolean
     *                             - se TRUE então o preenchimento é feito se o valor existe no map e nao existe no SInstance dá erro
     *                             - se FALSE então o preenchimento é permissivo, ou seja, só coloca os valores existentes da SInstance e nao se importa com os que nao existem no mapa
     * @return a propria instancia com os valores preenchidos
     * @throws Exception quando o strictMode é true e temos um valor existente no mapa que não é encontrado na SInstance.
     */
    public static SInstance mapToSInstace(Map<Integer, Map<String, Object>> pojoReferenceDataMap, Object pojo, SInstance rootInstance, boolean strictMode) {

        Map<String, Object> mapOfObject = pojoReferenceDataMap.get(System.identityHashCode(pojo));

        realMapToSInstance(pojoReferenceDataMap, mapOfObject, rootInstance, strictMode);

        return rootInstance;
    }

    @SuppressWarnings("unchecked")
    private static void realMapToSInstance(Map<Integer, Map<String, Object>> pojoReferenceDataMap, Object pojoDataMap, SInstance rootInstance, boolean strictMode) {
        SType<?> type = rootInstance.getType();
        if (type.isComposite()) {
            mapToSIComposite(pojoReferenceDataMap, (Map<String, Object>) pojoDataMap, (SIComposite) rootInstance, strictMode);
        } else if (type.isList()) {
            mapToSIList(pojoReferenceDataMap, (Map<String, Object>) pojoDataMap, (SIList<SInstance>) rootInstance, strictMode, type);
        } else {
            rootInstance.setValue(((Map<String, Object>) pojoDataMap).get(type.getNameSimple()));
        }
    }

    private static void mapToSIComposite(Map<Integer, Map<String, Object>> pojoReferenceDataMap, Map<String, Object> pojoDataMap, SIComposite rootInstance, boolean strictMode) {
        SIComposite composite = rootInstance;

        if (strictMode) {
            // verifica se os valores existem em ambos os lugares
            List<String> attributeNames = new ArrayList<>();
            composite.getAllChildren().forEach(inst -> attributeNames.add(inst.getType().getNameSimple()));
            Set<String> keySet = pojoDataMap.keySet();
            for (String string : keySet) {
                if (!attributeNames.contains(string)) {
                    throw new SingularFormException("Valor existente no mapa não encontrado no SInstance.");
                }
            }
        }
        for (SInstance child : composite.getAllChildren()) {
            Object object = pojoDataMap.get(child.getType().getNameSimple());
            // pega o objeto ou mapa que é referenciado

            Map<String, Object> mapNew = new HashMap<>();
            if (child.getType().isComposite()) {
                /*Caso ele tenha uma referencia já colocada, ela estará no pojoReferenceDataMap
                 * essa referencia terá atributos repetidos, mas por causa do strict mode, só colocará os que forem
                 * especificados no STYPE chamador, ou seja, nao vai entrar em recursão enchendo a heap se quem chamou nao repetir os atributos*/
                // TODO verificar quando tiver referencia circular
                if (object instanceof String && ((String) object).contains("codRef=")) {
                    String[] split = ((String) object).split("=");
                    mapNew = pojoReferenceDataMap.get(Integer.valueOf(split[split.length - 1]));
                } else {
                    mapNew = (Map<String, Object>) object;
                }
            } else {
                // mapa criado pra garantir que teremos a referencia do objeto salva(key)
                mapNew.put(child.getType().getNameSimple(), object);
            }


            realMapToSInstance(pojoReferenceDataMap, mapNew, child, strictMode);
        }
    }

    private static void mapToSIList(Map<Integer, Map<String, Object>> pojoReferenceDataMap, Map<String, Object> pojoDataMap, SIList<SInstance> rootInstance, boolean strictMode, SType<?> type) {
        SIList<SInstance> sIList = rootInstance;
        List<Object>      list   = (List<Object>) pojoDataMap.get(type.getNameSimple());

        if (list != null) {
            while (sIList.size() < list.size()) {
                sIList.addNew();
            }

            Iterator<SInstance> iterator = sIList.iterator();
            int                 contador = 0;
            while (iterator.hasNext()) {
                realMapToSInstance(pojoReferenceDataMap, list.get(contador), iterator.next(), strictMode);
                contador++;
            }
        }
    }

    public static SInstance pojoToSInstance(Object objectToConvert, SInstance instance, boolean strictMode) {
        Map<Integer, Map<String, Object>> pojoToMap = pojoToMap(objectToConvert);
        return mapToSInstace(pojoToMap, objectToConvert, instance, strictMode);
    }
}
