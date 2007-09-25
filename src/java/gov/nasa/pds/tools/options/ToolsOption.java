package gov.nasa.pds.tools.options;

import org.apache.commons.cli.Option;

/**
 * Class that extends Apache's Option class. Provides a simpler interface to describe a command-line
 * option.
 * 
 * Like its superclass, the ToolsOption is not created independantly, but through an
 * instance of the Options class.
 * 
 * @author mcayanan
 *
 */
public class ToolsOption extends Option {
	
	private final char argSeparator = ' ';
	
	/**
	 * Contstructor.
	 * 
	 * @param opt Short name of the option.
	 * @param longOpt Long name of the option. Can be set to 'null'.
	 * @param description Description of the option.
	 */
	public ToolsOption(String opt, String longOpt, String description) {
		super(opt, longOpt, false, description);
	}
			
	/**
	 * Requires a single argument to follow the option.
	 * 
	 * @param name Sets the display name of the argument value.
	 * @param type Sets the data type allowed for this argument.
	 */
	public void hasArg(String name, Object type) {
		hasArg(name, type, false);
	}
	
	/**
	 * Allows a single argument to be passed into the option. 
	 * 
	 * @param name Sets the display name of the argument value.
	 * @param type Sets the data type allowed for this argument.
	 * @param isOptional Set to 'true' if the argument is optional, 'false' otherwise.
	 */
	public void hasArg(String name, Object type, boolean isOptional) {
		char nullChar = '\0';
		hasArgs(1, name, type, nullChar, isOptional);
	}
	
	/**
	 * Requires an argument to follow the option. This method allows the option
	 * to take in multiple arguments. Does not define a maximum
	 * number of allowable arguments. 
	 * 
	 * The separator value is set to the space character ' '. 
	 *  
	 * @param name Sets the display name of the argument value.
	 * @param type Sets the data type allowed for this argument.
	 */
	public void hasArgs(String name, Object type) {
		hasArgs(name, type, argSeparator, false);
	}
	
	/**
	 * Requires an argument to follow the option. Allows multiple arguments
	 * to be passed in to the option. Does not define a maximum number of
	 * allowable arguments. 
	 * 
	 * 
	 * @param name Sets the display name of the argument value.
	 * @param type Sets the data type allowed for this argument.
	 * @param separator Sets the separator value allowed in between the
	 * argument values being passed in.
	 */
	public void hasArgs(String name, Object type, char separator) {
		hasArgs(name, type, separator, false);
	}
	
	/**
	 * Allows multiple arguments to be passed in to the option. Does not
	 * define a maximum number of allowable arguments.
	 * 
	 * @param name Sets the display name of the argument value.
	 * @param type Sets the data type allowed for this argument.
	 * @param separator Sets the separator value allowed in between the
	 * argument values being passed in.
	 * @param isOptional Set to 'true' if an argument is optional, 
	 * 'false' otherwise.
	 */
	public void hasArgs(String name, Object type, char separator, boolean isOptional) {
		hasArgs(Option.UNLIMITED_VALUES, name, type, separator, isOptional);
	}
	
	/**
	 * Defines an argument's "properties" for an option.
	 * 
	 * @param numArgs Max number of arguments allowed.
	 * @param name Sets the display name of the argument value.
	 * @param type Sets the data type allowed for this argument.
	 * @param separator Sets the separator value allowed in between the
	 * argument values being passed in.
	 * @param isOptional Set to 'true' if an argument is optional, 'false'
	 * otherwise.
	 */
	public void hasArgs(int numArgs, String name, Object type, char separator, boolean isOptional) {
		setArgs(numArgs);
		setArgName(name);
		setType(type);
		setValueSeparator(separator);
		setOptionalArg(isOptional);
	}

}