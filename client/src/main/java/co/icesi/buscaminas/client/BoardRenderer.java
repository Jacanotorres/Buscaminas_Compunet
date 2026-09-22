package co.icesi.buscaminas.client;

import co.icesi.buscaminas.model.Cell;

public class BoardRenderer {

    private static final String YELLOW = "[33m";
    private static final String RED = "[31m";
    private static final String RESET = "[0m";

    private BoardRenderer() {
    }

    public static void print(Cell[][] board) {
        if (board == null || board.length == 0) {
            System.out.println("(tablero vacio)");
            return;
        }
        int rows = board.length;
        int cols = board[0].length;

        System.out.print("     ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("%3d", j);
        }
        System.out.println();

        printSeparator(cols);

        for (int i = 0; i < rows; i++) {
            System.out.printf("%3d |", i);
            for (int j = 0; j < cols; j++) {
                System.out.print(" " + renderCell(board[i][j]) + " ");
            }
            System.out.println("|");
        }

        printSeparator(cols);
    }

    private static void printSeparator(int cols) {
        System.out.print("    +");
        for (int j = 0; j < cols; j++) {
            System.out.print("---");
        }
        System.out.println("+");
    }

    private static String renderCell(Cell cell) {
        if (cell.isMarked()) {
            return YELLOW + "M" + RESET;
        }
        if (cell.isHide() && !cell.isShowAll()) {
            return ".";
        }
        if (cell.isLandMine()) {
            return RED + "*" + RESET;
        }
        int value = cell.getValue();
        return value == 0 ? " " : String.valueOf(value);
    }
}
