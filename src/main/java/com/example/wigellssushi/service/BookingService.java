package com.example.wigellssushi.service;

import com.example.wigellssushi.VO.Customer;
import com.example.wigellssushi.VO.CustomerOrder;
import com.example.wigellssushi.VO.Dish;
import com.example.wigellssushi.VO.Room;
import com.example.wigellssushi.model.Booking;
import com.example.wigellssushi.exceptions.ResourceNotFoundException;
import com.example.wigellssushi.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking addBooking(Booking booking) {
        if (booking.getRoom() != null) {
            Room room = restTemplate.getForObject("http://room-service/rooms/" + booking.getRoom().getId(), Room.class);
            if (room == null) {
                throw new RuntimeException("Room not found with id " + booking.getRoom().getId());
            }
            booking.setRoom(room);
        }

        double totalPriceSek = 0;

        CustomerOrder customerOrder = booking.getCustomerOrder();
        if (customerOrder != null) {
            customerOrder.setCustomer(booking.getCustomer());
            List<Dish> dishes = new ArrayList<>();

            for (Dish dish : customerOrder.getDishes()) {
                if (dish.getId() != null) {
                    Dish existingDish = restTemplate.getForObject("http://dish-service/sushis/" + dish.getId(), Dish.class);
                    if (existingDish == null) {
                        throw new RuntimeException("Dish not found with id " + dish.getId());
                    }
                    dishes.add(existingDish);
                    totalPriceSek += existingDish.getPriceSEK();
                } else {
                    throw new RuntimeException("Dish must have either an ID or a name");
                }
            }

            customerOrder.setDishes(dishes);
        } else {
            throw new RuntimeException("Booking must have a CustomerOrder");
        }

        if (booking.getCustomerOrder().getTotalPriceSEK() == 0.0) {
            booking.getCustomerOrder().setTotalPriceSEK(totalPriceSek);
        }

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking with id: " + savedBooking.getId() + " successfully added.");
        return savedBooking;
    }

    public Booking updateBooking(Long id, Booking bookingDetails) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            Booking existingBooking = optionalBooking.get();

            // Update fields if they are not null
            if (bookingDetails.getCustomer() != null) {
                existingBooking.setCustomer(bookingDetails.getCustomer());
            }
            if (bookingDetails.getNumberOfGuests() != 0) {
                existingBooking.setNumberOfGuests(bookingDetails.getNumberOfGuests());
            }
            if (bookingDetails.getDate() != null) {
                existingBooking.setDate(bookingDetails.getDate());
            }
            if (bookingDetails.getTotalPriceSEK() != 0) {
                existingBooking.setTotalPriceSEK(bookingDetails.getTotalPriceSEK());
            }
            if (bookingDetails.getTotalPriceEuro() != 0) {
                existingBooking.setTotalPriceEuro(bookingDetails.getTotalPriceEuro());
            }
            if (bookingDetails.getRoom() != null) {
                existingBooking.setRoom(bookingDetails.getRoom());
            }
            if (bookingDetails.getCustomerOrder() != null) {
                existingBooking.setCustomerOrder(bookingDetails.getCustomerOrder());
            }

            Booking updatedBooking = bookingRepository.save(existingBooking);
            logger.info("Booking with id: " + updatedBooking.getId() + " successfully updated.");
            return updatedBooking;
        } else {
            throw new ResourceNotFoundException("Booking not found with id " + id);
        }
    }

    public List<Booking> getBookingsByUser(Customer customer) {
        return bookingRepository.findByCustomerId(customer.getId());
    }
}
