/**
 * Creation Date: Aug 9, 2008
 */
package com.sandy.apps.pluto.shared.dto ;

import java.io.Serializable ;
import java.util.Vector ;

/**
 * NOTE: This class is copied from NSE Applet and should NOT be modified under
 * any circumstances. This represents the data structure in which NSE streams
 * data to its applet.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
@SuppressWarnings("serial")
public class ChartData implements Serializable {

    public ChartData() {
    }

    public void setSymbol( final String s ) {
        this.symbol = s ;
    }

    public String getSymbol() {
        return this.symbol ;
    }

    public void setSymbol1( final String s ) {
        this.symbol1 = s ;
    }

    public String getSymbol1() {
        return this.symbol1 ;
    }

    public void setSymbol2( final String s ) {
        this.symbol2 = s ;
    }

    public String getSymbol2() {
        return this.symbol2 ;
    }

    public void setSeries( final String s ) {
        this.series = s ;
    }

    public String getSeries() {
        return this.series ;
    }

    public void setMktType( final String s ) {
        this.mktType = s ;
    }

    public String getMktType() {
        return this.mktType ;
    }

    public void setIndexName( final String s ) {
        this.indexName = s ;
    }

    public String getIndexName() {
        return this.indexName ;
    }

    public void setPrevClose( final String s ) {
        this.prevClose = s ;
    }

    public String getPrevClose() {
        return this.prevClose ;
    }

    @SuppressWarnings("unchecked")
    public void setStockData( final Vector avector[] ) {
        this.stockData = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getStockData() {
        return this.stockData ;
    }

    public void setInstrument( final String s ) {
        this.instrument = s ;
    }

    public String getInstrument() {
        return this.instrument ;
    }

    public void setExpiryDate( final String s ) {
        this.expiryDate = s ;
    }

    public String getExpiryDate() {
        return this.expiryDate ;
    }

    public void setOptionType( final String s ) {
        this.optionType = s ;
    }

    public String getOptionType() {
        return this.optionType ;
    }

    public void setStrikePrice( final String s ) {
        this.strikePrice = s ;
    }

    public String getStrikePrice() {
        return this.strikePrice ;
    }

    @SuppressWarnings("unchecked")
    public void setStock1Data( final Vector avector[] ) {
        this.stock1Data = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getStock1Data() {
        return this.stock1Data ;
    }

    @SuppressWarnings("unchecked")
    public void setStock2Data( final Vector avector[] ) {
        this.stock2Data = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getStock2Data() {
        return this.stock2Data ;
    }

    @SuppressWarnings("unchecked")
    public void setIndexData( final Vector avector[] ) {
        this.indexData = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getIndexData() {
        return this.indexData ;
    }

    @SuppressWarnings("unchecked")
    public void setContractData( final Vector avector[] ) {
        this.contractData = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getContractData() {
        return this.contractData ;
    }

    @SuppressWarnings("unchecked")
    public void setUnderlyingData( final Vector avector[] ) {
        this.underlyingData = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getUnderlyingData() {
        return this.underlyingData ;
    }

    public void setPlot1( final String s ) {
        this.plot1 = s ;
    }

    public String getPlot1() {
        return this.plot1 ;
    }

    public void setPlot2( final String s ) {
        this.plot2 = s ;
    }

    public String getPlot2() {
        return this.plot2 ;
    }

    @SuppressWarnings("unchecked")
    public void setPlot1Data( final Vector avector[] ) {
        this.plot1Data = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getPlot1Data() {
        return this.plot1Data ;
    }

    @SuppressWarnings("unchecked")
    public void setPlot2Data( final Vector avector[] ) {
        this.plot2Data = avector ;
    }

    @SuppressWarnings("unchecked")
    public Vector[] getPlot2Data() {
        return this.plot2Data ;
    }

    String symbol ;
    String symbol1 ;
    String symbol2 ;
    String indexName ;
    String series ;
    String mktType ;
    String prevClose ;
    String instrument ;
    String expiryDate ;
    String optionType ;
    String strikePrice ;
    String plot1 ;
    String plot2 ;
    @SuppressWarnings("unchecked")
    Vector stockData[] ;
    @SuppressWarnings("unchecked")
    Vector stock1Data[] ;
    @SuppressWarnings("unchecked")
    Vector stock2Data[] ;
    @SuppressWarnings("unchecked")
    Vector indexData[] ;
    @SuppressWarnings("unchecked")
    Vector contractData[] ;
    @SuppressWarnings("unchecked")
    Vector underlyingData[] ;
    @SuppressWarnings("unchecked")
    Vector plot1Data[] ;
    @SuppressWarnings("unchecked")
    Vector plot2Data[] ;
}
