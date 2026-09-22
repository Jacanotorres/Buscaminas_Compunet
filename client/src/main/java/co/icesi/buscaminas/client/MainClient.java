package co.icesi.buscaminas.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;
import co.icesi.buscaminas.model.Cell;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainClient {

    private static final Gson gson = new GsonBuilder().create();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;

        BuscaminasTCPClient client = new BuscaminasTCPClient();
        boolean exit = false;

        System.out.println("Conectando al servidor " + host + ":" + port);

        while (!exit) {
            printMenu();
            int option = readInt("Seleccione una opcion: ");
            try {
                switch (option) {
                    case 1: {
                        int n = readInt("Filas: ");
                        int m = readInt("Columnas: ");
                        int minas = readInt("Minas: ");
                        Request rq = buildRequest("INIT_GAME",
                                "n", String.valueOf(n), "m", String.valueOf(m), "minas", String.valueOf(minas));
                        Response rs = client.sendRequest(host, port, rq);
                        handleResponse(rs);
                        break;
                    }
                    case 2: {
                        int i = readInt("Fila: ");
                        int j = readInt("Columna: ");
                        Request rq = buildRequest("SELECT_CELL", "i", String.valueOf(i), "j", String.valueOf(j));
                        Response rs = client.sendRequest(host, port, rq);
                        handleResponse(rs);
                        break;
                    }
                    case 3: {
                        int i = readInt("Fila: ");
                        int j = readInt("Columna: ");
                        Request rq = buildRequest("MARK_CELL", "i", String.valueOf(i), "j", String.valueOf(j));
                        Response rs = client.sendRequest(host, port, rq);
                        handleResponse(rs);
                        break;
                    }
                    case 4: {
                        Request rq = buildRequest("GET_BOARD");
                        Response rs = client.sendRequest(host, port, rq);
                        handleResponse(rs);
                        break;
                    }
                    case 5: {
                        Request rq = buildRequest("SOW_ALL");
                        Response rs = client.sendRequest(host, port, rq);
                        handleResponse(rs);
                        System.out.println("Te has rendido. Tablero completo revelado.");
                        break;
                    }
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("Opcion invalida. Intente de nuevo.");
                }
            } catch (IOException e) {
                System.out.println("Error de comunicacion con el servidor: " + e.getMessage());
            }
        }

        System.out.println("Hasta luego!");
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=============================================");
        System.out.println("     BUSCAMINAS DISTRIBUIDO - CLIENTE TCP");
        System.out.println("=============================================");
        System.out.println("[1] Iniciar nueva partida (Filas, Columnas, Minas)");
        System.out.println("[2] Destapar celda (Fila, Columna)");
        System.out.println("[3] Marcar/Desmarcar bandera (Fila, Columna)");
        System.out.println("[4] Consultar estado actual del tablero");
        System.out.println("[5] Rendirse y revelar tablero completo");
        System.out.println("[6] Salir");
    }

    /**
     * Muestra el tablero recibido y, si la partida termino, el mensaje de victoria/derrota.
     * @return true si la partida termino (gameEnd == true), false en caso contrario.
     */
    private static boolean handleResponse(Response rs) {
        if (rs == null) {
            System.out.println("Respuesta vacia del servidor.");
            return false;
        }
        if (!"OK".equals(rs.status)) {
            String message = (rs.data != null && rs.data.has("message"))
                    ? rs.data.get("message").getAsString() : "Error desconocido";
            System.out.println("[ERROR] " + message);
            return false;
        }
        if (rs.data == null) {
            return false;
        }
        if (rs.data.has("board")) {
            Cell[][] board = gson.fromJson(rs.data.get("board"), Cell[][].class);
            BoardRenderer.print(board);
        }
        boolean win = rs.data.has("win") && rs.data.get("win").getAsBoolean();
        boolean gameEnd = rs.data.has("gameEnd") && rs.data.get("gameEnd").getAsBoolean();

        if (gameEnd && win) {
            System.out.println("*** GANASTE! Felicitaciones, despejaste el campo minado. ***");
        } else if (gameEnd && !win) {
            String message = rs.data.has("message") ? rs.data.get("message").getAsString() : "";
            System.out.println("*** BOOM! Pisaste una mina. Derrota. " + message + " ***");
        }
        return gameEnd;
    }

    private static Request buildRequest(String action, String... kv) {
        Request rq = new Request();
        rq.action = action;
        Map<String, String> data = new HashMap<>();
        for (int k = 0; k + 1 < kv.length; k += 2) {
            data.put(kv[k], kv[k + 1]);
        }
        rq.data = data;
        return rq;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese un numero entero valido.");
            }
        }
    }
}
