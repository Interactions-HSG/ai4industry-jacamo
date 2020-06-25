/* Initial beliefs and rules */

api_key("ee70edc576a65494d0c929876c516381").
td_url("https://raw.githubusercontent.com/Interactions-HSG/wot-td-java/feature/phantomx/samples/phantomXRobotArm.ttl").

sourceAngle(512). // ~180 degrees angle
targetAngle(256). // ~90 degrees angle

/* Initial goals */

!start.

/* Plans */

+!start : td_url(Url) <-
  .print("hello world.");

  makeArtifact("armRobot", "tools.ThingArtifact", [Url, false], ArtId);
  .print("Robot arm artifact created!");

  .print("Set API token");
  !setAuthentication;

  !deliver.

+!setAuthentication : api_key(Token) <-
  setAPIKey(Token)[artifact_name("armRobot")].

+!deliver : sourceAngle(Source) & targetAngle(Target) <-
  .print("Set base to " , Source);
  invokeAction("http://example.org/SetBase", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [Source])[artifact_name("armRobot")];
  !interval;
  .print("Set gripper to 512");
  invokeAction("http://example.org/SetGripper", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [512])[artifact_name("armRobot")];
  !interval;
  .print("Set wrist angle to 390");
  invokeAction("http://example.org/SetWristAngle", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [390])[artifact_name("armRobot")];
  !interval;
  .print("Set shoulder to 510");
  invokeAction("http://example.org/SetShoulder", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [510])[artifact_name("armRobot")];
  !interval;
  .print("Set gripper to 400");
  invokeAction("http://example.org/SetGripper", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [400])[artifact_name("armRobot")];
  !interval;
  .print("Set shoulder to 400");
  invokeAction("http://example.org/SetShoulder", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [400])[artifact_name("armRobot")];
  !interval;
  .print("Set base to " , Target);
  invokeAction("http://example.org/SetBase", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [Target])[artifact_name("armRobot")];
  !interval;
  .print("Set shoulder to 510");
  invokeAction("http://example.org/SetShoulder", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [510])[artifact_name("armRobot")];
  !interval;
  .print("Set gripper to 512");
  invokeAction("http://example.org/SetGripper", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [512])[artifact_name("armRobot")];
  !interval;
  .print("Set gripper to 400");
  invokeAction("http://example.org/SetShoulder", ["https://www.w3.org/2019/wot/json-schema#IntegerSchema"], [400])[artifact_name("armRobot")];
  !interval;

  readProperty("http://example.org/Posture", Types , Values)[artifact_name("armRobot")];
  .print(Values).

+!interval : true <- .wait(3000).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
