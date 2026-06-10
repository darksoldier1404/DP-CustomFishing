<center><img src="https://i.postimg.cc/MKPVVR1s/dplogo-512.png" alt="logo"></center>
<center><img src="https://i.postimg.cc/RZ9dqPFx/introduce.png" alt="introduce"></center>

This plugin adds a **custom fishing system** with a click-action minigame, rank-based custom fish, a sell shop GUI, and automated fishing contests.
Players catch custom fish through a fast-paced minigame, sell them for money, and compete in daily scheduled fishing competitions.

---

<center><img src="https://i.postimg.cc/RZ9dqP08/description.png" alt="description"></center>

- Catching a fish starts a **click-action minigame** — follow the left/right click sequence shown on screen before the timer runs out
- Fish are organized into **custom ranks**, each with its own item pool, weight (catch chance), length range, and base price
- Every caught fish gets a random **length**, and its sell price scales with length (`base price + length × price-per-length`)
- Built-in **sell shop GUI** — put fish in and sell them all at once, or shift-right-click to sell everything in your inventory
- **Fishing contests** with two modes: **longest fish** and **most catches**
  - Contests can be **scheduled to run daily** at a fixed time (HH:mm) or force-started by an admin
  - Live **boss bar timer** shows the remaining contest time to all players
  - Configurable rewards (console commands) for **1st / 2nd / 3rd place and participation**
- **Leaderboard placeholders** for total catches and top rankings (PlaceholderAPI)
- Fishing can be restricted to specific **allowed worlds**
- All ranks, player stats, and scheduled contests are **saved automatically**

---

<center><img src="https://i.postimg.cc/rwcjzhpH/depend-plugin.png" alt="depend-plugin"></center>

- All DP-Plugins require the **`DPP-Core`** plugin
- The plugin will not work if **`DPP-Core`** is not installed
- You can download **`DPP-Core`** here: <a href="https://github.com/DP-Plugins/DPP-Core/releases" target="_blank">Click me!</a>
- **`EssentialsX`** (economy) is required for selling fish and money rewards
- **`PlaceholderAPI`** is optional — needed only for leaderboard placeholders

---

<center><img src="https://i.postimg.cc/dV01RxJB/installation.png" alt="installation"></center>

1️⃣ Place the **`DPP-Core`** plugin and this plugin file (**`DP-CustomFishing-*.jar`**) into your server’s **`plugins`** folder

2️⃣ Restart the server, and the plugin will be automatically enabled

3️⃣ Configuration files will be generated on first run

---

<center><img src="https://i.postimg.cc/jSKcC85K/settings.png" alt="settings"></center>

- **`config.yml`**
    - Message prefix
    - `Settings.PricePerLength` — extra price added per 1cm of fish length (default: 10)
    - `Settings.AllowedWorlds` — list of worlds where custom fishing is enabled
    - `Settings.symbol.left` / `right` — minigame click symbols (`◀`, `▶`)
    - `Contest.Duration` — contest duration in minutes (default: 30)
    - `Contest.Rewards.<Length|MostCatch>.<1st|2nd|3rd|participation>.commands`
        - Reward commands executed by console; `%player%` is replaced with the winner's name

---

<center><img src="https://i.postimg.cc/SxqdjZKw/command.png" alt="command"></center>

❗ Most commands require admin permission (`dpcf.admin`)

Aliases: `/customfishing`, `/커스텀낚시`, `/낚시`

**Command List and Examples**

| Command | Permission | Description | Example |
|---|---|---|---|
| `/dpcf opensell` | dpcf.sell | Open the fish sell GUI | `/dpcf opensell` |
| `/dpcf create <rank>` | dpcf.admin | Create a new fish rank | `/dpcf create rare` |
| `/dpcf items <rank>` | dpcf.admin | Edit the item pool of a rank (GUI) | `/dpcf items rare` |
| `/dpcf price <rank> <price>` | dpcf.admin | Set base price of a rank | `/dpcf price rare 500` |
| `/dpcf length <rank> <min> <max>` | dpcf.admin | Set length range of a rank | `/dpcf length rare 30 80` |
| `/dpcf weight <rank> <weight>` | dpcf.admin | Set catch weight (chance) of a rank | `/dpcf weight rare 20` |
| `/dpcf delete <rank>` | dpcf.admin | Delete a rank | `/dpcf delete rare` |
| `/dpcf list` | dpcf.admin | List all ranks | `/dpcf list` |
| `/dpcf setpriceperlength <price>` | dpcf.admin | Set global price-per-length value | `/dpcf setpriceperlength 10` |
| `/dpcf contestcreate <name> <length\|mostcatch> <HH:mm>` | dpcf.admin | Register a daily repeating contest | `/dpcf contestcreate daily length 14:30` |
| `/dpcf contestedit <name> <length\|mostcatch> <HH:mm>` | dpcf.admin | Edit a registered repeating contest | `/dpcf contestedit daily mostcatch 20:00` |
| `/dpcf contestdelete <name>` | dpcf.admin | Delete a registered repeating contest | `/dpcf contestdelete daily` |
| `/dpcf contestlist` | dpcf.admin | List all registered repeating contests | `/dpcf contestlist` |
| `/dpcf conteststart <length\|mostcatch>` | dpcf.admin | Force start a contest now | `/dpcf conteststart length` |
| `/dpcf conteststop <length\|mostcatch>` | dpcf.admin | Force stop a contest (no rewards) | `/dpcf conteststop length` |
| `/dpcf reload` | dpcf.admin | Reload the plugin configuration | `/dpcf reload` |

**❗Notes when using commands**

- The minigame requires **3–5 correct clicks within 5 seconds** — a wrong click or timeout means the fish escapes
- A rank with no items in its pool will never produce a fish — add items with `/dpcf items <rank>`
- Higher **weight** means a higher chance for that rank to be selected
- Only one contest **per type** can run at the same time
- Force-stopped contests do **not** give rewards
- Fishing only works in worlds listed under `Settings.AllowedWorlds`

---

<center><img src="https://i.postimg.cc/Z5ZH0fqL/api-integration.png" alt="api-integration"></center>

- **PlaceholderAPI** placeholders:
    - `%dpcf_total_fishing%` — total number of fish the player has caught
    - `%dpcf_top_fishing_<rank>%` — catch count of the player at the given leaderboard rank (e.g. `%dpcf_top_fishing_1%`)
    - `%dpcf_top_name_<rank>%` — name of the player at the given leaderboard rank (e.g. `%dpcf_top_name_1%`)
- **Custom event for developers**: `FishingSuccessEvent` is fired whenever a player successfully completes the fishing minigame
- Fish items carry NBT tags: `dpcf_rank`, `dpcf_length`, `dpcf_price`

---

<center><a href="https://discord.gg/JnMCqkn2FX"><img src="https://i.postimg.cc/4xZPn8dC/discord.png" alt="discord"></a></center>

- https://discord.gg/JnMCqkn2FX
- Join our Discord for support, bug reports, or feature requests
- Suggestions and feedback are always welcome!

---
