package br.net.mirante.singular.flow.core;

import java.io.Serializable;

@Deprecated
//TODO renomear para algo mais representativo para o singular.
public interface MUser extends Comparable<MUser> {

    /**
     *
     * @return
     * @deprecated deveria ser serializable
     */
    //TODO refatorar
    @Deprecated
    public Serializable getCod();

    /**
     *
     * @return
     * @deprecated  nome de guerra só faz sentido no contexto da mirante
     */
    //TODO renomear para um nome mais representativo para o singular
    @Deprecated
    public String getNomeGuerra();

    public String getEmail();

    public default boolean is(MUser user2) {
        return (user2 != null) && getCod().equals(user2.getCod());
    }

    public default boolean not(MUser user2) {
        return !(is(user2));
    }

    @Override
    public default int compareTo(MUser p) {
        return getNomeGuerra().compareTo(p.getNomeGuerra());
    }
}
