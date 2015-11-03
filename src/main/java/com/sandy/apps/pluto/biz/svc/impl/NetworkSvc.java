/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.io.IOException ;
import java.net.InetAddress ;
import java.net.UnknownHostException ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.httpclient.ConnectTimeoutException ;
import org.apache.commons.httpclient.ConnectionPoolTimeoutException ;
import org.apache.commons.httpclient.Credentials ;
import org.apache.commons.httpclient.HttpClient ;
import org.apache.commons.httpclient.HttpException ;
import org.apache.commons.httpclient.HttpMethod ;
import org.apache.commons.httpclient.HttpStatus ;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager ;
import org.apache.commons.httpclient.NTCredentials ;
import org.apache.commons.httpclient.NameValuePair ;
import org.apache.commons.httpclient.NoHttpResponseException ;
import org.apache.commons.httpclient.UsernamePasswordCredentials ;
import org.apache.commons.httpclient.auth.AuthPolicy ;
import org.apache.commons.httpclient.auth.AuthScope ;
import org.apache.commons.httpclient.cookie.CookiePolicy ;
import org.apache.commons.httpclient.methods.GetMethod ;
import org.apache.commons.httpclient.methods.PostMethod ;
import org.apache.commons.httpclient.params.HttpClientParams ;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams ;
import org.apache.commons.httpclient.params.HttpMethodParams ;
import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.biz.svc.IUserPreferenceSvc ;
import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ChartData ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * Implementation of the {@link INetworkSvc} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NetworkSvc implements INetworkSvc, IEventSubscriber, ConfigKey {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NetworkSvc.class ) ;

    /**
     * A static set of error responses that we might receive. These are not
     * HTTP errors but error pages returned from the proxy. If the response
     * contains any of these string, it implies that we have a problem -
     * handle connection failure scenarios in this case.
     */
    private final String[] KNOWN_ERR_MSGS = {
       "Background: The server you are attempting to access has refused the connection with the gateway."
    } ;

    /** The maximum number of retrial attemps. */
    private int maxRetrialAttemps = ConfigKey.DEF_MAX_NETWORK_RETRIAL ;

    /** The number of milliseconds between retrial attempts. */
    private int retrialInterval = ConfigKey.DEF_RETRIAL_INTERVAL ;

    /**
     * A reference to the user preference service, from which user specific
     * configuration parameters will be extracted and used for configuring the
     * network access. This reference will be injected via the Spring
     * object factory.
     */
    private IUserPreferenceSvc userPrefSvc = null ;

    /** The encapsulation of the HTTP communication logic. */
    private HttpClient httpClient = null ;

    /** The multi-threaded Http connection manager we are using. */
    private final MultiThreadedHttpConnectionManager httpConnMgr =
            new MultiThreadedHttpConnectionManager() ;

    /**
     * Time interval for checking the network connectivity. Any request to
     * check for the network connectivity within last check time + check interval,
     * will return the result of the last network checked status. The
     * value of this parameter should be an integer and be specified as
     * milliseconds.
     */
    private int connCheckInterval = 0 ;

    /** The time when the last connectivity check was made. */
    private long lastCheckTime = 0 ;

    /** The last check status. */
    private boolean netAvailabilityStatus = true ;

    /** If the network service has been manually made off-line. */
    private boolean manualOffline = false ;

    /**
     * A bit based index used to keep track of the last 'n' number of network
     * attempts. The 'n' is specified in the configuration against the
     * 'connectivity.switch.off.num.failures' key.
     */
    private int pastConnectionAttemptStatus = 0x0 ;

    /**
     * A mask whose last 'n' bits are set to 1. The number of lowest order bits
     * set to 1 indicates the number of failure attempts after which the
     * network service goes offline automatically.
     */
    private int autoOfflineMask = 0x0 ;

    /** An object lock to safeguard against concurrent access to variables. */
    private final Object LOCK = new Object() ;

    /** A count of the number of bytes downloaded since the program was started. */
    private long numBytesDownloaded = 0 ;

    /** Public constructor. */
    public NetworkSvc() {
        super() ;
    }

    // ---------------------- INITIALIZATION BEGINS --------------------------
    /**
     * This method should be called post initialization to initialize the
     * network service by setting up the proxy etc. This method is invoked
     * by the container framework (Spring)
     */
    public void initialize() throws Exception {

        logger.info( "Initializing network service.." ) ;

        final ConfigManager cfgMgr = ConfigManager.getInstance() ;
        // Load the configured values which influence the behavior of this service
        this.maxRetrialAttemps = cfgMgr.getInt(
                MAX_NETWORK_RETRIALS, DEF_MAX_NETWORK_RETRIAL ) ;

        this.retrialInterval = ConfigManager.getInstance().getInt(
                NETWORK_RETRIAL_INTERVAL, DEF_RETRIAL_INTERVAL ) ;

        // Register this service as a consumer of preference change events
        final EventBus bus = EventBus.instance() ;
        bus.addSubscriberForEventTypes( this, EventType.USER_PREF_CHANGED ) ;

        // Initialize the HTTP connection manager parameters
        final HttpConnectionManagerParams connMgrParams = this.httpConnMgr.getParams() ;
        connMgrParams.setConnectionTimeout( 5000 ) ;
        connMgrParams.setDefaultMaxConnectionsPerHost( 50 ) ;
        connMgrParams.setMaxTotalConnections( 100 ) ;
        connMgrParams.setStaleCheckingEnabled( true ) ;

        // Initialize the HTTP connection manager
        this.httpClient = new HttpClient( this.httpConnMgr ) ;

        // Configure proxy settings and client parameters for the http client
        setClientParams() ;
        configureProxySettings() ;

        // The connectivity check interval as picked from the configuration,
        // default value is 10 seconds
        this.connCheckInterval = cfgMgr.getInt( CONNECTIVITY_CHECK_INTERVAL, 10000 ) ;

        // Set up the auto offline mask
        final int numFailureAttemps = cfgMgr.getInt( ConfigKey.CONNECTIVITY_SWITCH_OFF_NUM_FAILURES, -1 ) ;
        if( numFailureAttemps > 0 ) {
            for( int i=0; i<numFailureAttemps; i++ ) {
                this.autoOfflineMask <<= 1 ;
                this.autoOfflineMask |= 0x1 ;
            }
            logger.debug( "Setting auto failure attemps mask as " +
                          Integer.toBinaryString( this.autoOfflineMask ) ) ;
        }

        // Now force the network check to figure out what connectivity status
        // we are starting off with
        try {
            this.netAvailabilityStatus = checkNetworkStatus( true ) ;
        }
        catch ( final Exception e ) {
            logger.warn( "Network connectivity check failed. Msg = " + e.getMessage() ) ;
        }
    }

    /**
     * Sets the configured HTTP client parameters in the HTTP client. The
     * http client parameters are configured via the /stocktracker-config.properties
     */
    private void setClientParams() {

        final HttpClientParams clientParams = this.httpClient.getParams() ;
        final ConfigManager cfgMgr = ConfigManager.getInstance() ;

        final String userAgent = cfgMgr.getString( ConfigKey.USER_AGENT, null ) ;

        // Set the user agent that is to be sent as header with every request
        if( StringUtil.isNotEmptyOrNull( userAgent ) ) {
            clientParams.setParameter( HttpMethodParams.USER_AGENT, userAgent ) ;
        }

        clientParams.setCookiePolicy( CookiePolicy.RFC_2109 ) ;
    }

    /**
     * This method sets the proxy configurations for the network service
     * based on the user's preferences. The user's preferences related to
     * network can be retrieved from the user preference service.
     */
    private void configureProxySettings() throws UnknownHostException {

        final boolean useProxy = this.userPrefSvc.getBoolean(  ConfigKey.USE_PROXY, false ) ;
        if( useProxy ) {

            final String proxyHost = this.userPrefSvc.getUserPref( ConfigKey.PROXY_HOST, null ) ;
            final String proxyPort = this.userPrefSvc.getUserPref( ConfigKey.PROXY_PORT, null ) ;
            final String userName  = this.userPrefSvc.getUserPref( ConfigKey.PROXY_USER, null ) ;
            final String password  = this.userPrefSvc.getUserPref( ConfigKey.PROXY_PWD, null ) ;
            final boolean useAuth  = this.userPrefSvc.getBoolean(  ConfigKey.USE_AUTH, false ) ;

            // If proxy is to be used, set the proxy host and port. Also check if
            // the proxy needs authentication - if so, set the authentication credentials.
            if( useProxy &&
                !StringUtil.isEmptyOrNull( proxyHost ) &&
                !StringUtil.isEmptyOrNull( proxyPort ) ) {

                logger.info( "Registering proxy " + proxyHost + ":" + proxyPort ) ;
                final int port = Integer.parseInt( proxyPort ) ;
                this.httpClient.getHostConfiguration().setProxy( proxyHost, port ) ;

                if( useAuth &&
                    !StringUtil.isEmptyOrNull( userName ) &&
                    !StringUtil.isEmptyOrNull( password ) ) {

                    logger.info( "Enabling proxy authentication " + userName + ":" + password ) ;
                    enableProxyAuthentication( userName, password ) ;
                }
            }
        }
        else {
            LogMsg.info( "De-registering proxy." ) ;
            this.httpClient.getHostConfiguration().setProxyHost( null ) ;
            return ;
        }
    }

    /**
     * This method enables the proxy authentication for the HTTP Client with
     * the user supplied credentials. If the user name is in the format
     * [domain]\[username], it is assumed that NTLM authentication is used
     * and the domain is used as the realm.
     *
     * @param userName The user name to use. The user name can be domain qualified
     *        using the [domain]\[username] format.
     *
     * @param password The password to use
     *
     * @throws Exception In case the local host name could not be resolved.
     */
    private void enableProxyAuthentication( final String userName, final String password )
        throws UnknownHostException {

        String domain = null, name = null ;
        Credentials credentials = null ;

        // Extract the domain from the user name, if available
        int index = -1 ;
        if( ( index = userName.indexOf( '\\' ) ) != -1 ) {
            domain = userName.substring( 0, index ) ;
            name   = userName.substring( index+1 ) ;
        }
        else {
            name = userName ;
        }

        logger.debug( "Credentials = [domain=" + domain + ", user=" + name +
                      ", password=" + password ) ;

        // Create the appropriate credentials based on the supplied user name & password
        if( domain != null ) {
            logger.debug( "Enabling NTLM proxy authentication" ) ;
            final String localhost = InetAddress.getLocalHost().getHostAddress() ;
            credentials = new NTCredentials( name, password, localhost, domain ) ;
        }
        else {
            credentials = new UsernamePasswordCredentials( name, password ) ;
        }

        // Set the preferred list of authentication methods.
        final List<String> authPrefs = new ArrayList<String>();
        authPrefs.add( AuthPolicy.DIGEST ) ;
        authPrefs.add( AuthPolicy.BASIC ) ;
        authPrefs.add( AuthPolicy.NTLM ) ;
        this.httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs ) ;

        this.httpClient.getParams().setAuthenticationPreemptive( false ) ;
        this.httpClient.getState().setCredentials( AuthScope.ANY, credentials ) ;
    }

    // ---------------------- INITIALIZATION COMPLETE --------------------------

    /**
     * Receives events related to user preference changes and updates the
     * proxy configurations accordingly.
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public void handleEvent( final Event event ) {

        logger.debug( "Received event " + event.getEventType() ) ;
        if( event.getEventType() == EventType.USER_PREF_CHANGED ) {

            final Map<String, String> changedPrefs = (Map<String, String>)event.getValue() ;
            if( changedPrefs.containsKey( ConfigKey.USE_PROXY ) ) {
                try {
                    configureProxySettings() ;
                }
                catch ( final UnknownHostException e ) {
                    LogMsg.error( "Could not set proxy preferences. Msg = " + e.getMessage() ) ;
                    logger.error( "Could not set proxy preferences", e ) ;
                }
            }
        }
    }

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
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean checkNetworkStatus( final boolean force ) {
        final ConfigManager cfgMgr = ConfigManager.getInstance() ;
        final List<String> connectivityURLs = cfgMgr.getList( ConfigKey.CONNECTIVITY_CHECK_URLS ) ;

        logger.debug( "Checking network availability. Forced = " + force ) ;
        boolean status = false ;

        boolean performCheck = false ;
        if( this.manualOffline ) {
            // If the service has been manually set to offline, we return a
            // false all the time
            logger.debug( "Network under manual offline mode" ) ;
            performCheck = false ;
        }
        else if( force ) {
            logger.debug( "Performing forced network check" ) ;
            performCheck = true ;
        }
        else if( ( System.currentTimeMillis() - this.lastCheckTime ) >=
                  this.connCheckInterval ) {
            logger.debug( "Check interval check ok - checking" ) ;
            // If we are checking before the check interval, return the last
            // check status, else continue with the more rigorous checking.
            performCheck = true ;
        }

        if( performCheck  ) {
            // Try all the URLs specified, if all of them fail return a false,
            // else return a true.
            for( final String url : connectivityURLs ) {
                try {
                    logger.debug( "Checking for connectivity. URL=" + url ) ;
                    this.httpConnMgr.deleteClosedConnections() ;
                    getRawGETResult( url, (String[][])null ) ;
                    logger.info( "Network health pattern " +
                                 Integer.toBinaryString( this.pastConnectionAttemptStatus ) ) ;
                    status = true ;
                    break ;
                }
                catch ( final Exception e ) {
                    logger.debug( "Could not reach URL=" + url + ". Msg =" + e.getMessage() ) ;
                    continue ;
                }
            }

            logger.debug( "Network available = " + status ) ;
            this.lastCheckTime = System.currentTimeMillis() ;
        }

        // The net availability status would have updated the value of the
        // availability based on the heuristic logic.
        status = this.netAvailabilityStatus ;

        return status ;
    }

    /**
     * Returns the current network availability status.
     */
    public boolean isOnline() {
        return this.netAvailabilityStatus ;
    }

    /**
     * Sets the network service to offline mode and publishes the
     * {@link EventType#NETWORK_UNAVAILABLE} event. Any requests for post or
     * get while the network service is offline will result in an STException
     * being generated.
     */
    @Override
    public synchronized void setOffline() {
        this.manualOffline = true ;
        this.netAvailabilityStatus = false ;
        EventBus.publish( EventType.NETWORK_STATUS_CHANGE, Boolean.FALSE ) ;
    }

    /**
     * Sets the network service to online mode and publishes the
     * {@link EventType#NETWORK_AVAILABLE} event. Any requests for post or
     * get while the network service is offline will result in an STException
     * being generated. An attempt will be made to check the network status
     * before making the service online - if the underlying network is not
     * available, this method will not do anything.
     */
    @Override
    public synchronized void setOnline() {
        // Force check the network status. If we have set the network to manual
        // offline state - reset it before forcing the network check.
        this.manualOffline = false ;
        final boolean status = checkNetworkStatus( true ) ;
        EventBus.publish( EventType.NETWORK_STATUS_CHANGE, new Boolean( status ) ) ;
    }

    /**
     * Gets the contents of the specified URL by sending in the specified
     * name value parameters and returns the result returned by the server
     * as a string.
     *
     * @param url The URL to get the data from
     * @param parameters The parameters to append to the request URL
     *
     * @return The result of the get operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    @Override
    public byte[] getRawGETResult( final String url, final Map<String, String> parameters )
            throws STException {

        return communciate( url, "GET", parameters ) ;
    }

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
    public String getGETResult( final String url, final Map<String, String> parameters )
        throws STException {

        return new String( communciate( url, "GET", parameters ) ) ;
    }

    @Override
    public String getGETResult( final String url, final String[][] parameters )
            throws STException {
        return getGETResult( url, getMappedParams( parameters ) ) ;
    }

    @Override
    public byte[] getRawGETResult( final String url, final String[][] parameters )
            throws STException {
        return getRawGETResult( url, getMappedParams( parameters ) ) ;
    }

    @Override
    public String getGETResult( final String url )
            throws STException {
        return getGETResult( url, (Map<String, String>)null ) ;
    }

    @Override
    public byte[] getRawGETResult( final String url )
            throws STException {
        return getRawGETResult( url, (Map<String, String>)null ) ;
    }

    /**
     * Posts the name value pairs at the specified URL and returns the
     * result returned by the server as a string.
     *
     * @param url The URL to post the data (name value parameters) to.
     * @param parameters The parameters to post
     *
     * @return The result of the post operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    @Override
    public byte[] getRawPOSTResult( final String url, final Map<String, String> parameters )
            throws STException {

        return communciate( url, "POST", parameters ) ;
    }

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
    public String getPOSTResult( final String url, final Map<String, String> parameters )
        throws STException {

        return new String( communciate( url, "POST", parameters ) ) ;
    }


    @Override
    public String getPOSTResult( final String url, final String[][] parameters )
            throws STException {
        return getPOSTResult( url, getMappedParams( parameters ) ) ;
    }

    @Override
    public byte[] getRawPOSTResult( final String url, final String[][] parameters )
            throws STException {
        return getRawPOSTResult( url, getMappedParams( parameters ) ) ;
    }

    @Override
    public String getPOSTResult( final String url )
            throws STException {
        return getPOSTResult( url, (Map<String, String>)null ) ;
    }

    @Override
    public byte[] getRawPOSTResult( final String url )
            throws STException {
        return getRawPOSTResult( url, (Map<String, String>)null ) ;
    }

    /**
     * Posts or gets the contents at the specified URL with the name value
     * pairs specified as parameters.
     *
     * @param url The URL to post the data (name value parameters) to.
     * @param action The type of action to perform. Either "GET" or "POST"
     * @param parameters The parameters to post
     *
     * @return The result of the post operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation.
     */
    private byte[] communciate( final String url, final String action,
                                final Map<String, String> parameters )
        throws STException {

        final HttpMethod httpMethod = getHttpMethod( url, parameters, action ) ;
        byte[] response = null ;
        try {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Initiating URL communication with " + url ) ;
                logger.debug( "\tParameters = " + parameters ) ;
            }

            executeHTTPMethod( httpMethod, 0, null ) ;

            response = httpMethod.getResponseBody() ;

            // Update the number of bytes downloaded and publish an event
            // denoting that bytes have been downloaded from the network
            final long numBytes = response.length ;
            synchronized ( this.LOCK ) {
                this.numBytesDownloaded += numBytes ;
            }
            EventBus.publish( EventType.NETWORK_DATA_DOWNLOADED, new Long( numBytes ) ) ;

            for( int i=0; i<this.KNOWN_ERR_MSGS.length; i++ ) {
                final String responseStr = new String( response ) ;
                if( responseStr.contains( this.KNOWN_ERR_MSGS[i] ) ) {
                    throw new STException( "Proxy error while fetching url " + url +
                                           "\nMsg: " + this.KNOWN_ERR_MSGS[i],
                                           ErrorCode.NETWORK_UNAVAILABLE ) ;
                }
            }
            updateHeuristics( true ) ;
        }
        catch ( final Exception e ) {
            // Log the message appropriately
            LogMsg.error( "Network connection failed for URL " + url +
                          ". Reason = " + e.getMessage() ) ;
            if( e.getCause() != null ) {
                LogMsg.info( "   Root cause = " + e.getCause().getMessage() ) ;
            }

            updateHeuristics( false ) ;
            if( e instanceof STException ) {
                throw ( STException )e ;
            }
            else {
                throw new STException( action + " failed for URL " + url, e,
                                       ErrorCode.NETWORK_UNAVAILABLE ) ;
            }
        }
        finally {
            if( httpMethod != null ) {
                httpMethod.releaseConnection() ;
            }
        }
        return response ;
    }

    /**
     * Creates a HTTP method based on the URL, parameters and type of action
     * specified.
     *
     * @param url The URL to which this HTTP Method will point.
     * @param params The parameters which need to be sent to the target URL.
     * @param action The type of action that this HTTP method represents.
     *        Possible values are "GET" and "POST"
     *
     * @return A HttpMethod instance.
     */
    private HttpMethod getHttpMethod( final String url,
                                      final Map<String, String> params,
                                      final String action ) {
        HttpMethod httpMethod = null ;

        NameValuePair[] data = null ;

        if( params != null && !params.isEmpty() ) {
            data = new NameValuePair[params.size()] ;
            int index = 0 ;
            for( final String key : params.keySet() ) {
                final NameValuePair nvp = new NameValuePair() ;
                nvp.setName( key ) ;
                nvp.setValue( params.get( key ) ) ;
                data[index++] = nvp ;
            }
        }

        if( action.equals( "POST" ) ) {
            httpMethod = new PostMethod( url ) ;
            if( data != null && data.length > 0 ) {
                (( PostMethod )httpMethod).addParameters( data ) ;
            }
        }
        else {
            httpMethod = new GetMethod( url ) ;
            if( data != null && data.length > 0 ) {
                httpMethod.setQueryString( data ) ;
            }
        }

        httpMethod.setRequestHeader( "Cache-Control", "max-age=0" ) ;
        httpMethod.setRequestHeader( "Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5" ) ;
        httpMethod.setRequestHeader( "Accept-Encoding", "deflate,sdch" ) ;
        httpMethod.setRequestHeader( "Accept-Language", "en-US,en;q=0.8" ) ;
        httpMethod.setRequestHeader( "Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3" ) ;

        return httpMethod ;
    }

    /**
     * This operation executes the provided HTTP method and handles any
     * exceptions associated with the invocation.
     *
     * <pre>
     * java.io.IOException
     *    |- HttpException
     *    |  |- HttpContentTooLargeException
     *    |  |- HttpRecoverableException [Not thrown by anyone]
     *    |  |- ProtocolException
     *    |  |  |- AuthenticationException
     *    |  |  |   |- AuthChallengeException
     *    |  |  |   |- CredentialsNotAvailableException
     *    |  |  |   |- InvalidCredentialsException
     *    |  |  |- MalformedChallengeException
     *    |  |  |- cookie.MalformedCookieException
     *    |  |  |- RedirectException
     *    |  |      |- CircularRedirectException
     *    |  |      |- InvalidRedirectLocationException
     *    |  |- URIException
     *    |- NoHttpResponseException
     *    |- java.io.InterruptedIOException
     *       |- ConnectTimeoutException
     *          |- ConnectionPoolTimeoutException
     * </pre>
     *
     * @param method The HttpMethod to execute
     * @param trialCount The trial count of the execution attempt
     * @param lastError The last error because of which we are retrying the
     *        execution. If null, it would signify that this is the first time
     *        we are executing the method.
     */
    private void executeHTTPMethod( final HttpMethod method, final int trialCount,
                                    final Throwable lastError )
        throws STException {

        try {
            // If we have tried the HTTP method execution beyond the maximum
            // number of attemps, we fail
            if( trialCount > this.maxRetrialAttemps ) {
                throw new STException( "Maximum retrial attempts failed", lastError,
                                       ErrorCode.NETWORK_CONNECTION_FAILURE ) ;
            }

            if( logger.isDebugEnabled() ) {
                logger.debug( "Retrial count = " + trialCount ) ;
            }

            // If this is not the first invocation, i.e. we are retrying a failed
            // connection, we sleep for the configured retrial interval.
            if( trialCount != 0 ) {
                try {
                    Thread.sleep( this.retrialInterval ) ;
                }
                catch( final InterruptedException e ) { /* Ignore */ }
            }

            final int status = this.httpClient.executeMethod( method ) ;
            if( logger.isDebugEnabled() ) {
                logger.debug( "HTTP execution status = " + HttpStatus.getStatusText( status ) +
                              ". Status code = " + status ) ;
            }

            processHttpCommunicationStatus( status ) ;
        }
        catch ( final HttpException e ) {
            // For a protocol exception - there is no way we can recover. Print
            // an error message and throw an exception.
            final String errMsg = "Communication error - Unrecoverable protocol exception. Msg=" + e.getMessage() ;
            logger.error( errMsg ) ;
            throw new STException( errMsg, e, ErrorCode.NETWORK_CONNECTION_FAILURE ) ;
        }
        catch ( final IOException e ) {
            // IO exceptions can be recovered from. We just log the appropriate
            // cause of failure for this trial count and call on this method again
            if( e instanceof ConnectionPoolTimeoutException ) {
                logger.info( "Communication error - Time out occured trying to get a HTTP connection from pool" ) ;
            }
            else if( e instanceof ConnectTimeoutException ) {
                logger.info( "Communication error - Time out occured trying to execute HTTP connection" ) ;
            }
            else if( e instanceof NoHttpResponseException ) {
                logger.info( "Communication error - No response from server" ) ;
            }
            else {
                logger.info( "Communication error - " + e.getMessage() ) ;
            }

            // Call on the execute method again
            executeHTTPMethod( method, trialCount+1, e ) ;
        }
    }

    /**
     * Processes the return code of the HTTP execute method and classifies
     * them as recoverable or unrecoverable exceptions.
     */
    void processHttpCommunicationStatus( final int statusCode )
        throws IOException, HttpException {

        if( statusCode == HttpStatus.SC_UNAUTHORIZED ) {
            throw new HttpException( "Unauthorized access.. possibly needs credentials" ) ;
        }
    }

    /**
     * Gets the contents of the specified URL by sending in the specified
     * name value parameters and returns the result returned by the server
     * by type-casting it into a {@link ChartData} instance.
     *
     * @param url The URL to get the data from
     * @param params The parameters to append to the request URL
     *
     * @return The result of the get operation as returned by the server
     *
     * @throws STException If an exception is encountered during the network
     *         operation or if the network service is currently offline.
     */
    public ChartData getChartDataGET( final String url, final Map<String, String> params )
        throws STException {

        throw new UnsupportedOperationException( "New logic yet to be " +
                                     "implemented for getting intraday data" ) ;
        /*
        final ChartData  retVal     = null ;
        final HttpMethod httpMethod = null ;

        try {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Initiating URL communication with " + url ) ;
                logger.debug( "\tParameters = " + params ) ;
            }
            httpMethod = getHttpMethod( url, params, "GET" ) ;
            this.httpClient.executeMethod( httpMethod ) ;

            final byte[] response = httpMethod.getResponseBody() ;

            // Update the number of bytes downloaded and publish an event
            // denoting that bytes have been downloaded from the network
            final long numBytes = response.length ;
            synchronized ( this.LOCK ) {
                this.numBytesDownloaded += numBytes ;
            }
            EventBus.publish( EventType.NETWORK_DATA_DOWNLOADED, new Long( numBytes ) ) ;

            if( response == null || response.length < 4 ) {
                throw new STException( "Invalid serialized response",
                                       ErrorCode.NETWORK_UNAVAILABLE ) ;
            }
            else {
                for( int i=0; i<SERIALIZED_SIGNATURE.length; i++ ) {
                    if( response[i] != SERIALIZED_SIGNATURE[i] ) {
                        throw new STException( "Invalid serialized response",
                                               ErrorCode.NETWORK_UNAVAILABLE ) ;
                    }
                }

                final byte[] modBytes = new byte[ response.length +
                                                  REP_BYTE.length -
                                                  NSE_BYTE.length ] ;

                System.arraycopy( REP_BYTE, 0, modBytes, 0, REP_BYTE.length ) ;
                System.arraycopy( response, NSE_BYTE.length,
                                  modBytes, REP_BYTE.length,
                                  response.length - NSE_BYTE.length ) ;

                final ByteArrayInputStream bis = new ByteArrayInputStream( modBytes ) ;
                final ObjectInputStream    ois = new ObjectInputStream( bis ) ;
                retVal = ( ChartData )ois.readObject() ;

                updateHeuristics( true ) ;
            }
       }
        catch ( final Exception e ) {
            LogMsg.warn( "Network connection failed for URL " + url ) ;
            updateHeuristics( false ) ;
            if( e instanceof STException ) {
                throw ( STException )e ;
            }
            else {
                throw new STException( "GET failed for URL " + url, e,
                                       ErrorCode.NETWORK_UNAVAILABLE ) ;
            }
        }
        finally {
            if( httpMethod != null ) {
                httpMethod.releaseConnection() ;
            }
        }
        return retVal ; */
    }

    /**
     * Appends to the heuristics of the last 'n' connection attempts and
     * sets the availability status to true or false depending upon whether
     * the all the last 'n' connection attempts have been failures.
     *
     * @param status The status of the current attempt
     *
     * @return If availability has to be switched off based on past connection
     *         patterns. true indicates network is available, false implies
     *         it should be transitioned to the unavailable status.
     */
    private synchronized boolean updateHeuristics( final boolean status ) {

        // Publish the communication status on the event bus only if the status
        // has changed since the last time.
        EventBus.publish( EventType.NETWORK_COMM_STATUS, new Boolean( status ) ) ;

        if( this.autoOfflineMask <= 0 ) {
            return this.netAvailabilityStatus ;
        }

        logger.debug( "Updating network health heuristics" ) ;
        // Shift the earlier attempt stats by 1 to the left, making space for
        // storing the status of this attempt
        this.pastConnectionAttemptStatus <<= 1 ;

        // The last bit is 1 or 0 depending upon whether this attempt was a
        // success or failure.
        if( status ) {
            this.pastConnectionAttemptStatus |= 0x1 ;
        }
        else {
            this.pastConnectionAttemptStatus |= 0x0 ;
        }

        // Clear off the higher order bits
        this.pastConnectionAttemptStatus &= this.autoOfflineMask ;

        // Check if we should set the availability status to false.
        final int invertedAttempts = ~this.pastConnectionAttemptStatus ;

        final boolean oldStatus = this.netAvailabilityStatus ;
        if( ( invertedAttempts & this.autoOfflineMask ) == this.autoOfflineMask ) {
            logger.debug( "Last connection attemps failed - switching " +
                          "availability to false" ) ;
            this.netAvailabilityStatus = false ;
        }
        else {
            this.netAvailabilityStatus = true ;
        }

        // Publish the state change of network connectivity if the old
        // status is not the same as the new status.
        if( this.netAvailabilityStatus != oldStatus ) {
            EventBus.publish( EventType.NETWORK_STATUS_CHANGE, this.netAvailabilityStatus ) ;
            if( this.netAvailabilityStatus ) {
                LogMsg.info( "Network available" ) ;
            }
            else {
                LogMsg.error( "Network unavailable" ) ;
            }
        }

        return this.netAvailabilityStatus ;
    }

    /**
     * Returns the number of bytes downloaded since the time the application
     * was started.
     *
     * @return A long value indicating the number of bytes downloaded.
     */
    public long getNumBytesDownloaded() {
        return this.numBytesDownloaded ;
    }

    /** @return the userPrefSvc */
    public IUserPreferenceSvc getUserPrefSvc() {
        return this.userPrefSvc ;
    }

    /** @param userPrefSvc the userPrefSvc to set */
    public void setUserPrefSvc( final IUserPreferenceSvc userPrefSvc ) {
        this.userPrefSvc = userPrefSvc ;
    }

    /**
     * This private utility method converts a two dimensional array of name
     * value parameters into a Map<String, String> format. If the input array
     * is null or is of zero size, a null value is returned.
     */
    private Map<String, String> getMappedParams( final String[][] params ) {
        Map<String, String> retVal = null ;

        if( params != null && params.length>0 ) {
            retVal = new HashMap<String, String>() ;
            for( int i=0; i<params.length; i++ ) {
                retVal.put( params[i][0].trim(), params[i][1].trim() ) ;
            }
        }
        return retVal ;
    }
}
