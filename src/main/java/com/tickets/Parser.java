package com.tickets;

import com.tickets.Models.Event;
import com.tickets.Models.Reservation;
import com.tickets.Models.State;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private Parser() {}

    private static final class JsonParser {
        private final String json;
        private int pos = 0;

        JsonParser(String json) {
            this.json = json;
        }

        Event parseEventDoc() {
            Event event = new Event();
            skipWs();
            expect('{');
            while (true) {
                skipWs();
                if (peek() == '}') {
                    pos++;
                    break;
                }
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                if ("event".equals(key)) {
                    event.event = parseString();
                } else if ("capacity".equals(key)) {
                    event.capacity = parseInt();
                } else if ("per_person_limit".equals(key)) {
                    event.perPersonLimit = parseInt();
                }
                skipWs();
                if (peek() == ',') {
                    pos++;
                }
            }
            return event;
        }

        State parseReservationsDoc() {
            State state = new State();
            skipWs();
            expect('{');
            while (true) {
                skipWs();
                if (peek() == '}') {
                    pos++;
                    break;
                }
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                if ("reservations".equals(key)) {
                    parseArray(state.reservations);
                }
                skipWs();
                if (peek() == ',') {
                    pos++;
                }
            }
            return state;
        }

        private void parseArray(List<Reservation> target) {
            expect('[');
            skipWs();
            if (peek() == ']') {
                pos++;
                return;
            }
            while (true) {
                target.add(parseReservationObject());
                skipWs();
                if (peek() == ',') {
                    pos++;
                    skipWs();
                    continue;
                }
                expect(']');
                break;
            }
        }

        private Reservation parseReservationObject() {
            Reservation reservation = new Reservation();
            skipWs();
            expect('{');
            while (true) {
                skipWs();
                if (peek() == '}') {
                    pos++;
                    break;
                }
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                if ("person".equals(key)) {
                    reservation.person = parseString();
                } else if ("qty".equals(key)) {
                    reservation.qty = parseInt();
                }
                skipWs();
                if (peek() == ',') {
                    pos++;
                }
            }
            return reservation;
        }

        private char peek() {
            return pos < json.length() ? json.charAt(pos) : '\0';
        }

        private void expect(char c) {
            if (peek() != c) {
                throw new RuntimeException("JSON parse error: expected '" + c + "' at position " + pos);
            }
            pos++;
        }

        private void skipWs() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (pos < json.length() && json.charAt(pos) != '"') {
                if (json.charAt(pos) == '\\' && pos + 1 < json.length()) {
                    pos++;
                    char escaped = json.charAt(pos++);
                    switch (escaped) {
                        case 'n' -> value.append('\n');
                        case 'r' -> value.append('\r');
                        case 't' -> value.append('\t');
                        case '\\' -> value.append('\\');
                        case '"' -> value.append('"');
                        default -> value.append(escaped);
                    }
                } else {
                    value.append(json.charAt(pos++));
                }
            }
            expect('"');
            return value.toString();
        }

        private int parseInt() {
            if (!Character.isDigit(peek())) {
                throw new RuntimeException("JSON parse error: expected integer at position " + pos);
            }
            int value = 0;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                value = value * 10 + (json.charAt(pos) - '0');
                pos++;
            }
            return value;
        }
    }

    /// Parse the read-only event JSON.
    public static Event parseEventJson(String doc) {
        JsonParser parser = new JsonParser(doc);
        return parser.parseEventDoc();
    }

    /// Parse the reservations JSON.
    public static State parseReservationsJson(String doc) {
        JsonParser parser = new JsonParser(doc);
        return parser.parseReservationsDoc();
    }
}
