package br.net.mirante.singular.exemplos.notificacaosimpliciada.domain;

// Generated 16/03/2010 08:00:26 by Mirante 3.2.2.GA

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import br.net.mirante.singular.exemplos.notificacaosimpliciada.domain.enums.SimNao;
import br.net.mirante.singular.exemplos.notificacaosimpliciada.domain.generic.MedEntity;
import br.net.mirante.singular.persistence.entity.BaseEntity;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serializable;


/**
 * TipoTermo generated by Vinicius Uriel
 */

@XmlRootElement(name = "tipo-termo", namespace = "http://www.anvisa.gov.br/reg-med/schema/domains")
@XmlType(name = "tipo-termo", namespace = "http://www.anvisa.gov.br/reg-med/schema/domains")
@Entity
@Table(name = "TB_TIPO_TERMO", schema = "DBMEDICAMENTO")
@NamedQueries({
      @NamedQuery(name = "TipoTermo.findAll", query = "Select tipoTermo From TipoTermo as tipoTermo where tipoTermo.ativa = 'S'  Order by tipoTermo.descricao  ")
})
@SuppressWarnings("serial")
public class TipoTermo extends BaseEntity implements MedEntity<Long> {

   public static final String DESCRICAO_SUBSTANCIA = "SUBSTÂNCIA";

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TIPOTERMO")
   @SequenceGenerator(sequenceName = "SQ_COSEQTIPOTERMO", name = "SEQ_TIPOTERMO", initialValue = 1, allocationSize = 1)
   @Column(name = "CO_SEQ_TIPO_TERMO", unique = true, nullable = false, precision = 8, scale = 0)
   private Long id;

   @Column(name = "DS_TIPO_TERMO", nullable = false, length = 200)
   private String descricao;

   @XmlTransient
   @Column(name = "ST_REGISTRO_ATIVO", nullable = false, length = 1)
   @Type(type = "com.miranteinfo.seam.hibernate.usertype.GenericEnumUserType", parameters = {
         @Parameter(name = "enumClassName", value = SimNao.ENUM_CLASS_NAME),
         @Parameter(name = "identifierMethod", value = "getCodigo"),
         @Parameter(name = "valueOfMethod", value = "valueOfEnum")
   })
   private SimNao ativa;

   @Column(name = "NO_ENTIDADE", nullable = true, length = 50)
   private String nomeEntidade;

   public TipoTermo() {}

   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getDescricao() {
      return this.descricao;
   }

   public void setDescricao(String descricao) {
      this.descricao = descricao;
   }

   public String getNomeEntidade() {
      return nomeEntidade;
   }

   public void setNomeEntidade(String nomeEntidade) {
      this.nomeEntidade = nomeEntidade;
   }

   public SimNao getAtiva() {
      return ativa;
   }

   public void setAtiva(SimNao ativa) {
      this.ativa = ativa;
   }

   public boolean isSubstancia() {

      if (Substancia.class.getSimpleName()
            .equalsIgnoreCase(getNomeEntidade())) {
         return true;
      } else {
         return false;
      }
   }

   @Override
   public Serializable getCod() {
      return id;
   }
}
