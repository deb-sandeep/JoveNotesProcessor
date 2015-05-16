package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * An extremely thin wrapper of Apache's property configuration. This class just
 * introduces an easy wrapper over the initialization mechanism. 
 * 
 * Before using an instance of this class, one of the initialize method needs 
 * to be called on it.
 * 
 * @see <a href="http://commons.apache.org/proper/commons-configuration/javadocs/v1.10/apidocs/index.html?org/apache/commons/configuration/PropertiesConfiguration.html">PropertiesConfiguration</a>
 * 
 * @author Sandeep
 */
public class ConfigManager extends PropertiesConfiguration {

	private static final Logger logger = Logger.getLogger(ConfigManager.class);

	public static final String DEFAULT_CONFIG_RESOURCE = "/config.properties";

	public ConfigManager(){}

	/** 
	 * This method tries to initialize with the default configuration file by 
	 * the name of config.properties in the classpath.
	 */
	public synchronized void initialize() throws ConfigurationException {
		final URL cfgURL = ConfigManager.class.getResource( DEFAULT_CONFIG_RESOURCE );
		initialize( cfgURL );
	}

	/** 
	 * Configuration from one or more (possibly different) sources can be loaded
	 * via this method. 
	 * 
	 * @param cfgURLList An array which can contain a mix of String, URL or File
	 *        instances. The String instances point to resources on the classpath
	 *        which can be loaded as properties.
	 */
	public synchronized void initialize( final List<Object> cfgURLList ) 
		throws ConfigurationException {

		for( Iterator<Object> iter = cfgURLList.iterator(); iter.hasNext();) {
			
			final Object cfgRes = iter.next();
			
			if( cfgRes instanceof String ){
				super.load( ConfigManager.class.getResource((String) cfgRes) ) ;
			} 
			else if( cfgRes instanceof URL ){
				super.load((URL) cfgRes);
			} 
			else if( cfgRes instanceof File ){
				super.load((File) cfgRes);
			} 
			else{
				final String msg = "Unindentified configuration resource " + cfgRes;
				logger.error(msg);
				throw new ConfigurationException( msg ) ;
			}
		}
	}

	/** Initialize the configuration manager with a URL. */
	public synchronized void initialize( final URL cfgURL )
		throws ConfigurationException {
		
		super.load(cfgURL);
	}
}
