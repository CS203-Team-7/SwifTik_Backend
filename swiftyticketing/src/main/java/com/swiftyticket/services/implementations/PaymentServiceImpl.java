package com.swiftyticket.services.implementations;
import java.util.List;
import java.util.Optional;

import com.swiftyticket.exceptions.PaymentNotFoundException;
import com.swiftyticket.models.Payment;
import com.swiftyticket.repositories.PaymentRepository;
import com.swiftyticket.services.PaymentService;

public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepo;

    public PaymentServiceImpl(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public List<Payment> listPayments() {
        return paymentRepo.findAll();
    }

    @Override
    public Payment getPayment(Integer id) {
        return paymentRepo.findById(id).map(payment -> {return payment;}).orElse(null);
    }

    @Override
    public Payment addPayment(Payment payment) {
        return paymentRepo.save(payment);
    }

    public Payment updatePayment(Integer id, Payment newPaymentInfo) {
        return paymentRepo.findById(id).map(payment -> {
            payment.setId(payment.getId());
            payment.setAmountPaid(payment.getAmountPaid());
            payment.setPurchaseDate(payment.getPurchaseDate());
            return paymentRepo.save(payment);
        }).orElse(null);
    }

    @Override
    public void deletePayment(Integer id){
        Optional<Payment> p = paymentRepo.findById(id);
        if (p == null) throw new PaymentNotFoundException(id);
        paymentRepo.deleteById(id);
    }




}
