persistent-actors

## Exercise 7 > Persistent Actors

In this exercise we make the `PlayerRegistry` persistent to increase `resilience`. In order to do this, we will focus on changing the following:

- `PlayerRegistry.scala`:
    - Implement the `PlayerRegistry` as a `PersistentActor`.
    - You will need a `persistenceId`.
    - You will need to implement `receiveCommand`.
        - Make sure you check for `playerNameTaken`.
        - When name not taken, implement `persist` in `def registerPlayer`.
    - You will need to implement `receiveRecover`.
        - Create a `def onPlayerRegistered` method and use `saveSnapshot(Snapshot(players))` every 100 players.
        - You will need to handle `SnapshotOffer`.
- `PlayerSharding.scala`:
    - Implement the `Player` as a `PersistentActor`.
    - You will need a `persistenceId`.
    - You will need to implement `receiveCommand`.
    - You will need to implement `receiveRecover`.

We have taken a big step in `resilience` by introducing **akka persistence**. Now we can ensure we do not loose players even when a node crashes. Let's test our solution.

- Use the `sj` command alias to bootstrap `ShareJournal`.
- Open a second terminal window and start another `sbt` session:
    - Use the `sr` command alias to bootstrap `ScoresRepository`.
- Open a third terminal window and start another `sbt` session:
    - Use the `ge` command alias to bootstrap the `GameEngine`.
- Open a fourth terminal window and start another `sbt` session:
    - Use the `pr` command alias to bootstrap the `PlayerRegistry`.
    - From the `PlayerRegistry` prompt, create two `simple` players.
- Open a fifth terminal window and start another `sbt` session:
    - Use the `pr2` command alias to bootstrap another `PlayerRegistry`.
    - From the `PlayerRegistry` prompt, create two `simple` players.
- Open up a sixth terminal session:
    - `tail -f akka-collect.log` and verify the Akka Collect is working as expected.
- In the fourth terminal window, crash the `PlayerRegistry` by entering `s` for shutdown.
- Watch the `akka-collect.log` and see what happens!
    - Pretty **darn cool huh!**
- Use the `test` command to verify everything works as expected.
- Use the `koan next` command to move to the next exercise.
