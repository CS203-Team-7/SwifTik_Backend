package com.swiftyticket.repositories;
import com.swiftyticket.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer>{
}
