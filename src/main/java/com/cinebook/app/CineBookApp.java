package com.cinebook.app;

import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.SeatStatus;
import com.cinebook.enums.SeatType;
import com.cinebook.exception.BookingNotFoundException;
import com.cinebook.exception.CineBookException;
import com.cinebook.exception.InvalidCouponException;
import com.cinebook.exception.InvalidSeatException;
import com.cinebook.exception.SeatNotAvailableException;
import com.cinebook.model.Booking;
import com.cinebook.model.Customer;
import com.cinebook.model.Movie;
import com.cinebook.model.Seat;
import com.cinebook.model.Show;
import com.cinebook.service.BookingService;
import com.cinebook.util.DatabaseConnection;
import com.cinebook.util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * CineBook: Console-Based Cinema Ticket Booking System
 * Main application entry point providing interactive terminal UI,
 * input validation, and demonstration of syllabus concepts.
 */
public class CineBookApp {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private final BookingService bookingService;
    private final InputValidator input;

    public CineBookApp(Scanner scanner) {
        this.bookingService = new BookingService();
        this.input = new InputValidator(scanner);
    }

    public static void main(String[] args) {
        // Test and initialize database schema and seed data if needed
        initializeEnvironment();

        Scanner scanner = new Scanner(System.in);
        CineBookApp app = new CineBookApp(scanner);
        app.run();
    }

    /**
     * Verifies MySQL connection and ensures initial tables and seed data exist.
     */
    private static void initializeEnvironment() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.executeSqlScript("schema.sql");
            DatabaseConnection.executeSqlScript("data.sql");
        } catch (SQLException e) {
            System.err.println("Database connectivity warning: " + e.getMessage());
            System.err.println("Ensure MySQL service is running and db.properties credentials are correct.");
        }
    }

    /**
     * Primary application loop presenting the main menu.
     */
    public void run() {
        printBanner();
        boolean running = true;

        while (running) {
            printMainMenu();
            int choice = input.readInt("Enter your choice (1-8): ", 1, 8);
            System.out.println();

            switch (choice) {
                case 1 -> handleViewMovies();
                case 2 -> handleViewShows();
                case 3 -> handleViewSeatMap();
                case 4 -> handleBookTickets();
                case 5 -> handleViewBooking();
                case 6 -> handleViewBookingHistory();
                case 7 -> handleCancelBooking();
                case 8 -> {
                    System.out.println("Thank you for choosing CineBook Cinemas! Goodbye.");
                    running = false;
                }
            }
            if (running) {
                System.out.println();
            }
        }
    }

    private void printBanner() {
        System.out.println("========================================================================");
        System.out.println("                    C I N E B O O K   C I N E M A S                     ");
        System.out.println("             Java & JDBC Movie Ticket Booking Semester Project          ");
        System.out.println("========================================================================");
    }

    private void printMainMenu() {
        System.out.println("==================== CINEBOOK MAIN MENU ====================");
        System.out.println("  1. View Movies");
        System.out.println("  2. View Shows");
        System.out.println("  3. View Seat Map");
        System.out.println("  4. Book Tickets");
        System.out.println("  5. View Booking Details");
        System.out.println("  6. View Booking History");
        System.out.println("  7. Cancel Booking");
        System.out.println("  8. Exit");
        System.out.println("============================================================");
    }

    // =========================================================================
    // 1. VIEW MOVIES
    // =========================================================================
    private void handleViewMovies() {
        System.out.println("-------------------- CURRENT SCREENINGS --------------------");
        try {
            List<Movie> movies = bookingService.getAllMovies();
            if (movies.isEmpty()) {
                System.out.println("No movies currently scheduled.");
                return;
            }

            // Using Stream API to sort movies cleanly by ID
            System.out.printf("%-5s | %-25s | %-10s | %-20s | %-8s%n", "ID", "Movie Title", "Language", "Genre", "Duration");
            System.out.println("------------------------------------------------------------------------");
            movies.stream()
                    .sorted(Comparator.comparingInt(Movie::getMovieId))
                    .forEach(m -> System.out.printf("%-5d | %-25s | %-10s | %-20s | %d mins%n",
                            m.getMovieId(), m.getMovieName(), m.getLanguage(), m.getGenre(), m.getDuration()));
            System.out.println("------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Error loading movies from database: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. VIEW SHOWS
    // =========================================================================
    private void handleViewShows() {
        System.out.println("-------------------- SCHEDULED MOVIE SHOWS --------------------");
        try {
            List<Show> shows = bookingService.getAllShows();
            if (shows.isEmpty()) {
                System.out.println("No scheduled shows found.");
                return;
            }

            System.out.printf("%-8s | %-25s | %-12s | %-10s | %-10s%n", "Show ID", "Movie Title", "Date", "Time", "Screen");
            System.out.println("------------------------------------------------------------------------");
            shows.forEach(s -> System.out.printf("%-8d | %-25s | %-12s | %-10s | %-10s%n",
                    s.getShowId(),
                    s.getMovieName(),
                    s.getShowDate().format(DATE_FMT),
                    s.getShowTime().format(TIME_FMT),
                    s.getScreenName()));
            System.out.println("------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Error loading shows from database: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. VIEW SEAT MAP (5x8 Grid from Database)
    // =========================================================================
    private void handleViewSeatMap() {
        handleViewShows();
        int showId = input.readPositiveInt("Enter Show ID to view seat map: ");
        displaySeatMap(showId);
    }

    private boolean displaySeatMap(int showId) {
        try {
            List<Seat> seats = bookingService.getSeatMapForShow(showId);
            if (seats.isEmpty()) {
                System.out.println(">> Show #" + showId + " does not exist or has no seats initialized.");
                return false;
            }

            // Map seats by seatCode (e.g. "A1" -> Seat)
            Map<String, Seat> seatMap = new HashMap<>();
            seats.forEach(s -> seatMap.put(s.getSeatCode().toUpperCase(), s));

            System.out.println("\n============= SEAT MAP FOR SHOW #" + showId + " =============");
            System.out.println("                 [ SCREEN THIS WAY ]                 ");
            System.out.println("-----------------------------------------------------");
            System.out.println("      1   2   3   4   5   6   7   8");

            char[] rows = {'A', 'B', 'C', 'D', 'E'};
            for (char row : rows) {
                System.out.printf("%-4c", row);
                for (int col = 1; col <= 8; col++) {
                    String code = "" + row + col;
                    Seat seat = seatMap.get(code);
                    if (seat != null) {
                        char symbol = (seat.getStatus() == SeatStatus.AVAILABLE) ? 'O' : 'X';
                        System.out.printf("  %c ", symbol);
                    } else {
                        System.out.print("  . ");
                    }
                }
                SeatType type = SeatType.fromRow(row);
                System.out.printf("   [%-7s - Rs.%.0f]%n", type.getDisplayName(), type.getPrice());
            }

            System.out.println("-----------------------------------------------------");
            System.out.println("Legend:  O = Available  |  X = Booked");

            // Stream API calculation of seat availability stats
            long availableCount = seats.stream().filter(Seat::isAvailable).count();
            long bookedCount = seats.size() - availableCount;
            System.out.printf("Summary: %d Available  |  %d Booked  |  Total: %d%n",
                    availableCount, bookedCount, seats.size());
            System.out.println("=====================================================");
            return true;

        } catch (SQLException e) {
            System.err.println("Error fetching seat map: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 4. BOOK TICKETS (ACID Transaction)
    // =========================================================================
    private void handleBookTickets() {
        System.out.println("==================== TICKET BOOKING ====================");

        // Customer details
        String phone = input.readPhoneNumber("Enter Customer Phone Number: ");
        String name = input.readNonEmptyString("Enter Customer Full Name: ");
        Customer customer = new Customer(name, phone);

        // Show selection
        handleViewShows();
        int showId = input.readPositiveInt("Enter Show ID to book: ");

        // Display current seat availability
        if (!displaySeatMap(showId)) {
            return;
        }

        // Seat selection
        List<String> requestedSeats = input.readSeatCodes(
                "Enter seat codes to book (separated by comma, e.g. A1, A2, B3): ");

        // Coupon code input
        System.out.println("Tip: Use coupon 'CINE10' for a 10% discount on total price.");
        String couponCode = input.readOptionalString("Enter Coupon Code (or press Enter to skip): ");
        if (couponCode.isEmpty()) {
            couponCode = null;
        }

        // Execute booking via Service Layer (Transaction Management)
        try {
            System.out.println("\n>> Processing booking transaction in database...");
            Booking booking = bookingService.bookSeats(customer, showId, requestedSeats, couponCode);

            // Print booking confirmation
            printBookingReceipt(booking);

        } catch (SeatNotAvailableException e) {
            System.out.println("\n[BOOKING FAILED] " + e.getMessage());
            System.out.println(">> Please select different seats and try again.");
        } catch (InvalidSeatException e) {
            System.out.println("\n[INVALID SEATS] " + e.getMessage());
        } catch (InvalidCouponException e) {
            System.out.println("\n[COUPON ERROR] " + e.getMessage());
            System.out.println(">> Transaction cancelled. Please re-book with a valid coupon or skip coupon.");
        } catch (CineBookException e) {
            System.out.println("\n[ERROR] Booking could not be completed: " + e.getMessage());
        }
    }

    private void printBookingReceipt(Booking b) {
        System.out.println("\n========================================================");
        System.out.println("               BOOKING CONFIRMATION RECEIPT             ");
        System.out.println("========================================================");
        System.out.printf("  Booking ID    : CBK-%04d%n", b.getBookingId());
        System.out.printf("  Booking Time  : %s%n", b.getBookingTime().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a")));
        System.out.printf("  Customer Name : %s (%s)%n", b.getCustomerName(), b.getCustomerPhone());
        System.out.printf("  Movie         : %s%n", b.getMovieTitle());
        System.out.printf("  Screen        : %s%n", b.getScreenName());
        System.out.printf("  Show Date/Time: %s at %s%n",
                b.getShowDate().format(DATE_FMT),
                b.getShowTime().format(TIME_FMT));
        System.out.println("--------------------------------------------------------");
        System.out.printf("  Seats Booked  : %s (%d seat%s)%n",
                String.join(", ", b.getBookedSeats()),
                b.getBookedSeats().size(),
                b.getBookedSeats().size() > 1 ? "s" : "");
        System.out.printf("  Subtotal      : Rs. %10.2f%n", b.getSubtotal());
        if (b.getDiscountAmount() > 0) {
            System.out.printf("  Coupon (%s): -Rs. %10.2f%n", b.getCouponCode(), b.getDiscountAmount());
        }
        System.out.println("--------------------------------------------------------");
        System.out.printf("  TOTAL PAID    : Rs. %10.2f%n", b.getTotalAmount());
        System.out.printf("  Status        : %s%n", b.getStatus());
        if (b.getStatus() == BookingStatus.CANCELLED) {
            double refund = b.getTotalAmount() * BookingService.CANCELLATION_REFUND_PERCENT;
            System.out.printf("  Refund (80%%)  : Rs. %10.2f%n", refund);
        }
        System.out.println("========================================================");
        System.out.println(">> Keep your Booking ID safe for check-in or cancellation.");
    }

    // =========================================================================
    // 5. VIEW BOOKING DETAILS
    // =========================================================================
    private void handleViewBooking() {
        System.out.println("-------------------- VIEW BOOKING --------------------");
        int bookingId = input.readPositiveInt("Enter Booking ID: ");
        try {
            Optional<Booking> opt = bookingService.getBookingDetails(bookingId);
            if (opt.isEmpty()) {
                System.out.println(">> No booking found with ID: " + bookingId);
                return;
            }
            printBookingReceipt(opt.get());
        } catch (SQLException e) {
            System.err.println("Error looking up booking: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. VIEW BOOKING HISTORY
    // =========================================================================
    private void handleViewBookingHistory() {
        System.out.println("-------------------- CUSTOMER BOOKING HISTORY --------------------");
        String phone = input.readPhoneNumber("Enter Customer Phone Number: ");
        try {
            List<Booking> history = bookingService.getCustomerBookings(phone);
            if (history.isEmpty()) {
                System.out.println(">> No bookings found for phone number: " + phone);
                return;
            }

            System.out.printf("Found %d booking(s) for %s:%n%n", history.size(), phone);
            System.out.printf("%-10s | %-20s | %-12s | %-15s | %-10s | %-10s%n",
                    "Booking ID", "Movie", "Date", "Seats", "Total Paid", "Status");
            System.out.println("--------------------------------------------------------------------------------");
            history.forEach(b -> System.out.printf("CBK-%04d   | %-20s | %-12s | %-15s | Rs.%-7.2f | %-10s%n",
                    b.getBookingId(),
                    b.getMovieTitle(),
                    b.getShowDate().format(DATE_FMT),
                    String.join(",", b.getBookedSeats()),
                    b.getTotalAmount(),
                    b.getStatus()));
            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Error fetching booking history: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. CANCEL BOOKING (ACID Transaction & 80% Refund)
    // =========================================================================
    private void handleCancelBooking() {
        System.out.println("==================== BOOKING CANCELLATION ====================");
        int bookingId = input.readPositiveInt("Enter Booking ID to cancel: ");

        try {
            Optional<Booking> opt = bookingService.getBookingDetails(bookingId);
            if (opt.isEmpty()) {
                System.out.println(">> Booking #" + bookingId + " not found.");
                return;
            }

            Booking booking = opt.get();
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                System.out.println(">> Booking #" + bookingId + " is already CANCELLED.");
                return;
            }

            // Display current booking details before confirming
            double refundAmount = booking.getTotalAmount() * BookingService.CANCELLATION_REFUND_PERCENT;
            System.out.println("\nBooking Details Found:");
            System.out.printf("  Movie        : %s%n", booking.getMovieTitle());
            System.out.printf("  Show Date    : %s %s%n", booking.getShowDate().format(DATE_FMT), booking.getShowTime().format(TIME_FMT));
            System.out.printf("  Seats Booked : %s%n", String.join(", ", booking.getBookedSeats()));
            System.out.printf("  Amount Paid  : Rs. %.2f%n", booking.getTotalAmount());
            System.out.printf("  Refund Policy: 80%% (Rs. %.2f)%n", refundAmount);

            boolean confirm = input.readConfirmation("\nAre you sure you want to cancel this booking?");
            if (!confirm) {
                System.out.println(">> Cancellation aborted by user. Booking remains active.");
                return;
            }

            System.out.println(">> Processing cancellation and refund in database...");
            boolean success = bookingService.cancelBooking(bookingId);

            if (success) {
                System.out.println("\n========== CANCELLATION SUCCESS ==========");
                System.out.printf("  Booking ID       : CBK-%04d%n", bookingId);
                System.out.printf("  Status           : CANCELLED%n");
                System.out.printf("  Original Paid    : Rs. %.2f%n", booking.getTotalAmount());
                System.out.printf("  Refund (80%%)     : Rs. %.2f%n", refundAmount);
                System.out.printf("  Released Seats   : %s%n", String.join(", ", booking.getBookedSeats()));
                System.out.println("===========================================");
                System.out.println(">> All released seats are now immediately available for new bookings.");
            }

        } catch (BookingNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (CineBookException e) {
            System.out.println("[CANCELLATION FAILED] " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error during cancellation: " + e.getMessage());
        }
    }
}

