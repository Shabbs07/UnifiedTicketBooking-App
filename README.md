# Unified Ticket Booking Application (Android | Java)

One Android app to book **bus, train, movie and event tickets** — a single account, a single
seat-selection flow and a single "My bookings" wallet of QR e-tickets.

## 1. Problem statement
Users juggle separate apps for buses, trains, cinemas and live events, each with its own login,
UI and booking history. This project unifies them behind one app and one data model.

## 2. Features
| Module | What it does |
|---|---|
| Authentication | Register / login, input validation, PBKDF2-hashed passwords, session handling, logout |
| Unified catalogue | Bus, Train, Movie, Event listings in one `services` table; live search by name / city / venue |
| Seat selection | Visual seat grid (available / selected / booked), max 6 seats per booking |
| Booking engine | Atomic transaction re-checks seats, so double-booking is impossible |
| Payment (demo) | Simulated payment-mode picker (UPI / card / net banking / counter) |
| E-ticket | Booking reference + QR code (ZXing), status, amount, seats |
| My bookings | History of all bookings; cancel before departure releases the seats |

## 3. Tech stack
Java 8 · Android SDK (minSdk 26, targetSdk 34) · SQLite (`SQLiteOpenHelper`) · Material Components ·
RecyclerView · ZXing core (QR) · JUnit 4 · Gradle (AGP 8.5.2)

## 4. Architecture
```
ui/        Activities (Login, Register, Home, ServiceList, SeatSelection, Ticket, MyBookings)
adapter/   RecyclerView adapters (ServiceAdapter, BookingAdapter)
db/        DatabaseHelper  – schema, seed data, all queries & the booking transaction
model/     User, Service, Booking (plain Java objects)
util/      SecurityUtil (hashing, validation, refs), SessionManager, Formats
```
Flow: `Login → Home → Category list → Seat selection → Pay → E-ticket → My bookings`

## 5. Database design
```
users(id PK, name, email UNIQUE, phone, password_hash, salt)
services(id PK, category[BUS|TRAIN|MOVIE|EVENT], title, source, destination,
         travel_date, travel_time, price, total_seats, operator)
bookings(id PK, booking_ref UNIQUE, user_id FK→users, service_id FK→services,
         seats "3,4,5", amount, payment_mode, status[CONFIRMED|CANCELLED], booked_at)
```
`users 1 ──< N bookings N >── 1 services`

## 6. Security design
- Passwords: PBKDF2-HMAC-SHA256, 100 000 iterations, random 16-byte salt per user, constant-time compare.
- SQL injection: every query is parameterised.
- Broken access control: tickets are fetched with `WHERE id=? AND user_id=?` so one user cannot open or cancel another's booking.
- Generic login error message (no user enumeration); strong-password + email/phone validation.
- `allowBackup="false"`, non-launcher activities `exported="false"`, no INTERNET permission.
- Race conditions: seat availability re-validated inside a DB transaction.

## 7. How to run
1. Install **Android Studio (Koala or newer)**.
2. *File → Open* → select this folder → let Gradle sync (needs internet once).
3. Run on an emulator / device (Android 8.0+).
4. Register an account (password: 8+ chars, letters + digits; mobile: 10 digits starting 6–9) and book.

Run unit tests: `./gradlew test` (or right-click `SecurityUtilTest` → Run).

## 8. Test cases
| # | Scenario | Expected |
|---|---|---|
| 1 | Register with existing email | "already registered" error |
| 2 | Register with weak password | Validation error |
| 3 | Login with wrong password | Generic "Invalid email or password" |
| 4 | Select 7th seat | Toast: max 6 seats |
| 5 | Book seats, then re-open same trip | Those seats shown red / not selectable |
| 6 | Cancel confirmed future booking | Status CANCELLED, seats free again |
| 7 | Search "Pune" in Bus | Only Pune-related trips |
| 8 | Logout then press Back | Cannot return to Home |

## 9. Limitations & future scope
- Data is local (SQLite) with seeded demo inventory; payment is simulated.
- Next steps: REST backend (Spring Boot / Firebase), real payment gateway (Razorpay), push notifications,
  Room + ViewModel/LiveData (MVVM), QR scanner for ticket validation, date filters, dark mode,
  background threads for hashing/DB work, biometric login.
