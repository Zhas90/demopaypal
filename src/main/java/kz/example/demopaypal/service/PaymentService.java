package kz.example.demopaypal.service;

import kz.example.demopaypal.entity.DemoOrder;

import java.net.URI;

public interface PaymentService {

    DemoOrder createOrder(Double totalAmount, URI returnUrl);

    void captureOrder(String orderId);

}