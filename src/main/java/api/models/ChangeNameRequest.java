package api.models;

import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeNameRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z]{3,7} [A-Za-z]{3,7}$")
    private String name;
}
