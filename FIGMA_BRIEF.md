# Figma AI Design Brief: Bounce Up

## 1. Art Style & Color Palette

**Style:** Bright, playful cartoon aesthetic with rounded geometry and smooth gradients. Characters and platforms use soft cell-shading with subtle outlines to ensure clarity on small mobile screens. All visual elements feel weightless and whimsical, reinforcing the jumping fantasy. The overall mood is joyful and accessible for ages 8+, avoiding dark or aggressive imagery.

**Primary Color Palette:**
- Sky World: #87CEEB (soft sky blue), #FFD700 (warm gold), #FFFFFF (cloud white)
- Cave World: #8B7355 (earthy brown), #6A5ACD (slate purple), #D3D3D3 (light gray stone)
- Space World: #001a4d (deep navy), #00D9FF (cyan electric), #FFB6D9 (nebula pink)
- UI Neutral: #2C3E50 (dark charcoal), #ECF0F1 (off-white)

**Accent Colors:** #FF6B6B (warning red for hazards), #4ECDC4 (success teal for collectibles)

**Typography Mood:** Friendly, rounded sans-serif (geometric, not strict). Bold weights (600–700) for titles and CTAs; regular weight (400) for body text. All text renders crisp at 480×854 resolution with minimum 14px base size.

---

## 2. App Icon — icon_512.png (512×512px)

**Background:** Radial gradient from #87CEEB (top-left) to #4A90E2 (bottom-right), creating a vibrant sky atmosphere with subtle depth.

**Central Symbol:** A stylized bouncing character mid-leap, rendered in bright #FFD700 with rounded limbs and an expressive smile. The character is caught in an upward arc, with motion lines (3–4 curved strokes in #FFB6D9) trailing behind to convey momentum. Below the character, a single iconic platform in #FFFFFF with a subtle drop shadow creates a sense of just-left-ground.

**Glow & Depth:** Soft outer glow around the character in #FFED4E (lighter gold) at 12px blur radius. A subtle inner shadow on the platform (dark #0D1B2A at 8px offset, 4px blur) grounds it visually. The character has a small white highlight on its upper-left surface to suggest glossy roundness.

**Overall Mood:** Energetic, inviting, and immediately readable at small sizes. The icon communicates "jump," "up," and "fun" without text.

---

## 3. Backgrounds (480×854 portrait)

**Background File List:**
- backgrounds/bg_main.png
- backgrounds/bg_sky.png
- backgrounds/bg_cave.png
- backgrounds/bg_space.png

---

### backgrounds/bg_main.png (480×854)
Soft gradient background transitioning from #87CEEB at the top to #E0F6FF at the bottom, evoking a peaceful sky at sunrise. Scattered flat-design clouds in #FFFFFF with 30% opacity drift across the upper third, providing visual interest without distraction. A subtle sun glow in the top-right corner (radial gradient, #FFD700 to transparent) reinforces the welcoming theme. Use soft, flowing curves throughout to maintain the playful aesthetic. This is the backdrop for all main menu and navigation screens.

### backgrounds/bg_sky.png (480×854)
Bright daylight sky using a linear gradient from #87CEEB (top) to #E8F8FF (bottom), with no hard lines. Layer in 4–5 semi-transparent fluffy clouds (white, #FFFFFF at 60% opacity) at varying depths to create parallax potential. Add a distant mountain silhouette at the very bottom in #D4AF9F with soft, rounded edges. Include a faint sun or moon in the upper-right quadrant as a warm accent. The overall feel is serene yet energizing—the ideal arena for bouncing upward.

### backgrounds/bg_cave.png (480×854)
Dark, earthy underground setting using a base gradient from #6B5344 (dark brown, top) to #4A3F35 (darker brown, bottom). Layer in irregular stone textures using overlapping shapes in #8B7355, #7A6347, and #5C4A3D to suggest rocky walls. Add vertical striations and cracks using thin dark lines (#3D2F27) to enhance the cave-wall illusion. Introduce strategic highlights in #A0826D on upper-left edges of stones to suggest torch or bioluminescent light. Include hanging stalactites (smooth, cone-shaped silhouettes in #6B5344) in the upper portion. The mood is mysterious and slightly claustrophobic, creating tension during gameplay.

### backgrounds/bg_space.png (480×854)
Deep cosmic void using a radial gradient centered slightly above the screen center, transitioning from #0D1B2A (near-black center) to #001a4d (deep navy edges). Scatter stars of varying sizes (2–8px circles in #FFFFFF at 70–100% opacity) across the entire canvas, denser toward edges, sparse in the center. Add nebula clouds using soft, large radial gradients in #FF1493 (hot pink) and #00D9FF (cyan) at 20–30% opacity, positioned in the lower half to avoid obscuring gameplay. Include 2–3 distant planets as simple circles: one in #FFB6D9 (mid-size, lower-left), one in #4ECDC4 (smaller, upper-right). The mood is vast, otherworldly, and awe-inspiring—heightening the "endless journey" feeling.

---

## 4. UI Screens (480×854 portrait)

### main_menu.png (480×854)

Uses **backgrounds/bg_main.png**. At the top center (y: 100–150px), the title "BOUNCE UP" appears in bold, rounded sans-serif, #2C3E50, with a drop shadow in rgba(0,0,0,0.3). Below the title, a large, colorful bouncing character illustration (180×180px) is centered, rendered in the cartoon style from the icon. A prominent "PLAY" button (180×60px, #4ECDC4 background, #FFFFFF text, bold 24px) sits at center (y: 380px). Three smaller secondary buttons—"SHOP," "LEADERBOARD," "SETTINGS"—are arranged horizontally below the PLAY button (y: 460px), each 90×40px with #ECF0F1 background and #2C3E50 text. At the bottom (y: 800px), a small "v1.0" text in #999999 (10px, right-aligned). No navigation clutter—clean, spacious layout.

### world_select.png (480×854)

Uses **backgrounds/bg_main.png**. Header: "SELECT WORLD" centered at top in #2C3E50, bold 28px. Three world cards are stacked vertically (y: 150, 350, 550px), each 380×120px with rounded corners. 
- **Sky Card:** #87CEEB gradient background, "SKY" label in #2C3E50, "Best Height: X m" in 12px gray, unlock indicator or checkmark in #4ECDC4.
- **Cave Card:** #8B7355 gradient background, "CAVE" label in #ECF0F1, unlock status and best height.
- **Space Card:** #001a4d gradient background with cyan accent, "SPACE" label in #00D9FF, unlock status.
Each card has a subtle play icon or arrow on the right. At bottom (y: 800px), a back button ("< MAIN MENU") in #999999, 14px.

### game_sky.png (480×854)

Uses **backgrounds/bg_sky.png**. Full-screen gameplay view. Top-left: "HEIGHT: XXX m" in #2C3E50, 16px bold. Top-right: "STARS: XX" with a small star icon in #FFD700, 16px. The central play area occupies y: 100–750px and shows the bouncing character (centered horizontally) and 3–5 platform silhouettes at varying vertical positions. Platforms are light-colored (#FFFFFF or #FFE4B5) with soft shadows. A faint tilt indicator or subtle guides may appear near the left/right screen edges to hint at steering (low-opacity curved lines in #FF6B6B if tilting is active). No visible buttons during gameplay—interaction is accelerometer-only. Optional pause icon (very small, top-right corner, 24×24px, #2C3E50) is subtle.

### game_cave.png (480×854)

Uses **backgrounds/bg_cave.png**. Identical HUD layout to game_sky.png: "HEIGHT: XXX m" (top-left, now in #A0826D to contrast cave tones), "STARS: XX" (top-right, in #FFD700 to pop against dark background). The bouncing character appears slightly larger or with a glow effect to remain visible against darker tones. Platforms are rendered in shades of #8B7355, #7A6347 with edge highlights in #A0826D. Hazard elements (crumbling platform edges) may flicker or show cracks in #5C4A3D. Tilt guides, if present, use #FF6B6B. The mood is darker and more intense than the Sky variant.

### game_space.png (480×854)

Uses **backgrounds/bg_space.png**. HUD: "HEIGHT: XXX m" in #00D9FF (bright against dark space), top-left. "STARS: XX" in #FFD700, top-right. The bouncing character is rendered with a subtle glow or highlight outline (thin stroke in #00D9FF or #FFB6D9) to ensure visibility against the deep navy. Platforms are crystalline, rendered with geometric facets in #4ECDC4, #00D9FF, and #FFB6D9. Hazard meteors may streak across the screen (dark circles with trailing glow in #FF6B6B). Tilt guides, if visible, use #00D9FF. The overall tone is sci-fi, otherworldly, and sleek.

### game_over.png (480×854)

Uses **backgrounds/bg_main.png**. Centered overlay (340×400px) with #FFFFFF background, rounded corners, subtle drop shadow. Title: "GAME OVER" in #FF6B6B, bold 32px, centered. Below that:
- "FINAL HEIGHT: XXX m" in #2C3E50, 20px bold
- "STARS COLLECTED: XX ⭐" in #FFD700, 18px
- "BEST HEIGHT: YYY m" in gray, 14px (for comparison)

Two action buttons below (y: 320px relative to overlay):
- "RETRY" (180×50px, #4ECDC4 background, #FFFFFF text, bold 18px, left-aligned within overlay)
- "MAIN MENU" (180×50px, #ECF0F1 background, #2C3E50 text, 18px, right-aligned)

Optional: A small world-specific icon or badge in the top-right of the overlay indicating which world was just played.

### leaderboard.png (480×854)

Uses **backgrounds/bg_main.png**. Header: "LEADERBOARD" in #2C3E50, bold 28px, centered at top. Below that, three horizontal tabs (y: 80px): "SKY," "CAVE," "SPACE" in 14px, with the active tab highlighted in #4ECDC4. A scrollable list of top 10 entries occupies y: 130–750px. Each entry is a row (460×50px) containing:
- Rank number (1–10) in bold #2C3E50, left-aligned
- Player name (local device only, or "You") in #2C3E50, center-left
- Height score (e.g., "1,234 m") in bold #FFD700, right-aligned
Alternating row backgrounds: #FFFFFF (odd) and #F5F5F5 (even) for readability. At bottom (y: 800px), a back button in #999999.

### shop.png (480×854)

Uses **backgrounds/bg_main.png**. Header: "CHARACTER SHOP" in #2C3E50, bold 28px, at top. Below that, a scrollable grid of character skins. Each skin card is 180×200px with:
- Centered character illustration (120×120px) showing the skin variant
- Character name below (e.g., "Gold Knight," "Cyan Astronaut") in #2C3E50, 14px bold
- Star cost displayed at bottom-right corner (e.g., "⭐ 50") in #FFD700, 12px
- "OWNED" badge or checkmark in #4ECDC4 if already purchased; "BUY" button (#FFD700 background, #2C3E50 text, 40×30px) if not

Cards are arranged 2-up per row (y: 130–600px). At bottom: A "STAR BALANCE: XXX ⭐" info bar in light gray (#ECF0F1). Back button (y: 800px) in #999999.

### settings.png (480×854)

Uses **backgrounds/bg_main.png**. Header: "SETTINGS" in #2C3E50, bold 28px, at top. A vertical list of toggle and text options (y: 120–650px):
- "SOUND: ON/OFF" — toggle switch on right side, rendered as a simple circle slider
- "MUSIC: ON/OFF" — toggle switch
- "DIFFICULTY: EASY / NORMAL / HARD" — radio buttons or dropdown (appears below the difficulty label)
- "RESET GAME DATA" — text in #FF6B6B, with a small warning icon; tapping opens a confirmation dialog

Each option is a row (450×50px) with left-aligned label in #2C3E50 (16px) and control on the right. Below the option list (y: 680px), a "CREDITS" button (200×40px, #ECF0F1 background, #2C3E50 text). At very bottom (y: 800px), a back button in #999999. All toggles and controls use accent colors (#4ECDC4 for active, #999999 for inactive).

---

## 5. Export Checklist

- backgrounds/bg_main.png (480×854)
- backgrounds/bg_sky.png (480×854)
- backgrounds/bg_cave.png (480×854)
- backgrounds/bg_space.png (480×854)
- icon_512.png (512×512)
- main_menu.png (480×854)
- world_select.png (480×854)
- game_sky.png (480×854)
- game_cave.png (480×854)
- game_space.png (480×854)
- game_over.png (480×854)
- leaderboard.png (480×854)
- shop.png (480×854)
- settings.png (480×854)
