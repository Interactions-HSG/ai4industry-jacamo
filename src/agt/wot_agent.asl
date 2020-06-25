/* Initial beliefs and rules */

api_key_F("test-token").
td_url_F("https://raw.githubusercontent.com/Interactions-HSG/wot-td-java/feature/http-client/samples/forkliftRobot.ttl").

api_key_A("5b6ddbfc6167a0d0c118115cca468b3b").
td_url_A("https://raw.githubusercontent.com/Interactions-HSG/wot-td-java/feature/phantomx/samples/phantomXRobotArm.ttl").

/* Initial goals */

!start.

/* Plans */

+!start : td_url_F(UrlF) & td_url_A(UrlA) <-
  .print("hello world.");
  // To also execute the requests, remove the second init parameter (dryRun flag).
  // When dryRun is set to true, the requests are printed (but not executed).
  makeArtifact("forkliftRobot", "tools.ThingArtifact", [UrlF, true], ArtIdF);
  .print("Forklift artifact created!");
  makeArtifact("armRobot", "tools.ThingArtifact", [UrlA, false], ArtIdA);
  .print("Arm robot artifact created!");

  // Requests to forklift artifact
  // Write property of boolean type
  /*
  .print("Writing property: http://example.org/Status");
  writeProperty("http://example.org/Status", [true])[artifact_name("forkliftRobot")];
  // Read property of boolean type
  .print("Reading boolean property: http://example.org/Status");
  readProperty("http://example.org/Status", StatusValue)[artifact_name("forkliftRobot")];
  .println("Read value (if dry run, then <no-value>): ", StatusValue);
  // Read property of array type
  .print("Reading array property: http://example.org/Position");
  readProperty("http://example.org/Position", PositionValue)[artifact_name("forkliftRobot")];
  .println("Read value (if dry run, then <no-value>): ", PositionValue);
  // Read property of object type
  .print("Reading object property: http://example.org/LastCarry");
  readProperty("http://example.org/LastCarry", LastCarryTags, LastCarryValue)[artifact_name("forkliftRobot")];
  .println("Read value (if dry run, then <no-value>): ", LastCarryTags, ", ", LastCarryValue);
  // Invoke action with tagged nested lists (i.e., ObjectSchema payload)
  .print("Invoking action with object schema payload: http://example.org/CarryFromTo");
  invokeAction("http://example.org/CarryFromTo",
    ["http://example.org/SourcePosition", "http://example.org/TargetPosition"],
    [[30, 50, 70], [30, 60, 70]]
  )[artifact_name("forkliftRobot")];
  // Send an authenticated request
  .print("Setting test API token");
  ?api_key_F(TokenF);
  setAPIKey(TokenF)[artifact_name("forkliftRobot")];
  // Invoke action with nested lists (i.e., ArraySchema payload)
  .print("Invoking action with array schema payload: http://example.org/MoveTo");
  invokeAction("http://example.org/MoveTo", [30, 60, 70])[artifact_name("forkliftRobot")];
*/
  // Requests to arm robot artifact
  // Send authenticated requests
  .print("Setting API token");
  ?api_key_A(TokenA);
  setAPIKey(TokenA)[artifact_name("armRobot")];
  invokeAction("http://example.org/SetGripper", ["http://example.org/IntegerSchema"], [510])[artifact_name("armRobot")];
  invokeAction("http://example.org/SetBase", ["http://example.org/IntegerSchema"], [300])[artifact_name("armRobot")].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
