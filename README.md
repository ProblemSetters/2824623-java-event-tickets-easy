# Java: Event Tickets

Your task is to fix the event tickets tool so that every command updates the data correctly and prints the correct success message or error reason. It reads the state from `data/event.json` and `data/reservations.json`, processes the `reserve`, `release` and `transfer` commands, and writes the updated state back.

## Project Structure

```text
├── data/
│   ├── event.json              # Read-only event limits (event, capacity, per_person_limit)
│   └── reservations.json       # Persistent reservations state (reservations)
├── src/
│   ├── main/java/com/tickets/
│   │   ├── Commands.java       # Command handlers
│   │   ├── FileIo.java         # State file load and save
│   │   ├── Main.java           # CLI entry point
│   │   ├── Models.java         # Data models
│   │   └── Parser.java         # JSON parsing
│   └── test/java/com/tickets/
│       └── CommandsTest.java
├── tickets                     # CLI launcher script
├── run.sh                      # Setup and run script
├── reset.sh                    # Restore data/reservations.json to its initial state
├── test.sh                     # Test script (writes junit.xml)
├── pom.xml                     # Maven build configuration
└── README.md
```

## Commands

### Install

```bash
mvn -q -DskipTests package
```

### Run

```bash
bash run.sh
```

### Test

```bash
bash test.sh
```

### Reset

Restore `data/reservations.json` to its initial state.

```bash
bash reset.sh
```

## Examples

Each example below runs independently against the initial data files. Park Movie Night has 27 tickets and a limit of 4 per person. Eight people hold 25 of them, in this order: sam 4, riley 2, jordan 4, casey 1, taylor 4, quinn 3, morgan 4, drew 3. That leaves 2.

### Example 1: Reserving adds to what a person already holds

Command:

```bash
./tickets reserve casey 1
```

Output:

```text
Reserved 1 for casey (2 held, 1 left)
```

casey afterwards:

```json
{"person":"casey","qty":2}
```

casey held 1, so one more makes 2, and 1 ticket is left.

### Example 2: Releasing gives tickets back to the event

Command:

```bash
./tickets release sam 1
```

Output:

```text
Released 1 for sam (3 held, 3 left)
```

sam afterwards:

```json
{"person":"sam","qty":3}
```

sam goes from 4 to 3 and the ticket returns to the pool, so 3 are left. A person who gives back everything they hold leaves the list: `./tickets release casey 1` prints `Released 1 for casey (0 held, 3 left)` and casey's entry is removed.

### Example 3: Only tickets a person holds can be released

Commands:

```bash
./tickets release riley 3
./tickets release avery 1
```

Output:

```text
Error: NOT_ENOUGH_HELD
Error: NO_RESERVATION
```

riley holds 2 and avery holds none. Neither command changes the file.

### Example 4: A transfer moves tickets between two holders

Command:

```bash
./tickets transfer quinn riley 2
```

Output:

```text
Transferred 2 from quinn to riley
```

The two records that change. quinn:

```json
{"person":"quinn","qty":1}
```

riley:

```json
{"person":"riley","qty":4}
```

quinn goes from 3 to 1 and riley from 2 to 4. No ticket leaves or enters the pool, so 2 are still left. `./tickets transfer quinn riley 3` would take riley to 5, over the limit of 4, and answers `Error: PER_PERSON_LIMIT`.

### Example 5: A transfer needs a giver who holds tickets, and two different people

Commands:

```bash
./tickets transfer avery sam 1
./tickets transfer sam sam 1
```

Output:

```text
Error: NO_RESERVATION
Error: SAME_PERSON
```

avery holds no tickets, and sam cannot transfer to sam. Neither command changes the file.
