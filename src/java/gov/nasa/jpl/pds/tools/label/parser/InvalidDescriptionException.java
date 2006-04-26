//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
// $Id$ 
//

package gov.nasa.jpl.pds.tools.label.parser;

/**
 * @author pramirez
 * @version $Revision$
 * 
 */
public class InvalidDescriptionException extends Exception {
    private static final long serialVersionUID = 2387205637056116609L;

    public InvalidDescriptionException(String message) {
        super(message);
    }
}
