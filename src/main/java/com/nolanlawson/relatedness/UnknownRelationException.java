package com.nolanlawson.relatedness;

/**
 * Exception thrown when the parser can't figure out a relation
 * @author nolan
 *
 */
@SuppressWarnings("serial")
public class UnknownRelationException extends RuntimeException {

	public UnknownRelationException(String message) {
		super(message);
	}
}
