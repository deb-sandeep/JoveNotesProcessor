/**
 * Hosts utility classes such as configuration management, context management,
 * command line processors etc.
 * 
 * As a design principle, none of the utility classes are singleton by themselves.
 * Arguably some of these classes, for example configuration manager or command
 * line processor are better suited as singleton instances. Undeniable. However,
 * as a practice if a singleton instances is required, the management of instances
 * is left to a single separate class in the application.
 * 
 * @author Sandeep
 */
package com.sandy.jovenotes.processor.util;