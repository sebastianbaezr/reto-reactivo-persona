package co.com.bancolombia.r2dbc.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("person_bootcamps")
public class PersonBootcampData {
    private Long personId;
    private Long bootcampId;
}
