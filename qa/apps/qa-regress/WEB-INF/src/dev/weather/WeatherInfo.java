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

package dev.weather;

import java.io.Serializable;

public class WeatherInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String location;
    private String temperature;
    private String forecast;
    private String[] extendedForecast = new String[5];

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }
    
    public String[] getExtendedForecast() {
    	return extendedForecast;
	}
	    	
    public void setExtendedForecast(String monday, String tuesday, String wednesday, String thursday, String friday) {
    	this.extendedForecast[0] = monday;
    	this.extendedForecast[1] = tuesday;
    	this.extendedForecast[2] = wednesday;
    	this.extendedForecast[3] = thursday;
    	this.extendedForecast[4] = friday;
	}	
}
