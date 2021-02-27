package org.openlca.validation;


import java.util.Optional;

import org.openlca.core.model.descriptors.Descriptor;

public class Item {

	public enum Type {
		ERROR,
    WARNING,
    OK,
	}

	public final Type type;
	public final Optional<Descriptor> model;
	public final String message;

	Item (Type type, Descriptor model, String message) {
		this.type = type;
		this.model = Optional.ofNullable(model);
		this.message = message;
	}

  static Item ok(String message) {
    return new Item(Type.OK, null, message);
  }

  static Item error (String message) {
    return new Item(Type.ERROR, null, message);
  }

	static Item error(Descriptor model, String message) {
		return new Item(Type.ERROR, model, message);
	}

	static Item warning(Descriptor model, String message) {
		return new Item(Type.WARNING, model, message);
	}
}
