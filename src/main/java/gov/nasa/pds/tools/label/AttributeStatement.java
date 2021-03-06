// Copyright 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// • Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// • Redistributions must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other
// materials provided with the distribution.
// • Neither the name of Caltech nor its operating division, the Jet Propulsion
// Laboratory, nor the names of its contributors may be used to endorse or
// promote products derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package gov.nasa.pds.tools.label;

import gov.nasa.arc.pds.tools.util.StrUtils;
import gov.nasa.pds.tools.dict.parser.DictIDFactory;
import gov.nasa.pds.tools.util.Utility;

/**
 * @author pramirez
 * @author jagander
 * @version $Revision$
 * 
 */
public class AttributeStatement extends Statement {

    private Value value;

    private String namespace;

    private String elementIdentifier;

    /**
     * Constructs a new attribute statement with no value
     * 
     * @param lineNumber
     *            Line on which the statement starts
     * @param identifier
     *            Uniquely identifies the statement
     */
    protected AttributeStatement(Label sourcelabel, int lineNumber,
            String identifier) {
        this(sourcelabel, lineNumber, identifier, null);
    }

    /**
     * Constructs a new attribute statement with no line number or value
     * 
     * @param identifier
     *            Uniquely identifies the statement
     */
    public AttributeStatement(Label sourcelabel, String identifier) {
        this(sourcelabel, identifier, null);
    }

    /**
     * Constructs a new attribute statement with no line number
     * 
     * @param identifier
     *            Uniquely identifies the statement
     * @param value
     *            {@link Value} of the attribute
     */
    public AttributeStatement(Label sourcelabel, String identifier, Value value) {
        this(sourcelabel, -1, identifier, value);
    }

    /**
     * 
     * @param lineNumber
     *            Line on which the statement starts
     * @param identifier
     *            Uniquely identifies the statement
     * @param value
     *            {@link Value} of the attribute
     */
    @SuppressWarnings("nls")
    public AttributeStatement(Label sourcelabel, int lineNumber,
            String identifier, Value value) {
        super(sourcelabel, lineNumber, DictIDFactory
                .createElementDefId(identifier));

        this.namespace = "";
        if (identifier.indexOf(":") != -1) {
            this.namespace = identifier.substring(0, identifier.indexOf(":"));
        }

        if (identifier.indexOf(":") == -1) {
            this.elementIdentifier = identifier;
        } else {
            this.elementIdentifier = identifier.substring(identifier
                    .indexOf(":") + 1);
        }

        this.value = value;

        this.comment = null;
    }

    /**
     * Gets the namespace for this attribute
     * 
     * @return The namespace or "" if none is found.
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Gets the unqualified identifier for the att
     * 
     * @return Returns the element identifier.
     */
    public String getElementIdentifier() {
        return this.elementIdentifier;
    }

    /**
     * Retrieves the value of the attribute
     * 
     * @return {@link Value} of the attribute
     */
    public Value getValue() {
        return this.value;
    }

    /**
     * Sets the value for this attribute
     * 
     * @param value
     *            {@link Value} of the attribute
     */
    public void setValue(Value value) {
        this.value = value;
    }

    public boolean hasNamespace() {
        return this.namespace.length() > 0;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return StrUtils.toString(this.identifier) + " = "
                + StrUtils.toString(this.value) + "("
                + StrUtils.getNonNull(this.sourceFile, this.sourceURI) + ")";
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AttributeStatement)) {
            return false;
        }
        AttributeStatement as = (AttributeStatement) object;
        if (value == null) {
            return (identifier == as.identifier || (identifier != null && identifier
                    .equals(as.identifier)));
        } else {
            boolean sameIdentifier = (identifier == as.identifier || (identifier != null && identifier
                    .equals(as.identifier)));
            boolean sameValue = (value == as.value || (value != null && value
                    .equals(as.value)));
            if (sameValue == false) {
                // Strip out extra whitespaces and newline characters
                String strippedValue1 = Utility
                        .stripOnlyWhitespaceAndNewLine(value.toString());
                String strippedValue2 = Utility
                        .stripOnlyWhitespaceAndNewLine(as.value.toString());
                sameValue = (strippedValue1.equals(strippedValue2));
            }
            return (sameIdentifier && sameValue);
        }
    }

    public int hashcode() {
        int hash = 7;

        hash = 31 * hash + (null == identifier ? 0 : identifier.hashCode());
        if (value != null)
            hash = 31
                    * hash
                    + (null == value.toString() ? 0 : value.toString()
                            .hashCode());

        return hash;
    }

}
