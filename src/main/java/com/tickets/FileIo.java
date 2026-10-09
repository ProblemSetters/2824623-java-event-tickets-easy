package com.tickets;

import com.tickets.Models.Reservation;
import com.tickets.Models.State;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class FileIo {
    private FileIo() {}

    /// Escape a string for safe inclusion inside JSON double quotes.
    public static String escapeJsonString(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(ch);
            }
        }
        return escaped.toString();
    }

    /// Format a single reservation as a single-line JSON object.
    public static String reservationToJson(Reservation reservation) {
        return "{\"person\":\"" + escapeJsonString(reservation.person) + "\"" +
                ",\"qty\":" + reservation.qty + "}";
    }

    /// Read a text file as a single string.
    public static String readFileToString(String path) {
        try {
            return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open file: " + path, e);
        }
    }

    /// Format the reservations as JSON for data/reservations.json.
    public static String reservationsToJson(State state) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n  \"reservations\":[");
        List<Reservation> items = state.reservations;
        for (int i = 0; i < items.size(); i++) {
            if (i == 0) {
                builder.append("\n    ");
            } else {
                builder.append(",\n    ");
            }
            builder.append(reservationToJson(items.get(i)));
        }
        if (!items.isEmpty()) {
            builder.append("\n  ");
        }
        builder.append("]\n}");
        return builder.toString();
    }

    /// Write the reservations JSON to the given file.
    public static void writeReservationsJson(String path, State state) {
        try {
            Files.writeString(Paths.get(path), reservationsToJson(state) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open output file: " + path, e);
        }
    }
}
