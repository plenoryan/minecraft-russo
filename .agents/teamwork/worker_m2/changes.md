# Changes Log: Worker M2 (QoL & Dynamism Mods Integration)

## Summary of Changes
Implemented Requirement R4 by automating the download, integrity verification, and architectural segregation of 6 curated Quality of Life (QoL) and dynamism mods for Fabric 1.20.1.

## Files Created / Modified

### 1. `F:\Jogos\Mine\download_qol_mods.ps1` (NEW)
- **Purpose**: Autonomous PowerShell provisioning script to download, verify, and place Fabric 1.20.1 QoL mods from Modrinth CDN.
- **Key Features**:
  - Enforces TLS 1.2 protocol for secure CDN connections.
  - Downloads 6 curated zero-vocabulary utility mods directly from Modrinth CDN.
  - Verifies non-zero byte sizes and exact SHA-512 cryptographic checksums against official release digests.
  - Staging cache mechanism in `$env:TEMP\sayit_qol_downloads` to avoid redundant re-downloads.
  - Strict enforcement of client vs dedicated server mod segregation per `AGENTS.md` Lesson L13.
  - Performs post-deployment audit of both client and server directories, asserting presence of expected mods and absolute absence of client-only mods on the dedicated server.

### 2. `F:\Jogos\Mine\Instancia_Cliente\mods\` (MODIFIED - Added 6 QoL Mods)
- `omnimovement-fabric-1.0+mc1.20.1.jar` (29,222 bytes; SHA-512 verified) — 360-degree omnidirectional sprinting & 1-block auto step-up.
- `InventorySorter-1.9.0-1.20.jar` (222,894 bytes; SHA-512 verified) — Middle-click chest and inventory sorting with zero dependencies.
- `stack-to-nearby-chests-mc1.20.1-0.5.10.jar` (559,703 bytes; SHA-512 verified) — Terraria-style quick stack & restock to nearby containers.
- `entityculling-fabric-1.11.3-mc1.20.1.jar` (1,516,696 bytes; SHA-512 verified) — Occlusion culling for entity rendering performance.
- `krypton-0.2.3.jar` (185,895 bytes; SHA-512 verified) — Netty direct buffer network stack optimization for multiplayer sessions.
- `appleskin-fabric-mc1.20.1-2.5.2.jar` (1,097,240 bytes; SHA-512 verified) — Hunger & saturation HUD tooltips for Farmer's Delight.
Total client mods increased from 27 to 33 JARs.

### 3. `F:\Jogos\Mine\Servidor_Dedicado\mods\` (MODIFIED - Added 2 Universal Mods)
- `InventorySorter-1.9.0-1.20.jar` (222,894 bytes; SHA-512 verified) — Universal container sorting logic.
- `krypton-0.2.3.jar` (185,895 bytes; SHA-512 verified) — Server-side Netty pipeline and packet consolidation.
Total server mods increased from 10 to 12 JARs.
Client-only mods (`omnimovement`, `stack-to-nearby-chests`, `entityculling`, `appleskin`) strictly excluded from `Servidor_Dedicado\mods\`.

## Verification Performed
1. **Download & Hash Integrity**: Executed `download_qol_mods.ps1` with exit code 0; all 6 SHA-512 checksums verified.
2. **Dedicated Server Dry-Run Smoke Test**:
   - Command: `& "F:\Jogos\Mine\Java17\bin\java.exe" "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher" -jar "F:\Jogos\Mine\Servidor_Dedicado\fabric-server-launch.jar" --help`
   - Output: 89 mods loaded (including new mods `inventorysorter` and `krypton`), SpongePowered Mixin initialized, clean help display, exit code 0, 0 ClassNotFoundExceptions.
3. **Canonical E2E Test Suite**:
   - Command: `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`
   - Output: 45/45 tests passed, 0 failures, duration ~2.1s, exit code 0.
