package phong.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phong.demo.Entity.Payment;
import phong.demo.Repository.PaymentRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyFinanceService {


    @Autowired
    private PaymentRepository paymentRepository;


    public double getFinance(LocalDate localDate){

        List<Payment> payments = paymentRepository.findAllByDate(localDate);

        Double income = payments.stream().mapToDouble(h->h.getAmount()).sum();

        return income;
    }






}
