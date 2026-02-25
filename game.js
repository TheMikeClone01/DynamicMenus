import * as THREE from "https://unpkg.com/three@0.163.0/build/three.module.js";
import { PointerLockControls } from "https://unpkg.com/three@0.163.0/examples/jsm/controls/PointerLockControls.js";

const WORLD = {
  chunkSize: 16,
  maxHeight: 20,
  renderDistance: 3,
  blockSize: 1,
  gravity: 24,
};

const BLOCKS = {
  grass: { color: 0x62a64c, name: "Hierba" },
  dirt: { color: 0x8d5f3d, name: "Tierra" },
  stone: { color: 0x888888, name: "Piedra" },
  wood: { color: 0x6b4f33, name: "Madera" },
};

const scene = new THREE.Scene();
scene.background = new THREE.Color(0x7cb2ff);
scene.fog = new THREE.Fog(0x7cb2ff, 10, 140);

const renderer = new THREE.WebGLRenderer({ canvas: document.querySelector("#game"), antialias: true });
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
renderer.setSize(window.innerWidth, window.innerHeight);
renderer.shadowMap.enabled = true;

const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 500);
camera.position.set(0, 26, 0);

const controls = new PointerLockControls(camera, document.body);
scene.add(controls.getObject());

const hemi = new THREE.HemisphereLight(0xbfe0ff, 0x2d1b0f, 0.6);
scene.add(hemi);

const sun = new THREE.DirectionalLight(0xffffff, 1.1);
sun.position.set(20, 40, -10);
sun.castShadow = true;
sun.shadow.camera.left = -50;
sun.shadow.camera.right = 50;
sun.shadow.camera.top = 50;
sun.shadow.camera.bottom = -50;
scene.add(sun);

const crosshair = document.createElement("div");
crosshair.style.cssText = "position:fixed;inset:0;pointer-events:none;display:grid;place-items:center;font-size:20px;color:#fff;text-shadow:0 0 4px #000;";
crosshair.textContent = "+";
document.body.append(crosshair);

const statusEl = document.querySelector("#status");
const healthEl = document.querySelector("#health");
const hungerEl = document.querySelector("#hunger");
const dayEl = document.querySelector("#day");
const selectedEl = document.querySelector("#selected");

const keys = new Set();
const velocity = new THREE.Vector3();
let canJump = false;

const raycaster = new THREE.Raycaster();
const world = new Map();
const chunkMeshes = new Map();
const liveChunks = new Set();

const state = {
  selectedBlock: "dirt",
  health: 100,
  hunger: 100,
  day: 1,
  dayTime: 0,
  lastTick: performance.now(),
};

function seededNoise(x, z) {
  const n = Math.sin(x * 127.1 + z * 311.7) * 43758.5453123;
  return n - Math.floor(n);
}

function smoothHeight(x, z) {
  const n1 = seededNoise(x * 0.08, z * 0.08);
  const n2 = seededNoise(x * 0.02 + 100, z * 0.02 + 100) * 0.5;
  return Math.floor(6 + n1 * 8 + n2 * 10);
}

function toChunkCoord(value) {
  return Math.floor(value / WORLD.chunkSize);
}

function chunkKey(cx, cz) {
  return `${cx},${cz}`;
}

function blockKey(x, y, z) {
  return `${x},${y},${z}`;
}

function getBlockType(x, y, z) {
  const override = world.get(blockKey(x, y, z));
  if (override === null) return null;
  if (override) return override;

  const height = smoothHeight(x, z);
  if (y > height || y < 0) return null;
  if (y === height) return "grass";
  if (y > height - 3) return "dirt";
  return "stone";
}

function setBlockType(x, y, z, type) {
  world.set(blockKey(x, y, z), type);
  refreshChunkByBlock(x, z);
}

function refreshChunkByBlock(x, z) {
  for (let ox = -1; ox <= 1; ox++) {
    for (let oz = -1; oz <= 1; oz++) {
      const cx = toChunkCoord(x) + ox;
      const cz = toChunkCoord(z) + oz;
      const key = chunkKey(cx, cz);
      if (chunkMeshes.has(key)) {
        chunkMeshes.get(key).forEach((mesh) => scene.remove(mesh));
        buildChunk(cx, cz);
      }
    }
  }
}

function createBlockMesh(type, x, y, z) {
  const geometry = new THREE.BoxGeometry(1, 1, 1);
  const material = new THREE.MeshLambertMaterial({ color: BLOCKS[type].color });
  const cube = new THREE.Mesh(geometry, material);
  cube.castShadow = true;
  cube.receiveShadow = true;
  cube.position.set(x + 0.5, y + 0.5, z + 0.5);
  cube.userData.block = { x, y, z, type };
  return cube;
}

function buildChunk(cx, cz) {
  const key = chunkKey(cx, cz);
  const meshes = [];
  const startX = cx * WORLD.chunkSize;
  const startZ = cz * WORLD.chunkSize;

  for (let x = startX; x < startX + WORLD.chunkSize; x++) {
    for (let z = startZ; z < startZ + WORLD.chunkSize; z++) {
      const h = Math.min(smoothHeight(x, z), WORLD.maxHeight);
      for (let y = 0; y <= h; y++) {
        const type = getBlockType(x, y, z);
        if (!type) continue;
        if (!hasVisibleFace(x, y, z)) continue;
        const mesh = createBlockMesh(type, x, y, z);
        scene.add(mesh);
        meshes.push(mesh);
      }
      if (seededNoise(x * 0.3, z * 0.3) > 0.92) {
        spawnTree(x, h + 1, z, meshes);
      }
    }
  }

  chunkMeshes.set(key, meshes);
}

function spawnTree(x, y, z, meshes) {
  for (let i = 0; i < 3; i++) {
    if (getBlockType(x, y + i, z)) continue;
    const trunk = createBlockMesh("wood", x, y + i, z);
    scene.add(trunk);
    meshes.push(trunk);
  }
}

function hasVisibleFace(x, y, z) {
  const neighbors = [
    [1, 0, 0],
    [-1, 0, 0],
    [0, 1, 0],
    [0, -1, 0],
    [0, 0, 1],
    [0, 0, -1],
  ];
  return neighbors.some(([dx, dy, dz]) => !getBlockType(x + dx, y + dy, z + dz));
}

function updateChunks() {
  const px = Math.floor(camera.position.x);
  const pz = Math.floor(camera.position.z);
  const pcx = toChunkCoord(px);
  const pcz = toChunkCoord(pz);
  liveChunks.clear();

  for (let x = -WORLD.renderDistance; x <= WORLD.renderDistance; x++) {
    for (let z = -WORLD.renderDistance; z <= WORLD.renderDistance; z++) {
      const cx = pcx + x;
      const cz = pcz + z;
      const key = chunkKey(cx, cz);
      liveChunks.add(key);
      if (!chunkMeshes.has(key)) buildChunk(cx, cz);
    }
  }

  for (const [key, meshes] of chunkMeshes.entries()) {
    if (liveChunks.has(key)) continue;
    meshes.forEach((mesh) => scene.remove(mesh));
    chunkMeshes.delete(key);
  }
}

function applySurvival(dt) {
  state.dayTime += dt;
  if (state.dayTime > 120) {
    state.day += 1;
    state.dayTime = 0;
  }

  state.hunger = Math.max(0, state.hunger - dt * 0.4);
  if (state.hunger <= 0) {
    state.health = Math.max(0, state.health - dt * 1.3);
  }

  if (state.health <= 0) {
    state.health = 100;
    state.hunger = 100;
    camera.position.set(0, 30, 0);
    statusEl.textContent = "Has muerto de hambre. Respawn en origen.";
  }

  const sunAngle = (state.dayTime / 120) * Math.PI * 2;
  sun.position.set(Math.cos(sunAngle) * 40, Math.sin(sunAngle) * 40, -10);
  sun.intensity = Math.max(0.15, Math.sin(sunAngle) + 0.4);

  healthEl.textContent = Math.round(state.health);
  hungerEl.textContent = Math.round(state.hunger);
  dayEl.textContent = String(state.day);
}

function handleMovement(dt) {
  const speed = keys.has("ShiftLeft") ? 10 : 6;
  velocity.x -= velocity.x * 9 * dt;
  velocity.z -= velocity.z * 9 * dt;
  velocity.y -= WORLD.gravity * dt;

  const direction = new THREE.Vector3();
  if (keys.has("KeyW")) direction.z -= 1;
  if (keys.has("KeyS")) direction.z += 1;
  if (keys.has("KeyA")) direction.x -= 1;
  if (keys.has("KeyD")) direction.x += 1;
  direction.normalize();

  if (direction.length()) {
    velocity.z -= direction.z * speed * dt * 20;
    velocity.x -= direction.x * speed * dt * 20;
  }

  controls.moveRight(-velocity.x * dt);
  controls.moveForward(-velocity.z * dt);
  camera.position.y += velocity.y * dt;

  const ground = smoothHeight(Math.floor(camera.position.x), Math.floor(camera.position.z)) + 2;
  if (camera.position.y < ground) {
    velocity.y = 0;
    camera.position.y = ground;
    canJump = true;
  }

  if (camera.position.y < -20) {
    camera.position.set(0, 30, 0);
    velocity.set(0, 0, 0);
  }
}

function interact(place) {
  raycaster.setFromCamera(new THREE.Vector2(0, 0), camera);
  const candidates = Array.from(chunkMeshes.values()).flat();
  const intersects = raycaster.intersectObjects(candidates, false);
  if (!intersects.length) return;

  const hit = intersects[0];
  const block = hit.object.userData.block;
  if (!block) return;

  if (place) {
    const normal = hit.face.normal;
    const x = block.x + normal.x;
    const y = block.y + normal.y;
    const z = block.z + normal.z;
    if (y >= 0 && y <= WORLD.maxHeight + 12) {
      setBlockType(x, y, z, state.selectedBlock);
      state.hunger = Math.max(0, state.hunger - 0.25);
    }
  } else {
    setBlockType(block.x, block.y, block.z, null);
    state.hunger = Math.max(0, state.hunger - 0.15);
  }
}

function animate(now) {
  const dt = Math.min((now - state.lastTick) / 1000, 0.04);
  state.lastTick = now;

  if (controls.isLocked) {
    handleMovement(dt);
    applySurvival(dt);
    updateChunks();
  }

  renderer.render(scene, camera);
  requestAnimationFrame(animate);
}

window.addEventListener("keydown", (event) => {
  keys.add(event.code);

  if (event.code === "Space" && canJump) {
    velocity.y = 9;
    canJump = false;
  }

  if (event.code === "Digit1") state.selectedBlock = "dirt";
  if (event.code === "Digit2") state.selectedBlock = "stone";

  selectedEl.textContent = BLOCKS[state.selectedBlock].name;
});

window.addEventListener("keyup", (event) => keys.delete(event.code));
window.addEventListener("resize", () => {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
});

window.addEventListener("mousedown", (event) => {
  if (!controls.isLocked) return;
  if (event.button === 0) interact(false);
  if (event.button === 2) interact(true);
});

window.addEventListener("contextmenu", (event) => event.preventDefault());

controls.addEventListener("lock", () => {
  statusEl.textContent = "Sobrevive, recolecta bloques y sigue avanzando.";
});

controls.addEventListener("unlock", () => {
  statusEl.textContent = "Click para continuar.";
});

document.body.addEventListener("click", () => controls.lock());

updateChunks();
requestAnimationFrame(animate);
