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

package gov.nasa.pds.tools.label.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import gov.nasa.arc.pds.tools.util.FileUtils;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.constants.Constants.ProblemType;
import gov.nasa.pds.tools.label.CommentStatement;
import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.MalformedSFDULabel;
import gov.nasa.pds.tools.label.ManualPathResolver;
import gov.nasa.pds.tools.label.PointerResolver;
import gov.nasa.pds.tools.label.SFDULabel;
import gov.nasa.pds.tools.label.antlr.ODLLexer;
import gov.nasa.pds.tools.label.antlr.ODLParser;
import gov.nasa.pds.tools.label.validate.Validator;
import gov.nasa.pds.tools.util.MessageUtils;
import gov.nasa.pds.tools.util.VersionInfo;

/**
 * Default implementation
 * 
 * @author pramirez
 * @author jagander
 * @version $Revision$
 * 
 */

public class DefaultLabelParser implements LabelParser {

  private final boolean loadIncludes;

  private final boolean captureProblems;

  private final boolean allowExternalProblems;

  private final PointerResolver resolver;

  private final int MARK_LIMIT = 100;

  // default constructor, assumes you want to load included statements and
  // capture parse errors
  public DefaultLabelParser(final PointerResolver resolver) {
    this(true, true, resolver);
  }

  public DefaultLabelParser(final boolean loadIncludes, final boolean captureProblems,
      final PointerResolver resolver) {
    this(loadIncludes, captureProblems, false, resolver);
  }

  public DefaultLabelParser(final boolean loadIncludes, final boolean captureProblems,
      final boolean allowExternalProblems, final PointerResolver resolver) {
    this.loadIncludes = loadIncludes;
    this.captureProblems = captureProblems;
    this.allowExternalProblems = allowExternalProblems;
    this.resolver = resolver;

    // make sure resolver exists if loading includes
    if (loadIncludes && resolver == null) {
      // statement not externalized since only for internal development
      // use
      throw new RuntimeException("A PointerResolver is required to load include statements"); //$NON-NLS-1$
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.pds.tools.label.parser.LabelParser#parse(java.net.URL)
   */

  public Label parseLabel(final URL url) throws LabelParserException, IOException {
    return parseLabel(url, false);
  }

  public Label parseLabel(final File file) throws LabelParserException, IOException {
    return parseLabel(file, false);
  }

  public Label parseLabel(final URL url, final boolean forceParse)
      throws LabelParserException, IOException {
    final BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
    try {
      inputStream.mark(MARK_LIMIT);
      URI labelURI = null;
      try {
        labelURI = url.toURI();
      } catch (URISyntaxException e) {
        throw new LabelParserException("bad url", ProblemType.INVALID_LABEL, //$NON-NLS-1$
            url.getFile());
      }

      final Label label = new Label(labelURI);
      label.setCaptureProblems(this.captureProblems);
      label.setAllowExternalProblems(this.allowExternalProblems);
      return parseLabel(inputStream, label, forceParse);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  public Label parseLabel(final File file, final boolean forceParse)
      throws LabelParserException, IOException {
    BufferedInputStream inputStream = null;
    try {
      try {
        inputStream = new BufferedInputStream(new FileInputStream(file));
        inputStream.mark(MARK_LIMIT);
      } catch (FileNotFoundException e) {
        if (file.exists()) {
          throw new LabelParserException(file, null, null, "parser.error.unableToRead", //$NON-NLS-1$
              ProblemType.INVALID_LABEL, file.toString());
        }
        throw new LabelParserException(file, null, null, "parser.error.missingFile", //$NON-NLS-1$
            ProblemType.INVALID_LABEL, file.getName());
      }
      final Label label = new Label(file);
      label.setCaptureProblems(this.captureProblems);
      label.setAllowExternalProblems(this.allowExternalProblems);
      return parseLabel(inputStream, label, forceParse);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private Label parseLabel(final BufferedInputStream inputStream, final Label label,
      final boolean forceParse) throws LabelParserException, IOException {

    consumeSFDUHeader(inputStream);

    // Now look for PDS_VERSION_ID to ensure that this is a file we want to
    // validate

    // Set the buffer size to the mark limit to ensure that we don't
    // invalidate the mark and throw an exception when calling the
    // inputStream.reset() method
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), MARK_LIMIT);

    String versionLine = null;
    boolean hasExtraNewLines = false;
    do {
      versionLine = reader.readLine();
      if (versionLine != null && versionLine.trim().length() == 0) {
        hasExtraNewLines = true;

      }
    } while (versionLine != null && versionLine.trim().length() == 0);

    if (hasExtraNewLines) {
      label.addProblem(new CommentStatement(label, 1), "parser.error.mislocatedVersion", //$NON-NLS-1$
          ProblemType.PARSE_ERROR);
    }

    String[] line = new String[] {""}; //$NON-NLS-1$
    if (versionLine != null) {
      line = versionLine.trim().split("="); //$NON-NLS-1$
    }

    if (line.length != 2) {
      label.setInvalid();
      final LabelParserException lpe = new LabelParserException(label, null, null,
          "parser.error.missingVersion", ProblemType.INVALID_LABEL, //$NON-NLS-1$
          label.getSourceNameString());
      if (!forceParse) {
        throw lpe;
      }
      label.addProblem(lpe);
    } else {
      String name = line[0].trim();
      // String value = line[1].trim();

      if (!"PDS_VERSION_ID".equals(name)) { //$NON-NLS-1$
        label.setInvalid();
        final LabelParserException lpe = new LabelParserException(label, null, null,
            "parser.error.missingVersion", ProblemType.INVALID_LABEL, //$NON-NLS-1$
            label.getSourceNameString());
        if (!forceParse) {
          throw lpe;
        }
        label.addProblem(lpe);
      }
    }

    inputStream.reset();

    parseLabel(inputStream, label);

    // this is a label so it should end in END
    if (!label.hasEndStatement()) {
      label.addProblem(new CommentStatement(label, 1), "parser.error.missingEndStatement", //$NON-NLS-1$
          ProblemType.PARSE_ERROR);
    }
    return label;
  }

  private Label parseLabel(final BufferedInputStream inputStream, final Label label)
      throws LabelParserException, IOException {
    CustomAntlrInputStream customIs = null;
    try {
      customIs = new CustomAntlrInputStream(inputStream);
      CharStream antlrInput = new ANTLRInputStream(customIs);

      ODLLexer lexer = new ODLLexer(antlrInput);
      lexer.setLabel(label);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      ODLParser parser = new ODLParser(tokens);

      parser.setLoadIncludes(this.loadIncludes);

      if (this.loadIncludes) {
        parser.setPointerResolver(this.resolver);
      }

      try {
        parser.label(label);
      } catch (RecognitionException ex) {
        label.setInvalid();
        throw new LabelParserException(ex, ex.line, ex.charPositionInLine,
            ProblemType.INVALID_LABEL);
      }
      label.setAttachedStartByte(customIs.getAttachedContentStartByte());
      label.setHasBlankFill(customIs.hasBlankFill());

      return label;
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(customIs);
    }
  }

  private List<SFDULabel> consumeSFDUHeader(InputStream input) throws IOException {
    List<SFDULabel> sfdus = new ArrayList<SFDULabel>();
    boolean foundHeader = false;

    byte[] sfduLabel = new byte[20];
    int count = input.read(sfduLabel);
    if (count == 20) {
      try {
        SFDULabel sfdu = new SFDULabel(sfduLabel);
        if ("CCSD".equals(sfdu.getControlAuthorityId())) { //$NON-NLS-1$
          foundHeader = true;
          sfdus.add(sfdu);
          // Read in second SFDU label
          input.read(sfduLabel);
          sfdus.add(new SFDULabel(sfduLabel));
          consumePDSNewline(input);
        }
      } catch (MalformedSFDULabel e) {
        // For now we can ignore this error as there is likely not a
        // header.
      }

    }

    if (!foundHeader) {
      input.reset();
    }

    return sfdus;
  }

  // consume up to 2 newline type characters to conform to the PDS newline
  // NOTE: will consume CR || LF || CRLF || LFCR... etc
  private void consumePDSNewline(final InputStream input) throws IOException {
    // consume CR if present
    consumeNewline(input);
    // consume LF if present
    consumeNewline(input);
    // marking location so not rewound to just prior to last newline char
    input.mark(MARK_LIMIT);
  }

  private void consumeNewline(final InputStream input) throws IOException {
    byte[] newline = new byte[1];
    input.mark(1);

    int count = input.read(newline);
    if (count == 1) {
      String nl1 = new String(newline);
      if (!(nl1.equals("\n") || nl1.equals("\r"))) { //$NON-NLS-1$//$NON-NLS-2$
        // did not find newline char, reset
        input.reset();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.pds.tools.label.parser.LabelParser#getODLVersion()
   */
  public String getODLVersion() {
    return VersionInfo.getODLVersion();
  }

  public Label parsePartial(final File file, final Label parent)
      throws IOException, LabelParserException {
    return parsePartial(file, parent, this.captureProblems);
  }

  public Label parsePartial(final File file, final Label parent, final boolean captureProbs)
      throws IOException, LabelParserException {
    return parsePartial(file, parent, captureProbs, this.allowExternalProblems);
  }

  public Label parsePartial(final File file, final Label parent, final boolean captureProbs,
      final boolean allowExternalProbs) throws IOException, LabelParserException {
    BufferedInputStream inputStream = null;
    try {
      try {
        inputStream = new BufferedInputStream(new FileInputStream(file));
        inputStream.mark(MARK_LIMIT);
      } catch (FileNotFoundException e) {
        if (file.exists()) {
          throw new LabelParserException(file, null, null, "parser.error.unableToRead", //$NON-NLS-1$
              ProblemType.INVALID_LABEL, file.toString());
        }
        throw new LabelParserException(file, null, null, "parser.error.missingFile", //$NON-NLS-1$
            ProblemType.INVALID_LABEL, file.getName());
      }
      final Label label = new Label(file);
      label.setCaptureProblems(captureProbs);
      label.setAllowExternalProblems(allowExternalProbs);
      return parsePartial(inputStream, label, parent);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  public Label parsePartial(final URL url, final Label parent)
      throws IOException, LabelParserException {
    return parsePartial(url, parent, this.captureProblems);
  }

  public Label parsePartial(final URL url, final Label parent, final boolean captureProbs)
      throws IOException, LabelParserException {
    return parsePartial(url, parent, captureProbs, this.allowExternalProblems);
  }

  public Label parsePartial(final URL url, final Label parent, final boolean captureProbs,
      final boolean allowExternalProbs) throws IOException, LabelParserException {
    final BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
    try {
      inputStream.mark(MARK_LIMIT);
      URI labelURI = null;
      try {
        labelURI = url.toURI();
      } catch (URISyntaxException e) {
        throw new LabelParserException("bad url", ProblemType.INVALID_LABEL, //$NON-NLS-1$
            url.getFile());
      }

      final Label label = new Label(labelURI);
      label.setCaptureProblems(captureProbs);
      label.setAllowExternalProblems(allowExternalProbs);
      return parsePartial(inputStream, label, parent);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.pds.tools.label.parser.LabelParser#parsePartial(java.net.URL, boolean)
   */
  public Label parsePartial(final BufferedInputStream inputStream, final Label label,
      final Label parent) throws IOException, LabelParserException {

    // add ancestors to label to be able to test for circular
    // references
    if (parent != null) {
      label.addAncestors(parent.getAncestors());
    }
    label.addAncestor(label.getLabelPath());

    List<SFDULabel> sfdus = consumeSFDUHeader(inputStream);
    int numConsumed = sfdus.size();

    // Now look for PDS_VERSION_ID to ensure that this is a file we want to
    // validate

    // Set the buffer size to the mark limit to ensure that we don't
    // invalidate the mark and throw an exception when calling the
    // inputStream.reset() method
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), MARK_LIMIT);
    String versionLine = null;

    do {
      versionLine = reader.readLine();
    } while (versionLine != null && versionLine.trim().length() == 0);

    String[] line = new String[] {""}; //$NON-NLS-1$
    if (versionLine != null) {
      line = versionLine.trim().split("="); //$NON-NLS-1$
    }

    if (line.length == 2) {
      String name = line[0].trim();

      // Label fragments should not have PDS_VERSION_ID
      if ("PDS_VERSION_ID".equals(name)) { //$NON-NLS-1$
        label
            .addProblem(new LabelParserException(label, null, null, "parser.warning.versionPresent", //$NON-NLS-1$
                ProblemType.FRAGMENT_HAS_VERSION, getDisplayPath(label)));
      }
    }

    inputStream.reset();

    if (numConsumed != 0) {
      // TODO: when pds utils library updated, use getRelativePath(String,
      // String)
      label.addProblem(new LabelParserException(label, null, null, "parser.warning.sfduPresent", //$NON-NLS-1$
          ProblemType.FRAGMENT_HAS_SFDU, getDisplayPath(label)));
    }

    return parseLabel(inputStream, label);
  }

  // This function returns null if there if no relative path can be found.
  private String getRelativePath(final Label label) {
    String relativePath = null;
    if (label.getLabelFile() != null) {
      try {
        relativePath = FileUtils.getRelativePath(this.resolver.getBaseFile(), label.getLabelFile());
      } catch (RuntimeException re) {
        // If the files don't share a path
        // no op
      }
    } else {
      if (label.getLabelURI().toString().startsWith(this.resolver.getBaseURI().toString())) {
        relativePath = label.getLabelURI().toString()
            .substring(this.resolver.getBaseURI().toString().length());
      }
    }
    return relativePath;
  }

  private String getDisplayPath(final Label label) {
    String displayPath = getRelativePath(label);

    if (displayPath == null) {
      if (label.getLabelFile() != null) {
        displayPath = label.getLabelFile().getPath();
      } else {
        displayPath = label.getLabelURI().getPath();
      }
    }
    return displayPath;
  }

  public static void main(String[] args) throws Exception {
    ConfigurationBuilder<BuiltConfiguration> builder =
        ConfigurationBuilderFactory.newConfigurationBuilder();
    builder.setStatusLevel(Level.ERROR);

    AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
        .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
    appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%-5p %m%n"));
    LoggerContext ctx = Configurator.initialize(builder.build());
    ctx.updateLoggers();

    ManualPathResolver resolver = new ManualPathResolver();
    URL labelURL = new URL(args[0]);
    resolver.setBaseURI(ManualPathResolver.getBaseURI(labelURL.toURI()));
    DefaultLabelParser parser = new DefaultLabelParser(true, true, true, resolver);
    Label label = parser.parseLabel(labelURL, true);
    Validator validator = new Validator();
    validator.validate(label);
    System.out.println("Found " + label.getProblems().size() //$NON-NLS-1$
        + " problem(s):"); //$NON-NLS-1$
    for (LabelParserException problem : label.getProblems()) {
      if (problem.getLineNumber() != null) {
        System.out.print("Line " + problem.getLineNumber()); //$NON-NLS-1$
        if (problem.getColumn() != null) {
          System.out.print(", " + problem.getColumn()); //$NON-NLS-1$
        }
        System.out.print(": "); //$NON-NLS-1$
      }
      System.out.println(MessageUtils.getProblemMessage(problem));
    }
  }
}
