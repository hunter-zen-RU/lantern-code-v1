const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_host_123";
const BOB_UID = "bob_peer_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read rooms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("rooms").get());
});

test("Authenticated host (Alice): can create room with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("rooms").doc("mesh_room_1").set({
      roomId: "mesh_room_1",
      name: "LANtern Dev Room",
      hostUid: ALICE_UID,
      hostName: "Alice Dev",
      status: "active",
      activeFileName: "main.kt",
      activeContent: "fun main() {}",
    })
  );
});

test("Authenticated peer (Bob): cannot delete Alice's room", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("rooms").doc("mesh_room_1").set({
      roomId: "mesh_room_1",
      name: "LANtern Dev Room",
      hostUid: ALICE_UID,
      hostName: "Alice Dev",
      status: "active",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("rooms").doc("mesh_room_1").delete());
});

test("Authenticated peer (Bob): can post candidate and peer status", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("rooms").doc("mesh_room_1").set({
      roomId: "mesh_room_1",
      name: "LANtern Dev Room",
      hostUid: ALICE_UID,
      hostName: "Alice Dev",
      status: "active",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("rooms").doc("mesh_room_1").collection("candidates").doc("cand_1").set({
      senderUid: BOB_UID,
      candidate: "candidate:1 1 UDP 2130706431 192.168.1.50 5000 typ host",
    })
  );

  await assertSucceeds(
    bobDb.collection("rooms").doc("mesh_room_1").collection("peers").doc(BOB_UID).set({
      peerUid: BOB_UID,
      peerName: "Bob Dev",
      role: "Editor",
      status: "online",
      cursorLine: 1,
      cursorCol: 5,
    })
  );
});
