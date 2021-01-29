package org.padaiyal.utilities.vaidhiyar.abstractions;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Tests CpuLoadGenerator.
 */
public class CpuLoadGeneratorTest {

  /**
   * Test CPU load generator with invalid inputs.
   *
   * @param load CPU load to generate.
   * @param durationInMilliSeconds Duration for which the CPU load has to be generated.
   * @throws IOException If there is an issue reading the properties file.
   */
  @ParameterizedTest
  @CsvSource({
      // Invalid load value.
      "-1.0, 100",
      "1.2, 100",
      //Invalid samplingDuration
      "0.2, -10"

  })
  void testInvalidInputs(double load, long durationInMilliSeconds) throws IOException {
    CpuLoadGenerator cpuLoadGenerator = new CpuLoadGenerator();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> cpuLoadGenerator.start(load, durationInMilliSeconds)
    );
  }

  /**
   * Test if the CPU load generator terminates in a reasonable time frame.
   *
   * @param durationToWaitForInMinutes  Duration to wait for the CPU load generator to complete.
   * @throws IOException                If there is an issue reading the properties file.
   */
  @ParameterizedTest
  @CsvSource({
      "1"
  })
  void testCpuLoadGeneratorWaitTillTermination(long durationToWaitForInMinutes) throws IOException {
    try (
        MockedStatic<CpuLoadThread> threadMock = Mockito.mockStatic(CpuLoadThread.class)
    ) {
      threadMock.when(
          () -> CpuLoadThread.sleepCurrentThread(ArgumentMatchers.anyLong())
      ).thenAnswer((Answer<Void>) invocation -> null);
      CpuLoadGenerator cpuLoadGenerator = new CpuLoadGenerator();
      Assertions.assertTimeoutPreemptively(
          Duration.ofMinutes(durationToWaitForInMinutes),
          cpuLoadGenerator::run
      );
    }
  }

}
