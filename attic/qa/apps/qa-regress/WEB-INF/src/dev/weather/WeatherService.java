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

import java.util.HashMap;
import java.util.Map;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.MessageException;


public class WeatherService {

    public WeatherInfo getWeatherInfo(String zipCode) {
        //if the zipCode isn't a number we'll throw the exception
        Integer.parseInt(zipCode);

        WeatherInfo weatherInfo = new WeatherInfo();

        try {
            if (zipCode.startsWith("0")) {
                weatherInfo = getBostonWeather();
            } else if (zipCode.startsWith("9")) {
                weatherInfo = getSanFranciscoWeather();
            } else {
                weatherInfo = getDummyWeather();
            }
        } catch (NumberFormatException nfe) {
            weatherInfo = getDummyWeather();
        }

        return weatherInfo;
    }

    public String getTemperature(String zipCode) {
        return getWeatherInfo(zipCode).getTemperature();
    }

    public Map getWeatherMap(String zipCode) {
        HashMap map = new HashMap();
        WeatherInfo info = getWeatherInfo(zipCode);
        map.put("location", info.getLocation());
        map.put("temperature", info.getTemperature());
        map.put("forecast", info.getForecast());
        /*
        map.put("extended", new String[0]);
        String[] extended = info.getExtendedForecast();
        for (int i = 0; i < extended.length; ++i)
        {
            extended[i] = "extended" + i;
        }
        map.put("extended", extended);
        */
        return map;
    }
    
    public String getTemperatureWithClose(String zipCode) {
    	FlexSession session = FlexContext.getFlexSession();
    	return getTemperature(zipCode);
    }

    public String generateMessageExceptionWithExtendedData(String extraData)
    {       
        MessageException me = new MessageException("Testing extendedData.");
        Map extendedData = new HashMap();
        // The test method that invokes this expects an "extraData" slot in this map.
        extendedData.put("extraData", extraData);
        me.setExtendedData(extendedData);
        me.setCode("999");
        me.setDetails("Some custom details.");
        throw me;
    }

    private WeatherInfo getBostonWeather() {
        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setLocation("Boston, MA");
        weatherInfo.setTemperature("80");
        weatherInfo.setForecast("Sunny");
        weatherInfo.setExtendedForecast("sunny", "sunny", "sunny", "cloudy", "cloudy");
        return weatherInfo;
    }

    private WeatherInfo getSanFranciscoWeather() {
        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setLocation("San Francisco, CA");
        weatherInfo.setTemperature("50");
        weatherInfo.setForecast("Rainy");
        weatherInfo.setExtendedForecast("cloudy", "cloudy", "cloudy", "rainy", "rainy");
        return weatherInfo;
    }

    private WeatherInfo getDummyWeather() {
        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setLocation("Any Where, XY");
        weatherInfo.setTemperature("70");
        weatherInfo.setForecast("Clear");
        //weatherInfo.setExtendedForecast("sunny", "sunny", "sunny", "sunny", "sunny");
        return weatherInfo;
    }
}
