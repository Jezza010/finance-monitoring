package com.finmonitor.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Transaction {
    public Long id;
    public String personType;
    public String transactionType;
    public String category;
    public String dateTime;
    public String comment;
    public BigDecimal amount;
    public String status;
    public String senderBank;
    public String receiverBank;
    public String accountNumber;
    public String receiverAccountNumber;
    public String receiverINN;
    public String receiverPhone;
}