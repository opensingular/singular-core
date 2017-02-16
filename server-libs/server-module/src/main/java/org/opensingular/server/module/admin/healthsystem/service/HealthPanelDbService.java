package org.opensingular.server.module.admin.healthsystem.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.opensingular.lib.support.persistence.util.SqlUtil;
import org.opensingular.server.module.admin.healthsystem.dao.HealthSystemDAO;
import org.opensingular.server.module.admin.healthsystem.db.drivers.DriverOracle;
import org.opensingular.server.module.admin.healthsystem.db.drivers.IValidatorDatabase;
import org.opensingular.server.module.admin.healthsystem.db.objects.ColumnInfo;
import org.opensingular.server.module.admin.healthsystem.db.objects.HealthInfo;
import org.opensingular.server.module.admin.healthsystem.db.objects.TableInfo;
import org.springframework.stereotype.Service;

@Service
public class HealthPanelDbService {

	// TODO NÃO RESOLVER DESSA FORMA, FAZER, FALAR COM VINICIUS
	@Inject
	private DriverOracle driverOracle;

	@Inject
	private HealthSystemDAO saudeDao;

	//TODO REMOVER ESSE ATRIBUTO, RETORNAR CNA FUNCAO DE VERIFICAR DIALETO
	private IValidatorDatabase validator;
	
	public HealthInfo getAllDbMetaData(){
		verificaDialetoUtilizado();
		Map<String, ClassMetadata> map = saudeDao.getAllDbMetaData();
		
		List<TableInfo> tabelas = new ArrayList<>();
		
		map.forEach((k,v)->tabelas.add(getTableInfo(v)));
		
		validator.checkAllInfoTable(tabelas);
		
		return new HealthInfo(tabelas);
	}

	private TableInfo getTableInfo(ClassMetadata v) {
		TableInfo tableInfo = new TableInfo();
		
		AbstractEntityPersister persister = (AbstractEntityPersister) v;
		
		String[] name = SqlUtil.replaceSchemaName(persister.getTableName()).split("\\.");
		tableInfo.setSchema(name[0]);
		tableInfo.setTableName(name[1]);
		
		List<String> colunas = new ArrayList<>();

		String[] propertyNames = v.getPropertyNames();
		
		Arrays.asList(propertyNames).forEach(propertyName->
			colunas.add(persister.getPropertyColumnNames(propertyName)[0]));

		Arrays.asList(persister.getIdentifierColumnNames()).forEach(chave->{
			if(!colunas.contains(chave)){
				colunas.add(chave);
			}
		});
		
		List<ColumnInfo> colunasTypes = new ArrayList<>();
		colunas.forEach(col->colunasTypes.add(new ColumnInfo(col, true)));
		tableInfo.setColumnsInfo(colunasTypes);
		
		return tableInfo;
	}


	// TODO RETORNAR O VALIDATOR AO INVÉS DE SETAR NA CLASSE
	// TODO implementar outros drivers
	private void verificaDialetoUtilizado(){
		if(this.validator == null){
			String hibernateDialect = saudeDao.getHibernateDialect();
			//TODO VERIFICAR SE É SUBCLASSE AO INVÉS DE COMPARAR STRING
			if(hibernateDialect.toLowerCase().contains("oracle") ||
					hibernateDialect.equals("org.hibernate.dialect.Oracle9gDialect")
				|| hibernateDialect.equals("org.hibernate.dialect.Oracle10gDialect")
				|| hibernateDialect.equals("org.hibernate.dialect.Oracle11gDialect")
				|| hibernateDialect.equals("org.hibernate.dialect.Oracle12gDialect")){
				
				validator = driverOracle;
			}else if(hibernateDialect.equals("")){
				
			}else if(hibernateDialect.equals("")){
				
			}
		}
	}
}