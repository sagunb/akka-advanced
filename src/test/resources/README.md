complete-initial-state

## Exercise 1 > Completed Initial State

In this exercise we will implement the **TODO** comments in the following files:

- `Game.scala`:
    - Schedule sending `MoveTimeout` to this actor after `moveTimeout`.
    - Change the behavior to `handlingMove`.
    - Send `GameOver` to the parent actor, then stop this actor.
- `GameEngine.scala`:
    - Goto `State.Running` using new state data with `Some(tournament)`.
    - Create a `Tournament`, watch and return it
- `PlayerRegistry.scala`
    - Return `true` if there is already a player with the given `name`.
- `ScoresRepository`:
    - Merge the given `update` with the `scores` using the `merge` extension method provided by `MapOps` in `package.scala`.
- `Tournament.scala`:
    - Import the `Tournament` companion object.
    - For each element of the result of `partitionPlayers` create a `Game`, add it to `games` and watch it.

At this point, Akka Collect is essentially a monolithic application. In the next exercise we begin the process of distribution, but before we start, we want to make sure everything is working as expected. After you have completed the above steps, do the following:

- Start an `sbt` session.
- Use the `ge` command alias to bootstrap `GameEngine`:
- From the `GameEngine` prompt enter the following:

```scala
r jack simple
r jill simple
```

- Where `r` is for register, `jack` and `jill` are the names of the players, and `simple` is the player type.
- Open up a new terminal session and `tail -f akka-collect.log` and you should see something like the following:

```bash
14:36:14 WARN  ActorSystemImpl [ActorSystem(akkollect-system)] - GameEngineApp$ running
Enter [34mcommands[0m into the terminal: [34m[e.g. `s` or `shutdown`][0m
14:36:18 INFO  PlayerRegistry [akka://akkollect-system/user/player-registry] - Registering player jack
14:36:18 WARN  ActorSystemImpl [ActorSystem(akkollect-system)] - Registered player jack
14:36:19 INFO  GameEngine [akka://akkollect-system/user/game-engine] - Starting tournament
14:36:19 DEBUG GameEngine [akka://akkollect-system/user/game-engine] - Transitioning into running state
14:36:19 INFO  Tournament [akka://akkollect-system/user/game-engine/$a] - Starting games
14:36:19 INFO  Game [akka://akkollect-system/user/game-engine/$a/$a] - Game started with players: jack
14:36:21 INFO  PlayerRegistry [akka://akkollect-system/user/player-registry] - Registering player jill
14:36:21 WARN  ActorSystemImpl [ActorSystem(akkollect-system)] - Registered player jill
14:36:25 INFO  Game [akka://akkollect-system/user/game-engine/$a/$a] - Game over with scores: 
14:36:25 INFO  Tournament [akka://akkollect-system/user/game-engine/$a] - Tournament over with scores: 
14:36:25 DEBUG GameEngine [akka://akkollect-system/user/game-engine] - Transitioning into pausing state
...
14:36:41 INFO  GameEngine [akka://akkollect-system/user/game-engine] - Starting tournament
14:36:41 DEBUG GameEngine [akka://akkollect-system/user/game-engine] - Transitioning into running state
14:36:41 INFO  Tournament [akka://akkollect-system/user/game-engine/$c] - Starting games
14:36:41 INFO  Game [akka://akkollect-system/user/game-engine/$c/$a] - Game started with players: jack, jill
14:36:47 INFO  Game [akka://akkollect-system/user/game-engine/$c/$a] - Game over with scores: jack -> 2
14:36:47 INFO  Tournament [akka://akkollect-system/user/game-engine/$c] - Tournament over with scores: jack -> 2
14:36:47 INFO  ScoresRepository [akka://akkollect-system/user/scores-repository] - Updated scores: jack -> 2
...
```

- Use the `test` command to verify everything works as expected.
- Use the `koan next` command to move to the next exercise.
