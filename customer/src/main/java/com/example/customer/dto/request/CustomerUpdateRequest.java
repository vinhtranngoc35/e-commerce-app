package com.example.customer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerUpdateRequest(@NotBlank(message = "First name is required")
                                    String firstname,

                                    @NotBlank(message = "Last name is required")
                                    String lastname,

                                    @NotBlank(message = "Email is required")
                                    @Email(message = "Email should be valid")
                                    String email,

                                    @Valid
                                    AddressUpdateRequest address) {
}
