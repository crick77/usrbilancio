/*
 * This file is generated by jOOQ.
 */
package it.usr.web.usrbilancio.domain.tables;


import it.usr.web.usrbilancio.domain.Indexes;
import it.usr.web.usrbilancio.domain.Keys;
import it.usr.web.usrbilancio.domain.Usrbilancio;
import it.usr.web.usrbilancio.domain.tables.Codice.CodicePath;
import it.usr.web.usrbilancio.domain.tables.Competenza.CompetenzaPath;
import it.usr.web.usrbilancio.domain.tables.TipoDocumento.TipoDocumentoPath;
import it.usr.web.usrbilancio.domain.tables.TipoRts.TipoRtsPath;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Quietanza extends TableImpl<QuietanzaRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>usrbilancio.quietanza</code>
     */
    public static final Quietanza QUIETANZA = new Quietanza();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<QuietanzaRecord> getRecordType() {
        return QuietanzaRecord.class;
    }

    /**
     * The column <code>usrbilancio.quietanza.id</code>.
     */
    public final TableField<QuietanzaRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>usrbilancio.quietanza.id_competenza</code>.
     */
    public final TableField<QuietanzaRecord, Integer> ID_COMPETENZA = createField(DSL.name("id_competenza"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.id_codice</code>.
     */
    public final TableField<QuietanzaRecord, Integer> ID_CODICE = createField(DSL.name("id_codice"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.id_tipo_rts</code>.
     */
    public final TableField<QuietanzaRecord, Integer> ID_TIPO_RTS = createField(DSL.name("id_tipo_rts"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.id_tipo_documento</code>.
     */
    public final TableField<QuietanzaRecord, Integer> ID_TIPO_DOCUMENTO = createField(DSL.name("id_tipo_documento"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>usrbilancio.quietanza.numero_documento</code>.
     */
    public final TableField<QuietanzaRecord, String> NUMERO_DOCUMENTO = createField(DSL.name("numero_documento"), SQLDataType.VARCHAR(32), this, "");

    /**
     * The column <code>usrbilancio.quietanza.data_documento</code>.
     */
    public final TableField<QuietanzaRecord, LocalDate> DATA_DOCUMENTO = createField(DSL.name("data_documento"), SQLDataType.LOCALDATE, this, "");

    /**
     * The column <code>usrbilancio.quietanza.numero_pagamento</code>.
     */
    public final TableField<QuietanzaRecord, String> NUMERO_PAGAMENTO = createField(DSL.name("numero_pagamento"), SQLDataType.VARCHAR(32).nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.data_pagamento</code>.
     */
    public final TableField<QuietanzaRecord, LocalDate> DATA_PAGAMENTO = createField(DSL.name("data_pagamento"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.ordinante</code>.
     */
    public final TableField<QuietanzaRecord, String> ORDINANTE = createField(DSL.name("ordinante"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.descrizione_ordinanza</code>.
     */
    public final TableField<QuietanzaRecord, String> DESCRIZIONE_ORDINANZA = createField(DSL.name("descrizione_ordinanza"), SQLDataType.VARCHAR(512).nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.importo</code>.
     */
    public final TableField<QuietanzaRecord, BigDecimal> IMPORTO = createField(DSL.name("importo"), SQLDataType.DECIMAL(10, 2).nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.nomefile</code>.
     */
    public final TableField<QuietanzaRecord, String> NOMEFILE = createField(DSL.name("nomefile"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.nomefile_locale</code>.
     */
    public final TableField<QuietanzaRecord, String> NOMEFILE_LOCALE = createField(DSL.name("nomefile_locale"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>usrbilancio.quietanza.note</code>.
     */
    public final TableField<QuietanzaRecord, String> NOTE = createField(DSL.name("note"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>usrbilancio.quietanza.flag</code>. 0 = nullo
     * 1 = commissario
     */
    public final TableField<QuietanzaRecord, Integer> FLAG = createField(DSL.name("flag"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "0 = nullo\n1 = commissario");

    /**
     * The column <code>usrbilancio.quietanza.versione</code>.
     */
    public final TableField<QuietanzaRecord, Long> VERSIONE = createField(DSL.name("versione"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.inline("0", SQLDataType.BIGINT)), this, "");

    private Quietanza(Name alias, Table<QuietanzaRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Quietanza(Name alias, Table<QuietanzaRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>usrbilancio.quietanza</code> table reference
     */
    public Quietanza(String alias) {
        this(DSL.name(alias), QUIETANZA);
    }

    /**
     * Create an aliased <code>usrbilancio.quietanza</code> table reference
     */
    public Quietanza(Name alias) {
        this(alias, QUIETANZA);
    }

    /**
     * Create a <code>usrbilancio.quietanza</code> table reference
     */
    public Quietanza() {
        this(DSL.name("quietanza"), null);
    }

    public <O extends Record> Quietanza(Table<O> path, ForeignKey<O, QuietanzaRecord> childPath, InverseForeignKey<O, QuietanzaRecord> parentPath) {
        super(path, childPath, parentPath, QUIETANZA);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class QuietanzaPath extends Quietanza implements Path<QuietanzaRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> QuietanzaPath(Table<O> path, ForeignKey<O, QuietanzaRecord> childPath, InverseForeignKey<O, QuietanzaRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private QuietanzaPath(Name alias, Table<QuietanzaRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public QuietanzaPath as(String alias) {
            return new QuietanzaPath(DSL.name(alias), this);
        }

        @Override
        public QuietanzaPath as(Name alias) {
            return new QuietanzaPath(alias, this);
        }

        @Override
        public QuietanzaPath as(Table<?> alias) {
            return new QuietanzaPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Usrbilancio.USRBILANCIO;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.QUIETANZA_FK_QUIETANZA_CODICE_IDX, Indexes.QUIETANZA_FK_QUIETANZA_COMPETENZA_IDX, Indexes.QUIETANZA_FK_QUIETANZA_TIPODOCUMENTO_IDX, Indexes.QUIETANZA_FK_QUIETANZA_TIPORTS_IDX);
    }

    @Override
    public Identity<QuietanzaRecord, Integer> getIdentity() {
        return (Identity<QuietanzaRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<QuietanzaRecord> getPrimaryKey() {
        return Keys.KEY_QUIETANZA_PRIMARY;
    }

    @Override
    public List<ForeignKey<QuietanzaRecord, ?>> getReferences() {
        return Arrays.asList(Keys.FK_QUIETANZA_COMPETENZA, Keys.FK_QUIETANZA_CODICE, Keys.FK_QUIETANZA_TIPORTS, Keys.FK_QUIETANZA_TIPODOCUMENTO);
    }

    private transient CompetenzaPath _competenza;

    /**
     * Get the implicit join path to the <code>usrbilancio.competenza</code>
     * table.
     */
    public CompetenzaPath competenza() {
        if (_competenza == null)
            _competenza = new CompetenzaPath(this, Keys.FK_QUIETANZA_COMPETENZA, null);

        return _competenza;
    }

    private transient CodicePath _codice;

    /**
     * Get the implicit join path to the <code>usrbilancio.codice</code> table.
     */
    public CodicePath codice() {
        if (_codice == null)
            _codice = new CodicePath(this, Keys.FK_QUIETANZA_CODICE, null);

        return _codice;
    }

    private transient TipoRtsPath _tipoRts;

    /**
     * Get the implicit join path to the <code>usrbilancio.tipo_rts</code>
     * table.
     */
    public TipoRtsPath tipoRts() {
        if (_tipoRts == null)
            _tipoRts = new TipoRtsPath(this, Keys.FK_QUIETANZA_TIPORTS, null);

        return _tipoRts;
    }

    private transient TipoDocumentoPath _tipoDocumento;

    /**
     * Get the implicit join path to the <code>usrbilancio.tipo_documento</code>
     * table.
     */
    public TipoDocumentoPath tipoDocumento() {
        if (_tipoDocumento == null)
            _tipoDocumento = new TipoDocumentoPath(this, Keys.FK_QUIETANZA_TIPODOCUMENTO, null);

        return _tipoDocumento;
    }

    @Override
    public Quietanza as(String alias) {
        return new Quietanza(DSL.name(alias), this);
    }

    @Override
    public Quietanza as(Name alias) {
        return new Quietanza(alias, this);
    }

    @Override
    public Quietanza as(Table<?> alias) {
        return new Quietanza(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Quietanza rename(String name) {
        return new Quietanza(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Quietanza rename(Name name) {
        return new Quietanza(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Quietanza rename(Table<?> name) {
        return new Quietanza(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Quietanza where(Condition condition) {
        return new Quietanza(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Quietanza where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Quietanza where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Quietanza where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Quietanza where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Quietanza where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Quietanza where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Quietanza where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Quietanza whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Quietanza whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
