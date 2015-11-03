/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.shared;

/**
 * This enumeration contains the possible error codes that can result in an
 * {@link STException} getting generated.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public enum ErrorCode {

    UNKNOWN_EXCEPTION,
    EOD_IMPORT_FAILURE,
    ITD_IMPORT_FAILURE,
    DOWNLOAD_FAILURE,
    NETWORK_UNAVAILABLE,
    INIT_FAILURE,
    SCRIP_NOT_REGISTERED,
    INVALID_INDEX_NAME,
    RSS_PARSE_FAILURE,
    NETWORK_CONNECTION_FAILURE,
    INVALID_ARG
}
