CREATE SEQUENCE DBSINGULAR.SQ_CO_ARQUIVO START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_FORMULARIO  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_VERSAO_ANOTACAO START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_VERSAO_FORMULARIO  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_TIPO_FORMULARIO  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_CONTEUDO_ARQUIVO  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_CACHE_CAMPO  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE DBSINGULAR.SQ_CO_CACHE_VALOR  START WITH 1 INCREMENT BY 1;


ALTER TABLE DBSINGULAR.TB_FORMULARIO  ALTER COLUMN CO_FORMULARIO INTEGER NOT NULL;
ALTER TABLE DBSINGULAR.TB_ARQUIVO ALTER COLUMN 	CO_ARQUIVO INTEGER NOT NULL;
ALTER TABLE DBSINGULAR.TB_VERSAO_FORMULARIO ALTER COLUMN 	CO_VERSAO_FORMULARIO                INTEGER NOT NULL;
ALTER TABLE DBSINGULAR.TB_VERSAO_ANOTACAO_FORMULARIO ALTER COLUMN 	CO_VERSAO_ANOTACAO INTEGER  NOT NULL;
ALTER TABLE DBSINGULAR.TB_TIPO_FORMULARIO ALTER COLUMN 	CO_TIPO_FORMULARIO     INTEGER NOT NULL;
ALTER TABLE DBSINGULAR.TB_CONTEUDO_ARQUIVO ALTER COLUMN 	CO_CONTEUDO_ARQUIVO     INTEGER NOT NULL;
ALTER TABLE DBSINGULAR.TB_CACHE_CAMPO ALTER COLUMN 	CO_CACHE_CAMPO     INTEGER NOT NULL;
ALTER TABLE DBSINGULAR.TB_CACHE_VALOR ALTER COLUMN 	CO_CACHE_VALOR     INTEGER NOT NULL;
