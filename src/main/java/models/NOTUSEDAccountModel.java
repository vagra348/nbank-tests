package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

// Я попыталась создать модель сущности из списка, но не до конца понимаю, как вписать их в цепочку ассертов типа
//          .extract().as(GetUserAccountsResponse.class);
//          softly.assertThat(getUserAccountsResponse.getAccounts().getId).isNotEmpty();

public class NOTUSEDAccountModel {
    private int id;
    private String accountNumber;
    private double balance;
    private List<Object> transactions;


//    "transactions": [
//            {
//                "id": 19,
//                    "amount": 990.0,
//                    "type": "TRANSFER_IN",
//                    "timestamp": "Thu Oct 16 14:59:14 UTC 2025",
//                    "relatedAccountId": 4
//            }
//        ]
//    }
}
