package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PersonRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder de 50 caracteres")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder de 50 caracteres")
    private String lastname;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser valido")
    @Size(max = 100, message = "El email no puede exceder de 100 caracteres")
    private String email;

    @NotNull(message = "Bootcamp IDs es obligatorio")
    @Size(min = 1, max = 4, message = "Debe proveer entre 1 y 4 bootcamps")
    private List<Long> bootcampIds;
}
