package tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.clients.TDHttpRequest;
import ch.unisg.ics.interactions.wot.td.clients.TDHttpResponse;
import ch.unisg.ics.interactions.wot.td.io.TDGraphReader;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

/**
 * A CArtAgO artifact that can interpret a W3C WoT Thing Description (TD) and exposes the affordances 
 * of the described Thing to agents. The artifact uses the hypermedia controls provided in the TD to
 * compose and issue HTTP requests for the exposed affordances.
 * 
 * Contributors:
 * - Andrei Ciortea (author), Interactions-HSG, University of St. Gallen
 *
 */
public class ThingArtifact extends Artifact {
  private ThingDescription td;
  private boolean dryRun;
  
  /**
   * Method called by CArtAgO to initialize the artifact. The W3C WoT Thing Description (TD) used by
   * this artifact is retrieved and parsed during initialization. 
   * 
   * @param url A URL that dereferences to a W3C WoT Thing Description.
   */
  public void init(String url) {
    try {
     this.td = TDGraphReader.readFromURL(TDFormat.RDF_TURTLE, url);
    } catch (IOException e) {
      failed(e.getMessage());
    }
    
    this.dryRun = false;
  }
  
  /**
   * Method called by CArtAgO to initialize the artifact. The W3C WoT Thing Description (TD) used by
   * this artifact is retrieved and parsed during initialization.
   * 
   * @param url A URL that dereferences to a W3C WoT Thing Description.
   * @param dryRun When set to true, the requests are logged, but not executed.
   */
  public void init(String url, boolean dryRun) {
    init(url);
    this.dryRun = dryRun;
  }
  
  /**
   * CArtAgO operation for reading a property of a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the property type.
   * @param output The read value. Can be a list of one or more primitives, or a nested list of
   * primitives or arbitrary depth.
   */
  @OPERATION
  public void readProperty(String semanticType, OpFeedbackParam<Object[]> output) {
    readProperty(semanticType, Optional.empty(), output);
  }
  
  /**
   * CArtAgO operation for reading a property of a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the property type.
   * @param tags A list of IRIs, used if the property is an object schema.
   * @param output The read value. Can be a list of one or more primitives, or a nested list of
   * primitives or arbitrary depth.
   */
  @OPERATION
  public void readProperty(String semanticType, OpFeedbackParam<Object[]> tags, 
      OpFeedbackParam<Object[]> output) {
    readProperty(semanticType, Optional.of(tags), output);
  }
  
  /**
   * CArtAgO operation for writing a property of a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the property type.
   * @param tags A list of IRIs that identify parameters sent in the payload. Used for object schemas.
   * @param payload The payload to be issued when writing the property.
   */
  @OPERATION
  public void writeProperty(String semanticType, Object[] tags, Object[] payload) {
    validateParameters(semanticType, tags, payload);
    if (payload.length == 0) {
      failed("The payload used when writing a property cannot be empty.");
    }
    
    PropertyAffordance property = getFirstPropertyOrFail(semanticType);
    Optional<TDHttpResponse> response = executePropertyRequest(property, TD.writeProperty, tags, 
        payload);
    
    if (response.isPresent() && response.get().getStatusCode() != 200) {
      failed("Status code: " + response.get().getStatusCode());
    }
  }
  
  /**
   * CArtAgO operation for writing a property of a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the property type.
   * @param payload The payload to be issued when writing the property.
   */
  @OPERATION
  public void writeProperty(String semanticType, Object[] payload) {
    writeProperty(semanticType, new Object[0], payload);
  }
  
  /**
   * CArtAgO operation for invoking an action on a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the action type.
   * @param tags A list of IRIs that identify parameters sent in the payload. Used for object schemas.
   * @param payload The payload to be issued when invoking the action.
   */
  @OPERATION
  public void invokeAction(String semanticType, Object[] tags, Object[] payload) {
    validateParameters(semanticType, tags, payload);
    
    Optional<ActionAffordance> action = td.getFirstActionBySemanticType(semanticType);
    
    if (action.isPresent()) {
      Optional<Form> form = action.get().getFirstForm();
      
      if (!form.isPresent()) {
        // Should not happen (an exception will be raised by the TD library first)
        failed("Invalid TD: the invoked action does not have a valid form.");
      }
      
      Optional<DataSchema> inputSchema = action.get().getInputSchema();
      if (!inputSchema.isPresent() && payload.length > 0) {
        failed("This type of action does not take any input: " + semanticType);
      }
      
      Optional<TDHttpResponse> response = executeRequest(TD.invokeAction, form.get(), inputSchema, 
          tags, payload);
      
      if (response.isPresent() && response.get().getStatusCode() != 200) {
        failed("Status code: " + response.get().getStatusCode());
      }
    } else {
      failed("Unknown action: " + semanticType);
    }
  }
  
  /**
   * CArtAgO operation for invoking an action on a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the action type.
   * @param payload The payload to be issued when invoking the action.
   */
  @OPERATION
  public void invokeAction(String semanticType, Object[] payload) {
    invokeAction(semanticType, new Object[0], payload);
  }
  
  private void validateParameters(String semanticType, Object[] tags, Object[] payload) {
    // TODO: validate IRIs for semanticType and tags
    if (tags.length > 0 && tags.length != payload.length) {
      failed("Illegal arguments: the lists of tags and action parameters should have equal length.");
    }
  }
  
  private void readProperty(String semanticType, Optional<OpFeedbackParam<Object[]>> tags, 
      OpFeedbackParam<Object[]> output) {
    PropertyAffordance property = getFirstPropertyOrFail(semanticType);
    Optional<TDHttpResponse> response = executePropertyRequest(property, TD.readProperty, 
        new Object[0], new Object[0]);
    
    if (!dryRun) {
      if (!response.isPresent()) {
        failed("Something went wrong with the read property request.");
      }
      
      // Using numeric values here to avoid adding a dependency to the JaCaMo project
      if (response.get().getStatusCode() == 200) {
        readPayloadWithSchema(response.get(), property.getDataSchema(), tags, output);
      } else {
        failed("Status code: " + response.get().getStatusCode());
      }
    }
  }
  
  private PropertyAffordance getFirstPropertyOrFail(String semanticType) {
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(semanticType);
    
    if (!property.isPresent()) {
      failed("Unknown property: " + semanticType);
    }
    
    return property.get();
  }
  
  // TODO: Reading payloads of type object currently works with 2 limitations:
  // - only one semantic tag is retrieved for object properties (one that is not a data schema)
  // - we cannot use nested objects with the current JaCa bridge
  @SuppressWarnings("unchecked")
  private void readPayloadWithSchema(TDHttpResponse response, DataSchema schema, 
      Optional<OpFeedbackParam<Object[]>> tags, OpFeedbackParam<Object[]> output) {
    
    switch (schema.getDatatype()) {
      case DataSchema.BOOLEAN:
        output.set(new Boolean[] { response.getPayloadAsBoolean() });
        break;
      case DataSchema.STRING:
        output.set(new String[] { response.getPayloadAsString() });
        break;
      case DataSchema.INTEGER:
        output.set(new Integer[] { response.getPayloadAsInteger() });
        break;
      case DataSchema.NUMBER:
        output.set(new Double[] { response.getPayloadAsDouble() });
        break;
      case DataSchema.OBJECT:
        // Only consider this case if the invoked CArtAgO operation was for an object payload
        // (i.e., a list of tags is expected).
        if (tags.isPresent()) {
          Map<String, Object> payload = response.getPayloadAsObject((ObjectSchema) schema);
          List<String> tagList = new ArrayList<String>();
          List<Object> data = new ArrayList<Object>();
          
          for (String tag : payload.keySet()) {
            tagList.add(tag);
            Object value = payload.get(tag);
            if (value instanceof Collection<?>) {
              data.add(nestedListsToArrays((Collection<Object>) value));
            } else {
              data.add(value);
            }
          }
          
          tags.get().set(tagList.toArray());
          output.set(data.toArray());
        }
        break;
      case DataSchema.ARRAY:
        List<Object> payload = response.getPayloadAsArray((ArraySchema) schema);
        output.set(nestedListsToArrays(payload));
        break;
      default:
        break;
    }
  }
  
  @SuppressWarnings("unchecked")
  private Object[] nestedListsToArrays(Collection<Object> data) {
    Object[] out = data.toArray();
    
    for (int i = 0; i < out.length; i ++) {
      if (out[i] instanceof Collection<?>) {
        out[i] = nestedListsToArrays((Collection<Object>) out[i]);
      }
    }
    
    return out;
  }
  
  private Optional<TDHttpResponse> executePropertyRequest(PropertyAffordance property, 
    String operationType, Object[] tags, Object[] payload) {
    Optional<Form> form = property.getFirstFormForOperationType(operationType);
    
    if (!form.isPresent()) {
      // Should not happen (an exception will be raised by the TD library first)
      failed("Invalid TD: the property does not have a valid form.");
    }
    
    DataSchema schema = property.getDataSchema();
    
    return executeRequest(operationType, form.get(), Optional.of(schema), tags, payload);
  }
  
  private Optional<TDHttpResponse> executeRequest(String operationType, Form form, 
      Optional<DataSchema> schema, Object[] tags, Object[] payload) {
    if (schema.isPresent() && payload.length > 0) {
      // Request with payload
      if (tags.length > 0) {
        return executeRequestObjectPayload(operationType, form, schema.get(), tags, payload);
      } else if (payload.length == 1 && !(payload[0] instanceof Object[])) {
        return executeRequestPrimitivePayload(operationType, form, schema.get(), payload[0]);
      } else if (payload.length >= 1) {
        return executeRequestArrayPayload(operationType, form, schema.get(), payload);
      } else {
        failed("Could not detect the type of payload (primitive, object, or array).");
        return Optional.empty();
      }
    } else {
      // Request without payload
      TDHttpRequest request = new TDHttpRequest(form, operationType);
      
      if (this.dryRun) {
        log(request.toString());
        return Optional.empty();
      } else {
        return issueRequest(request);
      }
    }
  }
  
  /* Request with primitive payload: Boolean, Number, or String */
  private Optional<TDHttpResponse> executeRequestPrimitivePayload(String operationType, Form form, 
      DataSchema schema, Object payload) {
    TDHttpRequest request = new TDHttpRequest(form, operationType);
    
    try {
    if (payload instanceof Boolean) {
      request.setPrimitivePayload(schema, (boolean) payload);
    } else if (payload instanceof Byte || payload instanceof Integer || payload instanceof Long) {
      request.setPrimitivePayload(schema, Long.valueOf(String.valueOf(payload)));
    } else if (payload instanceof Float || payload instanceof Double) {
      request.setPrimitivePayload(schema, Double.valueOf(String.valueOf(payload)));
    } else if (payload instanceof String) {
      request.setPrimitivePayload(schema, (String) payload);
    } else {
      failed("Unable to detect the primitive datatype of payload: " 
          + payload.getClass().getCanonicalName());
    }
    } catch (IllegalArgumentException e) {
      failed(e.getMessage());
    }
    
    if (this.dryRun) {
      log(request.toString());
      return Optional.empty();
    } else {
      return issueRequest(request);
    }
  }
  
  /* Request with an ObjectSchema payload */
  private Optional<TDHttpResponse> executeRequestObjectPayload(String operationType, Form form, 
      DataSchema schema, Object[] tags, Object[] payload) {
    if (schema.getDatatype() != DataSchema.OBJECT) {
      failed("TD mismatch: illegal arguments, this affordance uses a data schema of type " 
          + schema.getDatatype());
    }
    
    TDHttpRequest request = new TDHttpRequest(form, operationType);
    
    Map<String, Object> requestPayload = new HashMap<String, Object>();
    
    for (int i = 0; i < tags.length; i ++) {
      if (tags[i] instanceof String) {
        requestPayload.put((String) tags[i], payload[i]);
      }
    }
    
    request.setObjectPayload((ObjectSchema) schema, requestPayload);
    
    if (this.dryRun) {
      log(request.toString());
      return Optional.empty();
    } else {
      return issueRequest(request);
    }
  }
  
  /* Request with an ArraySchema payload */
  private Optional<TDHttpResponse> executeRequestArrayPayload(String operationType, Form form, 
      DataSchema schema, Object[] payload) {
    if (schema.getDatatype() != DataSchema.ARRAY) {
      failed("TD mismatch: illegal arguments, this affordance uses a data schema of type " 
          + schema.getDatatype());
    }
    
    TDHttpRequest request = new TDHttpRequest(form, operationType)
        .setArrayPayload((ArraySchema) schema, Arrays.asList(payload));
    
    if (this.dryRun) {
      log(request.toString());
      return Optional.empty();
    } else {
      return issueRequest(request);
    }
  }
  
  private Optional<TDHttpResponse> issueRequest(TDHttpRequest request) {
    try {
      return Optional.of(request.execute());
    } catch (IOException e) {
      failed(e.getMessage());
    }
    
    return Optional.empty();
  }
}
