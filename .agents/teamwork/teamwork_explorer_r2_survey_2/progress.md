# Progress Tracker — survey_exp_2

Last visited: 2026-11-07T06:05:00Z

- [x] Initialized DISPATCH.md and BRIEFING.md
- [x] Inspected Packets.java / Packets.CoopCue: verified wire layout and existing fields (initiatorName, cueType, ruText, ptText, objective, durationTicks)
- [x] Inspected ChallengeManager.java (sendBilingualCue): confirmed packet dispatch logic
- [x] Inspected ChallengeHud.java, ClientCompat.java, and DrawContext (class_332)
- [x] Investigated Minecraft 1.20.1 Fabric player head rendering via PlayerFaceRenderer (class_7532.method_44443) and skin resolution via ClientPlayNetworkHandler (class_634.method_2874) and DefaultSkinHelper (class_1068.method_4648)
- [x] Investigated action item resolution and '?' indicator rendering via DrawContext.drawItemInSlot (method_51432)
- [x] Inspected Instancia_Cliente\options.txt and Iris keybinding definitions in iris-1.7.6+mc1.20.1.jar (iris.keybind.reload / key_iris.keybind.reload)
- [x] Formulated dual-layer Iris reload unbinding strategy: static options.txt edit to key.keyboard.unknown + programmatic enforcement in SayItClient.tick()
- [x] Formulated comprehensive 5-component handoff report
- [ ] Write handoff.md and send message to parent
