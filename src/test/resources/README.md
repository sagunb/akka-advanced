cluster-aware-routers

## Exercise 4 > Cluster Aware Routers

In this exercise we split off the `ScoresRepository` as a separate instance and connect by way of a group router using Akka cluster aware routing. In order to do this, we will focus on changing the following:

- `application.conf` (configure group routing):
    - Create a `deployment` section.  
    - Configure `random-group` router.
    - Use `/user/scores-repository` as the routees path.
    - Turn `allow-local-routees` off.
- `GameEngine.scala` (remove some code):
    - The `scoresRepository` is now accessed via `FromConfig`
    - **HINT**: Use a factory method (createScoresRepository) to facilitate testing
    - **HINT**: `Props` factory.
    - **HINT**: Actor constructor.
- `GameEngineApp.scala` (remove some code):
    - **HINT**: Think `scoresRepository`.
- `Tournament.scala` (provision against failure):
    - Because the `scoresRepository` is remote, messages could get lost. 
    - **HINT**: Think `ask` and `pipeTo`.
    - Log `No answer form scores repository, scores might have been lost!` at `error`.

We have our taken our third step in making Akka Collect **reactive** by introducing additional **elasticity** and **resilience**. Now it is time to test our solution.

- Use the `ge` command alias to bootstrap `GameEngine`.
- Open a second terminal window and start another `sbt` session:
    - Use the `sr` command alias to bootstrap the `ScoresRepository`.
- Open a third terminal window and start another `sbt` session:
    - Use the `pr` command alias to bootstrap the `PlayerRegistry`.
    - From the `PlayerRegistry` prompt, create two `simple` players.
- Open up a fourth terminal session:
    - `tail -f akka-collect.log` and verify the Akka Collect is working as expected.
- Use the `test` command to verify everything works as expected.
- Use the `koan next` command to move to the next exercise.
