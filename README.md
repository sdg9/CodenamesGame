# What is this project?

This project intends to reimplement the board game "Codenames" using [libgdx](https://github.com/libgdx/libgdx) as the game framework, [artemis-odb](https://github.com/junkdog/artemis-odb) as the Entity Component System (ECS), [KryoNet](https://github.com/EsotericSoftware/kryonet) for networking, and Kotlin as the programming language.

The intent is to be able to play on the couch via devices rather than a physical game.

It also is serving as my "Hello World" entry into game development. I'm a professional web and mobile app developer but all of these technologies are brand new to me.

## Journey thus far

- Udemy course on libgdx + kotlin
- Round 1: Libgdx + [Colysues](https://github.com/colyseus/colyseus)
  - Used Colysues node server as authoritative server
  - Implemented a redux like state management system (what I know from web/mobile world) for syncing (Found some kotlin redux implementations)
  - Didn't like that client logic was in kotlin and server logic was in node (duplicate dev effort) so re-implemented Colyseus in java
  - Initially used JSON for serialization, then moved over to message pack
  - Didn't love the idea for an autoritative server for my use case and how I'd need to host it on something like heroku (which I had working w/ Colyseus)
  - Found out Kryonet existed and moved on to a game hosted locally
- Round 2: Libgdx + ECS
  - Found out that ECS was a thing. Coming from a react-redux background liked how I could break things into components and systems feel like middleware. Very much at home.
  - Implemented project with Ashley for ECS.
  - Implemented basic KryoNet server.
  - Didn't like how I did serialization, all syncing between client & server was very manual and tedious based on my implementation.
  - Found [Ore Infinium](https://github.com/sreich/ore-infinium) Using libgdx, artemis-odb, and kryonet
  - Switched over to Artemis-ODB over Ashley, still didn't love serialization
- Round 3: Libgdx + ECS + automatic serializing
  - Found [DaanVanYperen's artemis-odb extensions](https://github.com/DaanVanYperen/artemis-odb-contrib)
  - Found [Argentum Online](https://github.com/ao-libre/ao-java) based game using libgdx, artemis-odb, and kryonet using above extensions
  - With two libgdx + artemis-odb + kryonet examples learned more from existing approaches
  - Implemented serializing in such a way where simply creating an entity with the appropriate component would cause it to appear in all worlds (server & clients)
    - Now I can "almost" develop without thinking about network connectivity for a good chunk of the game
- Future
  - Better understanding of how I should deal with libgdx scenes when using ECS
    - What should ECS systems render vs libgdx scenes
    - How do I deal with Scene2d?

# TODO

- [ ] When joining a game, the menu is not rendered but buttons are still clickable. So if you click in the same region as the "Host game" button you'll try and host again, causing game world to crash.
- [ ] Determine optimal approach for navigating across various game screens
- [ ] Support more than hardcoded localhost newtork play
  - [ ] Support join game screen where user can enter IP and PORT to connect to
  - [ ] Display IP for client running server
- [ ] Confirm behavior on Android
- [ ] Confirm behavior on iOS
- Items previously working in Round 1 now need to be addressed again
  - [ ] Restore ability to toggle over to list of words and what team they belong to (for the two cluegivers)
  - [ ] Ability to leave game
  - [ ] Ability to reset game (make a new one)
  - [ ] Animate card on press

# Screenshots
