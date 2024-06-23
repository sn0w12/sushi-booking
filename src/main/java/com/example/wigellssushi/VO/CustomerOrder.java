package com.example.wigellssushi.VO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Entity
@JsonIgnoreProperties({"customer"})
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "total_price_sek")
    private double totalPriceSEK;
    @Column(name = "total_price_euro")
    private double totalPriceEuro;

    @ManyToMany
    @JoinTable(
            name = "order_dish",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id")
    )
    private List<Dish> dishes;

    @Transient
    private RestTemplate restTemplate;

    public CustomerOrder() {
    }

    public CustomerOrder(Long id, Customer customer, double totalPriceSEK, double totalPriceEuro, List<Dish> dishes, boolean takeaway) {
        this.id = id;
        this.customer = customer;
        this.totalPriceSEK = totalPriceSEK;
        this.totalPriceEuro = totalPriceEuro;
        this.dishes = dishes;
        this.takeaway = takeaway;
    }

    boolean takeaway;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public double getTotalPriceSEK() {
        return totalPriceSEK;
    }

    public void setTotalPriceSEK(double totalPriceSEK) { this.totalPriceSEK = totalPriceSEK; }

    public double getTotalPriceEuro() {
        return totalPriceEuro;
    }

    public void setTotalPriceEuro(double totalPriceEuro) {
        this.totalPriceEuro = totalPriceEuro;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public boolean isTakeaway() {
        return takeaway;
    }

    public void setTakeaway(boolean takeaway) {
        this.takeaway = takeaway;
    }
}
