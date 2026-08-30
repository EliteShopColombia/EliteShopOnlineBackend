package com.eliteshop.colombia.seller.infrastructure.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRequest {

  @NotBlank(message = "El tipo de comercio es obligatorio")
  private String typeTrade;

  @NotBlank(message = "El tipo de documento es obligatorio")
  private String typeDni;

  @NotBlank(message = "El número de documento es obligatorio")
  @Size(min = 5, max = 20, message = "El número de documento debe tener entre 5 y 20 caracteres")
  private String dniNumber;

  @NotBlank(message = "El nombre comercial es obligatorio")
  @Size(min = 2, max = 100, message = "El nombre comercial debe tener entre 2 y 100 caracteres")
  private String tradeName;

  @NotBlank(message = "El nombre completo es obligatorio")
  @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
  private String fullname;

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email debe ser válido")
  private String email;

  @NotBlank(message = "El teléfono es obligatorio")
  private String phoneNumber;

  @NotBlank(message = "La dirección es obligatoria")
  @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
  private String tradeAddress;

  @NotBlank(message = "El departamento es obligatorio")
  @Size(min = 2, max = 60, message = "El departamento debe tener entre 2 y 60 caracteres")
  private String tradeDepartment;

  @NotBlank(message = "La ciudad es obligatoria")
  @Size(min = 2, max = 60, message = "La ciudad debe tener entre 2 y 60 caracteres")
  private String tradeCity;

  @NotBlank(message = "El nombre del banco es obligatorio")
  private String bankName;

  @NotBlank(message = "El tipo de cuenta es obligatorio")
  private String typeBankAccount;

  @NotBlank(message = "El número de cuenta es obligatorio")
  private String numberAccount;
}
