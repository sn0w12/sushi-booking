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
            Room room = restTemplate.getForObject("http://room-service/api/v3/rooms/" + booking.getRoom().getId(), Room.class);
            if (room == null) {
                throw new RuntimeException("Room not found with id " + booking.getRoom().getId());
            }
            booking.setRoom(room);
        }

        double totalDishPriceSek = 0;

        CustomerOrder customerOrder = booking.getCustomerOrder();
        if (customerOrder != null) {
            customerOrder.setCustomer(booking.getCustomer());
            List<Dish> dishes = new ArrayList<>();

            for (Dish dish : customerOrder.getDishes()) {
                if (dish.getId() != null) {
                    Dish existingDish = restTemplate.getForObject("http://dish-service/api/v3/sushis/" + dish.getId(), Dish.class);
                    if (existingDish == null) {
                        throw new RuntimeException("Dish not found with id " + dish.getId());
                    }
                    dishes.add(existingDish);
                    totalDishPriceSek += existingDish.getPriceSEK();
                } else {
                    throw new RuntimeException("Dish must have an ID");
                }
            }

            customerOrder.setDishes(dishes);
        } else {
            customerOrder = new CustomerOrder();
            customerOrder.setCustomer(booking.getCustomer());
            customerOrder.setDishes(new ArrayList<>());
            booking.setCustomerOrder(customerOrder);
        }

        if (booking.getCustomerOrder().getTotalPriceSEK() == 0.0) {
            booking.getCustomerOrder().setTotalPriceSEK(totalDishPriceSek);
            booking.getCustomerOrder().setTotalPriceEuro(restTemplate.getForObject("http://currency-service/api/v3/sektoeuro?amountInSEK=" + totalDishPriceSek, double.class));
        }

        if (booking.getTotalPriceSEK() == 0.0) {
            booking.setTotalPriceSEK(totalDishPriceSek);
            booking.setTotalPriceEuro(restTemplate.getForObject("http://currency-service/api/v3/sektoeuro?amountInSEK=" + totalDishPriceSek, double.class));
        } else {
            booking.setTotalPriceEuro(restTemplate.getForObject("http://currency-service/api/v3/sektoeuro?amountInSEK=" + booking.getTotalPriceSEK(), double.class));
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
                existingBooking.setTotalPriceEuro(restTemplate.getForObject("http://currency-service/api/v3/sektoeuro?amountInSEK=" + bookingDetails.getTotalPriceSEK(), double.class));
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
