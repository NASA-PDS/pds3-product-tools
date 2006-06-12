//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
// $Id$ 
//

package gov.nasa.jpl.pds.tools.dict.parser;

import gov.nasa.jpl.pds.tools.TraceableException;

/**
 * @author pramirez
 * @version $Revision$
 * 
 */
public class UnknownDefinitionException extends Exception {
    private static final long serialVersionUID = 3768577117762341629L;

    public UnknownDefinitionException(String message) {
        super(message);
    }

}
