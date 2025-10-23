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
public class NOTUSEDGetUserAccountsResponse extends BaseModel{

// Сложная структура json со списками объектов, не получилось реализовать пока, думаю, может, в следующем видео будет?

    private List<NOTUSEDAccountModel> accounts;

}
