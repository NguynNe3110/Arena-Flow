# Theme tokens

Dependencies flow from primitive to semantic to component:

1. **Primitive** (`primitive/`): raw color tones, bundled font family, weights, sizes and tracking. Internal to this module.
2. **Semantic** (`semantic/`): color roles and text roles installed by `ShowcaseTheme` as Material 3 `ColorScheme` and `Typography`.
3. **Component** (`../composable/`): shared components consume `MaterialTheme.colorScheme` and `MaterialTheme.typography`. Keep component-specific defaults beside the component when needed.

Screens must not import primitive tokens. Existing `TextTitleLarge` and `TextTitleMedium` demonstrate the component layer.

## Visual source

Use `docs/prototype/stitch/2789586751825382546/`, exported 17 November 2026.
The older `docs/prototype/index.html` and `stitch/original/` are explicitly superseded.

The source S1 mobile HTML (`screens/d4388387a3364bacb820eb34dfaa3267.html`) provides the semantic palette:
- Primary: #3D6943; on primary: #E9FFE6.
- Background/surface: #F8FAF3; on surface: #2D342C.
- Primary container: #BDEFBF; on primary container: #2F5B36.
- Error: #A73B21; error container: #FD795A.

Light color roles, including fixed roles, match that source exactly. Black is used for the scrim.
The source only defines light mode. Light is the default; dark mode is a derived green fallback, not a source-approved design. Dynamic color remains opt-in.

## Typography

Be Vietnam Pro is bundled under `feature/base/src/main/res/font/`, including the supplied upright/italic weights 100–900. It works offline.
The font copyright notice and SIL OFL 1.1 license ship in `src/main/assets/licenses/be_vietnam_pro_ofl.txt`.
Upstream: https://github.com/google/fonts/tree/main/ofl/bevietnampro

The mobile HTML uses 12/14/16px body/label text, semibold labels and bold headings (18/20/24px, plus the S1 26px hero).
Compose maps these to scalable sp with explicit line heights and zero body tracking.
Semantic display roles extend the same scale to 30/36sp for larger surfaces.
Heading tracking is normalized to -0.5sp; this is an Android mapping, not a literal transcription of every CSS utility.
Use `displaySmall` for the 26sp hero, `headlineLarge` for a 24sp heading, `headlineMedium` for 20sp section headings, and `titleMedium` for 16sp semibold titles.

Font and color tokens do not migrate existing screen layouts. Component-specific brand banners and status treatments should be implemented alongside those components using semantic roles.
