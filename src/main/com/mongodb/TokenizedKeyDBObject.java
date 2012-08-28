// BasicDBObject.java

/**
 *      Copyright (C) 2008 10gen Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.mongodb;

import com.mongodb.util.JSON;
import org.bson.BasicBSONObject;

import java.util.Map;

/**
 * a basic implementation of bson object that is mongo specific.
 * A <code>DBObject</code> can be created as follows, using this class:
 * <blockquote><pre>
 * DBObject obj = new BasicDBObject();
 * obj.put( "foo", "bar" );
 * </pre></blockquote>
 */
public class TokenizedKeyDBObject extends BasicDBObject implements DBObject {

    private static final long serialVersionUID = -4415279469780082174L;
   
    /**
     * creates an empty object
     * @param size an estimate of number of fields that will be inserted
     */
    public TokenizedKeyDBObject(){
        this.put("TOKENIZE", "1");
    }
    
    /**
     * creates an empty object
     * @param size an estimate of number of fields that will be inserted
     */
    public TokenizedKeyDBObject(int size){
        super(size);
        this.put("TOKENIZE", "1");
    }

    /**
     * creates an object with the given key/value
     * @param key  key under which to store
     * @param value value to stor
     */
    public TokenizedKeyDBObject(String key, Object value){
        super(key, value);
        this.put("TOKENIZE", "1");
    }

    /**
     * Creates an object from a map.
     * @param m map to convert
     */
    public TokenizedKeyDBObject(Map m) {
        super(m);
        this.put("TOKENIZE", "1");
    }
    
}
