package com.mongodb;



public class TokenizedDBCursor extends DBCursor {
    TokenConverter converter;
    public TokenizedDBCursor(DBCollection collection, DBObject q, DBObject k, ReadPreference preference, TokenConverter converter) {
        super(collection, q, k, preference);
        this.converter = converter;
    }

    @Override
    public DBObject next() {
        DBObject next = super.next();
        converter.transformAttrs(next, false);
        return next;
    }

}
