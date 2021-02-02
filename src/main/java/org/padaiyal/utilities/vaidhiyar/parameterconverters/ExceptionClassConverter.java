package org.padaiyal.utilities.vaidhiyar.parameterconverters;

import java.util.Locale;
import java.util.Objects;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.padaiyal.utilities.I18nUtility;

/**
 * Used to convert a given string into an exception class.
 */
public class ExceptionClassConverter implements ArgumentConverter {

  static {
    I18nUtility.addResourceBundle(
        ExceptionClassConverter.class,
        ExceptionClassConverter.class.getSimpleName(),
        Locale.US
    );
  }

  /**
   * Converts a given exception class string to the class object.
   *
   * @param expectedExceptionClassString String representation of the exception class.
   * @return                             The exception class object corresponding to the input
   *                                     string.
   * @throws ArgumentConversionException When an error occurs during conversion.
   */
  public static Class<? extends Exception> convertExceptionNameToClass(
      String expectedExceptionClassString
  ) throws ArgumentConversionException {
    // Input validation
    Objects.requireNonNull(expectedExceptionClassString);

    return switch (expectedExceptionClassString) {
      case "NullPointerException.class" -> NullPointerException.class;
      case "IllegalArgumentException.class" -> IllegalArgumentException.class;
      default -> throw new ArgumentConversionException(
          I18nUtility.getFormattedString(
              "ExceptionClassConverter.error.unableToParseExceptionClass",
              expectedExceptionClassString
          )
      );
    };
  }

  /**
   * Converts a given exception class string to the class object.
   *
   * @param expectedExceptionClassString String representation of the exception class.
   * @param context                      The parameter context where the converted object will be
   *                                     used.
   * @return                             The exception class object corresponding to the input
   *                                     string.
   * @throws ArgumentConversionException When an error occurs during conversion.
  */
  @Override
  public Object convert(Object expectedExceptionClassString, ParameterContext context)
      throws ArgumentConversionException {
    return convertExceptionNameToClass(expectedExceptionClassString.toString());
  }
}
