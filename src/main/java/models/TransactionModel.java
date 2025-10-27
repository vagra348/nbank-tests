package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionModel {
    private Integer id;
    private Double amount;
    private String type;

    @JsonFormat(pattern = "EEE MMM dd HH:mm:ss zzz yyyy", locale = "en_US")
    private Date timestamp;

    private Integer relatedAccountId;
}
