# eSHCAT — UI/UX Design System

> **Modern Minimalism + Soft Glassmorphism + Bento UI**
>
> The interface must feel like a modern digital public-service platform, not a traditional government website — while remaining professional, accessible, and fast.

## Design Principles

- **Less but better** — every component has a purpose; no excessive decorations, gradients, or shadows.
- **Content first** — service info (Service → Requirements → Process → Action) outweighs visual effects.
- **Clear actions** — primary actions are always obvious (Explore Services, Track Request, Apply Online).
- **Trust through design** — consistent spacing, clear type, predictable navigation, visible status.

## Visual Style

Three combined styles:

1. **Modern Minimalism** — whitespace, simple typography, clean layouts, strong hierarchy.
2. **Soft Glassmorphism** — subtle `rgba(255,255,255,0.68)` surfaces, `blur(18px)`, thin borders, soft shadows. Used sparingly (nav, hero panels, feature cards, sticky elements).
3. **Bento UI** — modular cards with unequal sizes in a responsive CSS Grid.

## Color System

```text
Primary           #2563EB
Deep Blue         #1E3A8A
Background        #F5F7FA
Surface           #FFFFFF
Glass Surface     rgba(255, 255, 255, 0.68)

Text primary      #111827
Text secondary    #6B7280
Text muted        #9CA3AF

Success           #16A34A
Warning           #D97706
Danger            #DC2626
Info              #2563EB
```

Status must never be communicated by color alone (always include text/labels).

## Typography

- Font: **Inter** with `system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI"` fallback.
- Display 48–64px / H1 36–44px / H2 28–32px / H3 20–24px (all 700 or 600), Body 15–17px, Small 13–14px.

## Radius, Spacing, Shadows

- Radius: small 10px, default 16px, large 20px (cards), hero 28px, pills 999px.
- 8px spacing system (4, 8, 12, 16, 24, 32, 40, 48, 64, 80).
- Shadows: `0 4px 12px rgba(15,23,42,0.05)` / `0 10px 30px rgba(15,23,42,0.08)`.

## Layout

- Container: `min(100% - 32px, 1180px)`.
- Floating glass navigation (sticky), compact top-bar on mobile.
- Bento grid: `repeat(6, 1fr)` sized spans on desktop, collapsing to 2-col then 1-col.
- Breakpoints: mobile `< 640px`, tablet `640–1023px`, desktop `1024px+`, large `1280px+`.

## Components

- **Buttons:** primary (blue), secondary (white/border), outline-light (hero), danger. Height 44–52px, radius 12px. "Submitting..." on pending with disabled state.
- **Status pills:** soft backgrounds with readable dark text + leading dot.
- **Cards:** white, 1px border, 20px radius, subtle hover lift (translateY(-4px)) + shadow.
- **Forms:** white inputs, 1px `#D1D5DB` border, 12px radius, 13–14px padding, focus ring `rgba(37,99,235,0.12)`.
- **Timeline:** vertical with done/current states.
- **Toasts:** bottom-right (bottom-center mobile); success/error/info colors.
- **Modals:** backdrop blur, white surface, 20–24px radius.
- **Loading:** skeleton shimmer + inline spinners; buttons show `Submitting...`.
- **Empty/error states:** helpful copy with retry actions.

## Public Pages

- **Home:** gradient hero (deep blue → blue) with subtle radial accents, search + track bento cards, popular services, 4-step "How It Works", announcements, emergency contacts.
- **Services:** searchable bento directory.
- **Service details:** requirements, process timeline, office info, sticky Apply CTA.
- **Apply:** centered form; success screen shows large copyable reference; offline shows "Saved Locally · Pending Sync" (never a fake reference).
- **Track:** reference input → status card + timeline.
- **Announcements, Offices, Appointments, Reports:** simple, content-focused.

## Staff Pages

- Sidebar + content area (sidebar becomes horizontal on ≤900px).
- Dashboard: compact stat tiles + recent applications table.
- Applications: filterable table; detail splits applicant info / form data / history vs. sticky status form + forward control.

## Accessibility

Semantic HTML, keyboard navigation, labeled fields, visible focus, ARIA only where necessary, error text alongside color, screen-reader-friendly status. Fonts/scale responsive.

## Motion

150–250ms, ease-out. Subtle microinteractions only (button/card hover, drawer, toasts, status changes). No bouncing or decorative loops.

> **Design principle:** *Make government services feel as simple as using a modern web app.*
>
> **One Municipality. Connected Services. Easier Access. — Walang Kanin Bossing**