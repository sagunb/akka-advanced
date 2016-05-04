remoting

## Exercise 2 > Remoting

In this exercise we split off the `PlayerRegistry` as a separate application by implementing Akka Remoting. In order to do this, we will focus on changing the following:

- `application.conf`:
    - Implement the `RemoteActorRefProvider`.
    - Add a section under game-engine for player-registry where you provide the hostname and port
    - **ATTENTION**: For `hostname` use `localhost`.
- `Settings.scala` (get player registry address from configuration):
    - Get the `hostname` and `port` from config.
    - Create a `val playerRegistryAddress` with these values.
    - Implement an `implicit val askTimeout` for asking if the `PlayerRegistry` is up.
- `PlayerRegistry.scala`
    - Implement `def pathFor(address: Address)` which returns an `ActorPath`
- `GameEngine.scala`:
    - Use an `Address` for `PlayerRegistry`
    - Implement a "createPlayerRegistry()" method that returns an ActorSelection.
    - Create an instance of the PlayerRegistry using the "createPlayerRegistry" method.
    - Pass the PlayerRegistry to downstream actors.
    - **HINT**: Think about the `def pathFor` method you just implemented.
- `GameEngineApp.scala` (remove unnecessary code):
    - Remove `CommandParse.register` as it is not needed anymore.
    - Remove the return value for `playerRegistry`.
    - Remove the `def register` method as it is not needed anymore.
- `Tournament.scala` (provision against communication failure between remote applications):
    - Implement `playerRegistry: ActorSelect` in the `Props` factory and actor constructor.
    - **HINT**: You will probably need the `askTimeout` as well.
    - Wait (without blocking) for the `PlayerRegistry` to come on line. Think `ask`.
    - Handle `Status.Failure` when `ask` times out.
    - Log at `error` when `PlayerRegistry` timeout occurs and stop the tournament.

Now we have our taken our first step in making Akka Collect **reactive**. It is time to test our solution.

- Use the `ge` command alias to bootstrap `GameEngine`.
- Open an new terminal window and start another `sbt` session.
- Use the `pr` command alias to bootstrap the `PlayerRegistry`.
- From the `PlayerRegistry` prompt, create two `simple` players.
- Open up a new terminal session and `tail -f akka-collect.log` and verify the Akka Collect is working as expected.
- Shutdown the `PlayerRegistry` and notice what the `akka-collect.log` reports.
- Use the `test` command to verify everything works as expected.
- Use the `koan next` command to move to the next exercise.
