# Java: Event Tickets

A community event gives out free tickets, with a limit on the total number of tickets and a limit on how many one person can hold. People can reserve tickets, return them, or transfer them to someone else. If one person can hold more than the limit, a transfer creates extra tickets, or more tickets are reserved than the event has, the event is overbooked and people with valid reservations are turned away.

Your task is to fix the event tickets tool so that every command updates the data correctly and prints the correct success message or error reason.

## State File

**File: `data/event.json` (read-only)**

```json
{
  "event": "Park Movie Night",
  "capacity": 27,
  "per_person_limit": 4
}
```

Fields:

- `event` (string): the event name.
- `capacity` (integer): the total tickets available.
- `per_person_limit` (integer): the most tickets one person may hold.

**File: `data/reservations.json`**

```json
{
  "reservations": [
    {"person":"sam","qty":4},
    {"person":"riley","qty":2}
  ]
}
```

Fields:

- `reservations` (list): one entry per person holding tickets, in the order they first reserved.
  - `person` (string): the holder.
  - `qty` (integer): tickets held.

Tickets left are `capacity` minus the sum of every `qty`. A person whose holding drops to 0 is removed from the list, and reserving again later appends them at the end. When writing `data/reservations.json` back, preserve the field order shown above, and keep reservations in the order they appear in the file.

## Command Output

Each command prints a one-line result on success, or `Error: <REASON>` on failure. Its rules are checked in the order listed, stopping at the first failure, and on failure `data/reservations.json` is not modified.

## Commands

### `reserve`

Reserves tickets for a person.

**Usage**

```bash
./tickets reserve <person> <qty>
```

**Output**

```text
Reserved <qty> for <person> (<held> held, <left> left)
```

**Rules**

Check these rules in order and stop at the first failure:

- If `qty` is not a positive whole number, fail with `QTY_INVALID`.
- If the person's holding plus `qty` would exceed `per_person_limit`, fail with `PER_PERSON_LIMIT`.
- If `qty` is more than the tickets left, fail with `SOLD_OUT`.

On success:

- Add `qty` to the person's holding, appending a new entry for a first reservation.
- `<held>` is the person's holding afterwards and `<left>` the tickets left.

### `release`

Returns some of a person's tickets to the event.

**Usage**

```bash
./tickets release <person> <qty>
```

**Output**

```text
Released <qty> for <person> (<held> held, <left> left)
```

**Rules**

Check these rules in order and stop at the first failure:

- If `qty` is not a positive whole number, fail with `QTY_INVALID`.
- If the person holds no tickets, fail with `NO_RESERVATION`.
- If `qty` is more than the person holds, fail with `NOT_ENOUGH_HELD`.

On success, take `qty` from the holding and remove the entry when it reaches 0.

### `transfer`

Moves tickets from one person to another.

**Usage**

```bash
./tickets transfer <from> <to> <qty>
```

**Output**

```text
Transferred <qty> from <from> to <to>
```

**Rules**

Check these rules in order and stop at the first failure:

- If `from` and `to` are the same person, fail with `SAME_PERSON`.
- If `qty` is not a positive whole number, fail with `QTY_INVALID`.
- If `from` holds no tickets, fail with `NO_RESERVATION`.
- If `qty` is more than `from` holds, fail with `NOT_ENOUGH_HELD`.
- If `to`'s holding plus `qty` would exceed `per_person_limit`, fail with `PER_PERSON_LIMIT`.

On success:

- Take `qty` from `from`, removing the entry at 0.
- Add it to `to`, appending a new entry when needed.
- The tickets left do not change.

> **Note:** Refer to `README.md` for more details and examples.
