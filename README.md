# Valley Day

# Game idea
The core concept of the game is to navigate a farmer (the player) through a top-down 2D farm plot that has become overgrown and cluttered with branches. The player must clear debris, plant and harvest crops before the day ends. 

# Player controls
- Move: Arrow keys
- A: Plant or harvest crops  
- D (hold): Clear debris  
- S: Scare away wild animals
- Esc: Open / close pause menu  

# Win/Lose Conditions
- Win: Reach the harvest goal → exit unlocks → reach the exit before time runs out
- Lose: Time runs out or player touches a wild animal

# Project Structure
- `core/` – Game logic, world rules, entities
- `screens/` – Different game screens (menu, game, game over) 
- `assets/` – audio, maps, skin, texture
- `desktop/` – Desktop launcher
- `.vscode/` – launch, settings

# checklist compliance
This project has been implemented to fully comply with the assignment checklist requirements, as detailed below.
- The game can read and run any map defined in a `.properties` file.
- Both destructible debris and indestructible fences are supported.
- Some tiles may hide items or the exit beneath destructible debris.
- If the map file does not define an exit, the game automatically places one under a random destructible debris tile.

- The player can be controlled using the arrow keys in four directions.
- The player can plant seeds by pressing **A** while facing a soil tile.
- The player can harvest crops by pressing **A**.
- Rotten crops remain on the tile and can be restored by collecting a watering can.
- The player can remove debris by holding **D** for 1 second.
- Debris removal speed is doubled when the player owns a shovel.
- The player can shoo wildlife on the tile directly in front by pressing **S**.

- Crops grow through multiple stages.
- If not harvested in time, crops will eventually rot.
- Tools such as shovels and watering cans can be found hidden under debris and collected by the player.

- Wildlife visitors are dynamic objects that move through the map.
- Wildlife cannot pass through fences or debris.
- If the player is touched by a wildlife visitor, the player becomes frightened, runs away, and the game ends.
- The player can actively shoo wildlife away using the **S** key.

#### Bonus Implementation
- Two types of wildlife visitors are implemented: **chickens** and **eagles**.
- Both chickens and eagles eat mature crops.
- Eagles are more intelligent than chickens: they actively search for mature crops within a certain range and move toward them.
