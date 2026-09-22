package co.icesi.buscaminas.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;

public class BuscaminasTCPClient {

    private final Gson gson = new GsonBuilder().create();

    public Response sendRequest(String host, int port, Request request) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // Serializar Request a JSON y enviar con salto de linea
            String jsonOut = gson.toJson(request);
            writer.write(jsonOut);
            writer.newLine();
            writer.flush();

            // Leer la respuesta delimitada por fin de linea
            String jsonIn = reader.readLine();
            if (jsonIn == null) {
                throw new IOException("El servidor cerro la conexion sin enviar respuesta");
            }
            return gson.fromJson(jsonIn, Response.class);
        }
    }
}
