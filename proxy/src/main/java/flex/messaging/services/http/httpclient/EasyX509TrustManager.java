/*
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

/*
 * This file has been modified by Adobe (Adobe Systems Incorporated).
 * Date: Oct 24, 2008 onwards.
 * Reason(s): Fixed the following issues:
 * BLZ-269 - Add support to proxy service for IBM X509
 * checkstyle warnings
 */
 
package flex.messaging.services.http.httpclient;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import flex.messaging.util.Trace;

/**
 *
 * <p>
 * EasyX509TrustManager unlike default {@link javax.net.ssl.X509TrustManager} accepts
 * self-signed certificates.
 * </p>
 * <p>
 * This trust manager SHOULD NOT be used for productive systems
 * due to security reasons, unless it is a concious decision and
 * you are perfectly aware of security implications of accepting
 * self-signed certificates
 * </p>
 *
 *  <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * use without additional customization.
 * </p>
 */
public class EasyX509TrustManager implements X509TrustManager
{
    private X509TrustManager standardTrustManager = null;

    private boolean trustStore;

    /**
     * Constructor for EasyX509TrustManager.
     * @param keystore the KeyStore to use
     * @throws NoSuchAlgorithmException, KeyStoreException if the construction process failed
     */
    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException
    {
        super();
        TrustManagerFactory factory = null;
        try
        {
            factory = TrustManagerFactory.getInstance("SunX509");
        }
        catch (NoSuchAlgorithmException nsae)
        {
            // Fallback attempt - try for an IbmX509 factory in case we're running in WAS with no Sun providers registered.
            try
            {
                factory = TrustManagerFactory.getInstance("IbmX509");
            }
            catch (NoSuchAlgorithmException nsae2)
            {
                throw new NoSuchAlgorithmException("Neither SunX509 nor IbmX509 trust manager supported.");
            }
        }
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0)
        {
            
            factory.init(keystore);
            trustmanagers = factory.getTrustManagers();
            
            // If we still have no trust managers, throw.
            if (trustmanagers.length == 0)
                throw new NoSuchAlgorithmException("Neither SunX509 nor IbmX509 trust manager supported.");
        }
        this.standardTrustManager = (X509TrustManager)trustmanagers[0];

        // very lax settings must be used if flex.trustStore is being used
        trustStore = (System.getProperty("flex.trustStore") != null);
    }

    /*
     *  (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[] x509Certificates, String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException
    {
        if (trustStore)
        {
            return;
        }
        standardTrustManager.checkServerTrusted(certificates, authType);
    }

    /*
     *  (non-Javadoc)
     * @see com.sun.net.ssl.X509TrustManager#isServerTrusted(X509Certificate[])
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException
    {
        if (trustStore)
        {
            return;
        }
        if (certificates != null)
        {
            if (Trace.ssl)
            {
                Trace.trace("Server certificate chain:");
                for (int i = 0; i < certificates.length; i++)
                {
                    Trace.trace("X509Certificate[" + i + "]=" + certificates[i]);
                }
            }
        }
        if ((certificates != null) && (certificates.length == 1))
        {
            X509Certificate certificate = certificates[0];
            try
            {
                certificate.checkValidity();
            }
            catch (CertificateException e)
            {
                if (Trace.ssl)
                {
                    Trace.trace(e.toString());
                }
                throw e;
            }
        }
        else
        {
            standardTrustManager.checkServerTrusted(certificates, authType);
        }
    }

    /*
     *  (non-Javadoc)
     * @see com.sun.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}
