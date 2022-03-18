package kz.example.demopaypal.service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import kz.example.demopaypal.entity.DemoOrder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class PaypalPaymentService implements PaymentService {

    private final String APPROVE_LINK_REL = "approve";

    private final PayPalHttpClient payPalHttpClient;

    public PaypalPaymentService(@Value("${paypal.client.id}") String clientId,
                                @Value("${paypal.client.secret}") String clientSecret) {
        payPalHttpClient = new PayPalHttpClient(new PayPalEnvironment.Sandbox(clientId, clientSecret));
    }

    @Override
    @SneakyThrows
    public DemoOrder createOrder(Double totalAmount, URI returnUrl) {
        final OrderRequest orderRequest = createOrderRequest(totalAmount, returnUrl);
        final OrdersCreateRequest ordersCreateRequest = new OrdersCreateRequest().requestBody(orderRequest);
        final HttpResponse<Order> orderHttpResponse = payPalHttpClient.execute(ordersCreateRequest);
        final Order order = orderHttpResponse.result();
        LinkDescription approveUri = extractApprovalLink(order);
        return new DemoOrder(order.id(), URI.create(approveUri.href()));
    }

    @Override
    @SneakyThrows
    public void captureOrder(String orderId) {
        final OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(orderId);
        final HttpResponse<Order> httpResponse = payPalHttpClient.execute(ordersCaptureRequest);
        log.info("Order Capture Status: {}", httpResponse.result().status());
    }

    private OrderRequest createOrderRequest(Double totalAmount, URI returnUrl) {
        final OrderRequest orderRequest = new OrderRequest();
        setCheckoutIntent(orderRequest);
        setPurchaseUnits(totalAmount, orderRequest);
        setApplicationContext(returnUrl, orderRequest);
        return orderRequest;
    }

    private void setApplicationContext(URI returnUrl, OrderRequest orderRequest) {
        orderRequest.applicationContext(new ApplicationContext().returnUrl(returnUrl.toString()));
    }

    private void setPurchaseUnits(Double totalAmount, OrderRequest orderRequest) {
        final PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(new AmountWithBreakdown().currencyCode("USD").value(totalAmount.toString()));
        orderRequest.purchaseUnits(List.of(purchaseUnitRequest));
    }

    private void setCheckoutIntent(OrderRequest orderRequest) {
        orderRequest.checkoutPaymentIntent("CAPTURE");
    }

    private LinkDescription extractApprovalLink(Order order) {
        return order.links().stream()
                .filter(link -> APPROVE_LINK_REL.equals(link.rel()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

}
