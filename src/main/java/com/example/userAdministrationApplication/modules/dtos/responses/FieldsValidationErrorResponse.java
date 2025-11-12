package com.example.userAdministrationApplication.modules.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FieldsValidationErrorResponse {
   private Map<String, String> errors;
}
