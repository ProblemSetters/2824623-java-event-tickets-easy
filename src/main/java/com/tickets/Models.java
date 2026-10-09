package com.tickets;

import java.util.ArrayList;
import java.util.List;

public final class Models {
    private Models() {}

    public static final class Event {
        public String event = "";
        public int capacity = 0;
        public int perPersonLimit = 3;

        public Event() {}

        public Event(String event, int capacity, int perPersonLimit) {
            this.event = event;
            this.capacity = capacity;
            this.perPersonLimit = perPersonLimit;
        }
    }

    public static final class Reservation {
        public String person = "";
        public int qty = 0;

        public Reservation() {}

        public Reservation(String person, int qty) {
            this.person = person;
            this.qty = qty;
        }
    }

    public static final class State {
        public List<Reservation> reservations = new ArrayList<>();

        public State() {}

        public State(List<Reservation> reservations) {
            this.reservations = new ArrayList<>(reservations);
        }
    }

    public enum Reason {
        NONE,
        QTY_INVALID,
        PER_PERSON_LIMIT,
        SOLD_OUT,
        NO_RESERVATION,
        NOT_ENOUGH_HELD,
        SAME_PERSON
    }

    public static final class CommandResult {
        public boolean ok = false;
        public String message = "";
        public Reason reason = Reason.NONE;

        public CommandResult() {}

        public CommandResult(boolean ok, String message, Reason reason) {
            this.ok = ok;
            this.message = message;
            this.reason = reason;
        }

        public static CommandResult success(String message) {
            return new CommandResult(true, message, Reason.NONE);
        }

        public static CommandResult failure(Reason reason) {
            return new CommandResult(false, "", reason);
        }
    }
}
