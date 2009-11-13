// Copyright 2006-2010, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id$
//

package gov.nasa.pds.tools.dict.type;

import gov.nasa.pds.tools.BaseTestCase;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.constants.Constants.ProblemType;
import gov.nasa.pds.tools.label.Label;

import java.io.File;
import java.io.IOException;

/**
 * @author pramirez
 * @author jagander
 * @version $Revision$
 * 
 */
// NOTE: character check is performed by StrUtils.isASCII() which is tested
// elsewhere
@SuppressWarnings("nls")
public class CharacterCheckerTest extends BaseTestCase {

    public void testTypeMismatch() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "character.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.TYPE_MISMATCH);
        assertProblemEquals(lpe, 5, null, "parser.error.typeMismatch",
                ProblemType.TYPE_MISMATCH, "RELEASE_ID", "CHARACTER",
                "Numeric", "7");
    }

    public void testNonACII() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "character.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.INVALID_TYPE);
        assertProblemEquals(lpe, 6, null, "parser.error.badCharacters",
                ProblemType.INVALID_TYPE, "�");
    }

    public void testLong() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "character.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.EXCESSIVE_VALUE_LENGTH);
        assertProblemEquals(lpe, 3, null, "parser.error.tooLong",
                ProblemType.EXCESSIVE_VALUE_LENGTH, " XXXXX ", "5", "4",
                "RELEASE_ID", "CHARACTER");
    }

    public void testShort() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "character.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        LabelParserException lpe = assertHasProblem(label,
                ProblemType.SHORT_VALUE);
        assertProblemEquals(lpe, 4, null, "parser.error.tooShort",
                ProblemType.SHORT_VALUE, "XXX  ", "3", "4", "RELEASE_ID",
                "CHARACTER");
    }

    // For convenience, all test parts in one file but tested in different
    // methods. This test just confirms we covered all tests
    public void testNumErrors() throws LabelParserException, IOException {
        final File testFile = new File(LABEL_DIR, "character.lbl");

        final Label label = PARSER.parseLabel(testFile);
        validate(label);

        assertEquals(4, label.getProblems().size());
    }

}