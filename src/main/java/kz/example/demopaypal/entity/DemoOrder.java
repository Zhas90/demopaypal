package kz.example.demopaypal.entity;

import lombok.Data;
import java.net.URI;

@Data
public class DemoOrder {

    private final String orderId;
    private final URI approvalLink;

}
