package com.example.wigellssushi.controller;

import com.example.wigellssushi.VO.Customer;
import com.example.wigellssushi.model.Booking;
import com.example.wigellssushi.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v3")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookroom")
    @PreAuthorize("hasRole('USER')")
    public Booking bookRoom(@RequestBody Booking booking) {
        return bookingService.addBooking(booking);
    }

    @PutMapping("/updatebooking/{id}")
    @PreAuthorize("hasRole('USER')")
    public Booking updateBooking(@PathVariable Long id, @RequestBody Booking bookingDetails) {
        return bookingService.updateBooking(id, bookingDetails);
    }

    @GetMapping("/mybookings")
    @PreAuthorize("hasRole('USER')")
    public List<Booking> getBookingsByUser(@RequestBody Customer customerDetails) {
        return bookingService.getBookingsByUser(customerDetails);
    }
}
