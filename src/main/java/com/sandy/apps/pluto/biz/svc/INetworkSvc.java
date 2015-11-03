/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.util.Map ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ChartData ;

/**
 * This interface handles all the network communication and keeps track of the
 * network connectivity.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface INetworkSvc {

    /**
     * Posts the name value pairs at the specified URL and returns the
     * result returned by the server as String.
     *
     * @param url The URL to post the data (name value parameters) to.
     * @param parameters The parameters to post
     *
     * @return The result of the post operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    String getPOSTResult( final String url, final Map<String, String> parameters )
        throws STException ;

    String getPOSTResult( final String url, final String[][] parameters )
        throws STException ;

    String getPOSTResult( final String url )
        throws STException ;

    /**
     * Posts the name value pairs at the specified URL and returns the
     * result returned by the server as raw bytes.
     *
     * @param url The URL to post the data (name value parameters) to.
     * @param parameters The parameters to post
     *
     * @return The result of the post operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    byte[] getRawPOSTResult( final String url, final Map<String, String> parameters )
        throws STException ;

    byte[] getRawPOSTResult( final String url, final String[][] parameters )
        throws STException ;

    byte[] getRawPOSTResult( final String url )
        throws STException ;

    /**
     * Gets the contents of the specified URL by sending in the specified
     * name value parameters and returns the result returned by the server
     * as String.
     *
     * @param url The URL to get the data from
     * @param parameters The parameters to append to the request URL
     *
     * @return The result of the get operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    String getGETResult( final String url, final Map<String, String> parameters )
        throws STException ;

    String getGETResult( final String url, final String[][] parameters )
        throws STException ;

    String getGETResult( final String url )
        throws STException ;

    /**
     * Gets the contents of the specified URL by sending in the specified
     * name value parameters and returns the result returned by the server
     * as raw bytes.
     *
     * @param url The URL to get the data from
     * @param parameters The parameters to append to the request URL
     *
     * @return The result of the get operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    byte[] getRawGETResult( final String url, final Map<String, String> parameters )
        throws STException ;

    byte[] getRawGETResult( final String url, final String[][] parameters )
        throws STException ;

    byte[] getRawGETResult( final String url )
        throws STException ;

    /**
     * Gets the contents of the specified URL by sending in the specified
     * name value parameters and returns the result returned by the server
     * by type-casting it into a {@link ChartData} instance.
     *
     * @param url The URL to get the data from
     * @param parameters The parameters to append to the request URL
     *
     * @return The result of the get operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    ChartData getChartDataGET( final String url,
                               final Map<String, String> parameters )
        throws STException ;

    /**
     * Checks for the network status and returns a true if the network is online.
     * If the check is being made before the connectivity check interval since
     * the last check, this method returns the result of the last check status.
     *
     * @param force A boolean flag to indicate force check. If this flag is false,
     *        the value of last check result will be returned if the interval
     *        of checking is less than the check interval.
     *
     * @return true if the network is online, false otherwise
     *
     * @event {@link EventType#NETWORK_UNAVAILABLE} or
     *        {@link EventType#NETWORK_AVAILABLE} depending upon the status
     *        of the network.
     */
    boolean checkNetworkStatus( final boolean force ) ;

    /**
     * Sets the network service to offline mode and publishes the
     * {@link EventType#NETWORK_UNAVAILABLE} event. Any requests for post or
     * get while the network service is offline will result in an STException
     * being generated.
     */
    void setOffline() ;

    /**
     * Sets the network service to online mode and publishes the
     * {@link EventType#NETWORK_AVAILABLE} event. Any requests for post or
     * get while the network service is offline will result in an STException
     * being generated. An attempt will be made to check the network status
     * before making the service online - if the underlying network is not
     * available, this method will not do anything.
     */
    void setOnline() ;

    /**
     * Returns true if the underlying network is available and the network
     * service is marked online.
     */
    boolean isOnline() ;

    /**
     * Returns the number of bytes downloaded since the time the application
     * was started.
     *
     * @return A long value indicating the number of bytes downloaded.
     */
    long getNumBytesDownloaded() ;
}
