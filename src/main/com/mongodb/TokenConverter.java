package com.mongodb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.BasicBSONList;

public class TokenConverter {

    public static final String TOKENIZE = "TOKENIZE";
    Map<String, String> mappedFieldsToTokens;
    Map<String, String> mappedTokenToFields;

    private Mongo _mongo;


    public TokenConverter(Mongo mongo) {
        this._mongo = mongo;
    }


    private void initMapping() {
        DBCollection mapping = _mongo.getDB("test").getCollection("keyMapping");
        DBCursor result = mapping.find();
        for (DBObject o : result) {
            if (o.get("_id").equals("toToken"))
                this.mappedFieldsToTokens = o.toMap();
            if (o.get("_id").equals("toField"))
                this.mappedTokenToFields = o.toMap();
        }
        if (this.mappedFieldsToTokens == null)
            this.mappedFieldsToTokens = Collections.synchronizedMap(new HashMap<String, String>());
        if (this.mappedTokenToFields == null)
            this.mappedTokenToFields = Collections.synchronizedMap(new HashMap<String, String>());
    }


    public void transformAttrs(DBObject o, boolean toToken, boolean createMapping, boolean preserveTokenizeAttr) {

        if (o == null)
            return;

        if (!(o instanceof BasicBSONList) && o.get(TOKENIZE) == null)
            return;

        if (mappedFieldsToTokens == null || mappedTokenToFields == null)
            initMapping();

        Map m = o.toMap();
        Map<String, Object> tmp = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();

            if (entry.getKey().equals("_id"))
                continue;
            if (!preserveTokenizeAttr && toToken && !createMapping && entry.getKey().equals(TOKENIZE)) {
                o.removeField(TOKENIZE);
                continue;
            } else if (entry.getKey().equals(TOKENIZE)) {
                continue;
            }

            Object value = o.removeField(entry.getKey());

            if (toToken)
                o.put(mapFieldToToken(entry.getKey(), createMapping), value);
            else if (!(o instanceof BasicBSONList)) {
                String field = mapTokenToField(entry.getKey());
                if (field == null)
                    field = entry.getKey();
                o.put(field, value);
            }
            if (value == null)
                continue;

            if (value instanceof List || o instanceof BasicBSONList) {
                for (Object dbo : (List) value)
                    if (dbo instanceof DBObject)
                        transformAttrs((DBObject) dbo, toToken, createMapping, preserveTokenizeAttr);
            } else if (value.getClass().isArray()) {
                for (Object dbo : (Object[]) value)
                    if (dbo instanceof DBObject)
                        transformAttrs((DBObject) dbo, toToken, createMapping, preserveTokenizeAttr);
            } else if (value instanceof DBObject)
                transformAttrs((DBObject) value, toToken, createMapping, preserveTokenizeAttr);
        }
    }


    private String mapFieldToToken(String field, boolean createMapping) {

        String token = mappedFieldsToTokens.get(field);
        if (token == null && !createMapping)
            return field;
        else if (token != null)
            return token;
        else {
            token = toString(mappedFieldsToTokens.size(), 16);
            mappedFieldsToTokens.put(field, token);
            mappedTokenToFields.put(token, field);

            DBCollection mapping = _mongo.getDB("test").getCollection("keyMapping");

            DBObject toToken = new BasicDBObject("_id", "toToken");
            toToken.putAll(mappedFieldsToTokens);

            DBObject toFields = new BasicDBObject("_id", "toField");
            toFields.putAll(mappedTokenToFields);

            mapping.save(toToken);
            mapping.save(toFields);

            return token;
        }

    }


    private String mapTokenToField(String token) {
        return mappedTokenToFields.get(token);
    }


    // converts integer n into a base b string
    public String toString(int n, int base) {
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


    public String toBinaryString(int n) {
        return toString(n, 2);
    }


    public String toHexString(int n) {
        return toString(n, 16);
    }


    public void inputError(String s) {
        throw new RuntimeException("Input error with" + s);
    }


    // convert a String representing a base b integer into an int
    public int fromString(String s, int b) {
        int result = 0;
        int digit = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9')
                digit = c - '0';
            else if (c >= 'A' && c <= 'Z')
                digit = 10 + c - 'A';
            else
                inputError(s);

            if (digit < b)
                result = b * result + digit;
            else
                inputError(s);
        }
        return result;
    }


    public int fromBinaryString(String s) {
        return fromString(s, 2);
    }


    public int fromHexString(String s) {
        return fromString(s, 16);
    }

}
