package com.swiftyticket.services;

import java.util.List;

import com.swiftyticket.models.Payment;

public interface PaymentService {
    List<Payment> listPayments();
    Payment getPayment(Integer id);
    Payment addPayment(Payment payment);
    Payment updatePayment(Integer id, Payment payment);
    void deletePayment(Integer id);
}
