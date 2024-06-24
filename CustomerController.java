package com.csc3402.lab.avr.controller;

import com.csc3402.lab.avr.model.*;
import com.csc3402.lab.avr.repository.*;
import com.csc3402.lab.avr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/")
public class CustomerController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.findUserByEmail(user.getEmail()) != null) {
            result.rejectValue("email", "error.user", "An account already exists for this email.");
            return "register";
        }

        userService.saveUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "signin";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("loginForm") LoginForm loginForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "signin";
        }

        User user = userService.findUserByEmail(loginForm.getEmail());
        if (user != null && user.getPassword().equals(loginForm.getPassword())) {
            return "redirect:/index.html";
        } else {
            model.addAttribute("error", "Invalid Credentials provided.");
            return "signin";
        }
    }

    public static class LoginForm {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);
        return "index";
    }

    @GetMapping("/customers/list")
    public String showCustomerList(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "list-customer";
    }

    @GetMapping("/customers/signup")
    public String showSignUpForm(Customer customer) {
        return "register";
    }

    @PostMapping("/customers/add")
    public String addCustomer(@Valid Customer customer, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        customerRepository.save(customer);
        return "redirect:/customers/list";
    }

    @GetMapping("/customers/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Customer customer = customerRepository.findById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
        model.addAttribute("customer", customer);
        return "update-customer";
    }

    @PostMapping("/customers/update/{id}")
    public String updateCustomer(@PathVariable("id") long id, @Valid Customer customer, BindingResult result, Model model) {
        if (result.hasErrors()) {
            customer.setCustid((int) id);
            return "update-customer";
        }
        customerRepository.save(customer);
        return "redirect:/customers/list";
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable("id") long id, Model model) {
        Customer customer = customerRepository.findById((int) id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
        customerRepository.delete(customer);
        return "redirect:/customers/list";
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam("selectedRoom") String selectedRoom,
                           @RequestParam("checkin") String checkin,
                           @RequestParam("checkout") String checkout,
                           @RequestParam("counterValueAdult") int counterValueAdult,
                           @RequestParam("counterValueChild") int counterValueChild,
                           Model model) {
        Room room = roomRepository.findByRoomType(selectedRoom);
        if (room == null) {
            model.addAttribute("errorMessage", "Invalid room type selected");
            return "error";
        }

        LocalDate checkinDate = LocalDate.parse(checkin);
        LocalDate checkoutDate = LocalDate.parse(checkout);
        long daysBetween = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
        double totalPrice = room.getPrice() * daysBetween;

        model.addAttribute("selectedRoom", selectedRoom);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("counterValueAdult", counterValueAdult);
        model.addAttribute("counterValueChild", counterValueChild);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("payment", new Payment());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String addPayment(@Valid Payment payment, BindingResult result, Model model,
                             @RequestParam("selectedRoom") String selectedRoom,
                             @RequestParam("checkin") String checkin,
                             @RequestParam("checkout") String checkout) {
        if (result.hasErrors()) {
            return "checkout";
        }

        Room room = roomRepository.findByRoomType(selectedRoom);
        if (room == null) {
            result.rejectValue("roomType", "error.roomType", "Invalid room type selected");
            return "checkout";
        }

        LocalDate checkinDate = LocalDate.parse(checkin);
        LocalDate checkoutDate = LocalDate.parse(checkout);

        long daysBetween = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
        double totalPrice = room.getPrice() * daysBetween;

        payment.setPaymentDate(new Date());
        payment.setTotalPrice(totalPrice);
        payment.setCheckinDate(Date.from(checkinDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        payment.setCheckoutDate(Date.from(checkoutDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Booking booking = new Booking();
        booking.setStart(Date.from(checkinDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        booking.setEndDate(Date.from(checkoutDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        booking.setNotes(payment.getCardholderName()); // Using cardholder name as guest name
        booking.setStatus("Confirmed");
        booking.setRoomType(selectedRoom);  // Assuming this field exists in Booking
        bookingRepository.save(booking);

        payment.setBooking(booking);
        paymentRepository.save(payment);

        return "redirect:/confirmation?bookingId=" + booking.getBookingId();
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam Integer bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            model.addAttribute("errorMessage", "Booking not found");
            return "error";
        }

        List<Customer> customers = customerRepository.findByBooking_BookingId(bookingId);
        Customer customer = customers.isEmpty() ? null : customers.get(0);

        model.addAttribute("booking", booking);
        model.addAttribute("customer", customer);
        return "bookingconfirmation";
    }
}
