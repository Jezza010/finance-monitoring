package com.finmonitor.model;


import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    
    public long id;
    public String personType;
    public String transactionType;
    public String dateTime;
    public String comment;
//    public double amount;
    public double sum;
    public String status;
    public String senderBank;
    public String receiverBank;
    public String receiverINN;
    public String receiverAccount;
    public String category;
    public String phone;
}