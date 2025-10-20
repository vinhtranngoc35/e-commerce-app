package com.example.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "order.topics")
public class OrderTopicsProperties {
    private String created;
    private String rejected;
    private String paymentCompleted;
    private String paymentFailed;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getRejected() {
        return rejected;
    }

    public void setRejected(String rejected) {
        this.rejected = rejected;
    }

    public String getPaymentCompleted() {
        return paymentCompleted;
    }

    public void setPaymentCompleted(String paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }

    public String getPaymentFailed() {
        return paymentFailed;
    }

    public void setPaymentFailed(String paymentFailed) {
        this.paymentFailed = paymentFailed;
    }
}


