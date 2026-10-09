package com.tickets;

import com.tickets.Models.CommandResult;
import com.tickets.Models.Event;
import com.tickets.Models.State;

import java.io.PrintStream;

public final class Main {
    private static final String EVENT_PATH = "data/event.json";
    private static final String RESERVATIONS_PATH = "data/reservations.json";

    private Main() {}

    /// Print the supported commands and their arguments.
    private static void printUsage(PrintStream out) {
        out.println("Usage:\n" +
                "  tickets reserve <person> <qty>\n" +
                "  tickets release <person> <qty>\n" +
                "  tickets transfer <from> <to> <qty>");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage(System.err);
            System.exit(1);
        }

        String command = args[0];

        if ("reserve".equals(command)) {
            if (args.length != 3) {
                printUsage(System.err);
                System.exit(1);
            }
        } else if ("release".equals(command)) {
            if (args.length != 3) {
                printUsage(System.err);
                System.exit(1);
            }
        } else if ("transfer".equals(command)) {
            if (args.length != 4) {
                printUsage(System.err);
                System.exit(1);
            }
        } else {
            System.err.println("Unknown command: " + command);
            printUsage(System.err);
            System.exit(1);
        }

        try {
            Event event = Parser.parseEventJson(FileIo.readFileToString(EVENT_PATH));
            State state = Parser.parseReservationsJson(FileIo.readFileToString(RESERVATIONS_PATH));

            CommandResult result;
            if ("reserve".equals(command)) {
                result = Commands.reserve(state, event, args[1], args[2]);
            } else if ("release".equals(command)) {
                result = Commands.release(state, event, args[1], args[2]);
            } else {
                result = Commands.transfer(state, event, args[1], args[2], args[3]);
            }

            if (result.ok) {
                FileIo.writeReservationsJson(RESERVATIONS_PATH, state);
                System.out.println(result.message);
            } else {
                System.out.println("Error: " + result.reason);
            }
        } catch (Exception error) {
            System.err.println("Error: " + error.getMessage());
            System.exit(1);
        }
    }
}
