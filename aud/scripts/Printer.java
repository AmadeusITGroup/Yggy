/// ANSI-colored console printer.
class Printer {
  private static final String RESET = "\033[0m";

  private Printer() {
    throw new IllegalArgumentException("Printer class cannot be instantiated");
  }

  static void printBlue(String msg) {
    System.out.println("\033[34m" + msg + RESET);
  }

  static void printRed(String msg) {
    System.err.println("\033[31m" + msg + RESET);
  }

  static void printGreen(String msg) {
    System.out.println("\033[32m" + msg + RESET);
  }

  static void printOrange(String msg) {
    System.out.println("\033[33m" + msg + RESET);
  }
}
