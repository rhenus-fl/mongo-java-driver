/**
 * Copyright (c) 2008 - 2012 10gen, Inc. <http://10gen.com>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.sun.tools.doclets.Taglet;

import java.util.Map;

public class ManualTaglet extends DocTaglet {

    public static void register( Map<String, Taglet> tagletMap ){
        ManualTaglet t = new ManualTaglet();
        tagletMap.put( t.getName() , t );
    }

    public String getName(){
        return "mongodb.driver.manual";
    }

    @Override
    protected String getBaseDocURI() {
        return "http://docs.mongodb.org/manual/";
    }

}
