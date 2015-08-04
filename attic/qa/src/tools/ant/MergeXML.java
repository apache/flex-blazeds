package tools.ant;

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

/*
 * File Name:        MergeXML
 * Description:      An ant task to wrap my XMLMerge class.  Used to insert canned
 *                   XML into another XML file (intended for updating Flex config files.
 */

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import utils.XMLMerge;


public class MergeXML extends Task {
    private String baseFile;
    private String importFile;
    private String destinationFile;
    private boolean debug = false;


    public void setBaseFile(String baseFile) {
       this.baseFile=baseFile;
    }

    public void setImportFile(String importFile) {
       this.importFile=importFile;
    }

    public void setDestinationFile(String destinationFile) {
       this.destinationFile=destinationFile;
    }

    public void setDebug(boolean debug) {
       this.debug = debug;
    }


    public void execute() throws BuildException {
        XMLMerge merger=new XMLMerge();
        merger.debug=debug;
        merger.mergeDocs(baseFile, importFile, destinationFile);
    }


}

