package com.mongodb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.BasicBSONList;


/**
 * The Class TokenConverter.
 *
 * @author jens.behrens
 *
 */
public class TokenConverter {

    /** The Constant TO_FIELD. */
    private static final String TO_FIELD = "toField";

    /** The Constant TO_TOKEN. */
    private static final String TO_TOKEN = "toToken";

    /** The Constant KEY_MAPPING_COLLECTION. */
    private static final String KEY_MAPPING_COLLECTION = "keyMapping";

    /** The Constant TOKENIZE_KEY_DB. */
    private static final String TOKENIZE_KEY_DB = "tokenizeKeyDB";

    /** The Constant TOKENIZE. */
    public static final String TOKENIZE = "TOKENIZE";

    /** The mapped fields to tokens. */
    private Map<String, String> mappedFieldsToTokens;
    
    /** The mapped token to fields. */
    private Map<String, String> mappedTokenToFields;
    
    /** The _mongo. */
    private Mongo _mongo;


    /**
     * Instantiates a new token converter.
     *
     * @param mongo the mongo
     */
    public TokenConverter(Mongo mongo) {
        this._mongo = mongo;
    }


    /**
     * Inits the mapping.
     */
    @SuppressWarnings("unchecked")
    private void initMapping() {
        synchronized (this._mongo) {
            DBCollection mapping = this._mongo.getDB(TOKENIZE_KEY_DB).getCollection(KEY_MAPPING_COLLECTION);
            DBCursor result = mapping.find();
            for (DBObject o : result) {
                if (o.get("_id").equals(TO_TOKEN))
                    this.mappedFieldsToTokens = o.toMap();
                if (o.get("_id").equals(TO_FIELD))
                    this.mappedTokenToFields = o.toMap();
            }
            if (this.mappedFieldsToTokens == null)
                this.mappedFieldsToTokens = Collections.synchronizedMap(new HashMap<String, String>());
            if (this.mappedTokenToFields == null)
                this.mappedTokenToFields = Collections.synchronizedMap(new HashMap<String, String>());
        }
    }


    /**
     * Transform attrs.
     *
     * @param o the o
     * @param toToken the to token
     * @param createMapping the create mapping
     * @param preserveTokenizeAttr the preserve tokenize attr
     */
    @SuppressWarnings("unchecked")
    public void transformAttrs(DBObject o, boolean toToken, boolean createMapping, boolean preserveTokenizeAttr) {

        if (o == null)
            return;

        if (!(o instanceof BasicBSONList) && o.get(TOKENIZE) == null)
            return;

        if (this.mappedFieldsToTokens == null || this.mappedTokenToFields == null)
            initMapping();

        Map<String, Object> m = o.toMap();
        Iterator<Map.Entry<String, Object>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();

            if (entry.getKey().equals("_id"))
                continue;
            if (!preserveTokenizeAttr && toToken && !createMapping && entry.getKey().equals(TOKENIZE)) {
                synchronized (this._mongo) {
                    o.removeField(TOKENIZE);
                }
                continue;
            } else if (entry.getKey().equals(TOKENIZE)) {
                continue;
            }

            String field = null;
            Object value = null;

            synchronized (this._mongo) {
                value = o.removeField(entry.getKey());

                if (toToken)
                    field = mapFieldToToken(entry.getKey(), createMapping);
                else if (!(o instanceof BasicBSONList))
                    field = mapTokenToField(entry.getKey());

                if (field == null)
                    field = entry.getKey();

                o.put(field, value);
            }

            if (value == null)
                continue;

            if (value instanceof List || o instanceof BasicBSONList) {
                if (value instanceof List) {
                    for (Object dbo : (List<?>) value)
                        if (dbo instanceof DBObject)
                            transformAttrs((DBObject) dbo, toToken, createMapping, preserveTokenizeAttr);
                } else if (value instanceof DBObject)
                    transformAttrs((DBObject) value, toToken, createMapping, preserveTokenizeAttr);
            } else if (value.getClass().isArray()) {
                for (Object dbo : (Object[]) value)
                    if (dbo instanceof DBObject)
                        transformAttrs((DBObject) dbo, toToken, createMapping, preserveTokenizeAttr);
            } else if (value instanceof DBObject)
                transformAttrs((DBObject) value, toToken, createMapping, preserveTokenizeAttr);
        }
    }


    /**
     * Map field to token.
     *
     * @param field the field
     * @param createMapping the create mapping
     * @return the string
     */
    private String mapFieldToToken(String field, boolean createMapping) {

        String token = this.mappedFieldsToTokens.get(field);
        if (token == null && !createMapping)
            return field;
        else if (token != null)
            return token;
        else {
            token = toString(this.mappedFieldsToTokens.size(), 16);

            this.mappedFieldsToTokens.put(field, token);
            this.mappedTokenToFields.put(token, field);

            DBCollection mapping = this._mongo.getDB(TOKENIZE_KEY_DB).getCollection(KEY_MAPPING_COLLECTION);

            DBObject toToken = new BasicDBObject("_id", TO_TOKEN);
            toToken.putAll(this.mappedFieldsToTokens);

            DBObject toFields = new BasicDBObject("_id", TO_FIELD);
            toFields.putAll(this.mappedTokenToFields);

            mapping.save(toToken, WriteConcern.JOURNAL_SAFE);
            mapping.save(toFields, WriteConcern.JOURNAL_SAFE);

            return token;

        }

    }


    /**
     * Map token to field.
     *
     * @param token the token
     * @return the string
     */
    private String mapTokenToField(String token) {
        return this.mappedTokenToFields.get(token);
    }


    // converts integer n into a base b string
    /**
     * To string.
     *
     * @param n the n
     * @param base the base
     * @return the string
     */
    private String toString(int n, int base) {
        // special case
        if (n == 0)
            return "0";

        String digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String s = "";
        while (n > 0) {
            int d = n % base;
            s = digits.charAt(d) + s;
            n = n / base;
        }
        return s;
    }

}
