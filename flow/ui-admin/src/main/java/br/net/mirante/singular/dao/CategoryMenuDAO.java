package br.net.mirante.singular.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryMenuDAO {

    @Inject
    private SessionFactory sessionFactory;

    private enum ResultColumn {
        definitionId(0, "COD"),
        definitionName(1, "NOD"),
        definitionCod(2, "SGD"),
        categoryId(3, "COC"),
        categoryName(4, "NOC"),
        definitionCounter(5, "QTD");

        private int pos;
        private String alias;

        ResultColumn(int pos, String alias) {
            this.pos = pos;
            this.alias = alias;
        }

        public int getPos() {
            return pos;
        }

        public String getAlias() {
            return alias;
        }
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public List<MenuItemDTO> retrieveAll() {
        List<MenuItemDTO> categories = new ArrayList<>();
        Map<Long, MenuItemDTO> categoriesMap = mountCategories(retrieveCategories());
        categories.addAll(categoriesMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        return categories;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private Map<Long, MenuItemDTO> mountCategories(List<Object[]> rawCategoies) {
        Map<Long, MenuItemDTO> categories = new HashMap<>();
        for (Object[] category : rawCategoies) {
            MenuItemDTO item = categories.get(category[ResultColumn.categoryId.getPos()]);
            if (item == null) {
                item = new MenuItemDTO((Long) category[ResultColumn.categoryId.getPos()],
                        (String) category[ResultColumn.categoryName.getPos()],
                        null, null);
                categories.put((Long) category[ResultColumn.categoryId.getPos()], item);
            }
            item.addItem(new MenuItemDTO((Long) category[ResultColumn.definitionId.getPos()],
                    (String) category[ResultColumn.definitionName.getPos()],
                    (String) category[ResultColumn.definitionCod.getPos()],
                    (Integer) category[ResultColumn.definitionCounter.getPos()]));
        }
        return categories;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> retrieveCategories() {
        String sql = "SELECT DEF.CO_DEFINICAO_PROCESSO AS COD, DEF.NO_PROCESSO AS NOD, DEF.SG_PROCESSO AS SGD,"
                + " CAT.CO_CATEGORIA AS COC, CAT.NO_CATEGORIA AS NOC, COUNT(DISTINCT INS.CO_INSTANCIA_PROCESSO) AS QTD"
                + " FROM TB_DEFINICAO_PROCESSO DEF"
                + " INNER JOIN TB_CATEGORIA CAT ON CAT.CO_CATEGORIA = DEF.CO_CATEGORIA"
                + " INNER JOIN TB_VERSAO_PROCESSO PRO ON DEF.CO_DEFINICAO_PROCESSO = PRO.CO_DEFINICAO_PROCESSO"
                + " LEFT JOIN TB_INSTANCIA_PROCESSO INS ON PRO.CO_VERSAO_PROCESSO = INS.CO_VERSAO_PROCESSO"
                + " WHERE INS.DT_FIM IS NULL AND DEF.se_ativo = 1"
                + " GROUP BY DEF.CO_DEFINICAO_PROCESSO, DEF.NO_PROCESSO, DEF.SG_PROCESSO,"
                + " CAT.CO_CATEGORIA, CAT.NO_CATEGORIA";
        Query query = getSession().createSQLQuery(sql)
                .addScalar(ResultColumn.definitionId.getAlias(), LongType.INSTANCE)
                .addScalar(ResultColumn.definitionName.getAlias(), StringType.INSTANCE)
                .addScalar(ResultColumn.definitionCod.getAlias(), StringType.INSTANCE)
                .addScalar(ResultColumn.categoryId.getAlias(), LongType.INSTANCE)
                .addScalar(ResultColumn.categoryName.getAlias(), StringType.INSTANCE)
                .addScalar(ResultColumn.definitionCounter.getAlias(), IntegerType.INSTANCE);

        return query.list();
    }

    public Object[] retrieveCategoryDefinitionIdsByCode(String processCode) {
        String sql = "SELECT CAT.CO_CATEGORIA AS COC, DEF.CO_DEFINICAO_PROCESSO AS COD"
                + " FROM TB_DEFINICAO_PROCESSO DEF"
                + "  INNER JOIN TB_CATEGORIA CAT ON CAT.CO_CATEGORIA = DEF.CO_CATEGORIA"
                + " WHERE DEF.SG_PROCESSO = :processCode";
        Query query = getSession().createSQLQuery(sql)
                .addScalar(ResultColumn.categoryId.getAlias(), LongType.INSTANCE)
                .addScalar(ResultColumn.definitionId.getAlias(), LongType.INSTANCE)
                .setParameter("processCode", processCode);
        return (Object[]) query.uniqueResult();
    }
}
