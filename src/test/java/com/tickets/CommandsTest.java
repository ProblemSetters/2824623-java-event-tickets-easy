package com.tickets;

import com.tickets.Models.CommandResult;
import com.tickets.Models.Event;
import com.tickets.Models.Reason;
import com.tickets.Models.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandsTest {

    private static void assertOk(CommandResult result, String expectedMessage) {
        assertTrue(result.ok, "expected ok with \"" + expectedMessage + "\", got ok=" + result.ok + " reason=" + result.reason);
        assertEquals(expectedMessage, result.message);
    }

    private static void assertError(CommandResult result, Reason expectedReason) {
        assertFalse(result.ok, "expected error " + expectedReason + ", got ok=" + result.ok + " message=\"" + result.message + "\"");
        assertEquals(expectedReason, result.reason);
    }

    private static void assertReservationsJson(State state, String expectedJson) {
        String got = FileIo.reservationsToJson(state).replace("\r\n", "\n").strip();
        String exp = expectedJson.replace("\r\n", "\n").strip();
        assertEquals(exp, got);
    }

    private static Event makeEvent(String eventDoc) {
        return Parser.parseEventJson(eventDoc);
    }

    private static State makeReservations(String reservationsDoc) {
        return Parser.parseReservationsJson(reservationsDoc);
    }

    @Test
    public void test_reserve_adds_tickets_up_to_the_personal_limit() {
        Event event = makeEvent("""
                {
                  "event":"Harbour Lights Concert",
                  "capacity":30,
                  "per_person_limit":5
                }""");
        State state = makeReservations("""
                {
                  "reservations":[
                    {"person":"riley","qty":3},
                    {"person":"sam","qty":2},
                    {"person":"jordan","qty":4}
                  ]
                }""");

        CommandResult upToLimit = Commands.reserve(state, event, "riley", "2");
        CommandResult pastLimit = Commands.reserve(state, event, "riley", "1");
        CommandResult topUpOver = Commands.reserve(state, event, "sam", "4");
        CommandResult newcomer = Commands.reserve(state, event, "quinn", "5");
        CommandResult newcomerOver = Commands.reserve(state, event, "avery", "6");
        CommandResult word = Commands.reserve(state, event, "avery", "two");
        CommandResult zero = Commands.reserve(state, event, "avery", "0");

        assertOk(upToLimit, "Reserved 2 for riley (5 held, 19 left)");
        assertError(pastLimit, Reason.PER_PERSON_LIMIT);
        assertError(topUpOver, Reason.PER_PERSON_LIMIT);
        assertOk(newcomer, "Reserved 5 for quinn (5 held, 14 left)");
        assertError(newcomerOver, Reason.PER_PERSON_LIMIT);
        assertError(word, Reason.QTY_INVALID);
        assertError(zero, Reason.QTY_INVALID);

        assertReservationsJson(state, """
                {
                  "reservations":[
                    {"person":"riley","qty":5},
                    {"person":"sam","qty":2},
                    {"person":"jordan","qty":4},
                    {"person":"quinn","qty":5}
                  ]
                }""");
    }

    @Test
    public void test_reserve_sells_the_last_tickets_and_then_refuses() {
        Event event = makeEvent("""
                {
                  "event":"Garden Theatre Matinee",
                  "capacity":12,
                  "per_person_limit":3
                }""");
        State state = makeReservations("""
                {
                  "reservations":[
                    {"person":"sam","qty":3},
                    {"person":"riley","qty":1},
                    {"person":"jordan","qty":3},
                    {"person":"casey","qty":3}
                  ]
                }""");

        CommandResult moreThanLeft = Commands.reserve(state, event, "avery", "3");
        CommandResult overLimit = Commands.reserve(state, event, "morgan", "4");
        CommandResult holderOver = Commands.reserve(state, event, "riley", "4");
        CommandResult lastTwo = Commands.reserve(state, event, "riley", "2");
        CommandResult overLimitFull = Commands.reserve(state, event, "sam", "4");
        CommandResult newcomerOverFull = Commands.reserve(state, event, "drew", "4");
        CommandResult nothingLeft = Commands.reserve(state, event, "drew", "1");

        assertError(moreThanLeft, Reason.SOLD_OUT);
        assertError(overLimit, Reason.PER_PERSON_LIMIT);
        assertError(holderOver, Reason.PER_PERSON_LIMIT);
        assertOk(lastTwo, "Reserved 2 for riley (3 held, 0 left)");
        assertError(overLimitFull, Reason.PER_PERSON_LIMIT);
        assertError(newcomerOverFull, Reason.PER_PERSON_LIMIT);
        assertError(nothingLeft, Reason.SOLD_OUT);

        assertReservationsJson(state, """
                {
                  "reservations":[
                    {"person":"sam","qty":3},
                    {"person":"riley","qty":3},
                    {"person":"jordan","qty":3},
                    {"person":"casey","qty":3}
                  ]
                }""");
    }

    @Test
    public void test_release_returns_tickets_and_drops_an_emptied_holder() {
        Event event = makeEvent("""
                {
                  "event":"Riverside Food Fair",
                  "capacity":10,
                  "per_person_limit":6
                }""");
        State state = makeReservations("""
                {
                  "reservations":[
                    {"person":"sam","qty":3},
                    {"person":"riley","qty":2},
                    {"person":"jordan","qty":1},
                    {"person":"casey","qty":2},
                    {"person":"taylor","qty":1}
                  ]
                }""");

        CommandResult giveOneBack = Commands.release(state, event, "riley", "1");
        CommandResult giveAllBack = Commands.release(state, event, "sam", "3");
        CommandResult nowFits = Commands.reserve(state, event, "quinn", "3");
        CommandResult bookAgain = Commands.reserve(state, event, "sam", "2");
        CommandResult leave = Commands.release(state, event, "jordan", "1");

        assertOk(giveOneBack, "Released 1 for riley (1 held, 2 left)");
        assertOk(giveAllBack, "Released 3 for sam (0 held, 5 left)");
        assertOk(nowFits, "Reserved 3 for quinn (3 held, 2 left)");
        assertOk(bookAgain, "Reserved 2 for sam (2 held, 0 left)");
        assertOk(leave, "Released 1 for jordan (0 held, 1 left)");

        assertReservationsJson(state, """
                {
                  "reservations":[
                    {"person":"riley","qty":1},
                    {"person":"casey","qty":2},
                    {"person":"taylor","qty":1},
                    {"person":"quinn","qty":3},
                    {"person":"sam","qty":2}
                  ]
                }""");
    }

    @Test
    public void test_release_checks_rules_in_order() {
        Event event = makeEvent("""
                {
                  "event":"Old Mill Open Day",
                  "capacity":15,
                  "per_person_limit":2
                }""");
        State state = makeReservations("""
                {
                  "reservations":[
                    {"person":"sam","qty":2},
                    {"person":"riley","qty":1}
                  ]
                }""");

        CommandResult zero = Commands.release(state, event, "sam", "0");
        CommandResult negative = Commands.release(state, event, "sam", "-1");
        CommandResult notHolding = Commands.release(state, event, "avery", "1");
        CommandResult notHoldingWord = Commands.release(state, event, "avery", "x");
        CommandResult moreThanOne = Commands.release(state, event, "riley", "2");
        CommandResult moreThanTwo = Commands.release(state, event, "sam", "3");

        assertError(zero, Reason.QTY_INVALID);
        assertError(negative, Reason.QTY_INVALID);
        assertError(notHolding, Reason.NO_RESERVATION);
        assertError(notHoldingWord, Reason.QTY_INVALID);
        assertError(moreThanOne, Reason.NOT_ENOUGH_HELD);
        assertError(moreThanTwo, Reason.NOT_ENOUGH_HELD);

        assertReservationsJson(state, """
                {
                  "reservations":[
                    {"person":"sam","qty":2},
                    {"person":"riley","qty":1}
                  ]
                }""");
    }

    @Test
    public void test_transfer_moves_tickets_and_appends_a_new_holder() {
        Event event = makeEvent("""
                {
                  "event":"Lakeside Jazz Evening",
                  "capacity":25,
                  "per_person_limit":6
                }""");
        State state = makeReservations("""
                {
                  "reservations":[
                    {"person":"sam","qty":5},
                    {"person":"riley","qty":3},
                    {"person":"casey","qty":2},
                    {"person":"jordan","qty":1},
                    {"person":"taylor","qty":4}
                  ]
                }""");

        CommandResult toHolder = Commands.transfer(state, event, "sam", "riley", "3");
        CommandResult toNewcomer = Commands.transfer(state, event, "casey", "quinn", "2");
        CommandResult emptyIntoHolder = Commands.transfer(state, event, "jordan", "taylor", "1");
        CommandResult partToNewcomer = Commands.transfer(state, event, "taylor", "avery", "1");

        assertOk(toHolder, "Transferred 3 from sam to riley");
        assertOk(toNewcomer, "Transferred 2 from casey to quinn");
        assertOk(emptyIntoHolder, "Transferred 1 from jordan to taylor");
        assertOk(partToNewcomer, "Transferred 1 from taylor to avery");

        assertReservationsJson(state, """
                {
                  "reservations":[
                    {"person":"sam","qty":2},
                    {"person":"riley","qty":6},
                    {"person":"taylor","qty":4},
                    {"person":"quinn","qty":2},
                    {"person":"avery","qty":1}
                  ]
                }""");
    }

    @Test
    public void test_transfer_checks_rules_in_order() {
        Event event = makeEvent("""
                {
                  "event":"Hilltop Star Party",
                  "capacity":18,
                  "per_person_limit":5
                }""");
        State state = makeReservations("""
                {
                  "reservations":[
                    {"person":"sam","qty":4},
                    {"person":"riley","qty":3},
                    {"person":"casey","qty":1}
                  ]
                }""");

        CommandResult toSelf = Commands.transfer(state, event, "sam", "sam", "2");
        CommandResult toSelfWord = Commands.transfer(state, event, "sam", "sam", "two");
        CommandResult zero = Commands.transfer(state, event, "sam", "riley", "0");
        CommandResult noTickets = Commands.transfer(state, event, "avery", "riley", "1");
        CommandResult noTicketsFull = Commands.transfer(state, event, "avery", "sam", "2");
        CommandResult noTicketsWord = Commands.transfer(state, event, "avery", "riley", "x");
        CommandResult tooFew = Commands.transfer(state, event, "casey", "riley", "2");
        CommandResult overLimit = Commands.transfer(state, event, "sam", "riley", "3");
        CommandResult bothApply = Commands.transfer(state, event, "casey", "riley", "3");

        assertError(toSelf, Reason.SAME_PERSON);
        assertError(toSelfWord, Reason.SAME_PERSON);
        assertError(zero, Reason.QTY_INVALID);
        assertError(noTickets, Reason.NO_RESERVATION);
        assertError(noTicketsFull, Reason.NO_RESERVATION);
        assertError(noTicketsWord, Reason.QTY_INVALID);
        assertError(tooFew, Reason.NOT_ENOUGH_HELD);
        assertError(overLimit, Reason.PER_PERSON_LIMIT);
        assertError(bothApply, Reason.NOT_ENOUGH_HELD);

        assertReservationsJson(state, """
                {
                  "reservations":[
                    {"person":"sam","qty":4},
                    {"person":"riley","qty":3},
                    {"person":"casey","qty":1}
                  ]
                }""");
    }
}
