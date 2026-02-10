package co.com.bancolombia.r2dbc.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BootcampEnrollmentData {
    private Long bootcampId;
    private Integer personCount;
}
