package edu.convergence.dto.customer;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTO {
    @NotBlank(message = "Customer name is required")
    private String name;
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    @NotBlank(message = "NIC is required")
    @Pattern(regexp = "^(?:\\d{9}[VvXx]|\\d{12})$", message = "NIC must be either 9 digits followed by 'V' or 'X', or 12 digits")
    private String nic;
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^0\\d{9}$",message = "Mobile number must start with 0 and be exactly 10 digits")
    private List<String> mobileNumbers;
    private List<CustomerDTO> familyMembers;
    private List<CustomerAddressDTO> addresses;
}
