@echo off

set cp=..\lib\aspectjweaver.jar;..\lib\chartfiles.jar;..\lib\commons-beanutils-1.7.0.jar;..\lib\commons-codec-1.3.jar;..\lib\commons-collections-3.2.jar;..\lib\commons-configuration-1.5.jar;..\lib\commons-dbcp-1.2.2.jar;..\lib\commons-digester-1.8.jar;..\lib\commons-httpclient-3.1-rc1.jar;..\lib\commons-lang-2.3.jar;..\lib\commons-logging-1.0.4.jar;..\lib\commons-pool-1.3.jar;..\lib\htmllexer.jar;..\lib\htmlparser.jar;..\lib\ibatis-2.3.0.677.jar;..\lib\javacsv.jar;..\lib\jdom.jar;..\lib\log4j-1.2.9.jar;..\lib\postgresql-8.2-507.jdbc4.jar;..\lib\prism-core-1.01.jar;..\lib\quartz-all-1.6.1-RC1.jar;..\lib\rome-1.0RC1.jar;..\lib\sandy.commons.jar;..\lib\spring-2.0.6.jar;..\lib\StockTracker.jar;..\config

start javaw -cp %cp% com.sandy.stocktracker.StockTracker
