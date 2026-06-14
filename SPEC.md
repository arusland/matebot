# MateBot — Reminder Text Format Specification

MateBot is a personal Telegram reminder and file bot. Its central feature is a
compact, single-line **text grammar** for creating reminders ("alerts"). You send a
message like `11:00 1-5 Standup` and the bot schedules a notification.

This document specifies that text format and the surrounding user-facing behavior.
It is derived from the parser in
`matebot-db/src/main/java/io/arusland/storage/file/AlertInfo.java` and the message
handlers in `matebot-core/.../bots/commands/CommonCommand.java` and `.../MateBot.java`.

---

## 1. Overview

- A reminder is created by sending the bot a plain text line — there is no `/add`
  command. Any message matching one of the time patterns below becomes an alert.
- All times are interpreted in **your timezone**, which you set with `/tz` (see §7).
  Default display format is `HH:mm dd.MM.yyyy`.
- When an alert's time arrives, the bot sends you a notification and (for repeating
  alerts) reschedules itself automatically.
- Alerts are stored per user as `.alert` files; the original text you typed is kept
  as the alert's *source*.

---

## 2. Reminder grammar

General shape:

```
HH:MM [ date | weekdays ] [ message ]
```

- **`HH:MM`** — required. Hour `0–23`, minute `0–59`. Single- or double-digit
  (`9:5` and `09:05` are both accepted).
- **date** — optional; pins the alert to a day / month / year (see §3).
- **weekdays** — optional; makes the alert recur on given days of the week (see §3).
  Date and weekdays are mutually exclusive.
- **message** — optional free text shown in the notification.

Any time component may carry a **period modifier `/N`** to make the alert repeat
every `N` of that unit (see §4).

If no date or weekday is given, the alert fires **today** at `HH:MM`, or **tomorrow**
if that time has already passed.

---

## 3. Accepted formats

| Format | Meaning | Example |
| --- | --- | --- |
| `HH:MM [msg]` | Today at `HH:MM`, or tomorrow if already past. | `14:30 Lunch` |
| `HH:MM D[-D][,D] [msg]` | Weekly, on the listed weekdays. | `09:00 1-5 Standup` |
| `HH:MM DD: [msg]` | Day of the (current/next) month. | `10:00 23: Rent` |
| `HH:MM DD:MM: [msg]` | Day + month, recurs yearly. | `10:00 23:05: Anniversary` |
| `HH:MM DD:MM:YYYY [msg]` | One exact date. | `10:00 12:06:2017 Trip` |

### Weekdays

Weekday codes (ISO): **1 = Monday … 7 = Sunday**. Combine with ranges and commas:

| Input | Days |
| --- | --- |
| `6` | Saturday only |
| `1-5` | Monday–Friday |
| `1,3,5` | Mon, Wed, Fri |
| `1-5,7` | Mon–Fri and Sunday |
| `5-7` | Fri, Sat, Sun |

A range must be ascending (`5-7`, not `7-5`); an invalid range makes the whole
weekday spec fail to parse.

### Notes on dates

- `DD:` — only the day is given; the alert targets that day of the month.
- `DD:MM:` — day and month, repeating every year.
- `DD:MM:YYYY` — a fully-qualified one-off date. Invalid calendar dates (e.g.
  `31:02:2020`) are rejected.

---

## 4. Periodic reminders (`/N`)

Append `/N` to any time component to repeat the alert every `N` units of that
component. The unit is determined by **which position** the `/N` follows:

| Position | Unit | Example | Repeats |
| --- | --- | --- | --- |
| hour | hours | `11/2:00` | every 2 hours |
| minute | minutes | `11:00/5` | every 5 minutes |
| day | days | `10:00 25/2:10:2017` | every 2 days |
| month | months | `10:00 23:10/3:2017` | every 3 months |
| year | years | `10:00 23:10:2017/1` | every year |

**Only one period may be applied per alert.** Using more than one `/N` (e.g.
`11/2:00/3`) is rejected with:

```
Period can be applied only once
```

Periods combine with weekdays and dates:

```
11:00/2 5-7      → fire at 11:00 on Fri–Sun, then every 2 hours after
10:00 25/2:10:2017 → fire on 25 Oct 2017, then every 2 days
```

### How an active period behaves

A periodic alert has a *base time* (`nextDate`) and, once it first fires, an
**active period** that walks forward by the step until the base time of the next
occurrence is reached. Illustrative trace for `11:00/2 5-7` (from
`matebot-db/periods.adoc`, times shown as `HH:MM dd:MM:yyyy`):

```
base:   11:00 17:11:2017   → fires, active period starts
        11:02, 11:04, 11:06, …   (every 2 minutes/hours of the chosen unit)
next base: 11:00 18:11:2017       (the cycle resets on the next scheduled day)
```

You can stop just the repeating part while keeping the alert with **`/cancelPeriod`**
(see §5–§6).

---

## 5. When a reminder fires

The bot sends a notification built in `MateBot` (`AlertsRunnerHandler`):

```
ALERT: <message>

Alert with period (<friendly period>)      ← only if the alert is periodic

❌/remove  ⏰/cancelPeriod                   ← ⏰/cancelPeriod only if periodic

Remind me in:
/1min  /5min  /10min  /30min  /1hour  /1day
```

- If the alert has no message, the first line is just `ALERT!!!`.
- **`/remove`** deletes the alert.
- **`/cancelPeriod`** cancels the active repeating period (shown only for periodic
  alerts).
- **Remind me in** buttons snooze/reschedule the same message: `/1min` = 1 min,
  `/5min`, `/10min`, `/30min`, `/1hour` = 60 min, `/1day` = 24 hours.

### Confirmation when an alert is created

After you send a valid reminder, the bot replies (from `CommonCommand`):

```
✅ Alert added!
Notification time: HH:mm dd.MM.yyyy [with period (<friendly period>)]
🔔 Notification in <friendly timespan>
```

If the computed next time is already in the past, the alert is discarded instead of
saved.

---

## 6. Viewing and managing alerts

| Command | Action |
| --- | --- |
| `/alerts` | Open the alerts section / list. |
| `/today` | Show today's alerts. |
| `/tomorrow` | Show tomorrow's alerts. |
| `/week` | Show alerts for the current week. |
| `/month` | Show alerts for the current month. |

Each listed alert is numbered and gets inline shortcuts:

```
/1 🔔 HH:mm <message>  /del1  /cancelPeriod1
/2 🔔 HH:mm <message>  /del2
```

- **`/N`** (e.g. `/1`) — open that alert. Its detail view shows the source text,
  next time, active state (`🔔 Notification in …` or `❌ Not active`) and the
  `❌/remove` / `⏰/cancelPeriod` / `✅/cancel` actions.
- **`/delN`** — delete alert N.
- **`/cancelPeriodN`** — cancel the repeating period of alert N (shown only when the
  alert is periodic).

**Delete all inactive alerts** — sending the relevant action prompts
`Are you sure to remove all inactive alerts?` with a `/remove` confirm; on confirm it
reports `Inactive alerts were removed: <count>`.

---

## 7. Worked examples

```
You:  14:30 Lunch
Bot:  ✅ Alert added!
      Notification time: 14:30 14.06.2026
      🔔 Notification in 1 hour 5 minutes
```

```
You:  09:00 1-5 Daily standup
Bot:  ✅ Alert added!
      Notification time: 09:00 15.06.2026
      🔔 Notification in 18 hours 35 minutes
```

```
You:  11:00/2 5-7 Drink water
Bot:  ✅ Alert added!
      Notification time: 11:00 19.06.2026 with period (2 hours)
      🔔 Notification in ...

(when it fires)
Bot:  ALERT: Drink water

      Alert with period (2 hours)

      ❌/remove  ⏰/cancelPeriod

      Remind me in:
      /1min  /5min  /10min  /30min  /1hour  /1day
```

```
You:  10:00 12:06:2027 Trip
Bot:  ✅ Alert added!
      Notification time: 10:00 12.06.2027
      🔔 Notification in 363 days ...
```
