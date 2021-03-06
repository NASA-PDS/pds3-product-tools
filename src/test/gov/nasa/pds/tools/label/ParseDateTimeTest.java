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

package gov.nasa.pds.tools.label;

import gov.nasa.pds.tools.BaseTestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ParseDateTimeTest extends BaseTestCase {

  private static final TimeZone UTC = TimeZone.getTimeZone("GMT");

  /**
   * Tests dates from several years, including both leap years and non-leap
   * years.
   */
  public void testParse() {
    parseForYear(1970, false);
    parseForYear(1972, true);
    parseForYear(2000, true);
    parseForYear(2009, false);
    parseForYear(2010, false);
    parseForYear(2100, false);
  }

  /**
   * Tests several representative days within a given year.
   * 
   * @param year
   *          the year to test
   * @param isLeapYear
   *          true, if the year is a leap year
   */
  private void parseForYear(int year, boolean isLeapYear) {
    parseForDay(year, 1);
    parseForDay(year, 59);
    parseForDay(year, 60);
    parseForDay(year, 61);
    parseForDay(year, 67);
    parseForDay(year, 68);
    parseForDay(year, 365);
    if (isLeapYear) {
      parseForDay(year, 366);
    }
  }

  private static final SimpleDateFormat YEAR_DAY_FMT = new SimpleDateFormat(
      "yyyy-DDD");
  private static final SimpleDateFormat YEAR_MONTH_DAY_FMT = new SimpleDateFormat(
      "yyyy-MM-dd");

  static {
    YEAR_DAY_FMT.setTimeZone(UTC);
    YEAR_MONTH_DAY_FMT.setTimeZone(UTC);
  }

  /**
   * Tests the parsing for a single day. Several prefixes of the given day's
   * date are tried, together with various granularities of representative
   * times.
   * 
   * @param year
   *          the year
   * @param day
   *          the day within the year
   */
  private void parseForDay(int year, int day) {
    parseForDayOnly(year, day);
    parseForDayTime(year, day, "00", 0, 0, 0, 0);
    parseForDayTime(year, day, "12", 12, 0, 0, 0);
    parseForDayTime(year, day, "23", 23, 0, 0, 0);
    parseForDayTime(year, day, "00:00", 0, 0, 0, 0);
    parseForDayTime(year, day, "12:13", 12, 13, 0, 0);
    parseForDayTime(year, day, "23:59", 23, 59, 0, 0);
    parseForDayTime(year, day, "00:00:00", 0, 0, 0, 0);
    parseForDayTime(year, day, "12:13:14", 12, 13, 14, 0);
    parseForDayTime(year, day, "23:59:59", 23, 59, 59, 0);
    parseForDayTime(year, day, "12:13:14.3", 12, 13, 14, 300);
    parseForDayTime(year, day, "23:59:59.32", 23, 59, 59, 320);
    parseForDayTime(year, day, "23:59:59.321", 23, 59, 59, 321);
  }

  private void parseForDayOnly(int year, int day) {
    Calendar calendar = new GregorianCalendar(UTC);
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.DAY_OF_YEAR, day);

    testParseForDayOnly(calendar.getTime(), new SimpleDateFormat("yyyy"));
    testParseForDayOnly(calendar.getTime(), new SimpleDateFormat("yyyy-DDD"));
    testParseForDayOnly(calendar.getTime(), new SimpleDateFormat("yyyy-MM"));
    testParseForDayOnly(calendar.getTime(), new SimpleDateFormat("yyyy-MM-dd"));
  }

  private void testParseForDayOnly(Date date, SimpleDateFormat fmt) {
    fmt.setTimeZone(UTC);
    String expected = fmt.format(date);
    Date parsedDate = ParseDateTime.parse(expected);
    String actual = fmt.format(parsedDate);
    assertEquals(expected, actual);
  }

  private void parseForDayTime(int year, int day, String time, int hours,
      int minutes, int seconds, int millis) {
    Calendar calendar = new GregorianCalendar(UTC);
    calendar.clear();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.DAY_OF_YEAR, day);
    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, seconds);
    calendar.set(Calendar.MILLISECOND, millis);

    parseForDateWithFmt(calendar.getTime(), time, YEAR_DAY_FMT);
    parseForDateWithFmt(calendar.getTime(), time, YEAR_MONTH_DAY_FMT);
  }

  private void parseForDateWithFmt(Date date, String time, SimpleDateFormat fmt) {
    fmt.setTimeZone(UTC);
    String expected = ISO8601_FMT.format(date);

    String parseString = fmt.format(date) + "T" + time;
    Date parsedDate = ParseDateTime.parse(parseString);

    String actual = ISO8601_FMT.format(parsedDate);
    assertEquals(expected, actual);
  }

  private static final SimpleDateFormat ISO8601_FMT = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss");
  static {
    ISO8601_FMT.setTimeZone(UTC);
  }

  /**
   * Tests a few specific dates that have had problems in the past.
   */
  public void testSpecificDates() {
    // These two are from Debra Kasden at PPI, from CO-S-INMS-3-L1A-U-V1.0,
    // the Cassini INMS data.
    parseSpecificDates("2009-067T02:00:15", "2009-03-08T02:00:15");
    parseSpecificDates("2009-067T02:59:53", "2009-03-08T02:59:53");
  }

  private void parseSpecificDates(String s, String expected) {
    Date date = ParseDateTime.parse(s);
    assertEquals(expected, ISO8601_FMT.format(date));
  }

  public void testParseBadDate() {
    for (String s : BAD_DATE_TESTS) {
      parseBadDate(s);
    }
  }

  private void parseBadDate(String s) {
    try {
      ParseDateTime.parse(s);
      fail("Date <" + s + "> should have thrown exception while parsing.");
    } catch (IllegalArgumentException ex) {
      // ignore
    }
  }

  private static String[] BAD_DATE_TESTS = {
      // Too few or too many parts.
      "", "T", "-T:", "1970-001T", "1970-01-01T",
      "T12",
      "T12:13",
      "T12:13:14",
      "1970-001T12:13:14T12",
      "1970-01-01T12:13:14T12",

      // Values out of range.
      "1970-13", "1970-366", "1970-01-32", "1970-13-01",
      "1970-01-01T24",
      "1970-01-01T23:60",
      "1970-01-01T23:59:60",
      "2000-12-22T23:59:59.9999",

      // Components too long or short.
      "197", "197-001", "197-01", "197-01-01", "19701", "19701-001",
      "19701-01", "19701-01-01", "1970-1", "1970-0001", "1970-1-01",
      "1970-001-01", "1970-01-01T0:00:00", "1970-01-01T000:00:00",
      "1970-01-01T00:0:00", "1970-01-01T00:000:00", "1970-01-01T00:00:0",
      "1970-01-01T00:00:000", "2000-12-22T23:59:59.1234567890" };

}