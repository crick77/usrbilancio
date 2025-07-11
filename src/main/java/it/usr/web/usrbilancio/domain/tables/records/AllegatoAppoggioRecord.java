/*
 * This file is generated by jOOQ.
 */
package it.usr.web.usrbilancio.domain.tables.records;


import it.usr.web.usrbilancio.domain.tables.AllegatoAppoggio;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class AllegatoAppoggioRecord extends UpdatableRecordImpl<AllegatoAppoggioRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>usrbilancio.allegato_appoggio.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>usrbilancio.allegato_appoggio.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>usrbilancio.allegato_appoggio.id_ordinativo_appoggio</code>.
     */
    public void setIdOrdinativoAppoggio(Integer value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>usrbilancio.allegato_appoggio.id_ordinativo_appoggio</code>.
     */
    public Integer getIdOrdinativoAppoggio() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>usrbilancio.allegato_appoggio.gruppo</code>.
     */
    public void setGruppo(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>usrbilancio.allegato_appoggio.gruppo</code>.
     */
    public String getGruppo() {
        return (String) get(2);
    }

    /**
     * Setter for <code>usrbilancio.allegato_appoggio.nomefile</code>.
     */
    public void setNomefile(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>usrbilancio.allegato_appoggio.nomefile</code>.
     */
    public String getNomefile() {
        return (String) get(3);
    }

    /**
     * Setter for <code>usrbilancio.allegato_appoggio.nomefile_locale</code>.
     */
    public void setNomefileLocale(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>usrbilancio.allegato_appoggio.nomefile_locale</code>.
     */
    public String getNomefileLocale() {
        return (String) get(4);
    }

    /**
     * Setter for <code>usrbilancio.allegato_appoggio.descrizione</code>.
     */
    public void setDescrizione(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>usrbilancio.allegato_appoggio.descrizione</code>.
     */
    public String getDescrizione() {
        return (String) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AllegatoAppoggioRecord
     */
    public AllegatoAppoggioRecord() {
        super(AllegatoAppoggio.ALLEGATO_APPOGGIO);
    }

    /**
     * Create a detached, initialised AllegatoAppoggioRecord
     */
    public AllegatoAppoggioRecord(Integer id, Integer idOrdinativoAppoggio, String gruppo, String nomefile, String nomefileLocale, String descrizione) {
        super(AllegatoAppoggio.ALLEGATO_APPOGGIO);

        setId(id);
        setIdOrdinativoAppoggio(idOrdinativoAppoggio);
        setGruppo(gruppo);
        setNomefile(nomefile);
        setNomefileLocale(nomefileLocale);
        setDescrizione(descrizione);
        resetChangedOnNotNull();
    }
}
