package com.amadeus.aud.libraries.checks.domain;

import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.types.DomainCheck;
import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
public final class ModelsAnnotations implements DomainCheck, WithId {
  private static final CharSequence CODE = DomainNumbers.DOM00001.name();

  @Override
  public String id() {
    return CODE.toString();
  }

  @Override
  public Collection<Message> check(
      CharSequence domain, Map<Class<? extends ModelElement>, List<ModelElement>> domainsData) {
    Collection<Message> messages = new ArrayList<>();

    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = factory.getValidator();

      for (ModelElement element : domainsData.values().stream().flatMap(List::stream).toList()) {
        for (ConstraintViolation<ModelElement> violation :
            validator.validateProperty(element, "apiVersion")) {
          messages.add(message("", element, violation));
        }
        for (ConstraintViolation<V1Metadata> violation :
            validator.validate(element.getMetadata())) {
          messages.add(message("metadata.", element, violation));
        }

        if (element.getSpec() != null) {
          for (ConstraintViolation<Object> violation : validator.validate(element.getSpec())) {
            messages.add(message("spec.", element, violation));
          }
        }
      }
    }

    return messages;
  }

  private static Message message(
      CharSequence prefix, ModelElement element, ConstraintViolation<?> violation) {
    String propertyPath = violation.getPropertyPath().toString();
    String message = violation.getMessage();
    return Message.errorWithDetails(CODE, prefix + propertyPath + " " + message, (V1Base) element);
  }
}
