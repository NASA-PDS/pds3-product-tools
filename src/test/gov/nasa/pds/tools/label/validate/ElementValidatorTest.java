// Copyright 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other
// materials provided with the distribution.
// * Neither the name of Caltech nor its operating division, the Jet Propulsion
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

package gov.nasa.pds.tools.label.validate;

import gov.nasa.pds.tools.BaseTestCase;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.constants.Constants;
import gov.nasa.pds.tools.constants.Constants.ProblemType;
import gov.nasa.pds.tools.label.AttributeStatement;
import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.Sequence;
import gov.nasa.pds.tools.label.Set;

import java.io.File;
import java.io.IOException;

// tests may duplicate more granular checks. should be left here to verify that implemented in this validator correctly
@SuppressWarnings("nls")
public class ElementValidatorTest extends BaseTestCase {

    public void testSet() throws LabelParserException, IOException {

        final File testFile = new File(LABEL_DIR, "setValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        AttributeStatement eccentricity = label.getAttribute("FILTER_NAME");
        assertTrue(eccentricity.getValue() instanceof Set);
        LabelParserException lpe = assertHasProblem(label,
                ProblemType.UNKNOWN_VALUE);
        assertProblemEquals(lpe, 4, null, "parser.warning.unknownValue",
                ProblemType.UNKNOWN_VALUE, "HAZEL", "FILTER_NAME", "HAZEL");

        // TODO: should only be 2 errors but NoViableAltException seems to be
        // tripled due to recovery issues
        assertEquals(4, label.getProblems().size());
    }

    public void testUnknownValue() throws LabelParserException, IOException {
        // minimize contents
        final File testFile = new File(LABEL_DIR, "newValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.UNKNOWN_VALUE, 3);
        assertProblemEquals(lpe, 3, null, "parser.warning.unknownValue",
                ProblemType.UNKNOWN_VALUE,
                "SATURN SATELLITE ASTROMETRY DATA FROM WFP2 2005",
                "VOLUME_SET_NAME",
                "SATURN SATELLITE ASTROMETRY DATA FROM WFP2 2005");
        assertEquals(1, label.getProblems().size());
    }

    public void testUnknownValueWInclude() throws LabelParserException,
            IOException {
        // minimize contents
        final File testFile = new File(LABEL_DIR, "newValueWInclude.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.UNKNOWN_VALUE, 3);
        assertProblemEquals(lpe, 3, null, "parser.warning.unknownValue",
                ProblemType.UNKNOWN_VALUE,
                "SATURN SATELLITE ASTROMETRY DATA FROM WFP2 2005",
                "VOLUME_SET_NAME",
                "SATURN SATELLITE ASTROMETRY DATA FROM WFP2 2005");
        assertEquals(1, label.getProblems().size());
    }

    public void testInvalidValue() throws LabelParserException, IOException {

        // TODO: minimize label contents and rename
        final File testFile = new File(LABEL_DIR, "invalidValue.lbl");

        Label label = PARSER.parseLabel(testFile);
        validate(label);

        // [INVALID_VALUE - "parser.error.invalidValue" on line 5] (ASCII
        // INTEGER, DATA_TYPE, ASCII INTEGER)
        // TODO: do more full test of results
        LabelParserException lpe = assertHasProblem(label,
                ProblemType.INVALID_VALUE, 5);
        assertProblemEquals(lpe, 5, null, "parser.error.invalidValue",
                ProblemType.INVALID_VALUE, "ASCII INTEGER", "DATA_TYPE",
                "ASCII INTEGER");
        assertEquals(1, label.getProblems().size());
    }

    // TODO: create more situations to test that value is empty when it should
    // be
    public void testMissingValue() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "missingValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        assertHasProblem(label, ProblemType.MISSING_VALUE);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.MISSING_VALUE, 2);
        assertProblemEquals(lpe, 2, null, "parser.error.missingValue",
                ProblemType.MISSING_VALUE, "FIRST_LINE");

        lpe = assertHasProblem(label, ProblemType.MISSING_VALUE, 4);
        assertProblemEquals(lpe, 4, null, "parser.error.missingValue",
                ProblemType.MISSING_VALUE, "GENERAL_CATALOG_FLAG");

        lpe = assertHasProblem(label, ProblemType.MISSING_VALUE, 7);
        assertProblemEquals(lpe, 7, null, "parser.error.missingValue",
                ProblemType.MISSING_VALUE, "DESCRIPTION");
        assertEquals(3, label.getProblems().size());
    }

    // tests that value is of type Set and that individual elements are
    // validated separately
    public void testSequence() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "sequenceValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        AttributeStatement eccentricity = label
                .getAttribute("AVERAGE_ECCENTRICITY");
        assertTrue(eccentricity.getValue() instanceof Sequence);
        LabelParserException lpe = assertHasProblem(label,
                ProblemType.TYPE_MISMATCH);
        assertProblemEquals(lpe, 4, null, "parser.error.typeMismatch",
                ProblemType.TYPE_MISMATCH, "AVERAGE_ECCENTRICITY", "REAL",
                "Symbol", "aaaa");

        AttributeStatement lookup = label
                .getAttribute("MRO:LOOKUP_CONVERSION_TABLE");
        assertTrue(lookup.getValue() instanceof Sequence);
        lpe = assertHasProblem(label, ProblemType.INVALID_TYPE);
        assertProblemEquals(lpe, 5, null, "parser.error.badInteger",
                ProblemType.INVALID_TYPE,
                "99999999999999999999999999999999999999999999999999999999999");
        assertEquals(2, label.getProblems().size());
    }

    public void testSkipValue() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "skipValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.PLACEHOLDER_VALUE);
        assertProblemEquals(lpe, 6, null, "parser.info.placeholderValue",
                ProblemType.PLACEHOLDER_VALUE, "DATA_SET_RELEASE_DATE",
                Constants.NULL_VAL_CONST);

        assertEquals(1, label.getProblems().size());
    }

    // TODO: validation incomplete, see standards ref for full coverage (need
    // support for all SI derived stuff and verification that type is consistent
    // with default unit)
    // NOTE: currently just checking to see if value exists in short or long
    // form.
    public void testUnitAllowed() throws LabelParserException, IOException {

        final File testFile = new File(LABEL_DIR, "unknownUnits.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.UNKNOWN_VALUE_TYPE, 7);
        assertProblemEquals(lpe, 7, null, "parser.warning.unknownUnits",
                ProblemType.UNKNOWN_VALUE_TYPE, "EXPOSURE_DURATION", "seconds");

        lpe = assertHasProblem(label, ProblemType.UNKNOWN_VALUE_TYPE, 10);
        assertProblemEquals(lpe, 10, null, "parser.warning.unknownUnits",
                ProblemType.UNKNOWN_VALUE_TYPE, "MASS_DENSITY", "g/m**3");

        assertEquals(2, label.getProblems().size());

    }

    public void testUnknownDefinition() throws LabelParserException,
            IOException {
        final File testFile = new File(LABEL_DIR, "unknownDefinition.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.UNKNOWN_KEY);
        assertProblemEquals(lpe, 4, null, "parser.error.definitionNotFound",
                ProblemType.UNKNOWN_KEY, "FAKE_DEF");

        assertEquals(1, label.getProblems().size());
    }

    public void testMissingKey() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "missingKey.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.MISSING_ID);
        assertProblemEquals(lpe, 4, null, "parser.error.missingId",
                ProblemType.MISSING_ID, "");

        assertEquals(1, label.getProblems().size());
    }

    // TODO: when unit conversion works, add here
    public void testNumericExceedsMax() throws LabelParserException,
            IOException {
        final File testFile = new File(LABEL_DIR, "numberExceedsMax.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label, ProblemType.OOR);
        assertProblemEquals(lpe, 4, null, "parser.error.exceedsMax",
                ProblemType.OOR, "91.0", "90.0", "DECLINATION", "REAL");

        assertEquals(1, label.getProblems().size());
    }

    // TODO: when unit conversion works, add here
    public void testNumericLessThanMin() throws LabelParserException,
            IOException {
        final File testFile = new File(LABEL_DIR, "numberLessThanMin.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label, ProblemType.OOR);
        assertProblemEquals(lpe, 4, null, "parser.error.lessThanMin",
                ProblemType.OOR, "-91.0", "-90.0", "DECLINATION", "REAL");
        assertEquals(1, label.getProblems().size());

    }

    public void testShortValue() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "shortValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.SHORT_VALUE);
        assertProblemEquals(lpe, 4, null, "parser.error.tooShort",
                ProblemType.SHORT_VALUE, "1", "1", "3", "MAXIMUM_RESOLUTION",
                "REAL");
        assertEquals(1, label.getProblems().size());
    }

    public void testLongValue() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "longValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.EXCESSIVE_VALUE_LENGTH);
        assertProblemEquals(lpe, 4, null, "parser.error.tooLong",
                ProblemType.EXCESSIVE_VALUE_LENGTH,
                "I am more than 30 chars long, just so you know ", "47", "30",
                "DELIMITING_PARAMETER_NAME", "CHARACTER");
        assertEquals(1, label.getProblems().size());
    }

    public void testCastError() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "badCastValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.INVALID_TYPE);
        assertProblemEquals(lpe, 4, null, "parser.error.badInteger",
                ProblemType.INVALID_TYPE, "43.3");
        assertEquals(1, label.getProblems().size());
    }

    public void testManipulatedValue() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "manipulatedValue.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.MANIPULATED_VALUE);
        assertProblemEquals(lpe, 4, null, "parser.warning.manipulatedValue",
                ProblemType.MANIPULATED_VALUE, "yes", "YES",
                "MISSING_PACKET_FLAG");
        assertEquals(1, label.getProblems().size());
    }

    public void testTypeMismatch() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "typeMismatch.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.TYPE_MISMATCH);
        assertProblemEquals(lpe, 4, null, "parser.error.typeMismatch",
                ProblemType.TYPE_MISMATCH, "DELIMITING_PARAMETER_NAME",
                "CHARACTER", "Numeric", "43");
        assertEquals(1, label.getProblems().size());
    }

    public void testNoProbsPastEnd() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "probPastEnd.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        assertEquals(0, label.getProblems().size());
    }

    public void testMissingEquals() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "missingEquals.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe1 = assertHasProblem(label,
                ProblemType.PARSE_ERROR, 2);
        assertProblemEquals(lpe1, 2, 16, "parser.error.noViableAlternative",
                ProblemType.PARSE_ERROR, "\"MARS\"");

        LabelParserException lpe2 = assertHasProblem(label,
                ProblemType.PARSE_ERROR, 4);
        assertProblemEquals(lpe2, 4, 17, "parser.error.noViableAlternative",
                ProblemType.PARSE_ERROR, "MER");

        LabelParserException lpe3 = assertHasProblem(label,
                ProblemType.PARSE_ERROR, 6);
        assertProblemEquals(lpe3, 6, 31, "parser.error.noViableAlternative",
                ProblemType.PARSE_ERROR, "\"LEAPSECS.KER\"");

        assertEquals(3, label.getProblems().size());
    }

}
