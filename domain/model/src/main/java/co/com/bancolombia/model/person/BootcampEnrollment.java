package co.com.bancolombia.model.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootcampEnrollment {
    private Long bootcampId;
    private Integer personCount;
}
