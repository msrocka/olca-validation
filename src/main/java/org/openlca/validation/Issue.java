package org.openlca.validation;


import java.util.Optional;

import org.openlca.core.model.descriptors.Descriptor;

public class Issue {

	public enum Type {
		ERROR,
    WARNING,
    OK,
	}

	public final Type type;
	public final Optional<Descriptor> model;
	public final String message;

	Issue (Type type, Descriptor model, String message) {
		this.type = type;
		this.model = Optional.ofNullable(model);
		this.message = message;
	}

  static Issue ok(String message) {
    return new Issue(Type.OK, null, message);
  }

	static Issue error(Descriptor model, String message) {
		return new Issue(Type.ERROR, model, message);
	}

	static Issue warning(Descriptor model, String message) {
		return new Issue(Type.WARNING, model, message);
	}
}
