# Disaster Detector

**Disaster Detector** is an educational Snakes and Ladders game that teaches environmental facts and disaster preparedness through play. Built as a JavaFX desktop application, it supports 1 to 7 players, includes a PostgreSQL persistence layer for saved games and history, and uses a custom pixel-art interface to give the game its own visual style.

### To Run the app

From the project root, run:

```bash
mvn javafx:run
```

This starts the JavaFX app using the main class app.Main.

## Board Squares

Most of the board is made up of safe squares, but several special squares change the flow of the game.

### Good Squares

Good squares are ladder tiles. Landing on one shows a positive environmental fact in the clue dialog and moves the player up the board.

| Tile | Moves To | Effect |
| --- | --- | --- |
| 15 | 35 | Renewable energy fact, then climb forward |
| 38 | 58 | Ozone recovery fact, then climb forward |
| 50 | 70 | Conservation progress fact, then climb forward |
| 62 | 82 | Electric vehicle growth fact, then climb forward |
| 74 | 94 | Costa Rica renewable electricity fact, then climb forward |

### Bad Squares

Bad squares are snake tiles. Landing on one shows a negative environmental fact in the clue dialog and sends the player back down the board.

| Tile | Moves To | Effect |
| --- | --- | --- |
| 22 | 3 | High CO2 warning, then slide back |
| 33 | 12 | Arctic warming warning, then slide back |
| 54 | 47 | Ocean plastic warning, then slide back |
| 76 | 65 | Deforestation warning, then slide back |
| 99 | 84 | Hottest decade warning, then slide back |

### Disaster Squares

Disaster squares present a multiple-choice disaster-preparedness question. A correct answer moves the player forward; a wrong answer moves them back.

| Tile | Correct | Wrong | Effect |
| --- | --- | --- | --- |
| 45 | +6 | -6 | Answer correctly to advance, wrong answer slides back |
| 89 | +8 | -8 | Answer correctly to advance, wrong answer slides back |

### Final Quiz

Tile **100** is the final square and uses **FinalTile**.

- Landing on tile **100** opens a disaster-preparedness multiple-choice quiz.
- A correct answer wins the game immediately.
- A wrong answer pushes the player back **3** squares, so they have to reach the final tile again.

The question pool currently includes topics such as earthquake safety, flood response, wildfire preparation, and emergency kit essentials.

## Save / Load / History

The game can run without a database, but the following features depend on PostgreSQL:

- Save game
- Load game
- View history

If PostgreSQL is not configured, the game should still open and play, but persistence-related features may show an error message.

### PostgreSQL setup

The app reads database settings from environment variables. You can use either:

- DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD
- or PostgreSQL-style variables: PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD

Example for zsh:

***
export PGHOST=localhost
export PGPORT=5432
export PGDATABASE=disaster_game
export PGUSER=postgres
export PGPASSWORD=your_password
***

The database schema is created automatically from **src/main/resources/schema.sql** when a connection is opened.


## Project Structure

- src/main/java/app - application entry points
- src/main/java/controller - JavaFX and game logic controllers
- src/main/java/model - board, player, tile, and game state classes
- src/main/java/util - navigation, dialogs, and dice utilities
- src/main/resources/view - FXML screens
- src/main/resources/images - game artwork and board assets
- src/main/resources/styles - CSS styling

## Main Screens

- Splash screen
- Main menu
- Game board
- Disaster quiz dialog
- History screen
- Win screen

