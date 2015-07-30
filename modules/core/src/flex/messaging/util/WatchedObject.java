/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package flex.messaging.util;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Used by redeployment manager to monitor for changes to the files.
 *
 *
 */
public class WatchedObject
{
    private String filename;
    private long modified;

    /**
     * Creates a watched object for the specified file.
     * 
     * @param filename file to watch
     * @throws FileNotFoundException when specified file could not be found
     */
    public WatchedObject(String filename) throws FileNotFoundException
    {
        this.filename = filename;
        File file = new File(filename);

        if (!file.isFile() && !file.isDirectory())
        {
            throw new FileNotFoundException();
        }
        this.modified = file.lastModified();
    }

    /**
     * Returns true if the watched file has been modified since creation of this 
     * watched object or since the last call to this method.
     * 
     * @return true if the watched file has been modified since creation of this 
     * watched object or since the last call to this method
     */
    public boolean isUptodate()
    {
        boolean uptodate = true;

        long current = new File(filename).lastModified();

        if (Math.abs(current - modified) > 1000)
        {
            uptodate = false;
        }

        modified = current;

        return uptodate;
    }
}
