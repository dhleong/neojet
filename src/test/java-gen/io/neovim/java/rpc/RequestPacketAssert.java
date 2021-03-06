package io.neovim.java.rpc;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * {@link RequestPacket} specific assertions - Generated by CustomAssertionGenerator.
 */
public class RequestPacketAssert extends AbstractAssert<RequestPacketAssert, RequestPacket> {

  /**
   * Creates a new <code>{@link RequestPacketAssert}</code> to make assertions on actual RequestPacket.
   * @param actual the RequestPacket we want to make assertions on.
   */
  public RequestPacketAssert(RequestPacket actual) {
    super(actual, RequestPacketAssert.class);
  }

  /**
   * An entry point for RequestPacketAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
   * With a static import, one can write directly: <code>assertThat(myRequestPacket)</code> and get specific assertion with code completion.
   * @param actual the RequestPacket we want to make assertions on.
   * @return a new <code>{@link RequestPacketAssert}</code>
   */
  public static RequestPacketAssert assertThat(RequestPacket actual) {
    return new RequestPacketAssert(actual);
  }

  /**
   * Verifies that the actual RequestPacket's args is equal to the given one.
   * @param args the given args to compare the actual RequestPacket's args to.
   * @return this assertion object.
   * @throws AssertionError - if the actual RequestPacket's args is not equal to the given one.
   */
  public RequestPacketAssert hasArgs(Object args) {
    // check that actual RequestPacket we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpecting args of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    Object actualArgs = actual.args;
    if (!Objects.areEqual(actualArgs, args)) {
      failWithMessage(assertjErrorMessage, actual, args, actualArgs);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual RequestPacket's method is equal to the given one.
   * @param method the given method to compare the actual RequestPacket's method to.
   * @return this assertion object.
   * @throws AssertionError - if the actual RequestPacket's method is not equal to the given one.
   */
  public RequestPacketAssert hasMethod(String method) {
    // check that actual RequestPacket we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpecting method of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualMethod = actual.method;
    if (!Objects.areEqual(actualMethod, method)) {
      failWithMessage(assertjErrorMessage, actual, method, actualMethod);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual RequestPacket's requestId is equal to the given one.
   * @param requestId the given requestId to compare the actual RequestPacket's requestId to.
   * @return this assertion object.
   * @throws AssertionError - if the actual RequestPacket's requestId is not equal to the given one.
   */
  public RequestPacketAssert hasRequestId(int requestId) {
    // check that actual RequestPacket we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpecting requestId of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // check
    int actualRequestId = actual.requestId;
    if (actualRequestId != requestId) {
      failWithMessage(assertjErrorMessage, actual, requestId, actualRequestId);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual RequestPacket's type is equal to the given one.
   * @param type the given type to compare the actual RequestPacket's type to.
   * @return this assertion object.
   * @throws AssertionError - if the actual RequestPacket's type is not equal to the given one.
   */
  public RequestPacketAssert hasType(Packet.Type type) {
    // check that actual RequestPacket we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpecting type of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    Packet.Type actualType = actual.type;
    if (!Objects.areEqual(actualType, type)) {
      failWithMessage(assertjErrorMessage, actual, type, actualType);
    }

    // return the current assertion for method chaining
    return this;
  }

}
