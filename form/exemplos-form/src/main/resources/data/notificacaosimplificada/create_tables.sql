

CREATE SCHEMA if not exists DBMEDICAMENTO;
CREATE SCHEMA if not exists DBGERAL;

CREATE TABLE DBMEDICAMENTO.TB_VOCABULARIO_CONTROLADO
(
  CO_SEQ_VOCABULARIO_CONTROLADO NUMBER  NOT NULL,
  CO_TIPO_TERMO                 NUMBER  NOT NULL,
  DS_DESCRICAO                  VARCHAR2(1000),
  ST_REGISTRO_ATIVO             CHAR(1) NOT NULL,
  DS_JUSTIFICATIVA_EXCLUSAO     VARCHAR2(1000),
  DT_CRIACAO                    DATE    NOT NULL,
  PRIMARY KEY (CO_SEQ_VOCABULARIO_CONTROLADO)
);

CREATE TABLE DBMEDICAMENTO.TB_SUBSTANCIA
(
  CO_SUBSTANCIA          NUMBER  NOT NULL,
  DS_SUBSTANCIA_INGLES   VARCHAR2(300),
  DS_FORMA_MOLECULAR     VARCHAR2(200),
  NU_DCB                 VARCHAR2(10),
  NU_CAS                 VARCHAR2(25),
  NU_PESO_MOLECULAR      NUMBER(15, 2),
  NU_PONTO_EBULICAO      NUMBER(15, 2),
  ST_SAL                 CHAR(1) NOT NULL,
  TP_FORMA_FISICA        CHAR(1) NOT NULL,
  NU_PONTO_FUSAO         NUMBER(15, 2),
  DS_ESTRUTURA_MOLECULAR VARCHAR2(200),
  PRIMARY KEY (CO_SUBSTANCIA)
);

CREATE TABLE DBMEDICAMENTO.TB_UNIDADE_MEDIDA_MEDICAMENTO
(
  CO_UNIDADE_MEDIDA_MEDICAMENTO NUMBER       NOT NULL,
  SG_UNIDADE_MEDIDA_MEDICAMENTO VARCHAR2(20) NOT NULL,
  CO_TIPO_UNIDADE_MEDIDA        NUMBER,
  PRIMARY KEY (CO_UNIDADE_MEDIDA_MEDICAMENTO)

);

CREATE TABLE DBMEDICAMENTO.TB_FAIXA_CONCENTRACAO
(
  CO_SEQ_FAIXA_CONCENTRACAO NUMBER       NOT NULL,
  CO_UNIDADE_MEDIDA         NUMBER       NOT NULL,
  NU_FAIXA                  NUMBER(7, 2) NOT NULL,
  DS_SINAL_FAIXA            CHAR(2)      NOT NULL,
  PRIMARY KEY (CO_SEQ_FAIXA_CONCENTRACAO)
);

CREATE TABLE DBMEDICAMENTO.TB_FORMA_FARMACEUTICA_BASICA
(
  CO_FORMA_FARMA_BASICA NUMBER        NOT NULL,
  DS_CONCEITO           VARCHAR2(400) NOT NULL,
  TP_ESTADO_FISICO      CHAR(1)       NOT NULL,
  PRIMARY KEY (CO_FORMA_FARMA_BASICA)
);

CREATE TABLE DBMEDICAMENTO.TB_FORMA_FARM_ESPECIFICA
(
  CO_FORMA_FARM_ESPEC      NUMBER         NOT NULL,
  CO_FORMA_FARMA_BASICA    NUMBER         NOT NULL,
  DS_CONCEITO              VARCHAR2(1000) NOT NULL,
  SG_FORMA_FARM_ESPECIFICA VARCHAR2(30),
  PRIMARY KEY (CO_FORMA_FARM_ESPEC)
);

CREATE TABLE DBMEDICAMENTO.TB_TIPO_UNIDADE_MEDICAMENTO
(
  CO_SEQ_TIPO_UNID_MEDICAMENTO NUMBER NOT NULL,
  DS_TIPO_UNIDADE_MEDICAMENTO  VARCHAR2(255),
  PRIMARY KEY (CO_SEQ_TIPO_UNID_MEDICAMENTO)
);

CREATE TABLE DBMEDICAMENTO.TB_TIPO_TERMO
(
  CO_SEQ_TIPO_TERMO NUMBER        NOT NULL,
  DS_TIPO_TERMO     VARCHAR2(200) NOT NULL,
  NO_ENTIDADE       VARCHAR2(50)  NOT NULL,
  ST_REGISTRO_ATIVO CHAR(1)       NOT NULL,
  PRIMARY KEY (CO_SEQ_TIPO_TERMO)
);

CREATE TABLE DBMEDICAMENTO.TB_EMBALAGEM_PRIMARIA
(
  CO_EMBALAGEM_PRIMARIA NUMBER       NOT NULL,
  SG_EMBALAGEM          VARCHAR2(30) NOT NULL,
  PRIMARY KEY (CO_EMBALAGEM_PRIMARIA)
);

CREATE TABLE DBMEDICAMENTO.TB_EMBALAGEM_SECUNDARIA
(
    CO_EMBALAGEM_SECUNDARIA NUMBER NOT NULL,
    SG_EMBALAGEM_SECUNDARIA VARCHAR2(15) NOT NULL,
    DS_CONCEITO VARCHAR2(400),
    PRIMARY KEY (CO_EMBALAGEM_SECUNDARIA)
);

CREATE TABLE DBMEDICAMENTO.TB_ETAPA_FABRICACAO
(
    CO_ETAPA_FABRICACAO NUMBER NOT NULL,
    TP_ETAPA_FABRICACAO CHAR(1) NOT NULL,
    PRIMARY KEY (CO_ETAPA_FABRICACAO)
);

CREATE TABLE DBMEDICAMENTO.TB_LINHA_CBPF
(
    CO_LINHA_CBPF NUMBER NOT NULL,
    ST_LINHA_RESTRITIVA CHAR(1) NOT NULL,
    PRIMARY KEY (CO_LINHA_CBPF)
);

CREATE TABLE DBGERAL.TB_CIDADE
(
    CO_SEQ_CIDADE NUMBER(6)NOT NULL,
    NO_CIDADE VARCHAR2(50) NOT NULL,
    NO_ANTERIOR VARCHAR2(50),
    CO_PAIS NUMBER(6),
    CO_UF VARCHAR2(2) NOT NULL,
    SG_CIDADE VARCHAR2(3),
    NU_DDD VARCHAR2(4),
    NU_CEP VARCHAR2(8),
    CO_MUNICIPIO_IBGE VARCHAR2(6),
    CO_MUNICIPIO_SIAFI VARCHAR2(4),
    CO_MUNICIPIO_INSS VARCHAR2(5),
    CO_MUNICIPIO_CORREIO VARCHAR2(6),
    CO_PAIS_SIAFI VARCHAR2(3),
    ST_COMUNIDADE_SOLIDARIA VARCHAR2(1) NOT NULL,
    ST_SECA VARCHAR2(1) NOT NULL,
    ST_PRMI VARCHAR2(1) NOT NULL,
    ST_CALAMIDADE VARCHAR2(1) NOT NULL,
    ST_INDIGENA VARCHAR2(1) NOT NULL,
    CO_DV_MUNICIPIO_IBGE VARCHAR2(1),
    CO_MUNICIPIO_CNES VARCHAR2(6)
);

CREATE TABLE DBGERAL.TB_EMPRESA_INTERNACIONAL
(
    CO_SEQ_EMPRESA_INTERNACIONAL NUMBER(6) NOT NULL,
    NO_RAZAO_SOCIAL VARCHAR2(130) NOT NULL,
    NO_FANTASIA VARCHAR2(120),
    CO_TIPO_EMPRESA NUMBER(3),
    ST_CONTROLE_ESPECIAL CHAR(1) NOT NULL,
    DS_CODIGO_GGINP VARCHAR2(6),
    ST_REGISTRO_ATIVO CHAR(1)
);

CREATE TABLE DBGERAL.TB_ENDERECO_EMP_INTERNACIONAL
(
    CO_EMPRESA_INTERNACIONAL NUMBER(6) NOT NULL,
    NU_SEQ_ENDERECO NUMBER(5) NOT NULL,
    DS_RUA_EMPRESA VARCHAR2(200),
    NU_ENDERECO_EMPRESA VARCHAR2(10),
    NO_BAIRRO_EMPRESA VARCHAR2(30),
    NU_CEP_EMPRESA VARCHAR2(15),
    NU_TELEFONE_EMPRESA VARCHAR2(15),
    NU_FAX_EMPRESA VARCHAR2(15),
    DS_EMAIL_EMPRESA VARCHAR2(70),
    DT_CADASTRAMENTO DATE DEFAULT sysdate,
    CO_CIDADE NUMBER(6),
    CO_PAIS NUMBER(6),
    DS_CIDADE_ESTRANGEIRA VARCHAR2(50)
);

CREATE TABLE DBGERAL.TB_PAIS
(
    CO_SEQ_PAIS NUMBER(6) NOT NULL,
    NO_PAIS VARCHAR2(50) NOT NULL,
    CO_GRUPO_PAIS NUMBER(6),
    DS_NACIONALIDADE VARCHAR2(50),
    CO_PAIS_SIAPE VARCHAR2(3),
    SG_PAIS VARCHAR2(3),
    ST_EXIGE_CIV VARCHAR2(1),
    CO_PAIS_PAF VARCHAR2(3),
    ST_BRASIL_EXIGE_CIV VARCHAR2(1),
    SG_ISO_A2 VARCHAR2(2),
    SG_ISO_A3 VARCHAR2(3),
    CO_ISO NUMBER(3),
    NU_DDI VARCHAR2(4),
    CO_CONTINENTE NUMBER(1),
    ST_MERCOSUL VARCHAR2(1) NOT NULL
);

CREATE TABLE DBGERAL.TB_UNIDADE_FEDERACAO
(
    CO_UF VARCHAR2(2) NOT NULL,
    NO_UF VARCHAR2(50) NOT NULL,
    CO_REGIAO_BRASIL NUMBER(1),
    NU_ORDEM_REGIAO NUMBER(2),
    DS_NATURALIDADE VARCHAR2(50),
    CO_UF_SIAFI VARCHAR2(2),
    CO_UF_INSS VARCHAR2(2),
    CO_CIDADE_CAPITAL NUMBER(6) NOT NULL,
    CO_UF_IBGE NUMBER(2),
    CO_UF_PAF VARCHAR2(2)
);


CREATE TABLE DBMEDICAMENTO.TB_NATUREZA_MEDICAMENTO
(
    CO_NATUREZA_MEDICAMENTO NUMBER NOT NULL
);


CREATE TABLE DBMEDICAMENTO.TB_CATEG_REGULATORIA_MEDICAMEN
(
    CO_CATEG_REGULA_MEDICAMEN NUMBER NOT NULL,
    CO_NATUREZA_MEDICAMENTO NUMBER NOT NULL
);

CREATE TABLE DBMEDICAMENTO.TB_FARMACOPEIA
(
  CO_FARMACOPEIA NUMBER NOT NULL
);