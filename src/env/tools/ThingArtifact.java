package tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cartago.Artifact;
import cartago.OPERATION;
import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.clients.TDHttpRequest;
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
  // Will be removed, currently used during dev:
  private final String test_td = "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" + 
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" + 
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" + 
      "@prefix dct: <http://purl.org/dc/terms/> .\n" + 
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" + 
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" + 
      "@prefix ex: <http://example.org/> .\n" + 
      "\n" + 
      "ex:forkliftRobot a td:Thing ; \n" + 
      "    dct:title \"forkliftRobot\" ;\n" + 
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" + 
      "    td:hasPropertyAffordance [\n" + 
      "        a td:PropertyAffordance, js:BooleanSchema, ex:Status ; \n" + 
      "        td:hasForm [\n" + 
      "            hctl:hasTarget <http://example.org/forkliftRobot/busy> ; \n" + 
      "        ] ; \n" + 
      "    ] ;\n" + 
      "    td:hasActionAffordance [\n" + 
      "        a td:ActionAffordance, ex:CarryFromTo ;\n" + 
      "        dct:title \"carry\" ; \n" + 
      "        td:hasForm [\n" + 
      "            hctl:hasTarget <http://example.org/forkliftRobot/carry> ; \n" + 
      "        ] ; \n" + 
      "        td:hasInputSchema [ \n" + 
      "            a js:ObjectSchema ;\n" + 
      "            js:properties [ \n" + 
      "                a js:ArraySchema, ex:SourcePosition, ex:3DCordinates ;\n" + 
      "                js:propertyName \"sourcePosition\";\n" + 
      "                js:minItems 3 ;\n" + 
      "                js:maxItems 3 ;\n" + 
      "                js:items [\n" + 
      "                    a js:NumberSchema ;\n" + 
      "                ] ;\n" + 
      "            ] ;\n" + 
      "            js:properties [\n" + 
      "                a js:ArraySchema, ex:TargetPosition, ex:3DCordinates ;\n" + 
      "                js:propertyName \"targetPosition\";\n" + 
      "                js:minItems 3 ;\n" + 
      "                js:maxItems 3 ;\n" + 
      "                js:items [\n" + 
      "                    a js:NumberSchema ;\n" + 
      "                ] ;\n" + 
      "            ] ;\n" + 
      "            js:required \"sourcePosition\", \"targetPosition\" ;\n" + 
      "        ] ; \n" + 
      "    ] ;" +
      "    td:hasActionAffordance [\n" + 
      "        a td:ActionAffordance, ex:MoveTo ;\n" + 
      "        dct:title \"moveTo\" ; \n" + 
      "        td:hasForm [\n" + 
      "            hctl:hasTarget <http://example.org/forkliftRobot/moveTo> ; \n" + 
      "        ] ; \n" + 
      "        td:hasInputSchema [ \n" + 
      "            a js:ArraySchema, ex:3DCordinates ;\n" + 
      "            js:minItems 3 ;\n" + 
      "            js:maxItems 3 ;\n" + 
      "            js:items [\n" + 
      "                a js:NumberSchema ;\n" + 
      "            ] ;\n" + 
      "        ] ; " +
      "    ] .";
  
  private ThingDescription td;
  private boolean dryRun;
  
  /**
   * Method called by CArtAgO to initialize the artifact. The W3C WoT Thing Description (TD) used by
   * this artifact is retrieved and parsed during initialization. 
   * 
   * @param url A URL that dereferences to a W3C WoT Thing Description.
   */
  public void init(String url) {
    this.td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, test_td);
    
    // TODO: To dereference the url provided as a parameter, use the follwing codeinstead:
//    try {
//     this.td = TDGraphReader.readFromURL(TDFormat.RDF_TURTLE, url);
//    } catch (IOException e) {
//      failed(e.getMessage());
//    }
    
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
   * CArtAgO operation for writing a property of a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the action type.
   * @param tags A list of IRIs that identify parameters sent in the payload. Used for object schemas.
   * @param payload The payload to be issued when invoking the action.
   */
  @OPERATION
  public void writeProperty(String semanticType, Object[] tags, Object[] payload) {
    validateParameters(semanticType, tags, payload);
    if (payload.length == 0) {
      failed("The payload used when writing a property cannot be empty.");
    }
    
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(semanticType);
    
    if (property.isPresent()) {
      Optional<Form> form = property.get()
          .getFirstFormForOperationType(TD.writeProperty);
      
      if (!form.isPresent()) {
        // Should not happen (an exception will be raised by the TD library first)
        failed("Invalid TD: the property does not have a valid form.");
      }
      
      DataSchema schema = property.get().getDataSchema();
      
      executeRequest(TD.writeProperty, form.get(), Optional.of(schema), tags, payload);
    } else {
      failed("Unknown property: " + semanticType);
    }
  }
  
  /**
   * CArtAgO operation for writing a property of a Thing using a semantic model of the Thing.
   * 
   * @param semanticType An IRI that identifies the action type.
   * @param payload The payload to be issued when invoking the action.
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
      
      executeRequest(TD.invokeAction, form.get(), inputSchema, tags, payload);
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
  
  private void executeRequest(String operationType, Form form, Optional<DataSchema> schema, 
      Object[] tags, Object[] payload) {
    if (schema.isPresent()) {
      // Request with payload
      if (tags.length > 0) {
        executeRequestObjectPayload(operationType, form, schema.get(), tags, payload);
      } else if (payload.length == 1 && !(payload[0] instanceof Object[])) {
        executeRequestPrimitivePayload(operationType, form, schema.get(), payload[0]);
      } else if (payload.length >= 1) {
        executeRequestArrayPayload(operationType, form, schema.get(), payload);
      } else {
        failed("Could not detect the type of payload (primitive, object, or array).");
      }
    } else {
      // Request without payload
      TDHttpRequest request = new TDHttpRequest(form, operationType);
      
      if (this.dryRun) {
        log(request.toString());
      } else {
        request.execute();
      }
    }
  }
  
  /* Request with primitive payload: Boolean, Number, or String */
  private void executeRequestPrimitivePayload(String operationType, Form form, DataSchema schema, 
      Object payload) {
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
    } else {
      request.execute();
    }
  }
  
  /* Request with an ObjectSchema payload */
  private void executeRequestObjectPayload(String operationType, Form form, DataSchema schema, 
      Object[] tags, Object[] payload) {
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
    } else {
      request.execute();
    }
  }
  
  /* Request with an ArraySchema payload */
  private void executeRequestArrayPayload(String operationType, Form form, DataSchema schema, 
      Object[] payload) {
    if (schema.getDatatype() != DataSchema.ARRAY) {
      failed("TD mismatch: illegal arguments, this affordance uses a data schema of type " 
          + schema.getDatatype());
    }
    
    TDHttpRequest request = new TDHttpRequest(form, operationType)
        .setArrayPayload((ArraySchema) schema, Arrays.asList(payload));
    
    if (this.dryRun) {
      log(request.toString());
    } else {
      request.execute();
    }
  }
}
