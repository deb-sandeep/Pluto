/**
 * Creation Date: Aug 2, 2008
 */

package com.sandy.apps.pluto.shared;

/**
 * This interface contains static constants identifying the configuration
 * parameters for this application.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface ConfigKey {

    /**
     * The configuration key which influences whether the EventBus dispatches
     * event in an asynchronous fashion. If this configuration is not explicitly
     * specified, the default value of false is used.
     *
     * @valueType "true" or "false"
     */
    String ASYNC_EVENT_DISPATCH = "event.bus.event.dispatch.async" ;

    /**
     * The configuration key, value of which signifies the directory where
     * the EOD historic data will be stored.
     */
    String EOD_DOWNLOAD_DIR = "nse.eod.historic.download.location" ;

    /**
     * The key against which the list of URLs to check for connectivity status
     * should be specified. The values should be delimited by a ,
     */
    String CONNECTIVITY_CHECK_URLS = "connectivity.check.urls" ;

    /**
     * The key against which the maximum number of network retrial attemps are
     * specified. The default value is set by the DEF_MAX_NETWORK_RETRIAL
     * constant.
     */
    String MAX_NETWORK_RETRIALS = "network.max.retrial.count" ;

    /** The default number of maximum retrials. */
    int DEF_MAX_NETWORK_RETRIAL = 5 ;

    /**
     * The key against which the retrial interval is specified in milliseconds.
     */
    String NETWORK_RETRIAL_INTERVAL = "network.retrial.interval" ;

    /** The default retrial interval in milliseconds. */
    int DEF_RETRIAL_INTERVAL = 500 ;

    /**
     * Time interval for checking the network connectivity. Any request to
     * check for the network connectivity within last check time + check interval,
     * will return the result of the last network checked status. The
     * value of this parameter should be an integer and be specified as
     * milliseconds.
     */
    String CONNECTIVITY_CHECK_INTERVAL = "connectivity.check.interval" ;

    /**
     * The number of consecutive failure attempts after which the network
     * service transitions itself to the offline status and maintains the
     * offline status till check connectivity is called upon explicitly. If this
     * value is not specified, the network service does not switch off after
     * connection failures.
     */
    String CONNECTIVITY_SWITCH_OFF_NUM_FAILURES = "connectivity.switch.off.num.failures" ;

    /** The proxy host. */
    String PROXY_HOST = "http.proxyHost" ;

    /** The proxy port. */
    String PROXY_PORT = "http.proxyPort" ;

    /** A boolean flag to indicate if proxy configuration should be used. */
    String USE_PROXY = "http.useProxy" ;

    /** A boolean flag to indicate if proxy credentials are to be used. */
    String USE_AUTH = "http.useAuthentication" ;

    /** The user name key for proxy authentication. */
    String PROXY_USER = "http.auth.userName" ;

    /** The passwork key for proxy authentication. */
    String PROXY_PWD = "http.auth.password" ;

    /**
     * The key against which the HTTP user agent is specified. This is a default
     * parameter and if not specified a default user agent name by the
     * underlying HTTP framework is used.
     */
    String USER_AGENT = "httpclient.useragent" ;

    /**
     * A user preference key to specify the resolution of the NSE index intra
     * day data that has to be persisted. If two ITD values are received
     * within the specified resolution interval, the latest one is not
     * considered for persistent. Although, the latest event is published
     * on the event bus for in memory consumption. Default value of this
     * parameter is 30000 milliseconds.
     */
    String NSE_INDEX_ITD_RESOLUTION = "nse.index.itd.resolution" ;

    /**
     * A user preference key to specify the resolution of the NSE scrip ITD
     * data that has to be persisted. If two ITD values are received
     * within the specified resolution interval, the latest one is not
     * considered for persistent. Although, the latest event is published
     * on the event bus for in memory consumption. Default value of this
     * parameter is 6000 milliseconds.
     */
    String NSE_SCRIP_ITD_RESOLUTION = "nse.scrip.itd.resolution" ;
}
