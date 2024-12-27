package ru.hh.school.unittesting.homework;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  @BeforeEach
  void initializeLibrary() {
    libraryManager.addBook("adventure101", 7);
    libraryManager.addBook("romance200", 3);
    libraryManager.addBook("sci-fi300", 0);
  }

  @Test
  void userCanBorrowBook() {
    when(userService.isUserActive("anna")).thenReturn(true);

    boolean borrowSuccess = libraryManager.borrowBook("adventure101", "anna");

    assertTrue(borrowSuccess);
    assertEquals(6, libraryManager.getAvailableCopies("adventure101"));
    verify(notificationService, times(1)).notifyUser("anna", "You have borrowed the book: adventure101");
  }

  @Test
  void inactiveUserCannotBorrowBook() {
    when(userService.isUserActive("john_doe")).thenReturn(false);

    boolean borrowResult = libraryManager.borrowBook("adventure101", "john_doe");

    assertFalse(borrowResult);
    verify(notificationService, times(1)).notifyUser("john_doe", "Your account is not active.");
    assertEquals(7, libraryManager.getAvailableCopies("adventure101"));
  }

  @Test
  void borrowBookWithZeroStockShouldFail() {
    when(userService.isUserActive("amy_123")).thenReturn(true);

    boolean result = libraryManager.borrowBook("sci-fi300", "amy_123");

    assertFalse(result);
    verify(notificationService, never()).notifyUser(eq("amy_123"), anyString());
  }

  @ParameterizedTest
  @CsvSource({
      "10, false, false, 5.00",
      "3, true, false, 2.25",
      "15, true, true, 9.00",
      "0, false, true, 0.00"
  })
  void lateFeeCalculationsTest(
      int overdueDays,
      boolean bestseller,
      boolean premiumUser,
      double expectedFee
  ) {
    double calculatedFee = libraryManager.calculateDynamicLateFee(overdueDays, bestseller, premiumUser);

    assertEquals(expectedFee, calculatedFee);
  }

  @Test
  void bookReturnByCorrectUser() {
    when(userService.isUserActive("jessica")).thenReturn(true);
    libraryManager.borrowBook("romance200", "jessica");

    boolean returnSuccess = libraryManager.returnBook("romance200", "jessica");

    assertTrue(returnSuccess);
    assertEquals(3, libraryManager.getAvailableCopies("romance200"));
    verify(notificationService, times(1)).notifyUser("jessica", "You have returned the book: romance200");
  }

  @Test
  void incorrectUserCannotReturnBook() {
    boolean result = libraryManager.returnBook("adventure101", "wrong_user");

    assertFalse(result);
    verify(notificationService, never()).notifyUser(eq("wrong_user"), anyString());
  }

  @Test
  void shouldThrowForNegativeDays() {
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-5, false, false)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @Test
  void testAddingNewBook() {
    libraryManager.addBook("horror500", 8);

    assertEquals(8, libraryManager.getAvailableCopies("horror500"));
  }

  @Test
  void addMoreCopiesToExistingBook() {
    libraryManager.addBook("adventure101", 3);

    assertEquals(10, libraryManager.getAvailableCopies("adventure101"));
  }

  @Test
  void lateFeeForPremiumAndBestseller() {
    double result = libraryManager.calculateDynamicLateFee(7, true, true);
    assertEquals(4.2, result);
  }
}

