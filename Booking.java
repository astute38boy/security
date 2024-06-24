package com.csc3402.lab.avr.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingId;
    private Date start;
    private Date endDate;
    private Date bookDate;
    private String notes;
    private String status;
    private String roomType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private Set<Customer> customers;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private Set<Payment> payments;

    public Booking() {
    }

    public Booking(Date start, Date endDate, Date bookDate, String notes, String status, String roomType, Set<Customer> customers, Set<Payment> payments) {
        this.start = start;
        this.endDate = endDate;
        this.bookDate = bookDate;
        this.notes = notes;
        this.status = status;
        this.roomType = roomType;
        this.customers = customers;
        this.payments = payments;
    }

    public Booking(Date start, Date endDate, Date bookDate, String notes, String status) {
        this.start = start;
        this.endDate = endDate;
        this.bookDate = bookDate;
        this.notes = notes;
        this.status = status;
    }

    // Getters and setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getBookDate() {
        return bookDate;
    }

    public void setBookDate(Date bookDate) {
        this.bookDate = bookDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Set<Customer> customers) {
        this.customers = customers;
    }

    public Set<Payment> getPayments() {
        return payments;
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", start=" + start +
                ", endDate=" + endDate +
                ", bookDate=" + bookDate +
                ", notes='" + notes + '\'' +
                ", status='" + status + '\'' +
                ", roomType='" + roomType + '\'' +
                ", customers=" + customers +
                ", payments=" + payments +
                ", user=" + user +
                '}';
    }
}
