package org.openlca.validation;


import java.util.Optional;

import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.LoggerFactory;

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

  static Item error(String message, Throwable err) {
    var m = message + ": " + err.getMessage();
    var log = LoggerFactory.getLogger(Validation.class);
    log.error(message, err);
    return error(m);
  }

	static Item error(Descriptor model, String message) {
		return new Item(Type.ERROR, model, message);
	}

	static Item warning(Descriptor model, String message) {
		return new Item(Type.WARNING, model, message);
	}
}
