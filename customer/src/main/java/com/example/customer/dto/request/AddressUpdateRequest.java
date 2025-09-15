package com.example.customer.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressUpdateRequest(@NotBlank(message = "Street is required")
                                   String street,

                                   @NotBlank(message = "House number is required")
                                   String houseNumber,

                                   @NotBlank(message = "Zip code is required")
                                   String zipCode) {
}
