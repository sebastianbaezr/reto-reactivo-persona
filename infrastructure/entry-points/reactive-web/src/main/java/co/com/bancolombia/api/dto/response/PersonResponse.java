package co.com.bancolombia.api.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PersonResponse {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
