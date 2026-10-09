package com.tickets;

import com.tickets.Models.CommandResult;
import com.tickets.Models.Event;
import com.tickets.Models.Reason;
import com.tickets.Models.Reservation;
import com.tickets.Models.State;

import java.util.Iterator;

public final class Commands {
    private Commands() {}

    /// Read the ticket count typed on the command line.
    private static Integer parseQty(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        long value = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < '0' || ch > '9') {
                return null;
            }
            value = Math.min(value * 10 + (ch - '0'), 1000000000L);
        }
        int qty = (int) value;
        return qty > 0 ? qty : null;
    }

    /// Find the reservation the person holds.
    private static Reservation findHolder(State state, String person) {
        for (Reservation entry : state.reservations) {
            if (entry.person.equals(person)) {
                return entry;
            }
        }
        return null;
    }

    /// Count the tickets the person holds.
    private static int holdingOf(State state, String person) {
        Reservation holder = findHolder(state, person);
        return holder == null ? 0 : holder.qty;
    }

    /// Count the tickets still available for the event.
    private static int ticketsLeft(State state, Event event) {
        int taken = 0;
        for (Reservation reservation : state.reservations) {
            taken += reservation.qty;
        }
        return event.capacity - taken;
    }

    /// Give tickets to a person, adding them to the end of the list on their first reservation.
    private static void addTickets(State state, String person, int qty) {
        Reservation holder = findHolder(state, person);
        if (holder == null) {
            state.reservations.add(new Reservation(person, qty));
            return;
        }
        holder.qty += qty;
    }

    /// Take tickets from a person, dropping them from the list once they hold none.
    private static void takeTickets(State state, String person, int qty) {
        Iterator<Reservation> it = state.reservations.iterator();
        while (it.hasNext()) {
            Reservation entry = it.next();
            if (entry.person.equals(person)) {
                entry.qty -= qty;
                if (entry.qty == 0) {
                    it.remove();
                }
                break;
            }
        }
    }

    /// Build a successful result carrying the command's output message.
    private static CommandResult success(String message) {
        return CommandResult.success(message);
    }

    /// Build a failed result carrying the reason code.
    private static CommandResult failure(Reason reason) {
        return CommandResult.failure(reason);
    }

    /// Reserve tickets for a person.
    public static CommandResult reserve(State state, Event event, String person, String qtyText) {
        Integer parsed = parseQty(qtyText);
        if (parsed == null) {
            return failure(Reason.QTY_INVALID);
        }
        int qty = parsed;
        int held = holdingOf(state, person);
        if (held + qty > event.perPersonLimit) {
            return failure(Reason.PER_PERSON_LIMIT);
        }
        if (qty > ticketsLeft(state, event)) {
            return failure(Reason.SOLD_OUT);
        }
        addTickets(state, person, qty);

        return success("Reserved " + qty + " for " + person + " (" +
                (held + qty) + " held, " +
                ticketsLeft(state, event) + " left)");
    }

    /// Give a person's tickets back.
    public static CommandResult release(State state, Event event, String person, String qtyText) {
        Integer parsed = parseQty(qtyText);
        if (parsed == null) {
            return failure(Reason.QTY_INVALID);
        }
        int qty = parsed;
        int held = holdingOf(state, person);
        if (held == 0) {
            return failure(Reason.NO_RESERVATION);
        }
        if (qty > held) {
            return failure(Reason.NOT_ENOUGH_HELD);
        }
        takeTickets(state, person, qty);

        return success("Released " + qty + " for " + person + " (" +
                holdingOf(state, person) + " held, " +
                ticketsLeft(state, event) + " left)");
    }

    /// Move tickets from one person to another.
    public static CommandResult transfer(State state, Event event, String from, String to, String qtyText) {
        if (from.equals(to)) {
            return failure(Reason.SAME_PERSON);
        }
        Integer parsed = parseQty(qtyText);
        if (parsed == null) {
            return failure(Reason.QTY_INVALID);
        }
        int qty = parsed;
        int fromHeld = holdingOf(state, from);
        if (fromHeld == 0) {
            return failure(Reason.NO_RESERVATION);
        }
        if (qty > fromHeld) {
            return failure(Reason.NOT_ENOUGH_HELD);
        }
        int toHeld = holdingOf(state, to);
        if (toHeld + qty > event.perPersonLimit) {
            return failure(Reason.PER_PERSON_LIMIT);
        }
        takeTickets(state, from, qty);
        addTickets(state, to, qty);

        return success("Transferred " + qty + " from " + from + " to " + to);
    }
}
