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
package flex.samples.marketdata;

import java.io.*;
import java.net.URLDecoder;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.List;
import java.util.ArrayList;

public class Portfolio {
	
	public static void main(String[] args) {
		Portfolio stockFeed = new Portfolio(); 
		stockFeed.getStocks();
	}
	
	public List getStocks() {

		List list = new ArrayList();
		
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("flex/samples/marketdata/portfolio.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            Document doc = factory.newDocumentBuilder().parse(is);
            NodeList stockNodes = doc.getElementsByTagName("stock");
            int length = stockNodes.getLength();
            Stock stock;
            Node stockNode;
            for (int i=0; i<length; i++) {
            	stockNode = stockNodes.item(i);
            	stock = new Stock();
            	stock.setSymbol( getStringValue(stockNode, "symbol") );
            	stock.setName( getStringValue(stockNode, "company") );
            	stock.setLast( getDoubleValue(stockNode, "last") );
            	stock.setHigh( stock.getLast() );
            	stock.setLow( stock.getLast() );
            	stock.setOpen( stock.getLast() );
            	stock.setChange( 0 );
            	list.add(stock);
            	System.out.println(stock.getSymbol());
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }

        return list;
	}
	
	private String getStringValue(Node node, String name) {
		return ((Element) node).getElementsByTagName(name).item(0).getFirstChild().getNodeValue();		
	}

	private double getDoubleValue(Node node, String name) {
		return Double.parseDouble( getStringValue(node, name) );		
	}

}