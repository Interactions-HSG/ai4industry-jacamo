/* Initial beliefs and rules */

td_url("http://andreiciortea.ro/tds/forkliftRobot.ttl").

operation("http://example.org/CarryFromTo").


/* Initial goals */

!start.

/* Plans */

+!start : td_url(Url) & operation(OpType) <- 
  .print("hello world.");
  // To also execute the requests, remove the second init parameter (dryRun flag).
  // When dryRun is set to true, the requests are printed (but not executed).
  makeArtifact("forkliftRobot", "tools.ThingArtifact", [Url, true], ArtId);
  .print("Artifact created!");
  // Write property
  .print("Writing property: http://example.org/Status");
  writeProperty("http://example.org/Status", [true])[artifact_name("forkliftRobot")];
  // Invoke action with tagged nested lists (i.e., ObjectSchema payload)
  .print("Invoking action with object schema payload: http://example.org/CarryFromTo");
  invokeAction("http://example.org/CarryFromTo", 
    ["http://example.org/SourcePosition", "http://example.org/TargetPosition"],
    [[30, 50, 70], [30, 60, 70]]
  )[artifact_name("forkliftRobot")];
  // Invoke action with nested lists (i.e., ArraySchema payload)
  .print("Invoking action with array schema payload: http://example.org/MoveTo");
  invokeAction("http://example.org/MoveTo", [[30, 50, 70], [30, 60, 70]])[artifact_name("forkliftRobot")].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
