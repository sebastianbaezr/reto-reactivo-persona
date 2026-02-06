package co.com.bancolombia.model.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import co.com.bancolombia.model.common.AuditableModel;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Person extends AuditableModel {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private List<Long> bootcampIds;
}
