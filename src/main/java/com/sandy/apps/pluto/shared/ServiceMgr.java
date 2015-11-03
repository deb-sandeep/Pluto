/**
 * Creation Date: Aug 12, 2008
 */

package com.sandy.apps.pluto.shared;

import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Method ;
import java.lang.reflect.Proxy ;

import com.sandy.apps.pluto.biz.dao.IITDIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ITradeDAO ;
import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc ;
import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc.AsyncTask ;
import com.sandy.apps.pluto.biz.svc.IEODImportSvc ;
import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.biz.svc.IITDBulkImportSvc ;
import com.sandy.apps.pluto.biz.svc.IITDImportSvc ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.biz.svc.IScripSvc ;
import com.sandy.apps.pluto.biz.svc.IUserPreferenceSvc ;
import com.sandy.apps.pluto.biz.svc.MethodExecutionAsyncTask ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.SpringObjectFactory ;
import com.sandy.apps.pluto.ui.menumgr.MenuManager ;
import com.sandy.apps.pluto.ui.menumgr.SystemEventMenuEnabler ;
import com.sandy.apps.pluto.ui.svc.STViewService ;

/**
 * This singleton utility class provides easy methods to lookup the service
 * implements via the DI container. This class also provides asynchronous
 * versions of the services. Asynchronous services are special service instances,
 * whose methods are invoked asynchronously on the real service instances
 * using the async executor.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ServiceMgr {

    // The names of the services exposed via the dependency injection container
    public static final String NETWORK_SVC          = "NetworkSvc" ;
    public static final String EX_INDEX_SVC         = "ExIndexSvc" ;
    public static final String JOB_SVC              = "JobSvc" ;
    public static final String USER_PREF_SVC        = "UserPreferenceSvc" ;
    public static final String EOD_IMPORT_SVC       = "EODImportSvc" ;
    public static final String ITD_IMPORT_SVC       = "ITDImportSvc" ;
    public static final String MENU_MANAGER         = "MenuManager" ;
    public static final String MENU_ENABLER         = "SystemEventMenuEnabler" ;
    public static final String ASYNC_EXEC_SVC       = "AsyncExecutorSvc" ;
    public static final String SCRIP_SVC            = "ScripSvc" ;
    public static final String ITD_BULK_IMPORT_SVC  = "ITDBulkImportSvc" ;
    public static final String ST_VIEW_SVC          = "STViewService" ;
    public static final String RSS_SVC              = "RSSSvc" ;

    public static final String SCRIP_ITD_DAO        = "ITDIndexDAO" ;
    public static final String TRADE_DAO            = "TradeDAO" ;

    // A reference to the business service object factory.
    private static SpringObjectFactory OF = BizObjectFactory.getInstance() ;

    /**
     * This invocation handler is used to create dynamic proxies for services
     * which are used to invoke methods asynchronously.
     *
     * @author Sandeep Deb [deb.sandeep@gmail.com]
     */
    private static class AsyncInvocationHandler implements InvocationHandler {

        private Object target = null ;

        /** Public constructor which accepts the target object. */
        public AsyncInvocationHandler( final Object tgt ) {
            this.target = tgt ;
        }

        /**
         * Invokes the specified method asynchronously on the target object
         * using the async executor service.
         */
        @Override
        public Object invoke( final Object proxy, final Method method,
                              final Object[] args )
                throws Throwable {
            final AsyncTask task = new MethodExecutionAsyncTask( this.target, method, args ) ;
            getAsyncExecutorSvc().submit( task ) ;
            return null ;
        }
    }

    /** Private constructor to enforce the singleton utility class pattern. */
    private ServiceMgr() {
        super() ;
    }

    public static INetworkSvc getNetworkSvc() {
        return ( INetworkSvc )OF.getBean( NETWORK_SVC ) ;
    }

    public static IExIndexSvc getExIndexSvc() {
        return ( IExIndexSvc )OF.getBean( EX_INDEX_SVC ) ;
    }

    public static IExIndexSvc getAsyncExIndexSvc() {
        final Class<?>[] interfaceClsArr = new Class[]{ IExIndexSvc.class } ;
        final IExIndexSvc svc = getExIndexSvc() ;
        final InvocationHandler handler = new AsyncInvocationHandler( svc ) ;
        return ( IExIndexSvc )Proxy.newProxyInstance( IExIndexSvc.class.getClassLoader(), interfaceClsArr, handler ) ;
    }

    public static IUserPreferenceSvc getUserPrefSvc() {
        return ( IUserPreferenceSvc )OF.getBean( USER_PREF_SVC ) ;
    }

    public static IEODImportSvc getEODImportSvc() {
        return ( IEODImportSvc )OF.getBean( EOD_IMPORT_SVC ) ;
    }

    public static IITDImportSvc getITDImportSvc() {
        return ( IITDImportSvc )OF.getBean( ITD_IMPORT_SVC ) ;
    }

    public static IITDImportSvc getAsyncITDImportSvc() {

        final Class<?>[] interfaceClsArr = new Class[]{ IITDImportSvc.class } ;
        final IITDImportSvc svc = getITDImportSvc() ;
        final InvocationHandler handler = new AsyncInvocationHandler( svc ) ;
        return ( IITDImportSvc )Proxy.newProxyInstance( IITDImportSvc.class.getClassLoader(), interfaceClsArr, handler ) ;
    }

    public static IITDBulkImportSvc getITDBulkImportSvc() {
        return ( IITDBulkImportSvc )OF.getBean( ITD_BULK_IMPORT_SVC ) ;
    }

    public static MenuManager getMenuManager() {
        return ( MenuManager )OF.getBean( MENU_MANAGER ) ;
    }

    public static SystemEventMenuEnabler getMenuEnabler() {
        return ( SystemEventMenuEnabler )OF.getBean( MENU_ENABLER ) ;
    }

    public static IJobSvc getJobSvc() {
        return ( IJobSvc )OF.getBean( JOB_SVC ) ;
    }

    public static IScripSvc getScripSvc() {
        return ( IScripSvc )OF.getBean( SCRIP_SVC ) ;
    }

    public static STViewService getSTViewService() {
        return ( STViewService )OF.getBean( ST_VIEW_SVC ) ;
    }

    public static IRSSSvc getRSSSvc() {
        return ( IRSSSvc )OF.getBean( RSS_SVC ) ;
    }

    public static IITDIndexDAO getITDIndexDAO() {
        return ( IITDIndexDAO )OF.getBean( SCRIP_ITD_DAO ) ;
    }

    public static ITradeDAO getTradeDAO() {
        return ( ITradeDAO )OF.getBean( TRADE_DAO ) ;
    }

    public static IJobSvc getAsyncJobSvc() {

        final Class<?>[] interfaceClsArr = new Class[]{ IJobSvc.class } ;
        final IJobSvc jobSvc = getJobSvc() ;
        final InvocationHandler handler = new AsyncInvocationHandler( jobSvc ) ;

        return ( IJobSvc )Proxy.newProxyInstance( IJobSvc.class.getClassLoader(), interfaceClsArr, handler ) ;
    }

    public static IAsyncExecutorSvc getAsyncExecutorSvc() {
        return ( IAsyncExecutorSvc )OF.getBean( ASYNC_EXEC_SVC ) ;
    }
}
