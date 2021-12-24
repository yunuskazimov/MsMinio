package az.xazar.msminio.model.clinet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String surname;
    private String fatherName;
    private LocalDate birthday;
    private String position;
    private String department;
    private String phoneNumber;
    private boolean isDeleted;
}
